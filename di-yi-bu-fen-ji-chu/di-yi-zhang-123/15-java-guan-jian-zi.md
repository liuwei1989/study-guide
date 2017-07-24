# super 关键词 {#super-关键词}

调用父类 \(Superclass\) 的成员或者方法

* 调用父类的构造函数

1. 调用父类 \(Superclass\) 的成员或者方法

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



