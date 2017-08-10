**1.Java的HashMap是如何工作的？**

HashMap是一个针对数据结构的键值，每个键都会有相应的值，关键是识别这样的值。

HashMap 基于 hashing 原理，我们通过 put \(\)和 get \(\)方法储存和获取对象。当我们将键值对传递给 put \(\)方法时，它调用键对象的 hashCode \(\)方法来计算 hashcode，让后找到 bucket 位置来储存值对象。当获取对象时，通过键对象的 equals \(\)方法找到正确的键值对，然后返回值对象。HashMap 使用 LinkedList 来解决碰撞问题，当发生碰撞了，对象将会储存在 LinkedList 的下一个节点中。 HashMap 在每个 LinkedList 节点中储存键值对对象。

**2.什么是快速失败的故障安全迭代器？**

快速失败的Java迭代器可能会引发ConcurrentModifcationException在底层集合迭代过程中被修改。故障安全作为发生在实例中的一个副本迭代是不会抛出任何异常的。快速失败的故障安全范例定义了当遭遇故障时系统是如何反应的。例如，用于失败的快速迭代器ArrayList和用于故障安全的迭代器ConcurrentHashMap。

**3.Java BlockingQueue是什么？**

Java BlockingQueue是一个并发集合util包的一部分。BlockingQueue队列是一种支持操作，它等待元素变得可用时来检索，同样等待空间可用时来存储元素。

**4.什么时候使用ConcurrentHashMap？**

在问题2中我们看到ConcurrentHashMap被作为故障安全迭代器的一个实例，它允许完整的并发检索和更新。当有大量的并发更新时，ConcurrentHashMap此时可以被使用。这非常类似于Hashtable，但ConcurrentHashMap不锁定整个表来提供并发，所以从这点上ConcurrentHashMap的性能似乎更好一些。所以当有大量更新时ConcurrentHashMap应该被使用。

**5.哪一个List实现了最快插入？**

LinkedList和ArrayList是另个不同变量列表的实现。ArrayList的优势在于动态的增长数组，非常适合初始时总长度未知的情况下使用。LinkedList的优势在于在中间位置插入和删除操作，速度是最快的。

LinkedList实现了List接口，允许null元素。此外LinkedList提供额外的get，remove，insert方法在LinkedList的首部或尾部。这些操作使LinkedList可被用作堆栈（stack），队列（queue）或双向队列（deque）。

ArrayList实现了可变大小的数组。它允许所有元素，包括null。 每个ArrayList实例都有一个容量（Capacity），即用于存储元素的数组的大小。这个容量可随着不断添加新元素而自动增加，但是增长算法并没有定义。当需要插入大量元素时，在插入前可以调用ensureCapacity方法来增加ArrayList的容量以提高插入效率。

**6.Iterator和ListIterator的区别**

●ListIterator有add\(\)方法，可以向List中添加对象，而Iterator不能。

●ListIterator和Iterator都有hasNext\(\)和next\(\)方法，可以实现顺序向后遍历，但是ListIterator有hasPrevious\(\)和previous\(\)方法，可以实现逆向（顺序向前）遍历。Iterator就不可以。

●ListIterator可以定位当前的索引位置，nextIndex\(\)和previousIndex\(\)可以实现。Iterator没有此功能。

●都可实现删除对象，但是ListIterator可以实现对象的修改，set\(\)方法可以实现。Iierator仅能遍历，不能修改。

**7.什么是CopyOnWriteArrayList，它与ArrayList有何不同？**

CopyOnWriteArrayList是ArrayList的一个线程安全的变体，其中所有可变操作（add、set等等）都是通过对底层数组进行一次新的复制来实现的。相比较于ArrayList它的写操作要慢一些，因为它需要实例的快照。

CopyOnWriteArrayList中写操作需要大面积复制数组，所以性能肯定很差，但是读操作因为操作的对象和写操作不是同一个对象，读之间也不需要加锁，读和写之间的同步处理只是在写完后通过一个简单的"="将引用指向新的数组对象上来，这个几乎不需要时间，这样读操作就很快很安全，适合在多线程里使用，绝对不会发生ConcurrentModificationException ，因此CopyOnWriteArrayList适合使用在读操作远远大于写操作的场景里，比如缓存。

**8.迭代器和枚举之间的区别**

**如果面试官问这个问题，那么他的意图一定是让你区分Iterator不同于Enumeration的两个方面：**

●Iterator允许移除从底层集合的元素。

●Iterator的方法名是标准化的。

**9.Hashmap如何同步?**

**当我们需要一个同步的HashMap时，有两种选择：**

●使用Collections.synchronizedMap（..）来同步HashMap。

