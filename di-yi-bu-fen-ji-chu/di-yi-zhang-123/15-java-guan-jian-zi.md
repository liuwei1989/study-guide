# super 关键词 {#super-关键词}

调用父类 \(Superclass\) 的成员或者方法

* 调用父类的构造函数

* 调用父类 \(Superclass\) 的成员或者方法

如果你的方法覆写一个父类成员的方法，你可以通过 super 关键字调用父类的方法。考虑下面的父类:

```
public class Superclass {

    public void printMethod() {
            System.out.println("Printed in Superclass.");
        }
}
```

下面是一个子类 \(subclass\), 叫做 Subclass, 覆写了 printMethod\(\)：

```
public class Subclass extends Superclass {

    // overrides printMethod in Superclass
    public void printMethod() {
        super.printMethod();
        System.out.println("Printed in Subclass");
    }
    public static void main(String[] args) {
        Subclass s = new Subclass();
        s.printMethod();
    }
}
```

输出

```
Printed in Superclass.
Printed in Subclass
```

1. 调用父类的构造函数

使用**super**关键字调用父类的构造函数。下面的 MountainBike 类是 Bicycle 类的子类. 它调用了父类的构造方法并加入了自己的初始化代码:

```
public MountainBike(int startHeight,
                    int startCadence,
                    int startSpeed,
                    int startGear) {
    super(startCadence, startSpeed, startGear);
    seatHeight = startHeight;
}
```

注：调用父类的构造体必须放在**第一行**.

使用

```
super();
```

或者:

```
super(parameter list);
```

通过 super\(\), 父类的无参构造体会被调用. 通过 super\(parameter list\), 父类对应参数的构造体会被调用.

注意: 构造体如果没有显式的调用父类的构造体, Java 编译器自动调用父类的无参构造。如果父类没有无参构造, 就会报错 \( compile-time error\)。

# Super 程序题 {#super-程序题}

**题目一**

```
class Base{
    Base(){
        System.out.println("Base");
    }
}

public class Checket extends Base{
    Checket(){
        System.out.println("Checket");
        super();
    }
    public static void main(String argv[]){
        Checket a = new Checket();
    }
}
```

输出是什么？ 是 compile time error. super\(\) 必须放在前面.

放在前面之后,输出为 Base Checket

**题目二**

```
import java.util.Date;

public class Test extends Date{

    public static void main(String[] args) {
       new Test().test();
    }

    public void test(){
       System.out.println(super.getClass().getName());
    }
}
```

返回的结果是 Test

因为super.getClass\(\).getName\(\) 调用了父类的 getClass\(\) 方法, 返回当前类

如果想得到父类的名称，应该用如下代码：

```
getClass().getSuperClass().getName()
```

**题目三**

```
public abstract class Car {

    String name = "Car";

    public String getName(){
        return name;
    }

    public abstract void demarre();
}

public class B extends Car{
    String name = "B";

    public String getName(){
        return name;
    }

    public void demarre() {
        System.out.println(getName() + " demarre");
    }
}

public class C extends B{
    String name = "C";

    public String getName(){
        return name;
    }

    public void demarreWithSuper() {
        System.out.println(super.getName() + " demarre");
    }

    public void demarreNoSuper() {
        System.out.println(getName() + " demarre");
    }
}

public class D extends B{
    public String getName(){
        return name;
    }

    public void demarreNoSuper() {
        System.out.println(getName() + " demarre");
    }
}

public class Test {
    public static void main(String[] args) {
        B b = new B();
        b.demarre();

        Car bCar = new B();
        bCar.demarre();

        C c = new C();
        c.demarre(); // c 里并没有定义这个函数
        c.demarreWithSuper();
        c.demarreNoSuper();

        D d = new D();
        d.demarre();

        transfer(c);    // TransferC
        transfer((B)c); // TransferB
        transfer(d);    // TransferB
    }

        public static void transfer(B b){
            System.out.println("TransferB");
            b.demarre();
        }

        public static void transfer(C c){
            System.out.println("TransferC");
            c.demarre();
        }
    }
}
```

输出是 B demarre B demarre C demarre B demarre C demarre B demarre TransferC C demarre TransferB C demarre TransferB B demarre



# this 程序题 {#this-程序题}

**题目一**

```
class Tester{
    int var;
    Tester(double var){this.var = (int)var};
    Tester(int var){this("hello");
    Tester(String s){
        this();
        System.out.println(s);
    }

    Tester(){ System.out.println("good-bye");}
}

```

Tester t = new Tester\(5\) 的输出是什么?

```
good-bye
hello

```

**题目二**

貌似和 this 无关但是很重要 

```
public class Base { int i; 
    Base(){
        add(1);
        System.out.println(i);
    }

    void add(int v){
        i+=v;
        System.out.println(i);
    }
}

public class MyBase extends Base{
    MyBase(){
        System.out.println("MyBase");
        add(2);
    }

    void add(int v){
        System.out.println("MyBase Add");
        i+=v*2;
        System.out.println(i);
    }
}

public class Test {
    public static void main(String[] args) {
        go(new MyBase());
    }

    static void go(Base b){
        b.add(8);
    }
}

```

输出的结果是 22

子类会首先调用父类的构造函数,在父类的构造函数 Base\(\) 中执行 add\(\) 方法. 但这个 add\(\) 方法由于是在新建 MyBase 对象时调用的. 所以是执行的 MyBase 中的 add 方法

在Java中，子类的构造过程中，必须 调用其父类的构造函数, 是因为有继承关系存在时, 子类要把父类的内容继承下来, 通过什么手段做到的？ 这样： 当你new一个子类对象的时候, 必须首先要new一个父类的对像出来, 这个父类对象位于子类对象的内部, 所以说，子类对象比父类对象大, 子类对象里面包含了一个父类的对象, 这是内存中真实的情况.

构造方法是new一个对象的时候, 必须要调的方法, 这是规定, 要new父类对象出来, 那么肯定要调用其构造方法, 所以**第一个规则：子类的构造过程中，必须 调用其父类的构造方法**

一个类, 如果我们不写构造方法, 那么编译器会帮我们加上一个默认的构造方法, 所谓默认的构造方法, 就是没有参数的构造方法, 但是如果你自己写了构造方法, 那么编译器就不会给你添加了

所以有时候当你new一个子类对象的时候，肯定调用了子类的构造方法，但是在子类构造方法中我们并没有显示的调用基类的构造方法，就是没写，如：super\(\); 并没有这样写，但是

**第二个规则：如果子类的构造方法中没有显示的调用基类构造方法，则系统默认调用基类无参数的构造方法**

注意：如果子类的构造方法中既没有显示的调用基类构造方法，而基类中又没有默认无参的构造方法，则编译出错，所以，通常我们需要显示的：super\(参数列表\)，来调用父类有参数的构造函数



# Static 关键字 {#static-关键字}

Static 关键字表明一个成员变量或者是成员方法可以在没有所属的类的实例的情况下直接被访问

声明为**static 的方法**有以下几条限制：+

1. 仅能调用其他的 static 方法
2. 只能访问 static 变量.
3. 不能以任何方式引用 this 或 super
4. 不能被覆盖.

声明为**static 的变量**实质上就是全局变量. \(+ final 就是全局**常**量\). 当声明一个对象时, 并不产生 static 变量的拷贝, 而是该类所有的实例变量共用同一个 static 变量.

对于静态类，只能用于嵌套类内部类中。



# final 关键字 {#final-关键字}

final 类是不能被继承的 这个类就是最终的了 不需要再继承修改 比如很多 java 标准库就是 final 类

final 方法不能被子方法重写

final + static 变量表示常量



