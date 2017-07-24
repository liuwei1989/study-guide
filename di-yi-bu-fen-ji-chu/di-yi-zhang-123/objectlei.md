**问：可以显示声明“继承Object类”吗？**

答：可以。在代码中明确地写出继承Object类没有语法错误。

Java把现实中的任何事物都当做一个对象\(Object\), Java是面向对象的，就是Object Orentied 简称OO 。此处的Object在Java中被定义为一个顶级父类，它是任何类父类，我们可以显示的继承它，也可以隐式继承，如以下实例：

```java
public class Dog extends Object{
}
或
public class Dog{
}
```

两种定义完全等价。Java没有强制声明“继承Object类”，如果这样的话，就不能继承除Object类之外别的类了，因为java不支持多继承。然而，即使不声明出来，也会默认继承了Object类。

Object类中包含的方法：

```java
equals(Object obj);
finalize();
getClass();
hashCode();
notify();
notifyAll();
wait();
Clone();
toString();protected Object clone()
boolean equals(Object obj)
protected void finalize()
Class< > getClass()
int hashCode()
void notify()
void notifyAll()
String toString()
void wait()
void wait(long timeout)
void wait(long timeout, int nanos)
```



**问：`clone()`函数是用来做什么的？**

答：`clone()`可以产生一个相同的类并且返回给调用者。



**问：`clone()`是如何工作的？**

