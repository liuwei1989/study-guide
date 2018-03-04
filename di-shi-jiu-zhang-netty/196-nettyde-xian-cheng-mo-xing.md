## **Reactor线程模型** {#reactor线程模型}

Reactor 模式基于事件驱动，适合处理海量的I/O 事件。它主要由多路复用器\(Acceptor\)、事件分发器\(Dispatcher\)、事件处理器\(Handler\)组成，可以分为三种。

### **单线程模型** {#单线程模型}

* 所有I/O操作都由一个线程完成，即多路复用、事件分发和处理都是在一个Reactor线程上完成的。

* 这是最简单的Reactor模型。Reactor线程负责多路分离套接字，Accept新连接，并分派请求到处理器链中。该模型适用于处理器链中业务处理组件能快速完成的场景。不过这种单线程模型不能充分利用多核资源，所以实际使用的不多。

**单线程模型图解**

![](http://img.blog.csdn.net/20170613124125733?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

通过上图可以看出**唯一的NIO 线程的职责：**

* 既要接收客户端的连接请求,又要向服务端发起连接（将server和client的功能集于一身）；

* 既要读取请求或响应消息,又要发送请求或应答消息（将server和client的功能集于一身）；

**单线程Netty核心代码：**

```
//设置单线程模式
EventLoopGroup simplethread = new NioEventLoopGroup(1);
//辅助启动类
ServerBootstrap bootstrap = new ServerBootstrap();
//设置线程池
bootstrap.group(simplethread);
```

**分析**：由代码可以看出，只有一个循环事件线程组\(NioEventLoopGroup\)来处理所有事件.对应于上图的单线程模型。

**单线程模型弊端**

理论上一个线程可以独立处理所有IO 相关的操作。对于数据量较小的场景，单线程模型的确适用。但是对于高负载、大并发的应用场景却不合适，原因如下：

* 一个NIO 线程同时处理成百上千的链路，性能上无法支撑，即使CPU 负荷已满；

* 当NIO 线程负载过重之后，处理速度将变慢，这会导致大量客户端连接  
  超时，超时后就会进行重发，最终会导致大量消息积压和处理超时；

* 若线程进入死循环，会导致整个程序不可用，不能接收和处理外部消息。

解决方法： Reactor 多线程模型。

### **多线程模型** {#多线程模型}

* 该模型在事件处理器（Handler）链部分采用了多线程（线程池），也是后端程序常用的模型；

* 有一组 NIO 线程只负责处理IO操作。

**多线程模型图解**

![](http://img.blog.csdn.net/20170613124147351?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

通过上图可以看出**多线程模型的职责：**

* 有一个NIO 线程（Acceptor） 只负责监听服务端，接收客户端的TCP 连接请求；

* NIO 线程池负责网络IO 的操作，即消息的读取、解码、编码和发送；

* 1 个NIO 线程可以同时处理N 条链路，但是1 个链路只对应1 个NIO 线程，这是为了防止发生并发操作问题。

**多线程Netty核心代码**

```
//设置接受客户端连接的线程数为1
EventLoopGroup boss = new NioEventLoopGroup(1);
EventLoopGroup worker = new NioEventLoopGroup();
//辅助启动类
ServerBootstrap bootstrap = new ServerBootstrap();
//设置线程池
bootstrap.group(boss,worker);
```

**分析**：由代码可以看出，boss单线程只负责处理客户端的连接请求，而worker线程组用于执行网络IO操作，对应上图的多线程模型。

**多线程模型弊端**

一般情况下，多线程模型都可以满足性能需求；但是，在并发百万客户端连接或需要安全认证时，一个Acceptor 线程可能会存在性能不足问题。

解决方法：主从Reactor 多线程模型。

### **主从多线程模型** {#主从多线程模型}

**主从多线程模型是将Reactor分成两部分：mainReactor和subReactor**

* mainReactor负责监听并accept新连接，然后将建立的socket通过多路复用器（Acceptor）分派给subReactor，mainReactor线程池就只用于客户端的登陆、握手和安全认证；

* subReactor负责多路分离已连接的socket，进行读写网络IO，编解码等操作，  
  注意：subReactor个数小于等于CPU个数。

  **主从多线程模型图解**

![](http://img.blog.csdn.net/20170613124207070?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

通过上图可以看出**主从多线程模型的职责：**

* Acceptor 线程用于绑定监听端口，接收客户端连接，创建新的SocketChannel，然后将其注册到主线程池的subReactor 线程上，从而保证mainReactor只负责接入认证、IP 黑白名单过滤、握手等操作；

* 将SocketChannel 从主线程池的Reactor 线程的多路复用器上移除，重新注册到Sub 线程池的线程上，用于处理I/O 的读写等操作。

**主从多线程Netty核心代码**

```
//创建两个线程组
EventLoopGroup boss = new NioEventLoopGroup();
EventLoopGroup worker = new NioEventLoopGroup();
//辅助启动类
ServerBootstrap bootstrap = new ServerBootstrap();
//设置线程池
bootstrap.group(boss,worker);
```

**分析**：由代码可以看出，boss线程组负责监听端口，worker线程组负责数据读写，对应上图的主从多线程模型。

## **Netty的线程模型** {#netty的线程模型}

* 说了这么多，我想你肯定想要问Netty的线程模型到底是什么呀，其实，我前面讲的全都是Netty的线程模型。

* Netty对Reactor的三种线程模型都提供支持，你可以根据不同场景选择不同的线程模型，

* **偷偷告诉你：Netty官网推荐使用第三种线程模型（主从多线程模型）**

  


