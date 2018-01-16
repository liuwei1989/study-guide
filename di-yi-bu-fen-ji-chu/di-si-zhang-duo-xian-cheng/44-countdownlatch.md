## **CountDownLatch工具类介绍** {#h2_0}

### **CountDownLatch描述** {#h3_1}

> **CountDownLatch**是一个同步工具类，**它允许一个或多个线程处于等待状态直到在其它线程中运行的一组操作完成为止**。CountDownLatch用一个给定的计数来实现初始化。await方法会一直处于阻塞状态，直到countDown方法调用而使当前计数达到零。当计数为零之后，所有处于等待的线程将被释放，await的任何后续调用将立即返回。这种现象只出现一次，计数是不能被重置的。
>
> **如果你需要一个可以重置计数的版本，需要考虑使用CyclicBarrier**。

### CountDownLatch工具类相关类图 {#h3_2}

> **CountDownLatch**中定义了一个内部类**Sync**，该类继承**AbstractQueuedSynchronizer**。从代码中可以看出，**CountDownLatch的await,countDown以及getCount方法都调用了Sync的方法。**

![](http://static.oschina.net/uploads/img/201611/05181054_7Li1.jpg)

## **CountDownLatch工具类的使用案例** {#h2_4}

### CountDownLatch的作用 {#h3_5}

> CountDownLatch的作用是控制一个计数器，每个线程在运行完毕后执行countDown，表示自己运行结束，这对于多个子任务的计算特别有效，比如一个异步任务需要拆分成10个子任务执行，主任务必须知道子任务是否完成，所有子任务完成后才能进行合并计算，从而保证了一个主任务逻辑的正确性。
>
> \(此段摘自于&lt;&lt;改善Java程序的151个建议&gt;&gt;, P254\)
>
> CountDownLatch最重要的方法是**countDown**\(\)和**await**\(\)，**前者主要是倒数一次，后者是等待倒数到0，如果没有到达0，就只有阻塞等待了**。

### 案例描述 {#h3_6}

> 使用CountDownLatch工具类来实现10个线程对1~100的求和，每个线程对10个数进行求和。

* 第一个线程对1 – 10的数字求和 
* 第二个线程对 11 – 20的数字求和 
* 第三个线程对21 – 30 的数字求和 
* ….. 
* 第十个线程对91 – 100的数字求和。 

### 代码与测试 {#h3_7}

因为需要用到每个线程执行后的求和结果，所以，先编写一个用于求和计算的类并实现Callable接口，

如：

```java
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * @author wangmengjun
 *
 */
public class Calculator implements Callable<Integer> {

    // 开始信号
    private final CountDownLatch startSignal;

    // 结束信号
    private final CountDownLatch doneSignal;

    private int groupNumber = 0;

    /**
     * @param startSignal
     * @param endSignal
     * @param groupId
     */
    public Calculator(CountDownLatch startSignal, CountDownLatch doneSignal,
            int groupNumber) {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        this.groupNumber = groupNumber;
    }

    public Integer call() throws Exception {
        startSignal.await();
        Integer result = sum(groupNumber);
        printCompleteInfor(groupNumber, result);
        doneSignal.countDown();

        return result;
    }

    private Integer sum(int groupNumber) {
        if (groupNumber < 1) {
            throw new IllegalArgumentException();
        }

        int sum = 0;
        int start = (groupNumber - 1) * 10 + 1;
        int end = groupNumber * 10;
        for (int i = start; i <= end; i++) {
            sum += i;
        }
        return sum;
    }

    private void printCompleteInfor(int groupNumber, int sum) {
        System.out.println(String.format(
                "Group %d is finished, the sum in this gropu is %d",
                groupNumber, sum));
    }
}
```

创建10个线程，然后对结果求和。

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CountDownLatchTest {

    public static void main(String[] args) throws Exception {
        /**
         * 1－100求和，分10个线程来计算，每个线程对10个数求和。
         */
        int numOfGroups = 10;
        CountDownLatch startSignal = new CountDownLatch(1);

        CountDownLatch doneSignal = new CountDownLatch(numOfGroups);

        ExecutorService service = Executors.newFixedThreadPool(numOfGroups);
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

        submit(futures, numOfGroups, service, startSignal, doneSignal);

        /**
         * 开始，让所有的求和计算线程运行
         */
        startSignal.countDown();

        /**
         * 阻塞，知道所有计算线程完成计算
         */
        doneSignal.await();

        shutdown(service);

        printResult(futures);
    }

    private static void submit(List<Future<Integer>> futures, int numOfGroups,
            ExecutorService service, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        for (int groupNumber = 1; groupNumber <= numOfGroups; groupNumber++) {
            futures.add(service.submit(new Calculator(startSignal, doneSignal,
                    groupNumber)));
        }
    }

    private static int getResult(List<Future<Integer>> futures)
            throws InterruptedException, ExecutionException {
        int result = 0;
        for (Future<Integer> f : futures) {
            result += f.get();
        }
        return result;
    }

    private static void printResult(List<Future<Integer>> futures)
            throws InterruptedException, ExecutionException {
        System.out.println("[1,100] Sum is :" + getResult(futures));
    }

    private static void shutdown(ExecutorService service)
    {
        service.shutdown();
    }

}
```

某次运行的结果如下：

```java
Group 7 is finished, the sum in this gropu is 655
Group 3 is finished, the sum in this gropu is 255
Group 8 is finished, the sum in this gropu is 755
Group 10 is finished, the sum in this gropu is 955
Group 6 is finished, the sum in this gropu is 555
Group 5 is finished, the sum in this gropu is 455
Group 4 is finished, the sum in this gropu is 355
Group 1 is finished, the sum in this gropu is 55
Group 9 is finished, the sum in this gropu is 855
Group 2 is finished, the sum in this gropu is 155
[1,100] Sum is :5050
```

## 小结 {#h2_8}

本文首先对java.util.concurrent包下的CountDownLatch工具类进行了简单的描述；接着，给出了CountDownLatch的作用；最后给出了一个使用CountDownLatch工具类完成10个线程求和的例子。

> **使用CountDownLatch时，它关注的一个线程或者多个线程需要在其它在一组线程完成操作之后，在去做一些事情**。比如：服务的启动等。

### CountDownLatch类源代码 {#h3_3}

```java
/*
 * @(#)CountDownLatch.java    1.5 04/02/09
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util.concurrent;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

/**
 * A synchronization aid that allows one or more threads to wait until
 * a set of operations being performed in other threads completes.
 *
 * <p>A <tt>CountDownLatch</tt> is initialized with a given
 * [i]count[/i].  The {@link #await await} methods block until the current
 * {@link #getCount count} reaches zero due to invocations of the
 * {@link #countDown} method, after which all waiting threads are
 * released and any subsequent invocations of {@link #await await} return
 * immediately. This is a one-shot phenomenon -- the count cannot be
 * reset.  If you need a version that resets the count, consider using
 * a {@link CyclicBarrier}.
 *
 * <p>A <tt>CountDownLatch</tt> is a versatile synchronization tool
 * and can be used for a number of purposes.  A
 * <tt>CountDownLatch</tt> initialized with a count of one serves as a
 * simple on/off latch, or gate: all threads invoking {@link #await await}
 * wait at the gate until it is opened by a thread invoking {@link
 * #countDown}.  A <tt>CountDownLatch</tt> initialized to [i]N[/i]
 * can be used to make one thread wait until [i]N[/i] threads have
 * completed some action, or some action has been completed N times.
 * <p>A useful property of a <tt>CountDownLatch</tt> is that it
 * doesn't require that threads calling <tt>countDown</tt> wait for
 * the count to reach zero before proceeding, it simply prevents any
 * thread from proceeding past an {@link #await await} until all
 * threads could pass.
 *
 * <p><b>Sample usage:</b> Here is a pair of classes in which a group
 * of worker threads use two countdown latches:
 * [list]
 * <li>The first is a start signal that prevents any worker from proceeding
 * until the driver is ready for them to proceed;
 * <li>The second is a completion signal that allows the driver to wait
 * until all workers have completed.
 * [/list]
 *
 * <pre>
 * class Driver { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch startSignal = new CountDownLatch(1);
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *     doSomethingElse();            // don't let run yet
 *     startSignal.countDown();      // let all threads proceed
 *     doSomethingElse();
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
 *      this.startSignal = startSignal;
 *      this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *      try {
 *        startSignal.await();
 *        doWork();
 *        doneSignal.countDown();
 *      } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }
 *
 * </pre>
 *
 * <p>Another typical usage would be to divide a problem into N parts,
 * describe each part with a Runnable that executes that portion and
 * counts down on the latch, and queue all the Runnables to an
 * Executor.  When all sub-parts are complete, the coordinating thread
 * will be able to pass through await. (When threads must repeatedly
 * count down in this way, instead use a {@link CyclicBarrier}.)
 *
 * <pre>
 * class Driver2 { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownLatch doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownLatch doneSignal, int i) {
 *      this.doneSignal = doneSignal;
 *      this.i = i;
 *   }
 *   public void run() {
 *      try {
 *        doWork(i);
 *        doneSignal.countDown();
 *      } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }
 *
 * </pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class CountDownLatch {
    /**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int count) {
            setState(count); 
        }

        int getCount() {
            return getState();
        }

        public int tryAcquireShared(int acquires) {
            return getState() == 0? 1 : -1;
        }

        public boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc)) 
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;
    /**
     * Constructs a <tt>CountDownLatch</tt> initialized with the given
     * count.
     * 
     * @param count the number of times {@link #countDown} must be invoked
     * before threads can pass through {@link #await}.
     *
     * @throws IllegalArgumentException if <tt>count</tt> is less than zero.
     */
    public CountDownLatch(int count) { 
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to 
     * zero, unless the thread is {@link Thread#interrupt interrupted}.
     *
     * <p>If the current {@link #getCount count} is zero then this method
     * returns immediately.
     * <p>If the current {@link #getCount count} is greater than zero then
     * the current thread becomes disabled for thread scheduling 
     * purposes and lies dormant until one of two things happen:
     * [list]
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread.
     * [/list]
     * <p>If the current thread:
     * [list]
     * <li>has its interrupted status set on entry to this method; or 
     * <li>is {@link Thread#interrupt interrupted} while waiting, 
     * [/list]
     * then {@link InterruptedException} is thrown and the current thread's 
     * interrupted status is cleared. 
     *
     * @throws InterruptedException if the current thread is interrupted
     * while waiting.
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to 
     * zero, unless the thread is {@link Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the current {@link #getCount count} is zero then this method
     * returns immediately with the value <tt>true</tt>.
     *
     * <p>If the current {@link #getCount count} is greater than zero then
     * the current thread becomes disabled for thread scheduling 
     * purposes and lies dormant until one of three things happen:
     * [list]
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread; or
     * <li>The specified waiting time elapses.
     * [/list]
     * <p>If the count reaches zero then the method returns with the
     * value <tt>true</tt>.
     * <p>If the current thread:
     * [list]
     * <li>has its interrupted status set on entry to this method; or 
     * <li>is {@link Thread#interrupt interrupted} while waiting, 
     * [/list]
     * then {@link InterruptedException} is thrown and the current thread's 
     * interrupted status is cleared. 
     *
     * <p>If the specified waiting time elapses then the value <tt>false</tt>
     * is returned.
     * If the time is 
     * less than or equal to zero, the method will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the <tt>timeout</tt> argument.
     * @return <tt>true</tt> if the count reached zero  and <tt>false</tt>
     * if the waiting time elapsed before the count reached zero.
     *
     * @throws InterruptedException if the current thread is interrupted
     * while waiting.
     */
    public boolean await(long timeout, TimeUnit unit) 
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Decrements the count of the latch, releasing all waiting threads if
     * the count reaches zero.
     * <p>If the current {@link #getCount count} is greater than zero then
     * it is decremented. If the new count is zero then all waiting threads
     * are re-enabled for thread scheduling purposes.
     * <p>If the current {@link #getCount count} equals zero then nothing
     * happens.
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * Returns the current count.
     * <p>This method is typically used for debugging and testing purposes.
     * @return the current count.
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * Returns a string identifying this latch, as well as its state.
     * The state, in brackets, includes the String 
     * "Count =" followed by the current count.
     * @return a string identifying this latch, as well as its
     * state
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }

}
```