答：`Object`将`clone()`作为一个本地方法来实现，这意味着它的代码存放在本地的库中。当代码执行的时候，将会检查调用对象的类\(或者父类\)是否实现了`java.lang.Cloneable`接口\(`Object`类不实现`Cloneable`\)。如果没有实现这个接口，`clone()`将会抛出一个检查异常\(`java.lang.CloneNotSupportedException`）。如果实现了这个接口，`clone()`会创建一个新的对象，并将原来对象的内容复制到新对象，最后返回这个新对象的引用。



**问：怎样调用`clone()`来克隆一个对象？**

答：用想要克隆的对象来调用`clone()`，将返回的对象从`Object`类转换到克隆的对象所属的类，赋给对象的引用。

```java
public class CloneDemo implements Cloneable {
    int x;
    public static void main(String[] args) throws CloneNotSupportedException {
        CloneDemo cd = new CloneDemo();
        cd.x = 5;
        System.out.printf("cd.x = %d%n", cd.x);
        CloneDemo cd2 = (CloneDemo) cd.clone();
        System.out.printf("cd2.x = %d%n", cd2.x);
    }
}
```



**问：什么情况下需要覆盖`clone()`方法呢？**

答：上面的例子中，调用`clone()`的代码是位于被克隆的类\(即`CloneDemo`类\)里面的，所以就不需要覆盖`clone()`了。但是，如果调用别的类中的`clone()`，就需要覆盖`clone()`了。否则，将会看到“`clone`在`Object`中是被保护的”提示，因为`clone()`在`Object`中的权限是`protected`。



**问：什么是浅克隆？**

答：浅克隆\(也叫做浅拷贝\)仅仅复制了这个对象本身的成员变量，该对象如果引用了其他对象的话，也不对其复制。如果一个对象中的所有成员变量都是原始类型，并且其引用了的对象都是不可改变的\(大多情况下都是\)时，使用浅克隆效果很好！但是，如果其引用了可变的对象，那么这些变化将会影响到该对象和它克隆出的所有对象！



**问：什么是深克隆？**

答：深克隆\(也叫做深复制\)会复制这个对象和它所引用的对象的成员变量，如果该对象引用了其他对象，深克隆也会对其复制。



**问：如何克隆一个数组？**

答：对数组类型进行浅克隆可以利用`clone()`方法。对数组使用`clone()`时，不必将`clone()`的返回值类型转换为数组类型

```java
class City {
    private String name;
    City(String name) {
        this.name = name;
    }
    String getName() {
        return name;
    }
    void setName(String name) {
        this.name = name;
    }    
}

public class CloneDemo {
    public static void main(String[] args) {
        double[] temps = { 98.6, 32.0, 100.0, 212.0, 53.5};
        for(double temp : temps)
            System.out.printf("%.1f ", temp);
        System.out.println();
        double[] temps2 = temps.clone();
        for(double temp : temps2)
            System.out.printf("%.1f ", temp);
        System.out.println();
        City[] cities = { new City("Denver"), new City("Chicago") };
        for(City city : cities)
            System.out.printf("%s ", city.getName());
        System.out.println();
        City[] cities2 = cities.clone();
        for(City city : cities2)
            System.out.printf("%s ", city.getName());
        System.out.println();
        cities[0].setName("Dallas");
        for(City city : cities2)
            System.out.printf("%s ", city.getName());
        System.out.println();
    }
}
```



**问：euqals\(\)函数是用来做什么的？**

答：equals\(\)函数可以用来检查一个对象与调用这个equals\(\)的这个对象是否相等。



**问：为什么不用“==”运算符来判断两个对象是否相等呢？**

答：虽然“==”运算符可以比较两个数据是否相等，但是要来比较对象的话，恐怕达不到预期的结果。就是说，“==”通过是否引用了同一个对象来判断两个对象是否相等，这被称为“引用相等”。这个运算符不能通过比较两个对象的内容来判断它们是不是逻辑上的相等。



**问：使用Object类的equals\(\)方法可以用来做什么样的对比？**

答：Object类默认的eqauls\(\)函数进行比较的依据是：调用它的对象和传入的对象的引用是否相等。也就是说，默认的equals\(\)进行的是引用比较。如果两个引用是相同的，equals\(\)函数返回true；否则，返回false。源码如下：

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```



**问：覆盖equals\(\)函数的时候要遵守那些规则？**

答：覆盖equals\(\)函数的时候需要遵守的规则在[Oracle](http://lib.csdn.net/base/oracle)官方的文档中都有申明：

* 自反性：对于任意非空的引用值x，x.equals\(x\)返回值为真。
* 对称性：对于任意非空的引用值x和y，x.equals\(y\)必须和y.equals\(x\)返回相同的结果。
* 传递性：对于任意的非空引用值x,y和z,如果x.equals\(y\)返回真，y.equals\(z\)返回真，那么x.equals\(z\)也必须返回真。
* 一致性：对于任意非空的引用值x和y，无论调用x.equals\(y\)多少次，都要返回相同的结果。在比较的过程中，对象中的数据不能被修改。
* 对于任意的非空引用值x，x.equals\(null\)必须返回假。



**问：能提供一个正确覆盖equals\(\)的示例吗？**

```java
class Employee {
    private String name;
    private int age;

    Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Employee))
            return false;
        Employee e = (Employee) o;
        return e.getName().equals(name) && e.getAge() == age;
    }

    String getName() {
        return name;
    }

    int getAge() {
        return age;
    }
}

