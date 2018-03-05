【情况一】：

java.lang.OutOfMemoryError: Java heap space：这种是java堆内存不够，一个原因是真不够，另一个原因是程序中有死循环；

如果是java堆内存不够的话，可以通过调整JVM下面的配置来解决：

&lt;jvm-arg&gt;-Xms3062m&lt;/jvm-arg&gt;

&lt;jvm-arg&gt;-Xmx3062m&lt;/jvm-arg&gt;

【情况二】

java.lang.OutOfMemoryError: GC overhead limit exceeded

【解释】：JDK6新增错误类型，当GC为释放很小空间占用大量时间时抛出；一般是因为堆太小，导致异常的原因，没有足够的内存。

【解决方案】：

1、查看系统是否有使用大内存的代码或死循环；

2、通过添加JVM配置，来限制使用内存：

&lt;jvm-arg&gt;-XX:-UseGCOverheadLimit&lt;/jvm-arg&gt;

【情况三】：

java.lang.OutOfMemoryError: PermGen space：这种是P区内存不够，可通过调整JVM的配置：

&lt;jvm-arg&gt;-XX:MaxPermSize=128m&lt;/jvm-arg&gt;

&lt;jvm-arg&gt;-XXermSize=128m&lt;/jvm-arg&gt;

【注】：

JVM的Perm区主要用于存放Class和Meta信息的,Class在被Loader时就会被放到PermGen space，这个区域成为方法区，GC在主程序运行期间不会对方法区进行清理，默认是64M大小，当程序需要加载的对象比较多时，超过64M就会报这部分内存溢出了，需要加大内存分配，一般128m足够。



Perm Gen可不叫年老代，而是永久代。 它还有一个逻辑概念，可以称为"方法区"。 在JDK1.8中Perm Gen已经去掉，取而代之的是本地内存Metaspace。



【情况四】：

java.lang.OutOfMemoryError: Direct buffer memory

调整-XX:MaxDirectMemorySize=参数，如添加JVM配置：

&lt;jvm-arg&gt;-XX:MaxDirectMemorySize=128m&lt;/jvm-arg&gt;

【情况五】：

java.lang.OutOfMemoryError: unable to create new native thread

【原因】：Stack空间不足以创建额外的线程，要么是创建的线程过多，要么是Stack空间确实小了。

【解决】：由于JVM没有提供参数设置总的stack空间大小，但可以设置单个线程栈的大小；而系统的用户空间一共是3G，除了Text/Data/BSS /MemoryMapping几个段之外，Heap和Stack空间的总量有限，是此消彼长的。因此遇到这个错误，可以通过两个途径解决：1.通过-Xss启动参数减少单个线程栈大小，这样便能开更多线程（当然不能太小，太小会出现StackOverflowError）；2.通过-Xms -Xmx两参数减少Heap大小，将内存让给Stack（前提是保证Heap空间够用）。

【情况六】：

java.lang.StackOverflowError

【原因】：这也内存溢出错误的一种，即线程栈的溢出，要么是方法调用层次过多（比如存在无限递归调用），要么是线程栈太小。

【解决】：优化程序设计，减少方法调用层次；调整-Xss参数增加线程栈大小。

