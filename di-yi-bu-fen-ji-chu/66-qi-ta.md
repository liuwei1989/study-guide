# 3.JIT编译器

1. JIT编译器是JVM的核心。它对于程序性能的影响最大。
2. CPU只能执行汇编代码或者二进制代码，所有程序都需要被翻译成它们，然后才能被CPU执行。
3. C++以及Fortran这类编译型语言都会通过一个静态的编译器将程序编译成CPU相关的二进制代码。
4. PHP以及Perl这列语言则是解释型语言，只需要安装正确的解释器，它们就能运行在任何CPU之上。当程序被执行的时候，程序代码会被逐行解释并执行。
5. 编译型语言的优缺点：
   * 速度快：因为在编译的时候它们能够获取到更多的有关程序结构的信息，从而有机会对它们进行优化。
   * 适用性差：它们编译得到的二进制代码往往是CPU相关的，在需要适配多种CPU时，可能需要编译多次。
6. 解释型语言的优缺点：
   * 适应性强：只需要安装正确的解释器，程序在任何CPU上都能够被运行
   * 速度慢：因为程序需要被逐行翻译，导致速度变慢。同时因为缺乏编译这一过程，执行代码不能通过编译器进行优化。
7. Java的做法是找到编译型语言和解释性语言的一个中间点：
   * Java代码会被编译：被编译成Java字节码，而不是针对某种CPU的二进制代码。
   * Java代码会被解释：Java字节码需要被java程序解释执行，此时，Java字节码被翻译成CPU相关的二进制代码。
   * JIT编译器的作用：在程序运行期间，将Java字节码编译成平台相关的二进制代码。正因为此编译行为发生在程序运行期间，所以该编译器被称为Just-In-Time编译器。

#### HotSpot 编译

HotSpot VM名字也体现了JIT编译器的工作方式。在VM开始运行一段代码时，并不会立即对它们进行编译。在程序中，总有那么一些“热点”区域，该区域的代码会被反复的执行。而JIT编译器只会编译这些“热点”区域的代码。

这么做的原因在于：

```
* 
编译那些只会被运行一次的代码性价比太低，直接解释执行Java字节码反而更快。

* 
JVM在执行这些代码的时候，能获取到这些代码的信息，一段代码被执行的次数越多，JVM也对它们愈加熟悉，因此能够在对它们进行编译的时候做出一些优化。
```

在HotSpot VM中内嵌有两个JIT编译器，分别为Client Compiler和Server Compiler，但大多数情况下我们简称为C1编译器和C2编译器。开发人员可以通过如下命令显式指定Java虚拟机在运行时到底使用哪一种即时编译器，如下所示：

```
-
client：指定Java虚拟机运行在Client模式下，并使用C1编译器；

-
server：指定Java虚拟机运行在Server模式下，并使用C2编译器。
```

除了可以显式指定Java虚拟机在运行时到底使用哪一种即时编译器外，默认情况下HotSpot VM则会根据操作系统版本与物理机器的硬件性能自动选择运行在哪一种模式下，以及采用哪一种即时编译器。简单来说，C1编译器会对字节码进行简单和可靠的优化，以达到更快的编译速度；而C2编译器会启动一些编译耗时更长的优化，以获取更好的编译质量。不过在Java7版本之后，一旦开发人员在程序中显式指定命令“-server”时，缺省将会开启分层编译（Tiered Compilation）策略，由C1编译器和C2编译器相互协作共同来执行编译任务。不过在早期版本中，开发人员则只能够通过命令“-XX:+TieredCompilation”手动开启分层编译策略。

#### 总结

1. Java综合了编译型语言和解释性语言的优势。
2. Java会将类文件编译成为Java字节码，然后Java字节码会被JIT编译器选择性地编译成为CPU能够直接运行的二进制代码。
3. 将Java字节码编译成二进制代码后，性能会被大幅度提升。





## [触发JVM进行Full GC的情况及应对策略](http://blog.csdn.net/chenleixing/article/details/46706039)

## **1、System.gc\(\)方法的调用**

此方法的调用是建议JVM进行Full GC,虽然只是建议而非一定,但很多情况下它会触发 Full GC,从而增加Full GC的频率,也即增加了间歇性停顿的次数。强烈影响系建议能不使用此方法就别使用，让虚拟机自己去管理它的内存，可通过通过-XX:+ DisableExplicitGC来禁止RMI调用System.gc。