public class EqualityDemo {
    public static void main(String[] args) {
        Employee e1 = new Employee("John Doe", 29);
        Employee e2 = new Employee("Jane Doe", 33);
        Employee e3 = new Employee("John Doe", 29);
        Employee e4 = new Employee("John Doe", 27 + 2);
        // 验证自反性。
        System.out.printf("Demonstrating reflexivity...%n%n");
        System.out.printf("e1.equals(e1): %b%n", e1.equals(e1));
        // 验证对称性。
        System.out.printf("%nDemonstrating symmetry...%n%n");
        System.out.printf("e1.equals(e2): %b%n", e1.equals(e2));
        System.out.printf("e2.equals(e1): %b%n", e2.equals(e1));
        System.out.printf("e1.equals(e3): %b%n", e1.equals(e3));
        System.out.printf("e3.equals(e1): %b%n", e3.equals(e1));
        System.out.printf("e2.equals(e3): %b%n", e2.equals(e3));
        System.out.printf("e3.equals(e2): %b%n", e3.equals(e2));
        // 验证传递性。
        System.out.printf("%nDemonstrating transitivity...%n%n");
        System.out.printf("e1.equals(e3): %b%n", e1.equals(e3));
        System.out.printf("e3.equals(e4): %b%n", e3.equals(e4));
        System.out.printf("e1.equals(e4): %b%n", e1.equals(e4));
        // 验证一致性。
        System.out.printf("%nDemonstrating consistency...%n%n");
        for (int i = 0; i < 5; i++) {
            System.out.printf("e1.equals(e2): %b%n", e1.equals(e2));
            System.out.printf("e1.equals(e3): %b%n", e1.equals(e3));
        }
        // 验证传入非空集合时，返回值为false。
        System.out.printf("%nDemonstrating null check...%n%n");
        System.out.printf("e1.equals(null): %b%n", e1.equals(null));
    }
}
```



**问：`finalize()`方法是用来做什么的？**

答：`finalize()`方法可以被子类对象所覆盖，然后作为一个终结者，当GC被调用的时候完成最后的清理工作（例如释放系统资源之类）。这就是终止。默认的`finalize()`方法什么也不做，当被调用时直接返回。

> 对于任何一个对象，它的`finalize()`方法都不会被JVM执行两次。如果你想让一个对象能够被再次调用（例如：分配它的引用给一个静态变量），注意当这个对象已经被GC回收的时候，`finalize()`方法不会被调用第二次。



**问： 有人说要避免使用`finalize()`方法，这是真的吗？**

答： 通常来讲，你应该尽量避免使用`finalize()`。相对于其他JVM实现，终结器被调用的情况较少——可能是因为终结器线程的优先级别较低的原因。如果你依靠终结器来关闭文件或者其他系统资源，可能会将资源耗尽，当程序试图打开一个新的文件或者新的系统资源的时候可能会崩溃，就因为这个缓慢的终结器。



**问： 如果`finalize()`抛出异常会怎样？**

答： 当`finalize()`抛出异常的时候会被忽略。而且，对象的终结将在此停止，导致对象处在一种不确定的状态。如果另一个进程试图使用这个对象的话，将产生不确定的结果。通常抛出异常将会导致线程终止并产生一个提示信息，但是从`finalize()`中抛出异常就不会。



**问：`gerClass()`方法是用来做什么的？**

答： 通过`gerClass()`方法可以得到一个和这个类有关的`java.lang.Class`对象。



**问： 还有其他方法得到`Class`对象吗？**

答： 获取`Class`对象的方法有两种。可以使用_类字面常量_，它的名字和类型相同，后缀位.class；例如：`Account.class`。另外一种就是调用`Class`的`forName()`方法。类字面常量更加简洁，并且编译器强制类型安全；如果找不到指定的类编译就不会通过。通过`forName()`可以动态地通过指定包名载入任意类型地引用。但是，不能保证类型安全，可能会导致`Runtime`异常。



**问：`hashCode()`方法是用来做什么的？**

答：`hashCode()`方法返回给调用者此对象的哈希码（其值由一个hash函数计算得来）。这个方法通常用在基于hash的集合类中，像`java.util.HashMap`,`java.until.HashSet`和`java.util.Hashtable。`



**问： 在类中覆盖`equals()`的时候，为什么要同时覆盖`hashCode()`？**

答： 在覆盖`equals()`的时候同时覆盖`hashCode()`可以保证对象的功能兼容于hash集合。这是一个好习惯，即使这些对象不会被存储在hash集合中。



**问：`hashCode()`有什么一般规则？**

答：`hashCode()`的一般规则如下：

* 在同一个Java程序中，对一个相同的对象，无论调用多少次`hashCode()`，`hashCode()`返回的整数必须相同，因此必须保证
  `equals()`方法比较的内容不会更改。但不必在另一个相同的Java程序中也保证返回值相同。
* 如果两个对象用`equals()`方法比较的结果是相同的，那么这两个对象调用`hashCode()`应该返回相同的整数值。
* 当两个对象使用`equals()`方法比较的结果是不同的，`hashCode()`返回的整数值可以不同。然而，`hashCode()`的返回值不同可以提高哈希表的性能。



**问： 如果覆盖了`equals()`却不覆盖`hashCode()`会有什么后果？**

答： 当覆盖`equals()`却不覆盖`hashCode()`的时候，在hash集合中存储对象时就会出现问题。



**问：`wait()、notify()` 和 `notifyAll()` 是用来干什么的？**  
答：`wait()、notify()` 和 `notifyAll()` 可以让线程协调完成一项任务。例如，一个线程生产，另一个线程消费。生产线程不能在前一产品被消费之前运行，而应该等待前一个被生产出来的产品被消费之后才被唤醒，进行生产。同理，消费线程也不能在生产线程之前运行，即不能消费不存在的产品。所以，应该等待生产线程执行一个之后才执行。利用这些方法，就可以实现这些线程之间的协调。从本质上说，一个线程等待某种状态（例如一个产品被生产），另一个线程正在执行，知道产生了某种状态（例如生产了一个产品）。



**问：不同的 `wait()` 方法之间有什么区别？**  
答：没有参数的 `wait()` 方法被调用之后，线程就会一直处于睡眠状态，直到本对象（就是 `wait()` 被调用的那个对象）调用 `notify()` 或 `notifyAll()` 方法。相应的`wait(long timeout)`和`wait(long timeout, int nanos)`方法中，当等待时间结束或者被唤醒时（无论哪一个先发生）将会结束等待。



**问：`notify()` 和 `notifyAll()` 方法有什么区别？**  
答：`notify()` 方法随机唤醒一个等待的线程，而 `notifyAll()` 方法将唤醒所有在等待的线程。



**问：线程被唤醒之后会发生什么？**  
答：当一个线程被唤醒之后，除非本对象（调用 `notify()` 或 `notifyAll()` 的对象）的同步锁被释放，否则不会立即执行。唤醒的线程会按照规则和其他线程竞争同步锁，得到锁的线程将执行。所以`notifyAll()`方法执行之后，可能会有一个线程立即运行，也可能所有的线程都没运行。



**问：为什么在使用等待、唤醒方法时，要放在同步代码中？**  
答：将等待和唤醒方法放在同步代码中是非常必要的，这样做是为了避免竞争条件。鉴于要等待的线程通常在调用wait\(\)之前会确认一种情况存在与否（通常是检查某一变量的值），而另一线程在调用notify\(\)之前通常会设置某种情况（通常是通过设置一个变量的值）。以下这种情况引发了竞争条件：

1. 线程一检查了情况和变量，发现需要等待。
2. 线程二设置了变量。
3. 线程二调用了
   `notify()`
   此时，线程一还没有等待，所以这次调用什么用都没有。
4. 线程一调用了
   `wait()`
   这下它永远不会被唤醒了。



**问：如果在同步代码之外使用这些方法会怎么样呢？**  
答：如果在同步代码之外使用了这些情况，就会抛出`java.lang.IllegalMonitorStateException`异常。



**问：如果在同步代码中调用这些方法呢？**  
答：当 `wait()` 方法在同步代码中被调用时，会根据同步代码中方法的优先级先后执行。在`wait()`方法返回值之前，该同步代码一直持有锁，这样就不会出现竞争条件了。在`wait()`方法可以接受唤醒之前，锁一直不会释放。



**问：为什么要把`wait()`调用放在`while`循环中，而不是`if`判断中呢？**  
答：为了防止[假唤醒](http://en.wikipedia.org/wiki/Spurious_wakeup)，可以在 stackoverflow上了解有关这类现象的更多信息——[假唤醒真的会发生吗？](http://stackoverflow.com/questions/1050592/do-spurious-wakeups-actually-happen)。



[参考文章](http://www.javaworld.com/article/2105982/learn-java/core-java-java-qanda-the-ultimate-superclass-part-1.html)



