**问：静态变量和实例变量的区别？**

答：

* 在语法定义上的区别：静态变量前要加static关键字，而实例变量前则不加。

* 在程序运行时的区别：实例变量属于某个对象的属性， 必须创建了实例对象\(比如 new 一个\)， 其中的实例变量才会被分配空间， 才能使用这个实例变量. 静态变量不属于某个实例对象， 而是属于类， 所以也称为类变量， 只要程序加载了类的字节码， 不用创建任何实例对象, 静态变量就会被分配空间, 静态变量就可以被使用了.

* 总之，实例变量必须创建对象后才可以通过这个对象来使用，静态变量则可以直接使用类名来引用.

例如, 对于下面的程序, 无论创建多少个实例对象, 永远都只分配了一个staticVar变量, 并且每创建一个实例对象, 这个staticVar就会加; 但是, 每创建一个实例对象, 就会分配一个instanceVar, 即可能分配多个instanceVar, 并且每个instanceVar的值都只自加了1次.

```java
public class VariantTest{

        public static int staticVar = 0;
        public int instanceVar = 0;

        public VariantTest(){
               staticVar++;
               instanceVar++;
               System.out.println(“staticVar=” + staticVar + ”,instanceVar=”+ instanceVar);
        }
}
```

**问：Java变量是否有默认值？**

答：Java变量的初始化，如果不赋值，将会有个默认值，对于基本类型，比如int,long是0, boolean 是false等，引用类型如果不设置，将会是null。

**问：java变量初始化顺序？**

答：Java 先初始化静态变量和静态块，然后再初始化非静态变量和块，这些都在构造方法调用前调用。

**问：说一说java变量与内存？**

答：

java变量主要分为基本类型和引用类型：

基本类型：

```
  布尔型：boolean\(1\)

  整型：byte\(8\),short\(16\),int\(32\),long\(64\)

  字符型：char\(16\)

  浮点型：float\(32\),double\(64\)
```

因为java是使用虚拟机的跨平台语言，所以java的这几个类型的长度是不会随着使用系统的不同而发生位数的变化的（这点不同于C++） 基本类型是可以进行转换的，包括：自动类型转换和强制类型转换。

