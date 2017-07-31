# 2.JVM结构

![](http://upload-images.jianshu.io/upload_images/5401760-cde4aefdad5438ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> java是基于一门虚拟机的语言，所以了解并且熟知虚拟机运行原理非常重要。

## 2.1 方法区

方法区，Method Area， 对于习惯在HotSpot虚拟机上开发和部署程序的开发者来说，很多人愿意把方法区称为“永久代”（Permanent Generation），本质上两者并不等价，仅仅是因为HotSpot虚拟机的设计团队选择把GC分代收集扩展至方法区，或者说使用永久代来实现方法区而已。对于其他虚拟机（如BEA JRockit、IBM J9等）来说是不存在永久代的概念的。

主要存放已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据（比如spring 使用IOC或者AOP创建bean时，或者使用cglib，反射的形式动态生成class信息等）。

> 注意：JDK 6 时，String等字符串常量的信息是置于方法区中的，但是到了JDK 7 时，已经移动到了Java堆。所以，方法区也好，Java堆也罢，到底详细的保存了什么，其实没有具体定论，要结合不同的JVM版本来分析。
>
> ### 异常
>
> 当方法区无法满足内存分配需求时，将抛出OutOfMemoryError。  
> 运行时常量池溢出：比如一直往常量池加入数据，就会引起OutOfMemoryError异常。

### 类信息

> 1. 类型全限定名。
> 2. 类型的直接超类的全限定名（除非这个类型是java.lang.Object，它没有超类）。
> 3. 类型是类类型还是接口类型。
> 4. 类型的访问修饰符（public、abstract或final的某个子集）。
> 5. 任何直接超接口的全限定名的有序列表。
> 6. 类型的常量池。
> 7. 字段信息。
> 8. 方法信息。
> 9. 除了常量意外的所有类（静态）变量。
> 10. 一个到类ClassLoader的引用。
> 11. 一个到Class类的引用。

## 2.1.1 常量池

### 2.1.1.1 Class文件中的常量池

在Class文件结构中，最头的4个字节用于存储Megic Number，用于确定一个文件是否能被JVM接受，再接着4个字节用于存储版本号，前2个字节存储次版本号，后2个存储主版本号，再接着是用于存放常量的常量池，由于常量的数量是不固定的，所以常量池的入口放置一个U2类型的数据\(constant\_pool\_count\)存储常量池容量计数值。

常量池主要用于存放两大类常量：字面量\(Literal\)和符号引用量\(Symbolic References\)，字面量相当于Java语言层面常量的概念，如文本字符串，声明为final的常量值等，符号引用则属于编译原理方面的概念，包括了如下三种类型的常量：

* 类和接口的全限定名
* 字段名称和描述符
* 方法名称和描述符

### 2.1.1.2 运行时常量池

CLass文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是常量池，用于存放编译期生成的各种字面量和符号引用，这部分内容将在类加载后进入方法区的运行时常量池中存放。

运行时常量池相对于CLass文件常量池的另外一个重要特征是具备动态性，Java语言并不要求常量一定只有编译期才能产生，也就是并非预置入CLass文件中常量池的内容才能进入方法区运行时常量池，运行期间也可能将新的常量放入池中，这种特性被开发人员利用比较多的就是String类的intern\(\)方法。

### 2.1.1.3 常量池的好处

常量池是为了避免频繁的创建和销毁对象而影响系统性能，其实现了对象的共享。

例如字符串常量池，在编译阶段就把所有的字符串文字放到一个常量池中。

* （1）节省内存空间：常量池中所有相同的字符串常量被合并，只占用一个空间。
* （2）节省运行时间：比较字符串时，\==比equals\(\)快。对于两个引用变量，只用==判断引用是否相等，也就可以判断实际值是否相等。

> 双等号==的含义
>
> * 基本数据类型之间应用双等号，比较的是他们的数值。
> * 复合数据类型\(类\)之间应用双等号，比较的是他们在内存中的存放地址。

### 2.1.1.4 基本类型的包装类和常量池

java中基本类型的包装类的大部分都实现了常量池技术，即Byte,Short,Integer,Long,Character,Boolean。

这5种包装类默认创建了数值\[-128，127\]的相应类型的缓存数据，但是超出此范围仍然会去创建新的对象。 两种浮点数类型的包装类Float,Double并没有实现常量池技术。

#### Integer与常量池

```
Integer i1 = 40;
Integer i2 = 40;
Integer i3 = 0;
Integer i4 = new Integer(40);
Integer i5 = new Integer(40);
Integer i6 = new Integer(0);

System.out.println("i1=i2   " + (i1 == i2));
System.out.println("i1=i2+i3   " + (i1 == i2 + i3));
System.out.println("i1=i4   " + (i1 == i4));
System.out.println("i4=i5   " + (i4 == i5));
System.out.println("i4=i5+i6   " + (i4 == i5 + i6));  
System.out.println("40=i5+i6   " + (40 == i5 + i6));


i1=i2   true
i1=i2+i3   true
i1=i4   false
i4=i5   false
i4=i5+i6   true
40=i5+i6   true
```

#### 解释：

* \(1\)Integer i1=40；Java在编译的时候会直接将代码封装成Integer i1=Integer.valueOf\(40\);，从而使用常量池中的对象。
* \(2\)Integer i1 = new Integer\(40\);这种情况下会创建新的对象。
* \(3\)语句i4 == i5 + i6，因为+这个操作符不适用于Integer对象，首先i5和i6进行自动拆箱操作，进行数值相加，即i4 == 40。然后Integer对象无法与数值进行直接比较，所以i4自动拆箱转为int值40，最终这条语句转为40 == 40进行数值比较。

#### String与常量池

```
String str1 = "abcd";
String str2 = new String("abcd");
System.out.println(str1==str2);//false

String str1 = "str";
String str2 = "ing";
String str3 = "str" + "ing";
String str4 = str1 + str2;
System.out.println(str3 == str4);//false

String str5 = "string";
System.out.println(str3 == str5);//true
```

#### 解释：

* \(1\)new String\("abcd"\)是在常量池中拿对象，"abcd"是直接在堆内存空间创建一个新的对象。只要使用new方法，便需要创建新的对象。
* \(2\)连接表达式 +
  只有使用引号包含文本的方式创建的String对象之间使用“+”连接产生的新对象才会被加入字符串池中。
  对于所有包含new方式新建对象（包括null）的“+”连接表达式，它所产生的新对象都不会被加入字符串池中。

```
public static final String A; // 常量A
public static final String B;    // 常量B
static {  
   A = "ab";  
   B = "cd";  
}  
public static void main(String[] args) {  
// 将两个常量用+连接对s进行初始化  
String s = A + B;  
String t = "abcd";  
if (s == t) {  
    System.out.println("s等于t，它们是同一个对象");  
  } else {  
    System.out.println("s不等于t，它们不是同一个对象");  
  }  
}
```

#### 解释：

s不等于t，它们不是同一个对象。

A和B虽然被定义为常量，但是它们都没有马上被赋值。在运算出s的值之前，他们何时被赋值，以及被赋予什么样的值，都是个变数。因此A和B在被赋值之前，性质类似于一个变量。那么s就不能在编译期被确定，而只能在运行时被创建了。

```
String s1 = new String("xyz"); //创建了几个对象？
```

#### 解释：

考虑类加载阶段和实际执行时。

* （1）类加载对一个类只会进行一次。”xyz”在类加载时就已经创建并驻留了（如果该类被加载之前已经有”xyz”字符串被驻留过则不需要重复创建用于驻留的”xyz”实例）。驻留的字符串是放在全局共享的字符串常量池中的。
* （2）在这段代码后续被运行的时候，”xyz”字面量对应的String实例已经固定了，不会再被重复创建。所以这段代码将常量池中的对象复制一份放到heap中，并且把heap中的这个对象的引用交给s1 持有。

这条语句创建了2个对象。

```
public static void main(String[] args) {
String s1 = new String("计算机");
String s2 = s1.intern();
String s3 = "计算机";
System.out.println("s1 == s2? " + (s1 == s2));
System.out.println("s3 == s2? " + (s3 == s2));
}
s1 == s2? false
s3 == s2? true
```

#### 解释：

String的intern\(\)方法会查找在常量池中是否存在一份equal相等的字符串,如果有则返回该字符串的引用,如果没有则添加自己的字符串进入常量池。

```
public class Test {public static void main(String[] args) {
 String hello = "Hello", lo = "lo";
 System.out.println((hello == "Hello") + " "); //true
 System.out.println((Other.hello == hello) + " "); //true
 System.out.println((other.Other.hello == hello) + " "); //true
 System.out.println((hello == ("Hel"+"lo")) + " "); //true
 System.out.println((hello == ("Hel"+lo)) + " "); //false
 System.out.println(hello == ("Hel"+lo).intern()); //true
 }
}

class Other {
 static String hello = "Hello";
}


package other;

public class Other {
 public static String hello = "Hello";

```

#### 解释：

在同包同类下,引用自同一String对象.

在同包不同类下,引用自同一String对象.

在不同包不同类下,依然引用自同一String对象.

在编译成.class时能够识别为同一字符串的,自动优化成常量,引用自同一String对象.

在运行时创建的字符串具有独立的内存地址,所以不引用自同一String对象.

## 2.2 堆

Heap（堆）是JVM的内存数据区。

一个虚拟机实例只对应一个堆空间，堆是线程共享的。堆空间是存放对象实例的地方，几乎所有对象实例都在这里分配。堆也是垃圾收集器管理的主要区域\(也被称为GC堆\)。堆可以处于物理上不连续的内存空间中，只要逻辑上相连就行。

 Heap 的管理很复杂，每次分配不定长的内存空间，专门用来保存对象的实例。在Heap 中分配一定的内存来保存对象实例，实际上也只是保存对象实例的属性值，属性的类型和对象本身的类型标记等，并不保存对象的方法（方法是指令，保存在Stack中）。而对象实例在Heap中分配好以后，需要在Stack中保存一个4字节的Heap 内存地址，用来定位该对象实例在Heap 中的位置，便于找到该对象实例。

> #### 异常
>
> 堆中没有足够的内存进行对象实例分配时，并且堆也无法扩展时，会抛出OutOfMemoryError异常。

![](http://upload-images.jianshu.io/upload_images/5401760-4dc5c9650f200baf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 2.3 Java栈

Stack（栈）是JVM的内存指令区。

描述的是java方法执行的内存模型：每个方法被执行的时候都会同时创建一个栈帧，用于存放局部变量表（基本类型、对象引用）、操作数栈、方法返回、常量池指针等信息。 由编译器自动分配释放， 内存的分配是连续的。Stack的速度很快，管理很简单，并且每次操作的数据或者指令字节长度是已知的。所以Java 基本数据类型，Java 指令代码，常量都保存在Stack中。

虚拟机只会对栈进行两种操作，以帧为单位的入栈和出栈。Java栈中的每个帧都保存一个方法调用的局部变量、操作数栈、指向常量池的指针等，且每一次方法调用都会创建一个帧，并压栈。

> #### 异常
>
> * 如果一个线程请求的栈深度大于虚拟机所允许的深度，将抛出StackOverflowError异常， 比如递归调用。
> * 如果线程生成数量过多，无法申请足够多的内存时，则会抛出OutOfMemoryError异常。比如tomcat请求数量非常多时，设置最大请求数。

### 2.3.1 栈帧

栈帧由三部分组成：局部变量区、操作数栈、帧数据区。

#### 2.3.1.1 局部变量区

包含方法的参数和局部变量。

以一个静态方法为例

```
public class Demo {
     public static int doStaticMethod(int i, long l, float f, Object o, byte b) {
         return 0;
     }
 }
```

编译之后的具备变量表字节码如下：

```
LOCALVARIABLEiIL0L10
LOCALVARIABLElJL0L11
LOCALVARIABLEfFL0L13
LOCALVARIABLEoLjava/lang/Object;L0L14
LOCALVARIABLEbBL0L15
MAXSTACK=1    //该方法操作栈的最大深度
MAXLOCALS=6  //确定了该方法所需要分配的最大局部变量表的容量
```

可以认为Java栈帧里的局部变量表有很多的槽位组成，每个槽最大可以容纳32位的数据类型，故方法参数里的int i 参数占据了一个槽位，而long l 参数就占据了两个槽（1和2），Object对象类型的参数其实是一个引用，o相当于一个指针，也就是32位大小。byte类型升为int，也是32位大小。如下：

```
0 int int i
1 long long l
3 float float f
4 reference Object o
5 int byte b
```

实例方法的局部变量表和静态方法基本一样，唯一区别就是实例方法在Java栈帧的局部变量表里第一个槽位（0位置）存的是一个this引用（当前对象的引用），后面就和静态方法的一样了。

#### 2.3.1.2 操作数栈

Java没有寄存器，故所有参数传递使用Java栈帧里的操作数栈，操作数栈被组织成一个以字长为单位的数组，它是通过标准的栈操作-入栈和出栈来进行访问，而不是通过索引访问。

看一个例子：

![](http://upload-images.jianshu.io/upload_images/5401760-65c59b4372215de0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 注意，对于局部变量表的槽位，按照从0开始的顺序，依次是方法参数，之后是方法内的局部变量，局部变量0就是a，1就是b，2就是c…… 编译之后的字节码为：

```
// access flags 0x9
  public static add(II)I
   L0
    LINENUMBER 18 L0 // 对应源代码第18行，以此类推
    ICONST_0 // 把常量0 push 到Java栈帧的操作数栈里
    ISTORE 2 // 将0从操作数栈pop到局部变量表槽2里（c），完成赋值
   L1
    LINENUMBER 19 L1
    ILOAD 0 // 将局部变量槽位0（a）push 到Java栈帧的操作数栈里
    ILOAD 1 // 把局部变量槽1（b）push到操作数栈 
    IADD // pop出a和b两个变量，求和，把结果push到操作数栈
    ISTORE 2 // 把结果从操作数栈pop到局部变量2（a+b的和给c赋值）
   L2
    LINENUMBER 21 L2
    ILOAD 2 // 局部变量2（c）push 到操作数栈
    IRETURN // 返回结果
   L3
    LOCALVARIABLE a I L0 L3 0
    LOCALVARIABLE b I L0 L3 1
    LOCALVARIABLE c I L1 L3 2
    MAXSTACK = 2
    MAXLOCALS = 3
```

发现，整个计算过程的参数传递和操作数栈密切相关！如图：

![](http://upload-images.jianshu.io/upload_images/5401760-72e1051ed23c63c1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



#### 2.3.1.3 栈数据区

存放一些用于支持常量池解析（常量池指针）、正常方法返回以及异常派发机制的信息。即将常量池的符号引用转化为直接地址引用、恢复发起调用的方法的帧进行正常返回，发生异常时转交异常表进行处理。

## 2.4 本地方法栈

Native Method Stack

访问本地方式时使用到的栈，为本地方法服务， 也就是调用虚拟机使用到的Native方法服务。也会抛出StackOverflowError和OutOfMemoryError异常。

## 2.5 PC寄存器

每个线程都拥有一个PC寄存器，线程私有的。  
PC寄存器的内容总是下一条将被执行指令的"地址"，这里的"地址"可以是一个本地指针，也可以是在方法字节码中相对于该方法起始指令的偏移量。如果该线程正在执行一个本地方法，则程序计数器内容为undefined，区域在Java虚拟机规范中没有规定任何OutOfMemoryError情况的区域。

## 2.6 堆与栈

### 2.6.1 堆与栈里存什么

* 1）堆中存的是对象。栈中存的是基本数据类型和堆中对象的引用。一个对象的大小是不可估计的，或者说是可以动态变化的，但是在栈中，一个对象只对应了一个4btye的引用。
* 2）为什么不把基本类型放堆中呢？因为其占用的空间一般是1~8个字节——需要空间比较少，而且因为是基本类型，所以不会出现动态增长的情况——长度固定，因此栈中存储就够了，如果把他存在堆中是没有什么意义的。可以这么说，基本类型和对象的引用都是存放在栈中，而且都是几个字节的一个数，因此在程序运行时，他们的处理方式是统一的。但是基本类型、对象引用和对象本身就有所区别了，因为一个是栈中的数据一个是堆中的数据。最常见的一个问题就是，Java中参数传递时的问题。
* 3）Java中的参数传递时传值呢？还是传引用？程序运行永远都是在栈中进行的，因而参数传递时，只存在传递基本类型和对象引用的问题。不会直接传对象本身。

```
int a = 0; //全局初始化区

char p1; //全局未初始化区

main(){

  int b; //栈

  char s[] = "abc"; //栈

  char p2; //栈

  char p3 = "123456"; //123456\0在常量区，p3在栈上。

  static int c =0； //全局（静态）初始化区

  p1 = (char *)malloc(10); //堆

  p2 = (char *)malloc(20); //堆

}
```

### 2.6.2 堆内存与栈内存的区别

* 申请和回收方式不同：栈上的空间是自动分配自动回收的，所以栈上的数据的生存周期只是在函数的运行过程中，运行后就释放掉，不可以再访问。而堆上的数据只要程序员不释放空间，就一直可以访问到，不过缺点是一旦忘记释放会造成内存泄露。
* 碎片问题：对于栈，不会产生不连续的内存块；但是对于堆来说，不断的new、delete势必会产生上面所述的内部碎片和外部碎片。
* 申请大小的限制：栈是向低地址扩展的数据结构，是一块连续的内存的区域。栈顶的地址和栈的最大容量是系统预先规定好的，如果申请的空间超过栈的剩余空间，就会产生栈溢出；对于堆，是向高地址扩展的数据结构，是不连续的内存区域。堆的大小受限于计算机系统中有效的虚拟内存。由此可见，堆获得的空间比较灵活，也比较大。
* 申请效率的比较：栈由系统自动分配，速度较快。但程序员是无法控制的；堆：是由new分配的内存，一般速度比较慢，而且容易产生内存碎片,不过用起来最方便。

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
* 编译那些只会被运行一次的代码性价比太低，直接解释执行Java字节码反而更快。

* JVM在执行这些代码的时候，能获取到这些代码的信息，一段代码被执行的次数越多，JVM也对它们愈加熟悉，因此能够在对它们进行编译的时候做出一些优化。
```

在HotSpot VM中内嵌有两个JIT编译器，分别为Client Compiler和Server Compiler，但大多数情况下我们简称为C1编译器和C2编译器。开发人员可以通过如下命令显式指定Java虚拟机在运行时到底使用哪一种即时编译器，如下所示：

```
-client：指定Java虚拟机运行在Client模式下，并使用C1编译器；

-server：指定Java虚拟机运行在Server模式下，并使用C2编译器。
```

除了可以显式指定Java虚拟机在运行时到底使用哪一种即时编译器外，默认情况下HotSpot VM则会根据操作系统版本与物理机器的硬件性能自动选择运行在哪一种模式下，以及采用哪一种即时编译器。简单来说，C1编译器会对字节码进行简单和可靠的优化，以达到更快的编译速度；而C2编译器会启动一些编译耗时更长的优化，以获取更好的编译质量。不过在Java7版本之后，一旦开发人员在程序中显式指定命令“-server”时，缺省将会开启分层编译（Tiered Compilation）策略，由C1编译器和C2编译器相互协作共同来执行编译任务。不过在早期版本中，开发人员则只能够通过命令“-XX:+TieredCompilation”手动开启分层编译策略。

#### 总结

1. Java综合了编译型语言和解释性语言的优势。
2. Java会将类文件编译成为Java字节码，然后Java字节码会被JIT编译器选择性地编译成为CPU能够直接运行的二进制代码。
3. 将Java字节码编译成二进制代码后，性能会被大幅度提升。