●使用ConcurrentHashMap的

这两个选项之间的首选是使用ConcurrentHashMap，这是因为我们不需要锁定整个对象，以及通过ConcurrentHashMap分区地图来获得锁。

**10.IdentityHashMap和HashMap的区别**

IdentityHashMap是Map接口的实现。不同于HashMap的，这里采用参考平等。

●在HashMap中如果两个元素是相等的，则key1.equals\(key2\)

●在IdentityHashMap中如果两个元素是相等的，则key1 == key2











# [来自投资银行的20个Java面试题](http://www.cnblogs.com/huajiezh/p/5790952.html)

## **问题一：在多线程环境中使用HashMap会有什么问题？在什么情况下使用get\(\)方法会产生无限循环？**

## 

## HashMap本身没有什么问题，有没有问题取决于你是如何使用它的。比如，你在一个线程里初始化了一个HashMap然后在多个其他线程里对其进行读取，这肯定没有任何问题。有个例子就是使用HashMap来存储系统配置项。当有多于一个线程对HashMap进行修改操作的时候才会真正产生问题，比如增加、删除、更新键值对的时候。因为put\(\)操作可以造成重新分配存储大小（re-sizeing）的动作，因此有可能造成无限循环的发生，所以这时需要使用Hashtable或者ConcurrentHashMap，而后者更优。

## 

## **问题二：不重写Bean的hashCode\(\)方法是否会对性能带来影响？**

## 

## 这个问题非常好，每个人可能都会有自己的体会。按照我掌握的知识来说，如果一个计算hash的方法写得不好，直接的影响是，当向HashMap中添加元素的时候会更频繁地造成冲突，因此最终增加了耗时。但是自从Java 8开始，这种影响不再像前几个版本那样显著了，因为当冲突的发生超出了一定的限度之后，链表类的实现将会被替换成二叉树（binary tree）实现，这时你仍可以得到O\(logN\)的开销，优于链表类的O\(n\)。

## 

## **问题三：对于一个不可修改的类，它的每个对象是不是都必须声明成final的？**



不尽然，因为你可以通过将成员声明成非final且private，并且不要在除了构造函数的其他地方来修改它。不要为它们提供setter方法，同时不会通过任何函数泄露出对此成员的引用。需要记住的是，把对象声明成final仅仅保证了它不会被重新赋上另外一个值，你仍然可以通过此引用来修改引用对象的属性。这一点是关键，面试官通常喜欢听到你强调这一点。



**问题四：String的substring\(\)方法内部是如何实现的？**



又一个Java面试的好问题，你应该答出“substring方法通过原字符串创建了一个新的对象”，否则你的回答肯定是不能令人满意的。这个问题也经常被拿来测试应聘者对于substring\(\)可能带来的内存泄漏风险是否有所了解。直到Java 1.7版本之前，substring会保存一份原字符串的字符数组的引用，这意味着，如果你从1GB大小的字符串里截取了5个字符，而这5个字符也会阻止那1GB内存被回收，因为这个引用是强引用。



到了Java 1.7，这个问题被解决了，原字符串的字符数组已经不再被引用，但是这个改变也使得substring\(\)创建字符串的操作更加耗时，以前的开销是O\(1\)，现在最坏情况是O\(n\)。





## 

## **问题五：能否写一个单例模式，并且保证实例的唯一性？**



这算是Java一个比较核心的问题了，面试官期望你能知道在写单例模式时应该对实例的初始化与否进行双重检查。记住对实例的声明使用Volatile关键字，以保证单例模式是线程安全的。下面是一段示例，展示了如何用一种线程安全的方式实现了单例模式：



> public class Singleton {
>
>
>
>     private static volatile Singleton \_instance;
>
>
>
>     /\*\*
>
>      \* Double checked locking code on Singleton
>
>      \* @return Singelton instance
>
>      \*/
>
>     public static Singleton getInstance\(\) {
>
>         if \(\_instance == null\) {
>
>             synchronized \(Singleton.class\) {
>
>                 if \(\_instance == null\) {
>
>                     \_instance = new Singleton\(\);
>
>                 }
>
>             }
>
>         }
>
>         return \_instance;
>
>     }
>
>
>
> }



**问题六：你在写存储过程或者在Java里调用存储过程的时候如何来处理错误情况？**



这是个很棘手的Java面试题，答案也并不固定。我的答案是，写存储过程的时候一旦有操作失败，则一定要返回错误码。但是在调用存储过程的时候出错的话捕捉SQLException却是唯一能做的。



**问题七：Executor.submit\(\)和Executor.execute\(\)这两个方法有什么区别？**



