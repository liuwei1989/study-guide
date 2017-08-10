# [异常常见面试题目](http://www.cnblogs.com/huajiezh/p/5804975.html)

**1\) Java中什么是Exception?**

　　这个问题经常在第一次问有关异常的时候或者是面试菜鸟的时候问。我从来没见过面高级或者资深工程师的

时候有人问这玩意，但是对于菜鸟，是很愿意问这个的。简单来说，异常是Java传达给你的系统和程序错误的方

式。在java中，异常功能是通过实现比如Throwable，Exception，RuntimeException之类的类，然后还有一

些处理异常时候的关键字，比如throw，throws，try，catch，finally之类的。 所有的异常都是通过Throwable

衍生出来的。Throwable把错误进一步划分为 java.lang.Exception 和 java.lang.Error.  java.lang.Error 用

来处理系统错误，例如java.lang.StackOverFlowError 或者 Java.lang.OutOfMemoryError 之类的。然后

 Exception用来处理程序错误，请求的资源不可用等等。

**2\) Java中的检查型异常和非检查型异常有什么区别？**

　　这又是一个非常流行的Java异常面试题，会出现在各种层次的Java面试中。检查型异常和非检查型异常的

主要区别在于其处理方式。检查型异常需要使用try, catch和finally关键字在编译期进行处理，否则会出现编译

器会报错。对于非检查型异常则不需要这样做。Java中所有继承自java.lang.Exception类的异常都是检查型

异常，所有继承自RuntimeException的异常都被称为非检查型异常。你也可以查看下一篇文章来了解

 更多关于检查型异常和非检查型异常之间的区别。

**3\) Java中的NullPointerException和ArrayIndexOutOfBoundException之间有什么相同之处？**

　　在Java异常面试中这并不是一个很流行的问题，但会出现在不同层次的初学者面试中，用来测试应聘者对检查

型异常和非检查型异常的概念是否熟悉。顺便说一下，该题的答案是，这两个异常都是非检查型异常，都继承自RuntimeException。该问题可能会引出另一个问题，即Java和C的数组有什么不同之处，因为C里面的数组是没有

大小限制的，绝对不会抛出ArrayIndexOutOfBoundException。

**4\)在Java异常处理的过程中，你遵循的那些最好的实践是什么？**

　　这个问题在面试技术经理是非常常见的一个问题。因为异常处理在项目设计中是非常关键的，所以精通异常处

理是十分必要的。异常处理有很多最佳实践，下面列举集中，它们提高你代码的健壮性和灵活性：

1\) 调用方法的时候返回布尔值来代替返回null，这样可以 NullPointerException。由于空指针是java异常里最恶

心的异常。

2\) catch块里别不写代码。空catch块是异常处理里的错误事件，因为它只是捕获了异常，却没有任何处理或者

提示。通常你起码要打印出异常信息，当然你最好根据需求对异常信息进行处理。

3\)能抛受控异常（checked Exception）就尽量不抛受非控异常\(checked Exception\)。通过去掉重复的异常处

理代码，可以提高代码的可读性。

4\) 绝对不要让你的数据库相关异常显示到客户端。由于绝大多数数据库和SQLException异常都是受控异常，在Java中，

你应该在DAO层把异常信息处理，然后返回处理过的能让用户看懂并根据异常提示信息改正操作的异常信息。

  
5\) 在Java中，一定要在数据库连接，数据库查询，流处理后，在finally块中调用close\(\)方法。  


**5\) 既然我们可以用RuntimeException来处理错误，那么你认为为什么Java中还存在检查型异常?**

　　这是一个有争议的问题，在回答该问题时你应当小心。虽然他们肯定愿意听到你的观点，但其实他们最感兴

趣的还是有说服力的理由。我认为其中一个理由是，存在检查型异常是一个设计上的决定，受到了诸如C++等比

Java更早的编程语言设计经验的影响。绝大多数检查型异常位于java.io包内，这是合乎情理的，因为在你请求了

不存在的系统资源的时候，一段强壮的程序必须能够优雅的处理这种情况。通过把IOException声明为检查型异

常，Java 确保了你能够优雅的对异常进行处理。另一个可能的理由是，可以使用catch或finally来确保数量受限

的系统资源（比如文件描述符）在你使用后尽早得到释放。  


**6\)  throw 和 throws这两个关键字在java中有什么不同?**

一个java初学者应该掌握的面试问题。 throw 和 throws乍看起来是很相似的尤其是在你还是一个java初学者的时

候。尽管他们看起来相似，都是在处理异常时候使用到的。但在代码里的使用方法和用到的地方是不同的。throws

总是出现在一个函数头中，用来标明该成员函数可能抛出的各种异常, 你也可以申明未检查的异常，但这不是编译

器强制的。如果方法抛出了异常那么调用这个方法的时候就需要将这个异常处理。另一个关键字  throw 是用来

抛出任意异常的，按照语法你可以抛出任意 Throwable \(i.e. Throwable 或任何Throwable的衍生类\) , throw

可以中断程序运行，因此可以用来代替return . 最常见的例子是用 throw 在一个空方法中需要return的地方抛出 UnSupportedOperationException 代码如下 :

  
1     private static voidshow\(\) {    
2          throw new UnsupportedOperationException\(“Not yet implemented”\);  
3      }

**7\) 什么是“异常链”?**

　　“异常链”是Java中非常流行的异常处理概念，是指在进行一个异常处理时抛出了另外一个异常，由此产生

了一个异常链条。该技术大多用于将“ 受检查异常” （ checked exception）封装成为“非受检查异常”

（unchecked exception\)或者RuntimeException。顺便说一下，如果因为因为异常你决定抛出一个新的异常，

你一定要包含原有的异常，这样，处理程序才可以通过getCause\(\)和initCause\(\)方法来访问异常最终的根源。

