#### **Selector BUG出现的原因** {#selector-bug出现的原因}

若Selector的轮询结果为空，也没有wakeup或新消息处理，则发生空轮询，CPU使用率100%，

#### **Netty的解决办法** {#netty的解决办法}

* 对Selector的select操作周期进行统计，每完成一次空的select操作进行一次计数，

* 若在某个周期内连续发生N次空轮询，则触发了epoll死循环bug。

* 重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的Selector上，并将原来的Selector关闭。

#### **Netty的高性能之道** {#netty的高性能之道}

**1.Netty心跳**

（1）定义：心跳其实就是一个简单的请求，

* 对于服务端：会定时清除闲置会话inactive\(netty5\)channelclose\(netty3\)

* 对于客户端:用来检测会话是否断开，是否重来，检测网络延迟！

（2）idleStateHandler类 用来检测会话状态

例：

```
public class IdleStateHandlerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));  //1
        pipeline.addLast(new HeartbeatHandler());
    }

    public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {
        private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.ISO_8859_1));  //2

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
             ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
                     .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);  //3
        } else {
            super.userEventTriggered(ctx, evt);  //4
        }
    }
}
```

分析：

• IdleStateHandler 将通过 IdleStateEvent 调用 userEventTriggered

• 如果连接没有接收或发送数据超过60秒钟，则心跳发送到远端

• 发送的心跳并添加一个侦听器，如果发送操作失败将关闭连接

• 若事件不是 IdleStateEvent ，就将它传递给下一个处理程序

**2.无锁化的串行设计理念**

（1）通过串行化设计，即消息的处理尽可能在同一个线程内完成，期间不进行线程切换，这样就避免了多线程竞争和同步锁。

（2）Netty的应用

Netty采用了串行无锁化设计，在IO线程内部进行串行操作，避免多线程竞争导致的性能下降。表面上看，串行化设计似乎CPU利用率不高，并发程度不够。但是，通过调整NIO线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队列-多个工作线程模型性能更优。

图解：

![](http://img.blog.csdn.net/20170616220507321?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

分析：NioEventLoop读取到消息之后，直接调用ChannelPipeline的fireChannelRead\(Object msg\)方法，只要用户不主动切换线程，一直会由NioEventLoop调用到用户的Handler，期间不进行线程切换。

**3.Netty的可靠性**

（1）链路有效性检测：链路空闲检测机制：读/写空闲超时机制

图解：

![](http://img.blog.csdn.net/20170617094922129?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

（2）内存保护机制：通过内存池重用ByteBuf;ByteBuf的解码保护

（3）优雅停机：

* 不再接收新消息

* 退出前的预处理操作

* 资源的释放操作

图解：

![](http://img.blog.csdn.net/20170617094944598?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

**4.Netty安全性**

（1）Netty支持的安全协议：SSL V2和V3，TLS，SSL单向认证、双向认证和第三方CA认证。

（2）SSL的三种认证方式

* 单向认证：客户端只验证服务端的合法性，服务端不验证客户端。

* 双向认证：与单向认证不同的是服务端也需要对客户端进行安全认证。这就意味着客户端的自签名证书也需要导入到服务端的数字证书仓库中。

* CA认证：基于自签名的SSL双向认证，只要客户端或者服务端修改了密钥和证书，就需要重新进行签名和证书交换，这种调试和维护工作量是非常大的。因此，在实际的商用系统中往往会使用第三方CA证书颁发机构进行签名和验证。我们的浏览器就保存了几个常用的CA\_ROOT。每次连接到网站时只要这个网站的证书是经过这些CA\_ROOT签名过的。就可以通过验证了。

**5.Netty的高效并发编程的体现**

（1）volatile的大量、正确使用;

（2）CAS和原子类的广泛使用；

（3）线程安全容器的使用；

（4）通过读写锁提升并发性能。

**6.IO通信性能三原则**

（1）三原则：传输（AIO）、协议（Http）、线程

（2）传统的RPC框架缺点

* 网络传输方式（一请求一应答），当并发量大时，会出现句柄溢出，线程堆溢出等问题。

* 序列化性能差：码流大、无法跨语言、CPU占用率高。

* 线程模型：同步阻塞I/O性能差

**7.Netty的TCP参数配置**

（1）定义：合理设置TCP参数在某些场景下对于性能的提升可以起到显著的效果，

（2）主要的TCP参数含义

* SO\_RCVBUF和SO\_SNDBUF：通常建议值为128K或者256K；

* SO\_TCPNODELAY：NAGLE算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率。但是对于时延敏感的应用场景需要关闭该优化算法；

* 软中断：如果Linux内核版本支持RPS（2.6.35以上版本），开启RPS后可以实现软中断，提升网络吞吐量。RPS根据数据包的源地址，目的地址以及目的和源端口，计算出一个hash值，然后根据这个hash值来选择软中断运行的cpu，从上层来看，也就是说将每个连接和cpu绑定，并通过这个hash值，来均衡软中断在多个cpu上，提升网络并行处理性能。

**8.流量整型的作用（变压器）**

（1）防止由于上下游网元性能不均衡导致下游网元被压垮，业务流中断

（2）防止由于通信模块接受消息过快，后端业务线程处理不及时导致撑死问题

图解：  
![](http://img.blog.csdn.net/20170617100620377?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

  