此问题来自另外一篇文章，《15个最流行的java多线程面试问题》，现在对熟练掌握并发技能的开发者的需求越来越大，因此这个问题也越来越引起大家的重视。答案是：前者返回一个Future对象，可以通过这个对象来获得工作线程执行的结果。



当我们考察异常处理的时候，又会发现另外一个不同。当你使用execute提交的任务抛出异常时，此异常将会交由未捕捉异常处理过程来处理（uncaught exception handler），当你没有显式指定一个异常处理器的话，默认情况下仅仅会通过System.err打印出错误堆栈。当你用submit来提交一个任务的时候，这个任务一旦抛出异常（无论是否是运行时异常），那这个异常是任务返回对象的一部分。对这样一种情形，当你调用Future.get\(\)方法的时候，这个方法会重新抛出这个异常，并且会使用ExecutionException进行包装。

## 

## **问题八：工厂模式和抽象工厂模式有何不同？**



抽象工厂模式提供了多一级的抽象。不同的工厂类都继承了同一个抽象工厂方法，但是却根据工厂的类别创建不同的对象。例如，AutomobileFactory, UserFactory, RoleFactory都继承了AbstractFactory，但是每个工厂类创建自己对应类型的对象。下面是工厂模式和抽象工厂模式对应的UML图。





## 

## **问题九：什么是单例模式？创建单例对象的时候是将整个方法都标记为**

## **synchronized好还是仅仅把创建的的语句标记为synchronized好？**



在Java中，单例类是指那些在整个Java程序中只存在一份实例的类，例如java.lang.Runtime就是一个单例类。在Java 4版本及以前创建单例会有些麻烦，但是自从Java 5引入了Enum类型之后，事情就变得简单了。可以去看看我的关于如何使用Enum来创建单例类的文章，同时再看看问题五来看看如何在创建单例类的时候进行双重检查。



## **问题十：能否写一段用Java 4或5来遍历一个HashMap的代码？**



事实上，用Java可以有四种方式来遍历任何一个Map，一种是使用keySet\(\)方法获取所有的键，然后遍历这些键，再依次通过get\(\)方法来获取对应的值。第二种方法可以使用entrySet\(\)来获取键值对的集合，然后使用for each语句来遍历这个集合，遍历的时候获得的每个键值对已经包含了键和值。这种算是一种更优的方式，因为每轮遍历的时候同时获得了key和value，无需再调用get\(\)方法，get\(\)方法在那种如果bucket位置有一个巨大的链表的时候的性能开销是O\(n\)。第三种方法是获取entrySet之后用iterator依次获取每个键值对。第四种方法是获得key set之后用iterator依次获取每个key，然后再根据key来调用get方法。



**问题十一：你在什么时候会重写hashCode\(\)和equals\(\)方法？**



当你需要根据业务逻辑来进行相等性判断、而不是根据对象相等性来判断的时候你就需要重写这两个函数了。例如，两个Employee对象相等的依据是它们拥有相同的emp\_id，尽管它们有可能是两个不同的Object对象，并且分别在不同的地方被创建。同时，如果你准备把它们当作HashMap中的key来使用的话，你也必须重写这两个方法。现在，作为Java中equals-hashcode的一个约定，当你重写equals的时候必须也重写hashcode，否则你会打破诸如Set, Map等集合赖以正常工作的约定。你可以看看我的另外一篇博文来理解这两个方法之间的微妙区别与联系。



**问题十二：如果不重写hashCode方法会有什么问题？**



如果不重写equals方法的话，equals和hashCode之间的约定就会被打破：当通过equals方法返回相等的两个对象，他们的hashCode也必须一样。如果不重写hashCode方法的话，即使是使用equals方法返回值为true的两个对象，当它们插入同一个map的时候，因为hashCode返回不同所以仍然会被插入到两个不同的位置。这样就打破了HashMap的本来目的，因为Map本身不允许存进去两个key相同的值。当使用put方法插入一个的时候，HashMap会先计算对象的hashcode，然后根据它来找到存储位置\(bucket\)，然后遍历此存储位置上所有的Map.Entry对象来查看是否与待插入对象相同。如果没有提供hashCode的话，这些就都做不到了。



**问题十三：我们要同步整个getInstance\(\)方法，还是只同步getInstance\(\)方法中的关键部分？**



答案是：仅仅同步关键部分（Critical Section）。这是因为，如果我们同步整个方法的话，每次有线程调用getInstance\(\)方法的时候都会等待其他线程调用完成才行，即使在此方法中并没有执行对象的创建操作。换句话说，我们只需要同步那些创建对象的代码，而创建对象的代码只会执行一次。一旦对象创建完成之后，根本没有必要再对方法进行同步保护了。事实上，从性能上来说，对方法进行同步保护这种编码方法非常要命，因为它会使性能降低10到20倍。下面是单例模式的UML图。







