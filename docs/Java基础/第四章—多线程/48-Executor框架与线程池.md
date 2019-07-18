# Executor框架简介

在[Java](http://lib.csdn.net/base/java)5之后，并发编程引入了一堆新的启动、调度和管理线程的API。Executor框架便是[java](http://lib.csdn.net/base/java)5中引入的，其内部使用了线程池机制，它在[Java](http://lib.csdn.net/base/java).util.cocurrent 包下，通过该框架来控制线程的启动、执行和关闭，可以简化并发编程的操作。因此，在Java 5之后，通过Executor来启动线程比使用Thread的start方法更好，除了更易管理，效率更好（用线程池实现，节约开销）外，还有关键的一点：有助于避免this逃逸问题——如果我们在构造器中启动一个线程，因为另一个任务可能会在构造器结束之前开始执行，此时可能会访问到初始化了一半的对象用Executor在构造器中。

    Executor框架包括：线程池，Executor，Executors，ExecutorService，CompletionService，Future，Callable等。

    Executor接口中之定义了一个方法execute（Runnable command），该方法接收一个Runable实例，它用来执行一个任务，任务即一个实现了Runnable接口的类。ExecutorService接口继承自Executor接口，它提供了更丰富的实现多线程的方法，比如，ExecutorService提供了关闭自己的方法，以及可为跟踪一个或多个异步任务执行状况而生成 Future 的方法。 可以调用ExecutorService的shutdown（）方法来平滑地关闭 ExecutorService，调用该方法后，将导致ExecutorService停止接受任何新的任务且等待已经提交的任务执行完成\(已经提交的任务会分两类：一类是已经在执行的，另一类是还没有开始执行的\)，当所有已经提交的任务执行完毕后将会关闭ExecutorService。因此我们一般用该接口来实现和管理多线程。

    ExecutorService的生命周期包括三种状态：运行、关闭、终止。创建后便进入运行状态，当调用了shutdown（）方法时，便进入关闭状态，此时意味着ExecutorService不再接受新的任务，但它还在执行已经提交了的任务，当素有已经提交了的任务执行完后，便到达终止状态。如果不调用shutdown（）方法，ExecutorService会一直处在运行状态，不断接收新的任务，执行新的任务，服务器端一般不需要关闭它，保持一直运行即可。

    Executors提供了一系列工厂方法用于创先线程池，返回的线程池都实现了ExecutorService接口。

   public static ExecutorService newFixedThreadPool\(int nThreads\)

    创建固定数目线程的线程池。

    public static ExecutorService newCachedThreadPool\(\)

    创建一个可缓存的线程池，调用execute将重用以前构造的线程（如果线程可用）。如果现有线程没有可用的，则创建一个新线   程并添加到池中。终止并从缓存中移除那些已有 60 秒钟未被使用的线程。

    public static ExecutorService newSingleThreadExecutor\(\)

    创建一个单线程化的Executor。

    public static ScheduledExecutorService newScheduledThreadPool\(int corePoolSize\)

    创建一个支持定时及周期性的任务执行的线程池，多数情况下可用来替代Timer类。

    这四种方法都是用的Executors中的ThreadFactory建立的线程，下面就以上四个方法做个比较

|    newCachedThreadPool\(\)                                                                                                                                           |  -缓存型池子，先查看池中有没有以前建立的线程，如果有，就 reuse.如果没有，就建一个新的线程加入池中 -缓存型池子通常用于执行一些生存期很短的异步型任务 因此在一些面向连接的daemon型SERVER中用得不多。但对于生存期短的异步任务，它是Executor的首选。 -能reuse的线程，必须是timeout IDLE内的池中线程，缺省     timeout是60s,超过这个IDLE时长，线程实例将被终止及移出池。  注意，放入CachedThreadPool的线程不必担心其结束，超过TIMEOUT不活动，其会自动被终止。   |
| :--- | :--- |
|   newFixedThreadPool\(int\)                                                        |  -newFixedThreadPool与cacheThreadPool差不多，也是能reuse就用，但不能随时建新的线程 -其独特之处:任意时间点，最多只能有固定数目的活动线程存在，**此时如果有新的线程要建立，只能放在另外的队列中等待**，**直到当前的线程中某个线程终止直接被移出池子** -和cacheThreadPool不同，FixedThreadPool没有IDLE机制（可能也有，但既然文档没提，肯定非常长，类似依赖上层的TCP或UDP IDLE机制之类的），所以**FixedThreadPool多数针对一些很稳定很固定的正规并发线程，多用于服务器** -从方法的源代码看，cache池和fixed 池调用的是同一个底层 池，只不过参数不同: **fixed池线程数固定，并且是0秒IDLE（无IDLE）    ** cache池线程数支持0-Integer.MAX\_VALUE\(显然完全没考虑主机的资源承受能力），60秒IDLE   |
| newScheduledThreadPool\(int\)  |  -调度型线程池 -这个池子里的线程可以按schedule依次delay执行，或周期执行   |
| SingleThreadExecutor\(\)  |  -单例线程，任意时间池中只能有一个线程 -用的是和cache池和fixed池相同的底层池，但线程数目是1-1,0秒IDLE（无IDLE）   |

    一般来说，CachedTheadPool在程序执行过程中通常会创建与所需数量相同的线程，然后在它回收旧线程时停止创建新线程，因此它是合理的Executor的首选，只有当这种方式会引发问题时（比如需要大量长时间

面向连接的线程时），才需要考虑用FixedThreadPool。（该段话摘自《Thinking in Java》第四版）

# Executor执行Runnable任务

通过Executors的以上四个静态工厂方法获得 ExecutorService实例，而后调用该实例的execute（Runnable command）方法即可。一旦Runnable任务传递到execute（）方法，该方法便会自动在一个线程上执行。下面是是Executor执行Runnable任务的示例代码：

```java
import java.util.concurrent.ExecutorService;   
import java.util.concurrent.Executors;   
  
public class TestCachedThreadPool{   
    public static void main(String[] args){   
        ExecutorService executorService = Executors.newCachedThreadPool();   
//      ExecutorService executorService = Executors.newFixedThreadPool(5);  
//      ExecutorService executorService = Executors.newSingleThreadExecutor();  
        for (int i = 0; i < 5; i++){   
            executorService.execute(new TestRunnable());   
            System.out.println("************* a" + i + " *************");   
        }   
        executorService.shutdown();   
    }   
}   
  
class TestRunnable implements Runnable{   
    public void run(){   
        System.out.println(Thread.currentThread().getName() + "线程被调用了。");   
    }   
}  
```

某次执行后的结果如下：

![](http://img.blog.csdn.net/20131221161412609)

   从结果中可以看出，pool-1-thread-1和pool-1-thread-2均被调用了两次，这是随机的，execute会首先在线程池中选择一个已有空闲线程来执行任务，如果线程池中没有空闲线程，它便会创建一个新的线程来执行任务。

# Executor执行Callable任务

    在Java 5之后，任务分两类：一类是实现了Runnable接口的类，一类是实现了Callable接口的类。两者都可以被ExecutorService执行，但是Runnable任务没有返回值，而Callable任务有返回值。并且Callable的call\(\)方法只能通过ExecutorService的submit\(Callable&lt;T&gt; task\) 方法来执行，并且返回一个 &lt;T&gt;Future&lt;T&gt;，是表示任务等待完成的 Future。

    Callable接口类似于Runnable，两者都是为那些其实例可能被另一个线程执行的类设计的。但是 Runnable 不会返回结果，并且无法抛出经过检查的异常而Callable又返回结果，而且当获取返回结果时可能会抛出异常。Callable中的call\(\)方法类似Runnable的run\(\)方法，区别同样是有返回值，后者没有。

    当将一个Callable的对象传递给ExecutorService的submit方法，则该call方法自动在一个线程上执行，并且会返回执行结果Future对象。同样，将Runnable的对象传递给ExecutorService的submit方法，则该run方法自动在一个线程上执行，并且会返回执行结果Future对象，但是在该Future对象上调用get方法，将返回null。

    下面给出一个Executor执行Callable任务的示例代码：

```java
import java.util.ArrayList;   
import java.util.List;   
import java.util.concurrent.*;   
  
public class CallableDemo{   
    public static void main(String[] args){   
        ExecutorService executorService = Executors.newCachedThreadPool();   
        List<Future<String>> resultList = new ArrayList<Future<String>>();   
  
        //创建10个任务并执行   
        for (int i = 0; i < 10; i++){   
            //使用ExecutorService执行Callable类型的任务，并将结果保存在future变量中   
            Future<String> future = executorService.submit(new TaskWithResult(i));   
            //将任务执行结果存储到List中   
            resultList.add(future);   
        }   
  
        //遍历任务的结果   
        for (Future<String> fs : resultList){   
                try{   
                    while(!fs.isDone);//Future返回如果没有完成，则一直循环等待，直到Future返回完成  
                    System.out.println(fs.get());     //打印各个线程（任务）执行的结果   
                }catch(InterruptedException e){   
                    e.printStackTrace();   
                }catch(ExecutionException e){   
                    e.printStackTrace();   
                }finally{   
                    //启动一次顺序关闭，执行以前提交的任务，但不接受新任务  
                    executorService.shutdown();   
                }   
        }   
    }   
}   
  
  
class TaskWithResult implements Callable<String>{   
    private int id;   
  
    public TaskWithResult(int id){   
        this.id = id;   
    }   
  
    /**  
     * 任务的具体过程，一旦任务传给ExecutorService的submit方法， 
     * 则该方法自动在一个线程上执行 
     */   
    public String call() throws Exception {  
        System.out.println("call()方法被自动调用！！！    " + Thread.currentThread().getName());   
        //该返回结果将被Future的get方法得到  
        return "call()方法被自动调用，任务返回的结果是：" + id + "    " + Thread.currentThread().getName();   
    }   
}  
```

    某次执行结果如下：

![](http://img.blog.csdn.net/20131221170327718)

    从结果中可以同样可以看出，submit也是首先选择空闲线程来执行任务，如果没有，才会创建新的线程来执行任务。另外，需要注意：如果Future的返回尚未完成，则get（）方法会阻塞等待，直到Future完成返回，可以通过调用isDone（）方法判断Future是否完成了返回。

# 自定义线程池

自定义线程池，可以用ThreadPoolExecutor类创建，它有多个构造方法来创建线程池，用该类很容易实现自定义的线程池，这里先贴上示例程序：

```java
import java.util.concurrent.ArrayBlockingQueue;   
import java.util.concurrent.BlockingQueue;   
import java.util.concurrent.ThreadPoolExecutor;   
import java.util.concurrent.TimeUnit;   
  
public class ThreadPoolTest{   
    public static void main(String[] args){   
        //创建等待队列   
        BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(20);   
        //创建线程池，池中保存的线程数为3，允许的最大线程数为5  
        ThreadPoolExecutor pool = new ThreadPoolExecutor(3,5,50,TimeUnit.MILLISECONDS,bqueue);   
        //创建七个任务   
        Runnable t1 = new MyThread();   
        Runnable t2 = new MyThread();   
        Runnable t3 = new MyThread();   
        Runnable t4 = new MyThread();   
        Runnable t5 = new MyThread();   
        Runnable t6 = new MyThread();   
        Runnable t7 = new MyThread();   
        //每个任务会在一个线程上执行  
        pool.execute(t1);   
        pool.execute(t2);   
        pool.execute(t3);   
        pool.execute(t4);   
        pool.execute(t5);   
        pool.execute(t6);   
        pool.execute(t7);   
        //关闭线程池   
        pool.shutdown();   
    }   
}   
  
class MyThread implements Runnable{   
    @Override   
    public void run(){   
        System.out.println(Thread.currentThread().getName() + "正在执行。。。");   
        try{   
            Thread.sleep(100);   
        }catch(InterruptedException e){   
            e.printStackTrace();   
        }   
    }   
}  
```

  运行结果如下：

![](http://img.blog.csdn.net/20131222102933609)

 从结果中可以看出，七个任务是在线程池的三个线程上执行的。这里简要说明下用到的ThreadPoolExecuror类的构造方法中各个参数的含义。

#### public ThreadPoolExecutor\(int corePoolSize, int maximumPoolSize, long         keepAliveTime, [TimeUnit](file:///D:/adt-bundle-windows-x86/sdk/docs/reference/java/util/concurrent/TimeUnit.html) unit,[BlockingQueue](file:///D:/adt-bundle-windows-x86/sdk/docs/reference/java/util/concurrent/BlockingQueue.html)&lt;[Runnable](file:///D:/adt-bundle-windows-x86/sdk/docs/reference/java/lang/Runnable.html)&gt; workQueue\)

corePoolSize：线程池中所保存的核心线程数，包括空闲线程。

maximumPoolSize：池中允许的最大线程数。

keepAliveTime：线程池中的空闲线程所能持续的最长时间。

unit：持续时间的单位。

workQueue：任务执行前保存任务的队列，仅保存由execute方法提交的Runnable任务。

    根据ThreadPoolExecutor源码前面大段的注释，我们可以看出，当试图通过excute方法讲一个Runnable任务添加到线程池中时，按照如下顺序来处理：

    1、如果线程池中的线程数量少于corePoolSize，即使线程池中有空闲线程，也会创建一个新的线程来执行新添加的任务；

    2、如果线程池中的线程数量大于等于corePoolSize，但缓冲队列workQueue未满，则将新添加的任务放到workQueue中，按照FIFO的原则依次等待执行（线程池中有线程空闲出来后依次将缓冲队列中的任务交付给空闲的线程执行）；

    3、如果线程池中的线程数量大于等于corePoolSize，且缓冲队列workQueue已满，但线程池中的线程数量小于maximumPoolSize，则会创建新的线程来处理被添加的任务；

    4、如果线程池中的线程数量等于了maximumPoolSize，有4种才处理方式（该构造方法调用了含有5个参数的构造方法，并将最后一个构造方法为RejectedExecutionHandler类型，它在处理线程溢出时有4种方式，这里不再细说，要了解的，自己可以阅读下源码）。

    总结起来，也即是说，当有新的任务要处理时，先看线程池中的线程数量是否大于corePoolSize，再看缓冲队列workQueue是否满，最后看线程池中的线程数量是否大于maximumPoolSize。

    另外，当线程池中的线程数量大于corePoolSize时，如果里面有线程的空闲时间超过了keepAliveTime，就将其移除线程池，这样，可以动态地调整线程池中线程的数量。

    我们大致来看下Executors的源码，newCachedThreadPool的不带RejectedExecutionHandler参数（即第五个参数，线程数量超过maximumPoolSize时，指定处理方式）的构造方法如下：

```java
public static ExecutorService newCachedThreadPool() {  
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,  
                                  60L, TimeUnit.SECONDS,  
                                  new SynchronousQueue<Runnable>());  
}  
```

    它将corePoolSize设定为0，而将maximumPoolSize设定为了Integer的最大值，线程空闲超过60秒，将会从线程池中移除。由于核心线程数为0，因此每次添加任务，都会先从线程池中找空闲线程，如果没有就会创建一个线程（SynchronousQueue&lt;Runnalbe&gt;决定的，后面会说）来执行新的任务，并将该线程加入到线程池中，而最大允许的线程数为Integer的最大值，因此这个线程池理论上可以不断扩大。

    再来看newFixedThreadPool的不带RejectedExecutionHandler参数的构造方法，如下：

```java
public static ExecutorService newFixedThreadPool(int nThreads) {  
    return new ThreadPoolExecutor(nThreads, nThreads,  
                                  0L, TimeUnit.MILLISECONDS,  
                                  new LinkedBlockingQueue<Runnable>());  
}  
```

它将corePoolSize和maximumPoolSize都设定为了nThreads，这样便实现了线程池的大小的固定，不会动态地扩大，另外，keepAliveTime设定为了0，也就是说线程只要空闲下来，就会被移除线程池，敢于LinkedBlockingQueue下面会说。



下面说说几种排队的策略：

    1、直接提交。缓冲队列采用 SynchronousQueue，它将任务直接交给线程处理而不保持它们。如果不存在可用于立即运行任务的线程（即线程池中的线程都在工作），则试图把任务加入缓冲队列将会失败，因此会构造一个新的线程来处理新添加的任务，并将其加入到线程池中。直接提交通常要求无界 maximumPoolSizes（Integer.MAX\_VALUE） 以避免拒绝新提交的任务。newCachedThreadPool采用的便是这种策略。

    2、无界队列。使用无界队列（典型的便是采用预定义容量的 LinkedBlockingQueue，理论上是该缓冲队列可以对无限多的任务排队）将导致在所有 corePoolSize 线程都工作的情况下将新任务加入到缓冲队列中。这样，创建的线程就不会超过 corePoolSize，也因此，maximumPoolSize 的值也就无效了。当每个任务完全独立于其他任务，即任务执行互不影响时，适合于使用无界队列。newFixedThreadPool采用的便是这种策略。

    3、有界队列。当使用有限的 maximumPoolSizes 时，有界队列（一般缓冲队列使用ArrayBlockingQueue，并制定队列的最大长度）有助于防止资源耗尽，但是可能较难调整和控制，队列大小和最大池大小需要相互折衷，需要设定合理的参数。

