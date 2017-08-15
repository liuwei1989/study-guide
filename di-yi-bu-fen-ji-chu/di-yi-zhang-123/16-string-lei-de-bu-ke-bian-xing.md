String很多实用的特性，比如说“不可变性”，是工程师精心设计的艺术品！艺术品易碎！用final就是拒绝继承，防止世界被熊孩子破坏，维护世界和平！

# 1. 什么是不可变？

String不可变很简单，如下图，给一个已有字符串"abcd"第二次赋值成"abcedl"，不是在原内存地址上修改数据，而是重新指向一个新对象，新地址。

![](http://img.blog.csdn.net/20160902151207962?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

# 2. String为什么不可变？

翻开JDK源码，[**Java**](http://lib.csdn.net/base/java)**.lang.String**类起手前三行，是这样写的：

```
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {  
    /** String本质是个char数组. 而且用final关键字修饰.*/  
    private final char value[];  
    ...  
    ...  
}
```

首先String类是用final关键字修饰，这说明String不可继承。再看下面，String类的主力成员字段value是个char\[ \]数组，而且是用**final**修饰的。final修饰的字段创建以后就不可改变。

有的人以为故事就这样完了，其实没有。因为虽然value是不可变，也只是value这个引用地址不可变。挡不住**Array数组是可变的**事实。Array的[数据结构](http://lib.csdn.net/base/datastructure)看下图

![](http://img.blog.csdn.net/20160902151342786)

也就是说Array变量只是stack上的一个引用，数组的本体结构在heap堆。String类里的value用final修饰，只是说stack里的这个叫value的引用地址不可变。没有说堆里array本身数据不可变。看下面这个例子：

```
final int[] value={1,2,3}  
int[] another={4,5,6};  
value=another;    //编译器报错，final不可变  
```

value用final修饰，编译器不允许我把value指向堆区另一个地址。但如果我直接对数组元素动手，分分钟搞定。

```
final int[] value={1,2,3};  
value[2]=100;  //这时候数组里已经是{1,2,100}  
```

或者更粗暴的反射直接改，也是可以的。

```
final int[] array={1,2,3};  
Array.set(array,2,100); //数组也被改成{1,2,100}  
```

所以String是不可变，**关键是**因为SUN公司的工程师，在后面所有String的方法里很小心的没有去动Array里的元素，没有暴露内部成员字段。

private final char value\[\]这一句里，private的私有访问权限的作用都比final大。而且设计师还很小心地把整个String设成final禁止继承，避免被其他人继承后破坏。所以**String是不可变的关键都在底层的实现，而不是一个final。**考验的是工程师构造数据类型，封装数据的功力。

# 3. 不可变有什么好处？

这个最简单的原因，就是为了**安全**。



## 不可变性支持线程安全



还有一个大家都知道，就是在并发场景下，多个线程同时读一个资源，是不会引发竟态条件的。只有对资源做写操作才有危险。不可变对象不能被写，所以线程安全。**  
  
**

## 不可变性支持字符串常量池

最后别忘了String另外一个字符串常量池的属性。像下面这样字符串one和two都用字面量"something"赋值。它们其实都指向同一个内存地址。

```
String one = "someString";  
String two = "someString";  
```

  


![](http://img.blog.csdn.net/20160902151942395)



这样在大量使用字符串的情况下，可以节省内存空间，提高效率。但之所以能实现这个特性，String的不可变性是最基本的一个必要条件。要是内存里字符串内容能改来改去，这么做就完全没有意义了。  


  