再补充一下，创建线程安全的单例对象有多种方法，你也可以顺便提一下。

## 

## **问题十四：HashMap，在调用get\(\)方法的时候equals\(\)和hashCode\(\)方法都起了什么样的作用？**



这个问题算是对问题十二的补充，应聘者应该知道的是，一旦你提到了hashCode\(\)方法，人们很可能要问HashMap是如何使用这个函数的。当你向HashMap插入一个key的时候，首先，这个对象的hashCode\(\)方法会被调用，调用结果用来计算将要存储的位置\(bucket\)。



因为某个位置上可能以链表的方式已经包含了多个Map.Entry对象，所以HashMap会使用equals\(\)方法来将此对象与所有这些Map.Entry所包含的key进行对比，以确定此key对象是否已经存在。

## 

## **问题十五：在Java中如何避免死锁？**



你可以通过打破互相等待的局面来避免死锁。为了达到这一点，你需要在代码中合理地安排获取和释放锁的顺序。如果获得锁的顺序是固定的，并且获得的顺序和释放的顺序刚好相反的话，就不会产生出现死锁的条件了。

## 

## **问题十六：创建字符串对象的时候，使用字面值和使用new String\(\)构造器这两种方式有什么不同？**



当我们使用new String构造器来创建字符串的时候，字符串的值会在堆中创建，而不会加入JVM的字符串池中。相反，使用字面值创建的String对象会被放入堆的PermGen段中。例如：



> String str=new String\(“Test”\);



这句代码创建的对象str不会放入字符串池中，我们需要显式调用String.intern\(\)方法来将它放入字符串池中。仅仅当你使用字面值创建字符串时，Java才会自动将它放入字符串池中，比如：String s=”Test”。顺便提一下，这里有个容易被忽视的地方，当我们将参数“Test”传入构造器的时候，这个参数是个字面值，因此它也会在字符串池中保存另外一份。想了解更多关于字面值字符串和字符串对象之间的差别，请看这篇文章。



下图很好地解释了这种差异。





## 

## **问题十七：什么是不可修改对象\(Immutable Object\)？你能否写一个例子？**



不可修改对象是那些一旦被创建就不能修改的对象。对这种对象的任何改动的后果都是会创建一个新的对象，而不是在原对象本身做修改。例如Java中的String类就是不可修改的。大多数这样的类通常都是final类型的，因为这样可以避免自己被继承继而被覆盖方法，在覆盖的方法里，不可修改的特性就难以得到保证了。你通常也可以通过将类的成员设置成private但是非final的来获得同样的效果。



另外，你同样要保证你的类不要通过任何方法暴露成员，特别是那些可修改类型的成员。同样地，当你的方法接收客户类传入的可修改对象的话，你应该使用一个复制的对象来防止客户代码来修改这个刚传入的可修改类。比如，传入java.util.Date对象的话，你应该自己使用clone\(\)方法来获得一个副本。



当你通过类函数返回一个可修改对象的时候，你也要采取类似的防护措施，返回一个类成功的副本，防止客户代码通过此引用修改了成员对象的属性。千万不要直接把你的可修改成员直接返回给客户代码。



## **问题十八：如何在不使用任何分析工具的情况下用最简单的方式计算某个方法的执行所花费的时间？**

## 

## 在执行此方法之前和之后获取一个系统时间，取这两个时间的差值，即可得到此方法所花费的时间。



需要注意的是，如果执行此方法花费的时间非常短，那么得到的时间值有可能是0ms。这时你可以在一个计算量比较大的方法上试一下效果。



> long start=System.currentTimeMillis\(\);
>
> method\(\);
>
> long end=System.currentTimeMillis\(\);
>
> System.out.println\("Time taken for execution is "+\(end-start\)\);



## **问题十九：当你要把某个类作为HashMap的key使用的话，你需要重写这个类的哪两个方法？**

## 

## 为了使类可以在HashMap或Hashtable中作为key使用，必须要实现这个类自己的equals\(\)和hashCode\(\)方法。具体请参考问题十四。



## **问题二十：你如何阻止客户代码直接初始化你的类的构造方法？例如，你有一个名为Cache的接口和两个具体的实现类MemoryCache和DiskCache，你如何保证这两个类禁止客户代码用new关键字来获取它们的实例？**



我把这最后一个问题留给你做练习吧，你可以在我给出答案之前好好思索一下。我确信你能够找到正确的方法的，因为这是将类的实现掌控在自己手中的一个重要的方法，同时也能为以后的维护提供巨大的好处。

