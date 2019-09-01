**1、什么是可变参数？**



可变参数允许调用参数数量不同的方法。请看下面例子中的求和方法。此方法可以调用1个int参数，或2个int参数，或多个int参数。

> //int\(type\) followed ... \(three dot's\) is syntax of a variable argument.
>
> public int sum\(int... numbers\) {
>
> //inside the method a variable argument is similar to an array.
>
> //number can be treated as if it is declared as int\[\] numbers;
>
> int sum = 0;
>
> for \(int number: numbers\) {
>
> sum += number;
>
> }
>
> return sum;
>
> }
>
>
>
> public static void main\(String\[\] args\) {
>
> VariableArgumentExamples example = new VariableArgumentExamples\(\);
>
> //3 Arguments
>
> System.out.println\(example.sum\(1, 4, 5\)\);//10
>
> //4 Arguments
>
> System.out.println\(example.sum\(1, 4, 5, 20\)\);//30
>
> //0 Arguments
>
> System.out.println\(example.sum\(\)\);//0
>
> }



**2、断言的用途？**



断言是在Java 1.4中引入的。它能让你验证假设。如果断言失败（即返回false），就会抛出AssertionError（如果启用断言）。基本断言如下所示。

> private int computerSimpleInterest\(int principal,float interest,int years\){
>
> assert\(principal&gt;0\);
>
> return 100;
>
> }

**  
3、什么时候使用断言？**  




断言不应该用于验证输入数据到一个public方法或命令行参数。IllegalArgumentException会是一个更好的选择。在public方法中，只用断言来检查它们根本不应该发生的情况。



**4、什么是垃圾回收？**



垃圾回收是Java中自动内存管理的另一种叫法。垃圾回收的目的是为程序保持尽可能多的可用堆（heap）。 JVM会删除堆上不再需要从堆引用的对象。



**5、用一个例子解释垃圾回收？**



比方说，下面这个方法就会从函数调用。

> void method\(\){
>
> Calendar calendar = new GregorianCalendar\(2000,10,30\);
>
> System.out.println\(calendar\);
>
> }

通过函数第一行代码中参考变量calendar，在堆上创建了GregorianCalendar类的一个对象。



函数结束执行后，引用变量calendar不再有效。因此，在方法中没有创建引用到对象。



JVM认识到这一点，会从堆中删除对象。这就是所谓的垃圾回收。



**6、什么时候运行垃圾回收？**



垃圾回收在JVM突发奇想和心血来潮时运行（没有那么糟糕）。运行垃圾收集的可能情况是：



* 堆可用内存不足

* CPU空闲



**7、垃圾回收的最佳做法？**



用编程的方式，我们可以要求（记住这只是一个请求——不是一个命令）JVM通过调用System.gc\(\)方法来运行垃圾回收。



当内存已满，且堆上没有对象可用于垃圾回收时，JVM可能会抛出OutOfMemoryException。



对象在被垃圾回收从堆上删除之前，会运行finalize\(\)方法。我们建议不要用finalize\(\)方法写任何代码。



**8、什么是初始化数据块？**



初始化数据块——当创建对象或加载类时运行的代码。



**有两种类型的初始化数据块：**



**静态初始化器：**加载类时运行的的代码

**实例初始化器：**创建新对象时运行的代码



**9、什么是静态初始化器？**



请看下面的例子：static{ 和 }之间的代码被称为静态初始化器。它只有在第一次加载类时运行。只有静态变量才可以在静态初始化器中进行访问。虽然创建了三个实例，但静态初始化器只运行一次。

> public class InitializerExamples {
>
> static int count;
>
> int i;
>
>
>
> static{
>
> //This is a static initializers. Run only when Class is first loaded.
>
> //Only static variables can be accessed
>
> System.out.println\("Static Initializer"\);
>
> //i = 6;//COMPILER ERROR
>
> System.out.println\("Count when Static Initializer is run is " + count\);
>
> }
>
>
>
> public static void main\(String\[\] args\) {
>
> InitializerExamples example = new InitializerExamples\(\);
>
> InitializerExamples example2 = new InitializerExamples\(\);
>
> InitializerExamples example3 = new InitializerExamples\(\);
>
> }
>
> }