##  2、老年代代空间不足

  
老年代空间只有在新生代对象转入及创建为大对象、大数组时才会出现不足的现象，当执行Full GC后空间仍然不足，则抛出如下错误：  
[Java](http://lib.csdn.net/base/java).lang.OutOfMemoryError:[Java](http://lib.csdn.net/base/java)heap space  
为避免以上两种状况引起的Full GC，调优时应尽量做到让对象在Minor GC阶段被回收、让对象在新生代多存活一段时间及不要创建过大的对象及数组。  


## 3、永生区空间不足

  
JVM规范中运行时数据区域中的方法区，在HotSpot虚拟机中又被习惯称为永生代或者永生区，Permanet Generation中存放的为一些class的信息、常量、静态变量等数据，当系统中要加载的类、反射的类和调用的方法较多时，Permanet Generation可能会被占满，在未配置为采用CMS GC的情况下也会执行Full GC。如果经过Full GC仍然回收不了，那么JVM会抛出如下错误信息：  
java.lang.OutOfMemoryError: PermGen space  
为避免Perm Gen占满造成Full GC现象，可采用的方法为增大Perm Gen空间或转为使用CMS GC。

##  4、CMS GC时出现promotion failed和concurrent mode failure

  
对于采用CMS进行老年代GC的程序而言，尤其要注意GC日志中是否有promotion failed和concurrent mode failure两种状况，当这两种状况出现时可能

会触发Full GC。  
promotion failed是在进行Minor GC时，survivor space放不下、对象只能放入老年代，而此时老年代也放不下造成的；concurrent mode failure是在

执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足造成的（有时候“空间不足”是CMS GC时当前的浮动垃圾过多导致暂时性的空间不足触发Full GC）。  
对措施为：增大survivor space、老年代空间或调低触发并发GC的比率，但在JDK 5.0+、6.0+的版本中有可能会由于JDK的bug29导致CMS在remark完毕

后很久才触发sweeping动作。对于这种状况，可通过设置-XX: CMSMaxAbortablePrecleanTime=5（单位为ms）来避免。

##  5、统计得到的Minor GC晋升到旧生代的平均大小大于老年代的剩余空间

  
这是一个较为复杂的触发情况，Hotspot为了避免由于新生代对象晋升到旧生代导致旧生代空间不足的现象，在进行Minor GC时，做了一个判断，如果之

前统计所得到的Minor GC晋升到旧生代的平均大小大于旧生代的剩余空间，那么就直接触发Full GC。  
例如程序第一次触发Minor GC后，有6MB的对象晋升到旧生代，那么当下一次Minor GC发生时，首先检查旧生代的剩余空间是否大于6MB，如果小于6MB，

则执行Full GC。  
当新生代采用PS GC时，方式稍有不同，PS GC是在Minor GC后也会检查，例如上面的例子中第一次Minor GC后，PS GC会检查此时旧生代的剩余空间是否

大于6MB，如小于，则触发对旧生代的回收。  
除了以上4种状况外，对于使用RMI来进行RPC或管理的Sun JDK应用而言，默认情况下会一小时执行一次Full GC。可通过在启动时通过- java -

Dsun.rmi.dgc.client.gcInterval=3600000来设置Full GC执行的间隔时间或通过-XX:+ DisableExplicitGC来禁止RMI调用System.gc。



## 6、堆中分配很大的对象



所谓大对象，是指需要大量连续内存空间的java对象，例如很长的数组，此种对象会直接进入老年代，而老年代虽然有很大的剩余空间，但是无法找到足够大的连续空间来分配给当前对象，此种情况就会触发JVM进行Full GC。

为了解决这个问题，CMS垃圾收集器提供了一个可配置的参数，即-XX:+UseCMSCompactAtFullCollection开关参数，用于在“享受”完Full GC服务之后额外免费赠送一个碎片整理的过程，内存整理的过程无法并发的，空间碎片问题没有了，但提顿时间不得不变长了，JVM设计者们还提供了另外一个参数 -XX:CMSFullGCsBeforeCompaction,这个参数用于设置在执行多少次不压缩的Full GC后,跟着来一次带压缩的。

