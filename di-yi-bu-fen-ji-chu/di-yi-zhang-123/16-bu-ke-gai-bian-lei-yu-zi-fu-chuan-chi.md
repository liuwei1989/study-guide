（1）immutable ——不可改变  
（2）字符串池——String pool

①immutable class/Object  
不可改变类——是指类的状态不变，一旦创建，状态就是固定不变的  
②字符串池

```
String a = "HELLO";
String b = "HELLO";
String c = new String("HELLO");
String d = new String("HELLO");
System.out.println(a == b);
System.out.println(b == c);
System.out.println(c == d);
System.out.println(a.equals(b));
System.out.println(b.equals(c));
System.out.println(c.equals(d));
```

结果为：true，false，false，true，true，true  
这里就涉及到字符串池，pools是为了提高[Java](http://lib.csdn.net/base/java)内存利用率而采用的措施，当遇到String a = “HELLO”时，JAVA会先在字符串池中查找是否存在“HELLO”这个字符串，如果没有，则新创建一个对象，然后变量a指向这个地址，然后再遇到String b = “HELLO”时，由于字符串池中以及有了“HELLO”这个对象，所以直接将变量b的地址指向“HELLO”，省去了重新分配的麻烦，如图：  
![](http://img.blog.csdn.net/20160124101302679 "这里写图片描述")  
在JAVA中，“==”对于两个基本类型，判断内容是否相等，对于对象判断两个对象的地址是否相等，所以此时的a，b的地址相等，返回true。  
那么String c = new String\(“Hello”\)又如何处理呢？如果是这种写法，则不会去访问字符串池，而是先为变量 c 开辟空间，然后将值写入空间。所以b == c返回false，c == d同样返回false。至于String的equals方法，因为它比较的不是对象的地址，而是对象的值，所以都返回true就不奇怪了。  
Java虚拟机有一个字符串池，保存着几乎所有的字符串对象。字符串表达式总是指向字符串池中的一个对象。使用new操作创建的字符串对象不指向字符串池中的对象，但是可以使用intern方法使其指向字符串池中的对象（注：如果池中已经有相同的 字符串–使用equals方法确定，则直接返回池中的字符串，否则先将字符串添加到池中，再返回）。

既然已经理解了上述几点后，再对该问题进行阐述：  
1、只有当字符串是不可变的，字符串池才有可能实现。字符串池的实现可以在运行时节约很多heap空间，因为不同的字符串变量都指向池中的同一个字符串。但如果字符串是可变的，那么String interning将不能实现\(注：String interning是指对不同的字符串仅仅只保存一个，即不会保存多个相同的字符串。\)，因为这样的话，如果变量改变了它的值，那么其它指向这个值的变量的值也会一起改变。

2、如果字符串是可变的，那么会引起很严重的安全问题。譬如，[数据库](http://lib.csdn.net/base/mysql)的用户名、密码都是以字符串的形式传入来获得数据库的连接，或者在socket编程中，主机名和端口都是以字符串的形式传入。因为字符串是不可变的，所以它的值是不可改变的，否则黑客们可以钻到空子，改变字符串指向的对象的值，造成安全漏洞。

3、因为字符串是不可变的，所以是多线程安全的，同一个字符串实例可以被多个线程共享。这样便不用因为线程安全问题而使用同步。字符串自己便是线程安全的。

4、类加载器要用到字符串，不可变性提供了安全性，以便正确的类被加载。譬如你想加载java.sql.Connection类，而这个值被改成了myhacked.Connection，那么会对你的数据库造成不可知的破坏。

5、因为字符串是不可变的，所以在它创建的时候hashcode就被缓存了，不需要重新计算。这就使得字符串很适合作为Map中的键，字符串的处理速度要快过其它的键对象。这就是HashMap中的键往往都使用字符串。