**示例输出**

> Static Initializer
>
> Count when Static Initializer is run is 0.



**10、什么是实例初始化块？**



让我们来看一个例子：每次创建类的实例时，实例初始化器中的代码都会运行。

> public class InitializerExamples {
>
> static int count;
>
> int i;
>
> {
>
> //This is an instance initializers. Run every time an object is created.
>
> //static and instance variables can be accessed
>
> System.out.println\("Instance Initializer"\);
>
> i = 6;
>
> count = count + 1;
>
> System.out.println\("Count when Instance Initializer is run is " + count\);
>
> }
>
>
>
> public static void main\(String\[\] args\) {
>
> InitializerExamples example = new InitializerExamples\(\);
>
> InitializerExamples example1 = new InitializerExamples\(\);
>
> InitializerExamples example2 = new InitializerExamples\(\);
>
> }
>
> }

**示例输出**

> Instance Initializer
>
> Count when Instance Initializer is run is 1
>
> Instance Initializer
>
> Count when Instance Initializer is run is 2
>
> Instance Initializer
>
> Count when Instance Initializer is run is 3



**11、什么是正则表达式？**



正则表达式能让解析、扫描和分割字符串变得非常容易。Java中常用的正则表达式——Patter，Matcher和Scanner类。



**12、什么是令牌化？**



令牌化是指在分隔符的基础上将一个字符串分割为若干个子字符串。例如，分隔符；分割字符串ac;bd;def;e为四个子字符串ac，bd，def和e。

分隔符自身也可以是一个常见正则表达式。



String.split\(regex\)函数将regex作为参数。



**13、给出令牌化的例子？**

> private static void tokenize\(String string,String regex\) {
>
> String\[\] tokens = string.split\(regex\);
>
> System.out.println\(Arrays.toString\(tokens\)\);
>
> }
>
>
>
> tokenize\("ac;bd;def;e",";"\);//\[ac, bd, def, e\]

**14、如何使用扫描器类（Scanner Class）令牌化？**

> private static void tokenizeUsingScanner\(String string,String regex\) {
>
> Scanner scanner = new Scanner\(string\);
>
> scanner.useDelimiter\(regex\);
>
> List&lt;String&gt; matches = new ArrayList&lt;String&gt;\(\);
>
> while\(scanner.hasNext\(\)\){
>
> matches.add\(scanner.next\(\)\);
>
> }
>
> System.out.println\(matches\);
>
> }
>
>
>
> tokenizeUsingScanner\("ac;bd;def;e",";"\);//\[ac, bd, def, e\]



**15、如何添加小时\(hour\)到一个日期对象（Date Objects）？**



现在，让我们如何看看添加小时到一个date对象。所有在date上的日期操作都需要通过添加毫秒到date才能完成。例如，如果我们想增加6个小时，那么我们需要将6小时换算成毫秒。6小时= 6 \* 60 \* 60 \* 1000毫秒。请看以下的例子。

> Date date = new Date\(\);
>
>
>
> //Increase time by 6 hrs
>
> date.setTime\(date.getTime\(\) + 6 \* 60 \* 60 \* 1000\);
>
> System.out.println\(date\);
>
>
>
> //Decrease time by 6 hrs
>
> date = new Date\(\);
>
> date.setTime\(date.getTime\(\) - 6 \* 60 \* 60 \* 1000\);
>
> System.out.println\(date\);



**16、如何格式化日期对象？**



格式化日期需要使用DateFormat类完成。让我们看几个例子。

> //Formatting Dates
>
> System.out.println\(DateFormat.getInstance\(\).format\(
>
> date\)\);//10/16/12 5:18 AM

带有区域设置的格式化日期如下所示：

> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.FULL, new Locale\("it", "IT"\)\)
>
> .format\(date\)\);//marted&ldquo; 16 ottobre 2012
>
>
>
> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.FULL, Locale.ITALIAN\)
>
> .format\(date\)\);//marted&ldquo; 16 ottobre 2012
>
>
>
> //This uses default locale US
>
> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.FULL\).format\(date\)\);//Tuesday, October 16, 2012
>
>
>
> System.out.println\(DateFormat.getDateInstance\(\)
>
> .format\(date\)\);//Oct 16, 2012
>
> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.SHORT\).format\(date\)\);//10/16/12
>
> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.MEDIUM\).format\(date\)\);//Oct 16, 2012
>
>
>
> System.out.println\(DateFormat.getDateInstance\(
>
> DateFormat.LONG\).format\(date\)\);//October 16, 2012

**17、Java中日历类（Calendar Class）的用途？**



Calendar类（Youtube视频链接 - https://www.youtube.com/watch?v=hvnlYbt1ve0）在Java中用于处理日期。Calendar类提供了增加和减少天数、月数和年数的简便方法。它还提供了很多与日期有关的细节（这一年的哪一天？哪一周？等等）



**18、如何在Java中获取日历类（Calendar Class）的实例？**



Calendar类不能通过使用new Calendar创建。得到Calendar类实例的最好办法是在Calendar中使用getInstance\(\) static方法。

> //Calendar calendar = new Calendar\(\); //COMPILER ERROR
>
> Calendar calendar = Calendar.getInstance\(\);

**19、解释一些日历类（Calendar Class）中的重要方法？**



在Calendar对象上设置日（day），月（month）或年（year）不难。对Day，Month或Year调用恰当Constant的set方法。下一个参数就是值。

> calendar.set\(Calendar.DATE, 24\);
>
> calendar.set\(Calendar.MONTH, 8\);//8 - September
>
> calendar.set\(Calendar.YEAR, 2010\);

calendar get方法



要获取一个特定日期的信息——2010年9月24日。我们可以使用calendar get方法。已被传递的参数表示我们希望从calendar中获得的值—— 天或月或年或……你可以从calendar获取的值举例如下：

> System.out.println\(calendar.get\(Calendar.YEAR\)\);//2010
>
> System.out.println\(calendar.get\(Calendar.MONTH\)\);//8
>
> System.out.println\(calendar.get\(Calendar.DATE\)\);//24
>
> System.out.println\(calendar.get\(Calendar.WEEK\_OF\_MONTH\)\);//4
>
> System.out.println\(calendar.get\(Calendar.WEEK\_OF\_YEAR\)\);//39
>
> System.out.println\(calendar.get\(Calendar.DAY\_OF\_YEAR\)\);//267
>
> System.out.println\(calendar.getFirstDayOfWeek\(\)\);//1 -&gt; Calendar.SUNDAY
>
> 数字格式化类（Number Format Class）的用途？

**20、数字格式化类（Number Format Class）的用途？**



数字格式用于格式化数字到不同的区域和不同格式中。



使用默认语言环境的数字格式

> System.out.println\(NumberFormat.getInstance\(\).format\(321.24f\)\);//321.24

使用区域设置的数字格式



使用荷兰语言环境格式化数字：

> System.out.println\(NumberFormat.getInstance\(new Locale\("nl"\)\).format\(4032.3f\)\);//4.032,3

使用德国语言环境格式化数字：

> System.out.println\(NumberFormat.getInstance\(Locale.GERMANY\).format\(4032.3f\)\);//4.032,3

使用默认语言环境格式化货币

> System.out.println\(NumberFormat.getCurrencyInstance\(\).format\(40324.31f\)\);//$40,324.31

使用区域设置格式化货币



使用荷兰语言环境格式化货币：

> System.out.println\(NumberFormat.getCurrencyInstance\(new Locale\("nl"\)\).format\(40324.31f\)\);//? 40.324,31





