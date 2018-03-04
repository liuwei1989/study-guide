### **零拷贝的定义** {#零拷贝的定义}

* Zero-copy, 就是在操作数据时, 不需要将数据 buffer 从一个内存区域拷贝到另一个内存区域. 因为少了一次内存的拷贝, 因此 CPU 的效率就得到的提升.

* 在 OS 层面上的 Zero-copy 通常指避免在 用户态\(User-space\) 与 内核态\(Kernel-space\) 之间来回拷贝数据。

* 但Netty 中的 Zero-copy 与 OS 的 Zero-copy 不太一样, Netty的 Zero-coyp 完全是在用户态\(Java 层面\)的, 它的 Zero-copy 的更多的是偏向于 优化数据操作 。

### **Netty的“零拷贝”主要体现以下几个方面：** {#netty的零拷贝主要体现以下几个方面}

1.Netty的接收和发送ByteBuffer采用DIRECT BUFFERS，使用堆外直接内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。如果使用传统的堆内存（HEAP BUFFERS）进行Socket读写，JVM会将堆内存Buffer拷贝一份到直接内存中，然后才写入Socket中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。

2.Netty 提供了 CompositeByteBuf 类, 它可以将多个 ByteBuf 合并为一个逻辑上的 ByteBuf, 避免了传统通过内存拷贝的方式将几个小Buffer合并成一个大的Buffer。

3.通过 FileRegion 包装的FileChannel.tranferTo方法 实现文件传输, 可以直接将文件缓冲区的数据发送到目标 Channel，避免了传统通过循环write方式导致的内存拷贝问题。

4.通过 wrap 操作, 我们可以将 byte\[\] 数组、ByteBuf、ByteBuffer等包装成一个 Netty ByteBuf 对象, 进而避免了拷贝操作。

### **零拷贝的具体分析** {#零拷贝的具体分析}

**1.ByteBuffer分配Direct Buffers**

源码如下：

![](http://img.blog.csdn.net/20170616211402651?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")  
![](http://img.blog.csdn.net/20170616211419265?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

分析：从源码知，ByteBuffer由ChannelConfig分配，而ChannelConfig创建ByteBufAllocator默认使用Direct Buffer，这就避免了读写数据的二次内存拷贝问题，从而实现了读写Socket的零拷贝功能，

**2.用CompositeByteBuf 类实现了将多个 ByteBuf 合并为一个逻辑上的 ByteBuf**

例：

```
//定义两个ByteBuf类型的 body 和 header 
CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
compositeByteBuf.addComponents(true, header, body);
```

分析：addComponents方法将 header 与 body 合并为一个逻辑上的 ByteBuf, 这两个 ByteBuf 在CompositeByteBuf 内部都是单独存在的, CompositeByteBuf 只是逻辑上是一个整体

图解：  
![](http://img.blog.csdn.net/20170616212330821?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

**注：**

* addComponents方法的参数是 true, 它表示当添加新的 ByteBuf 时, 自动递增 CompositeByteBuf 的 writeIndex，若没有这个参数，那么 compositeByteBuf 的 writeIndex 仍然是0, 就不可能从 compositeByteBuf 中读取到数据,

* 除了直接使用 CompositeByteBuf 类外, 还可以使用 Unpooled.wrappedBuffer 方法, 它底层封装了 CompositeByteBuf 操作,  
  例：`ByteBuf allByteBuf = Unpooled.wrappedBuffer(header, body);`

**3.通过 FileRegion 实现零拷贝**

例3.1：使用NIO实现零拷贝

```
public static void copyFileWithFileChannel(String srcFileName, String destFileName) throws Exception {
    RandomAccessFile srcFile = new RandomAccessFile(srcFileName, "r");
    FileChannel srcFileChannel = srcFile.getChannel();

    RandomAccessFile destFile = new RandomAccessFile(destFileName, "rw");
    FileChannel destFileChannel = destFile.getChannel();

    long position = 0;
    long count = srcFileChannel.size();

    srcFileChannel.transferTo(position, count, destFileChannel);
}
```

分析：有了 FileChannel 后, 就可以直接将源文件的内容通过transferTo\)方法直接拷贝到目的文件中, 而不需要额外借助一个临时 buffer, 避免了不必要的内存操作.

**4.通过 wrap / slice 实现零拷贝**

例4.1： wrap方法

```
ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
```

分析：通过wrappedBuffer 方法来将 bytes 包装成为一个 UnpooledHeapByteBuf 对象, 而在包装的过程中, 是不会有拷贝操作的.

例4.2：slice 方法

```
ByteBuf header = byteBuf.slice(0, 5);
ByteBuf body = byteBuf.slice(5, 10);
```

分析： slice 操作可以将一个 ByteBuf 切片 为多个共享一个存储区域的 ByteBuf 对象.它产生 header 和 body 的过程是没有拷贝操作的, header 和 body 对象在内部其实是共享了 byteBuf 存储空间的不同部分而已.

注：也可以设置Netty的接收Buffer为堆内存模式，有两种方法

* boot.option\(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT\)

* socketchannel.config.setAllocator\(UnpooledByteBufAllocator.DEFAULT\)  

参考资料：

1. [对 Netty 的Zero Copy理解](https://segmentfault.com/a/1190000007560884)

2. [Netty高性能之道](http://www.infoq.com/cn/articles/netty-high-performance?utm_source=infoq&utm_medium=popular_links...)



