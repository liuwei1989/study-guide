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