**8\) 你曾经自定义实现过异常吗？怎么写的?**

　　很显然，我们绝大多数都写过自定义或者业务异常，像AccountNotFoundException。在面试过程中询问

这个Java异常问题的主要原因是去发现你如何使用这个特性的。这可以更准确和精致的去处理异常，当然这也跟

你选择checked 还是unchecked exception息息相关。通过为每一个特定的情况创建一个特定的异常，你就为

调用者更好的处理异常提供了更好的选择。相比通用异常（general exception\)，我更倾向更为精确的异常。大

量的创建自定义异常会增加项目class的个数，因此，在自定义异常和通用异常之间维持一个平衡是成功的关键。

**9\) JDK7中对异常处理做了什么改变？**

  
　　这是最近新出的Java异常处理的面试题。JDK7中对错误\(Error\)和异常\(Exception\)处理主要新增加了2个特性，

一是在一个catch块中可以出来多个异常，就像原来用多个catch块一样。另一个是自动化资源管理\(ARM\), 也称为

try-with-resource块。这2个特性都可以在处理异常时减少代码量，同时提高代码的可读性。对于这些特性了解，

不仅帮助开发者写出更好的异常处理的代码，也让你在面试中显的更突出。我推荐大家读一下Java 7攻略，这样

可以更深入的了解这2个非常有用的特性。

**10\) 你遇到过 OutOfMemoryError 错误嘛？你是怎么搞定的？**

　　这个面试题会在面试高级程序员的时候用，面试官想知道你是怎么处理这个危险的OutOfMemoryError错误的。

必须承认的是，不管你做什么项目，你都会碰到这个问题。所以你要是说没遇到过，面试官肯定不会买账。要是

你对这个问题不熟悉，甚至就是没碰到过，而你又有3、4年的Java经验了，那么准备好处理这个问题吧。在回答

这个问题的同时，你也可以借机向面试秀一下你处理内存泄露、调优和调试方面的牛逼技能。我发现掌握这些技

术的人都能给面试官留下深刻的印象。

  
**11\) 如果执行finally代码块之前方法返回了结果，或者JVM退出了，finally块中的代码还会执行吗？**

　　这个问题也可以换个方式问：“如果在try或者finally的代码块中调用了System.exit\(\)，结果会是怎样”。

了解finally块是怎么执行的，即使是try里面已经使用了return返回结果的情况，对了解Java的异常处理都非常

有价值。只有在try里面是有System.exit\(0\)来退出JVM的情况下finally块中的代码才不会执行。

  
**12\)Java中final,finalize,finally关键字的区别**

  
　　这是一个经典的Java面试题了。我的一个朋友为Morgan Stanley招电信方面的核心Java开发人员的时候就

问过这个问题。final和finally是Java的关键字，而finalize则是方法。final关键字在创建不可变的类的时候

非常有用，只是声明这个类是final的。而finalize\(\)方法则是垃圾回收器在回收一个对象前调用，但也Java规

范里面没有保证这个方法一定会被调用。finally关键字是唯一一个和这篇文章讨论到的异常处理相关的关键字。

在你的产品代码中，在关闭连接和资源文件的是时候都必须要用到finally块。

**13\)下面的代码都有哪些错误：**

01     public static void start\(\) throws IOException, RuntimeException{  
02        
03        throw new RuntimeException\(“Not able to Start”\);  
04     }  
05        
06     public static void main\(String args\[\]\) {  
07        try {  
08              start\(\);  
09        } catch \(Exception ex\) {  
10                ex.printStackTrace\(\);  
11        } catch \(RuntimeException re\) {  
12                re.printStackTrace\(\);  
13        }  
14     }

　　这段代码会在捕捉异常代码块的RuntimeException类型变量“re”里抛出编译异常错误。因为Exception是RuntimeException的超类，在start方法中所有的RuntimeException会被第一个捕捉异常块捕捉，这样就无法到

达第二个捕捉块，这就是抛出“exception java.lang.RuntimeException has already been caught”的编译错误原因。

  
**14）下面的Java代码都有哪些错误：**  
  
01     public classSuperClass {   
02         public void start\(\) throws IOException{  
03             throw new IOException\(“Not able to open file”\);  
04         }  
05     }  
06        
07     public class SubClass extendsSuperClass{   
08         public void start\(\) throws Exception{  
09             throw new Exception\(“Not able to start”\);  
10         }  
11     }

　　这段代码编译器将对子类覆盖start方法产生不满。因为每个Java中方法的覆盖是有规则的，一个覆盖的方法

不能抛出的异常比原方法继承关系高。因为这里的start方法在超类中抛出了IOException，所有在子类中的start

方法只能抛出要么是IOExcepition或是其子类，但不能是其超类，如Exception。

**15）下面的Java异常代码有什么错误：**  
01     public static void start\(\){  
02        System.out.println\(“Java Exception interivew question Answers for Programmers”\);  
03     }  
04        
05     public static void main\(String args\[\]\) {  
06        try{  
07           start\(\);  
08        }catch\(IOException ioe\){  
09           ioe.printStackTrace\(\);  
10        }  
11     }

　　上面的Java异常例子代码中，编译器将在处理IOException时报错，因为IOException是受检查异常，而start

方法并没有抛出IOException，所以编译器将抛出“异常， java.io.IOException 不会在try语句体中抛出”，但

是如果你将IOException改为Exception，编译器报错将消失，因为Exception可以用来捕捉所有运行时异常，这样

就不需要声明抛出语句。我喜欢这样带有迷惑性的Java异常面试题，因为它不会让人轻易的找出是IOException还

是Exception。你也可以在 Joshua Bloach和Neil Gafter的Java谜题中找到一些有关Java错误和异常的具有迷惑

性问题。

