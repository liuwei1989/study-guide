### **TCP粘包/分包问题的由来** {#tcp粘包分包问题的由来}

因为TCP是以流的方式来处理数据，一个完整的包可能会被TCP拆分成多个包进行发送，也可能把小的封装成一个大的数据包发送。

这样说可能比较抽象，下面举例来说明TCP拆包/粘包问题！

* 图解：如果客户端分别发送两个数据包D1和D2给服务端，由于服务端一次读取到的字节数是不确定的，可能会出现四种情况。

![](http://img.blog.csdn.net/20170613160039468?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

（1）服务端分别读取到D1和D2，没有产生粘包和拆包的情况，如下图：

![](http://img.blog.csdn.net/20170613155756780?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

（2）服务端一次接收到二个数据包，D1和D2粘合在一起，被成为TCP粘包。如下图：

![](http://img.blog.csdn.net/20170613155848000?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

（3）服务端分二次读取到了二个数据包，第一次读取到了完整的D1包和D2包的一部分，第二次读取到了D2包的剩余部分，这被成为TCP拆包（D2拆包），如下图：

![](http://img.blog.csdn.net/20170613155942251?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

（4）服务器还是分二次读取到了二个数据包，但第一次是读取到了D1包的部分内容 ，第二次读取到了D1包剩余部分和完整的D2包，这被成为TCP拆包（D1拆包），如下图：

![](http://img.blog.csdn.net/20170613160214098?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

### **出现TCP粘包/分包的原因** {#出现tcp粘包分包的原因}

1.应用程序写入的字节大小大于套接字发送缓冲区的大小，会发生拆包现象，而应用程序写入数据小于套接字缓冲区大小，网卡将应用多次写入的数据发送到网络上，这将会发生粘包现象；

2.进行mss（最大报文长度）大小的TCP分段，当TCP报文长度-TCP头部长度&gt;MSS的时候将发生拆包

3.以太网帧的payload（净荷）大于MTU（1500字节）进行ip分片。

**图解：**

![](http://img.blog.csdn.net/20170613164350124?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

### **TCP粘包/分包的解决方法** {#tcp粘包分包的解决方法}

**1.消息定长**

例如：每个报文的大小固定为200个字节，如果不够，空位补空格

对应Netty中的定长类 ：FixedLengthFrameDecoder

**2.在包尾都增加特殊字符进行分割**

例如：加回车、加换行、FTP协议等

对应Netty中的类

* 自定义分隔符类 ：DelimiterBasedFrameDecoder
* 行分隔符类：LineBasedFrameDecoder

**3.将消息分为消息头和消息体**

例：在消息头中包含表示消息总长度的字段，然后进行业务逻辑的处理。

对应Netty中的基于消息头指定消息长度类：LengthFieldBasedFrameDecoder

  
[http://blog.csdn.net/baiye\_xing/article/details/73188847](http://blog.csdn.net/baiye_xing/article/details/73188847)

[http://blog.csdn.net/baiye\_xing/article/details/73189191](http://blog.csdn.net/baiye_xing/article/details/73189191)