![](http://upload-images.jianshu.io/upload_images/1902495-767477733ca23333.png?imageMogr2/auto-orient/strip|imageView2/2/w/1240)  
强制类型转换：当低精度的数据转化为高精度的数据时，数据值不会丢失，反之会影响数据精度

引用类型：类，接口，数组（null也可作为一个类型）

数组类型：

```
    定义数组：type\[\] arrayName;或者 type arrayName\[\];  

    初始化数组： 静态初始化：arrayName = new type\[\]{element1,element2,element3...}; 如：int\[\] a = {5,6,7,8};  

    动态初始化：arrayName = new type\[length\];如：int a = new int\[5\]; 
```

数组在内存中是占用连续的内存空间，所以数组的运行效率比较高优于集合。数组的引用变量是存放在栈内存中，引用的实际数组存放在堆内存中。数组变量引用的是数组在堆内存中的首地址，当使用具体下标查找某个数组时指向的是该对应数据的引用地址。

String类:首先String是不可变的，一旦生成值就不可以再改变，可以另外生成一个新的String，改变引用，但是不会改变之前生成的String。如下:部分String源码

```java
public final class String implement java.io.Seriallizable,Comparable<String>,CharSequence{ 
    private final char value[]; 
    private final int offset;  
    private final int count; 
    private int hash;
}
```

从代码中可以看出来，String类其实是对字符数组的封装，value是封装的数组，offset是这个value数组的初始位置,count是数组所占字符的个数。从代码中可以看出来，所提供的这几个变量全都是private的，并且由final修饰（当然这些也是引用，是可以通过反射修改的）。实际的String内存存储情况为

![](http://upload-images.jianshu.io/upload_images/1902495-9babe25b5da8d220.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

关于

String str0 = "hello";

String str1 = "hello";

String str2 = new String\("hello"\);

String str3 = new String\("hello"\);

"hello"是在编译器就确定的存放在常量池中，str0和str1指向的是常量池中的同一个对象，而str2和str3指向的是对象，存放在堆内存中是不同的对象。

关于

String str1 = "ab"+"cd"+"ef";  与

StringBuffer str2 =new StringBuffer\(\);

str2.append\(cd\);str2.append\("ef"\)

创建的字符个数：str1生成了"abcdef"变量，也就是生成了一个String变量。而str2始终一直是只有一个StringBuffer变量。所以效率之间的比较也就一目了然。

详细内存分析：

Java内存模型把Java虚拟机内部划分为线程栈和堆。这张图演示了Java内存模型的逻辑视图。

![](http://upload-images.jianshu.io/upload_images/1902495-4fe89dbff2e47459.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

每一个运行在Java虚拟机里的线程都拥有自己的线程栈。这个线程栈包含了这个线程调用的方法当前执行点相关的信息。一个线程仅能访问自己的线程栈。一个线程创建的本地变量对其它线程不可见，仅自己可见。即使两个线程执行同样的代码，这两个线程任然在在自己的线程栈中的代码来创建本地变量。因此，每个线程拥有每个本地变量的独有版本。

所有原始类型的本地变量都存放在线程栈上，因此对其它线程不可见。一个线程可能向另一个线程传递一个原始类型变量的拷贝，但是它不能共享这个原始类型变量自身。

堆上包含在Java程序中创建的所有对象，无论是哪一个对象创建的。这包括原始类型的对象版本。如果一个对象被创建然后赋值给一个局部变量，或者用来作为另一个对象的成员变量，这个对象任然是存放在堆上。下面这张图演示了调用栈和本地变量存放在线程栈上，对象存放在堆上。

![](http://upload-images.jianshu.io/upload_images/1902495-e8c80bbbf6927036.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

一个本地变量可能是原始类型，在这种情况下，它总是“呆在”线程栈上。  
一个本地变量也可能是指向一个对象的一个引用。在这种情况下，引用（这个本地变量）存放在线程栈上，但是对象本身存放在堆上。

一个对象可能包含方法，这些方法可能包含本地变量。这些本地变量任然存放在线程栈上，即使这些方法所属的对象存放在堆上。

一个对象的成员变量可能随着这个对象自身存放在堆上。不管这个成员变量是原始类型还是引用类型。

静态成员变量跟随着类定义一起也存放在堆上。

存放在堆上的对象可以被所有持有对这个对象引用的线程访问。当一个线程可以访问一个对象时，它也可以访问这个对象的成员变量。如果两个线程同时调用同一个对象上的同一个方法，它们将会都访问这个对象的成员变量，但是每一个线程都拥有这个本地变量的私有拷贝。

下图演示了上面提到的点：

![](http://upload-images.jianshu.io/upload_images/1902495-84b097d017609c15.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

两个线程拥有一些列的本地变量。其中一个本地变量（Local Variable 2）执行堆上的一个共享对象（Object 3）。这两个线程分别拥有同一个对象的不同引用。这些引用都是本地变量，因此存放在各自线程的线程栈上。这两个不同的引用指向堆上同一个对象。

注意，这个共享对象（Object 3）持有Object2和Object4一个引用作为其成员变量（如图中Object3指向Object2和Object4的箭头）。通过在Object3中这些成员变量引用，这两个线程就可以访问Object2和Object4。

这张图也展示了指向堆上两个不同对象的一个本地变量。在这种情况下，指向两个不同对象的引用不是同一个对象。理论上，两个线程都可以访问Object1和Object5，如果两个线程都拥有两个对象的引用。但是在上图中，每一个线程仅有一个引用指向两个对象其中之一。

```
public class MyRunnable implements Runnable {
        public void run() {
            methodOne();
        }

        public void methodOne() {
            int localVariable1 = 45;
            MySharedObject localVariable2 = MySharedObject.sharedInstance;     //... do more with local variables.
            methodTwo();
        }

        public void methodTwo() {
            Integer localVariable1 = new Integer(99);     //... do more with local variable.
        }
}
```

```java
public class MySharedObject { //static variable pointing to instance of MySharedObject

        public static final MySharedObject sharedInstance = new MySharedObject(); //member variables pointing to two objects on the heap

        public Integer object2 = new Integer(22);
        public Integer object4 = new Integer(44);
        public long member1 = 12345;
        public long member2 = 67890;
    }
```

如果两个线程同时执行run\(\)方法，就会出现上图所示的情景。run\(\)方法调用methodOne\(\)方法，methodOne\(\)调用methodTwo\(\)方法。

methodOne\(\)声明了一个原始类型的本地变量和一个引用类型的本地变量。

每个线程执行methodOne\(\)都会在它们对应的线程栈上创建localVariable1和localVariable2的私有拷贝。localVariable1变量彼此完全独立，仅“生活”在每个线程的线程栈上。一个线程看不到另一个线程对它的localVariable1私有拷贝做出的修改。

每个线程执行methodOne\(\)时也将会创建它们各自的localVariable2拷贝。然而，两个localVariable2的不同拷贝都指向堆上的同一个对象。代码中通过一个静态变量设置localVariable2指向一个对象引用。仅存在一个静态变量的一份拷贝，这份拷贝存放在堆上。因此，localVariable2的两份拷贝都指向由MySharedObject指向的静态变量的同一个实例。MySharedObject实例也存放在堆上。它对应于上图中的Object3。

注意，MySharedObject类也包含两个成员变量。这些成员变量随着这个对象存放在堆上。这两个成员变量指向另外两个Integer对象。这些Integer对象对应于上图中的Object2和Object4.

注意，methodTwo\(\)创建一个名为localVariable的本地变量。这个成员变量是一个指向一个Integer对象的对象引用。这个方法设置localVariable1引用指向一个新的Integer实例。在执行methodTwo方法时，localVariable1引用将会在每个线程中存放一份拷贝。这两个Integer对象实例化将会被存储堆上，但是每次执行这个方法时，这个方法都会创建一个新的Integer对象，两个线程执行这个方法将会创建两个不同的Integer实例。methodTwo方法创建的Integer对象对应于上图中的Object1和Object5。

还有一点，MySharedObject类中的两个long类型的成员变量是原始类型的。因为，这些变量是成员变量，所以它们任然随着该对象存放在堆上，仅有本地变量存放在线程栈上。

硬件内存架构

现代硬件内存模型与Java内存模型有一些不同。理解内存模型架构以及Java内存模型如何与它协同工作也是非常重要的。这部分描述了通用的硬件内存架构，下面的部分将会描述Java内存是如何与它“联手”工作的。

下面是现代计算机硬件架构的简单图示：

![](http://upload-images.jianshu.io/upload_images/1902495-525c0967e29e4edd.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

一个现代计算机通常由两个或者多个CPU。其中一些CPU还有多核。从这一点可以看出，在一个有两个或者多个CPU的现代计算机上同时运行多个线程是可能的。每个CPU在某一时刻运行一个线程是没有问题的。这意味着，如果你的Java程序是多线程的，在你的Java程序中每个CPU上一个线程可能同时（并发）执行。

每个CPU都包含一系列的寄存器，它们是CPU内内存的基础。CPU在寄存器上执行操作的速度远大于在主存上执行的速度。这是因为CPU访问寄存器的速度远大于主存。

每个CPU可能还有一个CPU缓存层。实际上，绝大多数的现代CPU都有一定大小的缓存层。CPU访问缓存层的速度快于访问主存的速度，但通常比访问内部寄存器的速度还要慢一点。一些CPU还有多层缓存，但这些对理解Java内存模型如何和内存交互不是那么重要。只要知道CPU中可以有一个缓存层就可以了。

一个计算机还包含一个主存。所有的CPU都可以访问主存。主存通常比CPU中的缓存大得多。

通常情况下，当一个CPU需要读取主存时，它会将主存的部分读到CPU缓存中。它甚至可能将缓存中的部分内容读到它的内部寄存器中，然后在寄存器中执行操作。当CPU需要将结果写回到主存中去时，它会将内部寄存器的值刷新到缓存中，然后在某个时间点将值刷新回主存。

当CPU需要在缓存层存放一些东西的时候，存放在缓存中的内容通常会被刷新回主存。CPU缓存可以在某一时刻将数据局部写到它的内存中，和在某一时刻局部刷新它的内存。它不会再某一时刻读/写整个缓存。通常，在一个被称作“cache lines”的更小的内存块中缓存被更新。一个或者多个缓存行可能被读到缓存，一个或者多个缓存行可能再被刷新回主存。

Java内存模型和硬件内存架构之间的桥接

上面已经提到，Java内存模型与硬件内存架构之间存在差异。硬件内存架构没有区分线程栈和堆。对于硬件，所有的线程栈和堆都分布在主内中。部分线程栈和堆可能有时候会出现在CPU缓存中和CPU内部的寄存器中。如下图所示：

![](http://upload-images.jianshu.io/upload_images/1902495-71cecd817fd82aac.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

当对象和变量被存放在计算机中各种不同的内存区域中时，就可能会出现一些具体的问题。主要包括如下两个方面：

线程对共享变量修改的可见性

当读，写和检查共享变量时出现race conditions

下面我们专门来解释以下这两个问题。

共享对象可见性

如果两个或者更多的线程在没有正确的使用Volatile声明或者同步的情况下共享一个对象，一个线程更新这个共享对象可能对其它线程来说是不接见的。

想象一下，共享对象被初始化在主存中。跑在CPU上的一个线程将这个共享对象读到CPU缓存中。然后修改了这个对象。只要CPU缓存没有被刷新会主存，对象修改后的版本对跑在其它CPU上的线程都是不可见的。这种方式可能导致每个线程拥有这个共享对象的私有拷贝，每个拷贝停留在不同的CPU缓存中。

下图示意了这种情形。跑在左边CPU的线程拷贝这个共享对象到它的CPU缓存中，然后将count变量的值修改为2。这个修改对跑在右边CPU上的其它线程是不可见的，因为修改后的count的值还没有被刷新回主存中去。

![](http://upload-images.jianshu.io/upload_images/1902495-49d455bef3a7605f.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)

解决这个问题你可以使用Java中的volatile关键字。volatile关键字可以保证直接从主存中读取一个变量，如果这个变量被修改后，总是会被写回到主存中去。

Race Conditions

如果两个或者更多的线程共享一个对象，多个线程在这个共享对象上更新变量，就有可能发生race conditions。

想象一下，如果线程A读一个共享对象的变量count到它的CPU缓存中。再想象一下，线程B也做了同样的事情，但是往一个不同的CPU缓存中。现在线程A将count加1，线程B也做了同样的事情。现在count已经被增在了两个，每个CPU缓存中一次。

如果这些增加操作被顺序的执行，变量count应该被增加两次，然后原值+2被写回到主存中去。

然而，两次增加都是在没有适当的同步下并发执行的。无论是线程A还是线程B将count修改后的版本写回到主存中取，修改后的值仅会被原值大1，尽管增加了两次。

下图演示了上面描述的情况：

![](http://upload-images.jianshu.io/upload_images/1902495-2bc2bc75e544475a.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1240/format/jpg)  
解决这个问题可以使用Java同步块。一个同步块可以保证在同一时刻仅有一个线程可以进入代码的临界区。同步块还可以保证代码块中所有被访问的变量将会从主存中读入，当线程退出同步代码块时，所有被更新的变量都会被刷新回主存中去，不管这个变量是否被声明为volatile。

