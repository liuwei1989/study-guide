### **1.Netty简介** {#1netty简介}

* Netty是一个高性能、异步事件驱动的NIO框架，它提供了对TCP、UDP和文件传输的支持，作为一个异步NIO框架，Netty的所有IO操作都是异步非阻塞的，通过Future-Listener机制，用户可以方便的主动获取或者通过通知机制获得IO操作结果，比Mina更高效。

* Netty已经得到了成百上千的商业项目的验证，例如Hadoop的RPC框架Avro就使用了Netty作为底层通信框架，其他的业界主流RPC框架，例如：Dubbo、Google 开源的gRPC、新浪微博开源的Motan、Twitter 开源的 finagle也使用Netty来构建高性能的异步通信能力。另外，阿里巴巴开源的消息中间件RocketMQ也使用Netty作为底层通信框架…….

**Netty核心架构图**

![](http://img.blog.csdn.net/20170612212623057?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

### **2.Netty的特点** {#2netty的特点}

（1） 使用更高效的socket底层，对epoll空轮询引起的cpu占用飙升在内部进行了处理，避免了直接使用NIO的陷阱，简化了NIO的处理方式。

（2） 采用多种decoder/encoder 支持，对TCP粘包/分包进行自动化处理

（3） 可使用接受/处理线程池，提高连接效率，对重连、心跳检测的简单支持

（4）可配置IO线程数、TCP参数， TCP接收和发送缓冲区使用直接内存代替堆内存，通过内存池的方式循环利用ByteBuf

（5） 通过引用计数器及时申请释放不再引用的对象，降低了GC频率

（6）使用单线程串行化的方式，高效的Reactor线程模型

（7） 无锁化的串行设计，采用环形数组缓冲区实现无锁化并发编程

（8）大量使用了volitale、使用了CAS和原子类、线程安全类的使用、读写锁的使用

  


