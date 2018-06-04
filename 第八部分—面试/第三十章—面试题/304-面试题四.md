# [9个Java初始化和回收的面试题](http://www.cnblogs.com/huajiezh/p/5790940.html)



**1.Java中是如何区分重载方法的？**



通过重载方法的参数类型和顺序来进行区分的。



注意：若参数类型和顺序均相同时，不管参数名是否相同，编译器均会报错，提示方法已经被定义。且不能根据返回值类型来区分，如果根据返回值来区分的话，有时程序里调用方法时并不需要返回值，那么程序都无法确定该调用那个重载方法。



**2.阅读以下程序，解释其中的错误。**

> public static void testLong\(long i\) {
>
> System.out.println\("test long"\);
>
> }
>
>
>
> public static void testFloat\(float i\) {
>
> System.out.println\("test float"\);
>
> }
>
> public static void main\(String\[\] args\) {
>
> testLong\(50\);
>
> testFloat\(1.5\);
>
> }

testLong没有问题，因为传递的参数50是int型的，而接收方参数是long型的，小范围可以自动转型为大范围的数据类型；testFloat不会通过编译，因为传递的参数1.5是double类型的，而接收方参数是float类型的，大范围转型为小范围数据类型需要显式转换，即改为testFloat\(1.5f\)。



**3.阅读以下程序，解释其中的错误。**

> public static class A {
>
> A\(int i\) {
>
> System.out.println\("A\(int i\)"\);
>
> }
>
> }
>
> public static void main\(String\[\] args\) {
>
> A a = new A\(\);
>
> }

在定义了自定义构造器后，若要使用默认构造器，则需要显式指定默认构造器，否则A a = new A\(\);不能编译通过。



**4.阅读以下程序，解释其中的错误。**



> public static class A {
>
> A\(\) {
>
> System.out.println\("A\(\)"\);
>
> }
>
> A\(int i\) {
>
> System.out.println\("A\(int i\)"\);
>
> }
>
> A\(int i, int j\) {
>
> A\(\);
>
> A\(i\);
>
> System.out.println\("A\(int i, int j\)"\);
>
> }
>
> }

在一个构造器中调用其它构造器时，需要使用this关键字进行调用，如this\(\)；在一个构造器中可调用仅一个其它构造器，并且调用其它构造器的语句需放在调用者（即发出调用行为的构造器）语句块的第一行。



**5.阅读以下程序，写出执行结果。**

> public static class A {
>
> private int i;
>
> private String j;
>
> int getI\(\) {
>
> return i;
>
> }
>
> String getJ\(\) {
>
> return j;
>
> }
>
> A\(int i\) {
>
> i = i;
>
> }
>
> A\(String j\) {
>
> this.j = j;
>
> }
>
> }
>
> public static void main\(String\[\] args\) {
>
> System.out.println\(new A\(5\).getI\(\)\);
>
> System.out.println\(new A\("hello"\).getJ\(\)\);
>
> }

**执行结果为：**



0



hello



对于i = i;这个语句而言，它并未改变实例变量i的值，且i的默认值为0，因此结果也为0，若需要改变实例变量i的值，需要改为this.i = i;



**6.在一个类中，声明了若干个static方法和非static方法，请谈谈声明的static方法是否能访问声明的非static方法，说明理由？**



static方法不能访问非static方法，因为static方法是属于这个类本身的一个方法，在编译期间就已经确定了；而非static方法是属于这个类的对象的方法，需要在实例化之后才能访问到。若在static方法中访问非static方法，将不能通过编译。



**7.static关键字为何不能修饰局部变量？**



static关键字修饰的变量或方法是属于类的，在编译时就已经确定了；而普通变量或方法是属于该由类生成的对象，需要在实例化后才能确定。因此，若static关键字修饰了方法的局部变量，一方面方法需要在实例化之后才能确定，另一方面static修饰的变量需要在编译时确定，这就会导致矛盾。



**8.finalize\(\)有何用途？什么情况下需要调用这个函数？**



在需要释放内存的地方调用finalize\(\)，则在下一轮垃圾回收时会回收占用的内存，一般情况下不需要显式调用此函数。



垃圾回收器只能回收那些由new关键字创建的对象所占用的内存，那么有些不是通过这种方式（比如调用C++本地方法）所占用的内存如何回收呢？那么就需要使用finalize\(\)了。由于C++中需要使用free\(\)函数来释放内存，所以Java程序在调用C++时需要调用finalize\(\)方法来释放内存。



**9.列出并简要解释几种常见垃圾回收技术。**



**引用计数：**每个对象都包含了一个引用计数器，每被引用一次，计数器都加1，引用者被置为null或者销毁，计数器就减1。垃圾收集器进行轮询，一旦发现计数器的值小于1，就回收该对象占用的内存。



**停止复制：**在垃圾回收机制运行时，程序需要停止运行，将每个活动的对象由一个堆转移到另一个堆，留下的垃圾会被回收。



**标记清除：**从堆栈和静态存储区域开始，寻找到活的对象就对其进行标记，所有的标记过程完成后，就对垃圾进行回收。

