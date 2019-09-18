### **1.Buffer简介** {#1buffer简介}

NIO中的Buffer 主要用于与NIO 通道进行交互，数据是从通道读入缓冲区，从缓冲区写入通道中的。

在NIO中，所有的数据都是用Buffer处理的，它是NIO读写数据的中转池。Buffer实质上是一个数组，通常是一个字节数据，但也可以是其他类型的数组。但一个缓冲区不仅仅是一个数组，重要的是它提供了对数据的结构化访问，而且还可以跟踪系统的读写进程。

### **2.Buffer使用步骤** {#2buffer使用步骤}

（1）写入数据到 Buffer；

（2）调用 flip\(\) 方法；

（3）从 Buffer 中读取数据；

（4）调用 clear\(\) 方法或者 compact\(\) 方法。

### **3.Buffer类的继承关系** {#3buffer类的继承关系}

![](http://img.blog.csdn.net/20170612180924659?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

从上图可以看出，Buffer 的常用子类有：

* ByteBuffer
* CharBuffer
* ShortBuffer
* IntBuffer
* LongBuffer
* FloatBuffer
* DoubleBuffer

注：它们都通过allocate方法获取一个Buffer 对象：

static XxxBuffer allocate\(int capacity\) : 创建一个容量为capacity 的XxxBuffer 对象

**Buffer的常用方法**

| 方法 | 描述 |
| :--- | :--- |
| Buffer clear\(\) | 清空缓冲区并返回对缓冲区的引用 |
| Buffer flip\(\) | 将缓冲区的界限设置为当前位置，并将当前位置充值为0 |
| int capacity\(\) | 返回Buffer 的capacity 大小 |
| boolean hasRemaining\(\) | 判断缓冲区中是否还有元素 |
| int limit\(\) | 返回Buffer 的界限\(limit\) 的位置 |
| Buffer limit\(int n\) | 将设置缓冲区界限为n, 并返回一个具有新limit 的缓冲区对象 |
| Buffer mark\(\) | 对缓冲区设置标记 |
| int position\(\) | 返回缓冲区的当前位置position |
| Buffer position\(int n\) | 将设置缓冲区的当前位置为n , 并返回修改后的Buffer 对象 |
| int remaining\(\) | 返回position 和limit 之间的元素个数，可读数据 |
| Buffer reset\(\) | 将位置position 转到以前设置的mark 所在的位置 |
| Buffer rewind\(\) | 将位置设为0，limit不变， 取消设置的mark |

  
**针对Buffer的子类，我在此重点讲一下最常用的ByteBuffer**

  


### **ByteBuffer详解** {#bytebuffer详解}

**1.Buffer主要的属性**

源码如下：

```
 private int mark = -1;
 private int position = 0;
 private int limit;
 private int capacity;
```

* capacity ： 容量，表示缓冲区中最大存储数据的容量。一旦声明，不可改变。
* limit ： 界限，表示缓冲区中可以操作数据的大小。
* position ： 位置，表示缓冲区中正在操作数据的位置。
* mark：标记是一个索引，通过Buffer 中的mark\(\) 方法指定Buffer 中一个特定的position，之后可以通过调用reset\(\) 方法恢复到这个position.

* 标记、位置、限制、容量遵守以下不变式：  
  0&lt;=mark&lt;=position&lt;=limit&lt;=capacity

**2.常用方法**

（1）allocate\(int capacity\) ： 分配一个新的字节缓冲区

源码如下：

```
public static ByteBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }
```

注：通过源码可以看出该方法分配了一个堆缓冲区。

（2）allocateDirect\(int capacity\) ：分配新的直接字节缓冲区

源码如下：

```
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }
```

注：通过源码可以看出该方法分配了一个直接缓冲区。

**那么问题来了，直接缓冲区与非直接缓冲区到底有什么区别呢？**

直接字节缓冲区可以通过调用此类的allocateDirect\(\) 工厂方法来创建。此方法返回的缓冲区进行分配和取消分配所需成本通常高于非直接缓冲区。直接缓冲区的内容直接建立在物理内存\(操作系统内存页\)中，可以驻留在常规的垃圾回收堆之外，

非直接缓冲区通过allocate\(\)方法创建，此方法分配的缓冲区是堆缓冲区，在JVM缓存中，由ＪＶＭ进行管理。但相比直接缓冲区多了一次拷贝，但却是可控的。

**小结：**

* 使用了DirectByteBuffer，一般来说可以减少一次系统空间到用户空间的拷贝。但Buffer创建和销毁的成本更高，更不宜维护，通常会用内存池来提高性能。

* 如果数据量比较小的中小应用情况下，可以考虑使用heapBuffer；反之可以用directBuffer。

* 因为直接缓冲区是不可控的，它们对应用程序的内存需求量造成的影响可能并不明显。所以，建议将直接缓冲区主要分配给那些易受基础系统的本机I/O 操作影响的大型、持久的缓冲区。仅在直接缓冲区能在程序性能方面带来明显好处时分配它们，否则分配非直接缓冲区。

（３）get方法 ：获取缓冲区中的数据

* get\(\) ：读取单个字节（position向后递增）
* get\(byte\[\] dst\)：批量读取多个字节到dst 中
* get\(int index\)：读取指定索引位置的字节\(不会移动position\)

（4）put方法 ：存入数据到缓冲区中

* put\(byte b\)：将给定单个字节写入缓冲区的当前位置（position+1）
* put\(byte\[\] src\)：将src 中的字节写入缓冲区的当前位置
* put\(int index, byte b\)：将指定字节写入缓冲区的索引位置\(不会移动position\)

（5）wrap\(byte\[\] array\) ：将 byte 数组包装到缓冲区中

```
    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public static ByteBuffer wrap(byte[] array,
                                    int offset, int length)
    {
        try {
            return new HeapByteBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }
```

注：从源码可以看出，直接将字节数组放入ByteBuffer中，其实也是放入了堆内存中。

（6）flip方法 ： 反转此缓冲区

```
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }
```

注：可以看出该方法将position置为0，其实就是切换读写模式。

（7）clear方法 ：清除此缓冲区

```
  public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }
```

（8）rewind方法 ： 重绕此缓冲区

```
  public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }
```

（9）remaining方法 ： 返回当前位置与限制之间的元素数

```
    public final int remaining() {
        return limit - position;
    }
```

（10）mark方法 ：在此缓冲区的位置设置标记

```
   public final Buffer mark() {
        mark = position;
        return this;
    }
```

（11）reset方法　：将此缓冲区的位置重置为以前标记的位置

```
    public final Buffer reset() {
        int m = mark;
        if (m < 0)
            throw new InvalidMarkException();
        position = m;
        return this;
    }
```

**3.实例验证**

（1）例

```
public void byteBuffer() {
        //分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(11);
        System.out.println("位置： "+buffer.position());
        System.out.println("界限： "+buffer.limit());
        System.out.println("容量： "+buffer.capacity());

        //存入数据到缓冲区
        buffer.put("dream".getBytes());

        System.out.println("位置1： "+buffer.position());
        System.out.println("界限1： "+buffer.limit());
        System.out.println("容量1： "+buffer.capacity());

        //切换到读数据模式（position置为0，limit设置为position）
        buffer.flip();
        //可读字节数
        System.out.println("可读字节数 ："+buffer.remaining());

        System.out.println("位置2： "+buffer.position());
        System.out.println("界限2： "+buffer.limit());
        System.out.println("容量2： "+buffer.capacity());

        //读取缓冲区中的数据
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes,0,bytes.length));

        System.out.println("位置3： "+buffer.position());
        System.out.println("界限3： "+buffer.limit());
        System.out.println("容量3： "+buffer.capacity());

        //将位置设置为0（可重复读数据）
        buffer.rewind();

        System.out.println("位置4： "+buffer.position());
        System.out.println("界限4： "+buffer.limit());
        System.out.println("容量4： "+buffer.capacity());

        //清空缓冲区,数据依然存在，但处于被遗忘状态。
        buffer.clear();

        System.out.println("位置5： "+buffer.position());
        System.out.println("界限5： "+buffer.limit());
        System.out.println("容量5： "+buffer.capacity());

        System.out.println((char)buffer.get(0));
    }

    @Test
    public void markTest() {
        System.out.println("markTest方法");
        String str = "possible";
        ByteBuffer buf = ByteBuffer.allocate(512);
        buf.put(str.getBytes());
        buf.flip();
        byte[] bt = new byte[buf.limit()];
        buf.get(bt,0,2);
        System.out.println(new String(bt,0,2));
        System.out.println(buf.position());

        //标记
        buf.mark();
        buf.get(bt,2,2);
        System.out.println(new String(bt,2,2));
        System.out.println(buf.position());

        //恢复到mark的位置
        buf.reset();
        System.out.println(buf.position());
    }

```

运行结果：

```
位置： 0
界限： 11
容量： 11
位置1： 5
界限1： 11
容量1： 11
可读字节数 ：5
位置2： 0
界限2： 5
容量2： 11
dream
位置3： 5
界限3： 5
容量3： 11
位置4： 0
界限4： 5
容量4： 11
位置5： 0
界限5： 11
容量5： 11
d

markTest方法
po
2
ss
4
2

```

（2）图解

![](http://img.blog.csdn.net/20170612191010637?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")  
利用allocate\(11\)方法创建了一个大小为11个byte的数组的缓冲区，初始状态如上图，position的位置为0，capacity和limit默认都是数组长度11。当我们用put\(“dream”.getBytes\(\)\)方法写入5个字节时，变化如下图：

![](http://img.blog.csdn.net/20170612191250239?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

如果我们需要读取缓冲区的数据，就需要调用flip\(\)方法，变化如下图所示\(position设回0，并将limit设成之前的position的值\)

![](http://img.blog.csdn.net/20170612191355021?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYmFpeWVfeGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

这时底层操作系统就可以从缓冲区中正确读取这个5个字节数据，在下一次写数据之前我们再调用clear\(\)方法，缓冲区的索引位置又回到了初始位置。



[http://blog.csdn.net/baiye\_xing/article/details/73134818](http://blog.csdn.net/baiye_xing/article/details/73134818)

