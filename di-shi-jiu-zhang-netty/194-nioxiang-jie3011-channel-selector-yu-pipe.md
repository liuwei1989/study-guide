### **Channel** {#channel}

**1.定义**： Channel 表示 IO 源与目标打开的连接。Channel 类似于传统的“流”。只不过Stream是单向的，如：InputStream, OutputStream.而Channel是双向的，既可以用来进行读操作，又可以用来进行写操作。但Channel 本身不能直接访问数据，Channel 只能与Buffer 进行交互。

图解：  
![](http://img.blog.csdn.net/20170612194345276?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

**2.常用类**

•FileChannel：用于读取、写入、映射和操作文件的通道。  
•DatagramChannel：通过UDP 读写网络中的数据通道。  
•SocketChannel：通过TCP 读写网络中的数据。  
•ServerSocketChannel：可以监听新进来的TCP 连接，对每一个新进来的连接都会创建一个SocketChannel。

**3.获取通道（Channel）**

①对支持通道的对象调用

getChannel\(\) 方法。支持通道的类如下：

* 本地IO
  FileInputStream 、 FileOutputStream 、 RandomAccessFile
* 网络IO
  DatagramSocket 、 Socket 、 ServerSocket

②使用Files 类的静态方法newByteChannel\(\) 获取字节通道。

③通过通道的静态方法open\(\) 打开并返回指定通道。

**4.读写操作**

* 将Buffer 中数据写入Channel

例如：

```
//将Buffer中数据写入Channel中
int bytesWritten = inChannel.write(buf);
```

* 从Channel 读取数据到Buffer

例如：

```
//从Channel读取数据到Buffer中
int
 bytesRead = inChannel.read(buf);
```

**5.用FileChannel进行文件读写**

例1：写一个字符串到文件中

```
 public void fileTOFileChannel () throws IOException {
        //获取输入输出文件流
        FileInputStream in = new FileInputStream("G:\\剑指BAT\\代码\\FileChannel.txt");
        FileOutputStream out = new FileOutputStream("G:\\剑指BAT\\代码\\OutFileChannel.txt");
        //创建一个大小为512字节缓冲区
        ByteBuffer buf = ByteBuffer.allocate(512);
        //获取文件输入输出管道
        FileChannel fin = in.getChannel();
        FileChannel fo = out.getChannel();

        System.out.println("读取数据之前 ："+buf);
        //读取文件管道中的内容到ByteBuffer中
        int read = fin.read(buf);
        System.out.println("读取数据之后 ："+buf);
        //反转缓冲区，以便写入数据
        buf.flip();
        System.out.println("反转缓冲区 ："+buf);
        //将获取到的内容写入到文件管道中
        int write = fo.write(buf);
        System.out.println("写入数据之后 ："+buf);

        System.out.println("读取的字节数  :"+read);
        System.out.println("写入的字节数  :"+write);

    }
```

运行结果：

```
写前 ：java.nio.HeapByteBuffer[pos=0 lim=17 cap=512]
写后 ：java.nio.HeapByteBuffer[pos=17 lim=17 cap=512]
写入TXT文本的字节数  :17
```

程序运行前没有FileChannel.txt文件，即为空；  
程序运行后，产生了FileChannel.txt文件，内容为 Dream it possible

例2：将一个文本写入到另一个文本中

```
 public void fileTOFileChannel () throws IOException {
        //获取输入输出文件流
        FileInputStream in = new FileInputStream("G:\\剑指BAT\\代码\\FileChannel.txt");
        FileOutputStream out = new FileOutputStream("G:\\剑指BAT\\代码\\OutFileChannel.txt");
        //创建一个大小为512字节缓冲区
        ByteBuffer buf = ByteBuffer.allocate(512);
        //获取文件输入输出管道
        FileChannel fin = in.getChannel();
        FileChannel fo = out.getChannel();

        System.out.println("读取数据之前 ："+buf);
        //读取文件管道中的内容到ByteBuffer中
        int read = fin.read(buf);
        System.out.println("读取数据之后 ："+buf);
        //反转缓冲区，以便写入数据
        buf.flip();
        System.out.println("反转缓冲区 ："+buf);
        //将获取到的内容写入到文件管道中
        int write = fo.write(buf);
        System.out.println("写入数据之后 ："+buf);

        System.out.println("读取的字节数  :"+read);
        System.out.println("写入的字节数  :"+write);

    }
```

运行结果：

```
读取数据之前 ：java.nio.HeapByteBuffer[pos=0 lim=512 cap=512]
读取数据之后 ：java.nio.HeapByteBuffer[pos=17 lim=512 cap=512]
反转缓冲区 ：java.nio.HeapByteBuffer[pos=0 lim=17 cap=512]
写入数据之后 ：java.nio.HeapByteBuffer[pos=17 lim=17 cap=512]
读取的字节数  :17
写入的字节数  :17
```

例2读取的是例1中的FileChannel.txt文件，然后通过文件管道把该文本的内容写入到OutFileChannel.txt中，内容是 Dream it possible

注：OutFileChannel.txt文件原来并不存在，只是程序运行后产生的文件！

**小结：**

通过阅读源码可知

**FileChannel 的read方法的步骤如下：**

* 申请一块和缓存同大小的DirectByteBuffer；
* 读取数据到缓存，底层由NativeDispatcher的read实现；
* 把DirectByteBuffer的数据读取到用户定义的缓存，在jvm中分配内存。

**FileChannel 的write方法的步骤如下：**

* 申请一块DirectByteBuffer，大小为ByteBuffer中的limit - position；
* 复制byteBuffer中的数据到DirectByteBuffer中；
* 把数据从DirectByteBuffer中写入到文件，底层由NativeDispatcher的write实现。

**其实，read方法和write方法都导致数据复制了两次！**

  
**6.分散读取（Scattering Reads）与聚集写入（Gathering Writes）**

* 分散读取是指从Channel 中读取的数据“分散”到多个Buffer 中。
 
  注意：按照缓冲区的顺序，从Channel 中读取的数据依次将Buffer 填满。

图解  
![](http://img.blog.csdn.net/20170612195056802?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

* 聚集写入是指将多个Buffer 中的数据“聚集” 到Channel。
 
  注意：按照缓冲区的顺序，写入position 和limit 之间的数据到Channel 。

![](http://img.blog.csdn.net/20170612195303273?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

注：scatter / gather经常用于需要将传输的数据分开处理的场合，例如传输一个由消息头和消息体组成的消息，你可能会将消息体和消息头分散到不同的buffer中，这样你可以方便的处理消息头和消息体。

**7.符集 ChatSet**

编码：字符串→字节数组  
解码：字节数组→字符串

###  **Selector**

**1.引入**

* 传统的IO 流都是阻塞式的。也就是说，当一个线程调用read\(\) 或write\(\) 时，该线程被阻塞，直到有一些数据被读取或写入，该线程在此期间不能执行其他任务。因此，在完成网络通信进行IO 操作时，由于线程会阻塞，所以服务器端必须为每个客户端都提供一个独立的线程进行处理， 当服务器端需要处理大量客户端时，性能急剧下降。

* NIO 是非阻塞模式的。当线程从某通道进行读写数据时，若没有数据可用时，该线程可以进行其他任务。线程通常将非阻塞IO 的空闲时间用于在其他通道上执行IO 操作，所以单独的线程可以管理多个输入和输出通道。因此，NIO 可以让服务器端使用一个或有限几个线程来同时处理连接到服务器端的所有客户端。

**2.定义**

多路复用器器（Selector） 是SelectableChannle 对象的多路复用器，Selector 可以同时监控多个SelectableChannel 的IO 状况，也就是说，利用Selector 可使一个单独的线程管理多个Channel。Selector 是非阻塞IO 的核心。

SelectableChannle 的结构如下图：

![](http://img.blog.csdn.net/20170612195708604?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

**3.常用方法**

（1）open方法 ： 创建Selector

例：

```
//创建选择器
Selector selector = Selector.open();
```

（2）register方法 ：向多路复用器器注册通道

注：当调用register\(Selector sel, int ops\) 将通道注册选择器时，选择器对通道的监听事件，需要通过第二个参数ops 指定。

* 可以监听的事件类型（可使用SelectionKey 的四个常量表示）：
 
  读: SelectionKey.OP\_READ （1）
 
  写: SelectionKey.OP\_WRITE （4）
 
  连接: SelectionKey.OP\_CONNECT （8）
 
  接收: SelectionKey.OP\_ACCEPT （16）

例：

```
SelectionKey key = channel.register(selector,Selectionkey.OP_READ);
```

注：注册事件后会产生一个SelectionKey：它表示SelectableChannel 和Selector 之间的注册关系。每次向选择器注册通道时就会选择一个事件\(选择键\)。选择键包含两个表示为整数值的操作集。操作集的每一位都表示该键的通道所支持的一类可选择操作。

（3）wakeup方法：使尚未返回的第一个选择操作立即返回

**作用：**

* 解除阻塞在Selector.select\(\)/select\(long\)上的线程，立即返回。

* 两次成功的select之间多次调用wakeup等价于一次调用。

* 如果当前没有阻塞在select上，则本次wakeup调用将作用于下一次select“记忆”作用。

**为什么要唤醒？**

* 注册了新的channel或者事件。

* channel关闭，取消注册。

* 优先级更高的事件触发（如定时器事件），希望及时处理。

（4）其它方法

| 方法 | 作用 |
| :--- | :--- |
| abstract void close\(\) | 关闭此选择器 |
| abstract boolean isOpen\(\) | 告知此选择器是否已打开。 |
| abstract Set keys\(\) | 返回此选择器的键集 |
| abstract SelectorProvider provider\(\) | 返回创建此通道的提供者 |
| abstract int select\(\) | 选择一组键，其相应的通道已为 I/O 操作准备就绪 |
| abstract int select\(long timeout\) | 选择一组键，其相应的通道已为 I/O 操作准备就绪 |
| abstract Set selectedKeys\(\) | 返回此选择器的已选择键集。 |
| abstract int selectNow\(\) | 选择一组键，其相应的通道已为 I/O 操作准备就绪 |

  


**4.EPollSelectorImpl类**

（1）定义

JDK 1.7 NIO Selector在linux平台上的实现类是sun.nio.ch.EPollSelectorImpl，这个类通过linux下的epoll系列系统调用实现NIO，epoll是poll/select系统调用的一个改进版本，能以更高的性能实现IO事件的检测和分发（主要归功于epoll的事件回调机制）

（2）实现

Java类sun.nio.ch.EPollSelectorImpl主要的功能都委托给sun.nio.ch. EPollArrayWrapper实现

```
package sun.nio.ch;

class EPollArrayWrapper{
    private native int epollCreate();
    private native void epollCtl(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
    private native int epollWait(long paramLong1, int paramInt1, long paramLong2, int paramInt2) throws IOException;
}
```

分析：可看到这三个native方法正是对上述epoll系列系统调用的包装。

（3）重要方法

EPollSelectorImpl. implRegister方法\(Selector.register方法的具体实现\)，通过调用epoll\_ctl向epoll实例中注册事件：

```
protected void implRegister(SelectionKeyImpl paramSelectionKeyImpl) {
    if (this.closed)
    throw new ClosedSelectorException();

    SelChImpl localSelChImpl = paramSelectionKeyImpl.channel;
    this.fdToKey.put(Integer.valueOf(localSelChImpl.getFDVal()), paramSelectionKeyImpl);
    this.pollWrapper.add(localSelChImpl);
    this.keys.add(paramSelectionKeyImpl);
}

```

分析：

* 上述方法中，除了向epoll实例注册事件外，还将注册的文件描述符\(fd\)与SelectionKey的对应关系添加到fdToKey中，这个map维护了文件描述符与SelectionKey的映射。

* 每当向Selector中注册一个Channel时，向此map中添加一条记录，而当Channel.close、SelectionKey.cancel方法调用时，则从fdToKey中移除与Channel的fd相关联的SelectionKey，

* 具体代码在EPollSelectorImpl.implDereg方法中。此方法的主要流程概括如下：

  * 通过epoll\_wait调用（this.pollWrapper.poll）获取已就绪的文件描述符集合
  * 通过fdToKey查找文件描述符对应的SelectionKey，并更新之，更新SelectionKey的具体代码在EPollSelectorImpl .updateSelectedKeys中：

（4）关于fdToKey的几个问题：

* 为什么fdToKey会变得非常大？

  * 注册到Selector上的Channel非常多，例如一个长连接服务器可能要同时维持数十万条连接；
  * 过期或失效的Channel没有及时关闭，因而对应的记录会一直留在fdToKey中，时间久了就会越积越多；

* 为何fdToKey总是串行读取?  
  fdToKey中记录的读取，是在select方法中进行的，而select方法一般而言总是单线程调用\(Selector不是线程安全的\)。

* tcp发包堆积对导致fdToKey变大吗？  
  一般而言不会，因为fdToKey只负责管理注册到Selector上的channel，与数据传输过程无关。当然，如果tcp发包堆积导致IO框架的空闲连接检测机制失效，无法及时检测并关闭空闲的连接，则有可能导致fdToKey变大。

### **Pipe** {#pipe}

1.定义

Java NIO 管道是2个线程之间的单向数据连接。Pipe有一个source通道和一个sink通道。数据会被写到sink通道，从source通道读取

图解  
![](http://img.blog.csdn.net/20170612201506115?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

2.实例：

```
  public void test1() throws IOException{
        //1. 获取管道
        Pipe pipe = Pipe.open();

        ByteBuffer buf = ByteBuffer.allocate(1024);
        Pipe.SinkChannel sinkChannel = pipe.sink();
        buf.put("通过单向管道发送数据".getBytes());
        buf.flip();

        //2. 将缓冲区中的数据写入管道
        sinkChannel.write(buf);

        //3. 读取缓冲区中的数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        buf.flip();
        int len = sourceChannel.read(buf);
        System.out.println(new String(buf.array(), 0, len));

        sourceChannel.close();
        sinkChannel.close();
    }
```

  
参考资料：

[Java NIO之EPollSelectorImpl详解](https://yq.aliyun.com/articles/58917)

[浅谈 Linux 中 Selector 的实现原理](http://www.jianshu.com/p/2b71ea919d49)

[http://blog.csdn.net/baiye\_xing/article/details/73135566](http://blog.csdn.net/baiye_xing/article/details/73135566)





