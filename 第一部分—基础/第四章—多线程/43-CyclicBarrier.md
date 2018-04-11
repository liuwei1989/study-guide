## **CyclicBarrier工具类介绍** {#h2_0}

### CyclicBarrier描述 {#h3_1}

> **CyclicBarrier**是一个同步辅助工具类，**它允许一组线程相互等待，直到到达一个公共的栏栅点。**
>
> **CyclicBarrier对于那些包含一组固定大小线程，并且这些线程必须不时地相互等待的程序非常有用**。之所以将其称之为循环的Barrier是因为该Barrier在等待的线程释放之后可以重用。

CyclicBarrier 支持一个可选的 Runnable 命令，在一组线程中的最后一个线程到达之后（但在释放所有线程之前），该命令只在每个屏障点运行一次。若在继续所有参与线程之前更新共享状态，此屏障操作很有用。

### CyclicBarrier工具类相关类图 {#h3_2}

> **CyclicBarrier**采用**Condition**和**Lock**来完成线程之间的同步。

相关的类图是CyclicBarrier类内容如下：

![](http://static.oschina.net/uploads/img/201611/06094846_ft9Z.jpg)

## **CyclicBarrier工具类的使用案例** {#h2_4}

### 案例描述 {#h3_5}

> CyclicBarrier可以让所有线程都处于等待状态\(阻塞\)，然后在满足条件的情况下继续执行。

使用CyclicBarrier模拟几个小组出去游玩的场景，如：

> 几个小组包一辆车去旅游，一天行程包括上午小组自由活动和下午自由活动。
>
> 各个小组早上自由活动，但是11点半大巴车上集合，然后吃饭并赶赴下一个景区。
>
> 各个小组下午自由活动，但是要5点半大巴车上集合，然后一起回去。

### 代码与测试 {#h3_6}

```java
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 
 * @author wangmengjun
 *
 */
public class TeamGroup implements Runnable {

    private final CyclicBarrier barrier;

    private int groupNumber;

    /**
     * @param barrier
     * @param groupNumber
     */
    public TeamGroup(CyclicBarrier barrier, int groupNumber) {
        this.barrier = barrier;
        this.groupNumber = groupNumber;
    }

    public void run() {
        try {
            print();
            barrier.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void print() {
        System.out.println(String.format("第%d组完成该地景点浏览，并回到集合点", groupNumber));
    }

}
```

```java
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CyclicBarrierTest {

    private static final int THREAD_SLEEP_MILLIS = 6000;

    /** 旅游小数的个数 */
    private static final int NUMBER_OF_GROUPS = 6;

    /** 观光是否结束的标识 */
    private static boolean tourOver = false;

    public static void main(String[] args) {

        ExecutorService service = Executors
                .newFixedThreadPool(NUMBER_OF_GROUPS);

        CyclicBarrier cb = new CyclicBarrier(NUMBER_OF_GROUPS, new Runnable() {

            public void run() {
                /*
                 * 如果一天的游玩结束了，大家可以坐大巴回去了... ...
                 */
                if (isTourOver()) {
                    System.out.println("各个小组都集合到大巴上，准备回家.. ...");
                }

            }
        });

        System.out.println("用CyclicBarrier辅助工具类模拟旅游过程中小组集合:：");

        /**
         * 上午各个小组自由活动，然后在某个点，比如11点半集合到大巴上。
         */
        tourInTheMorning(service, cb);
        sleep(THREAD_SLEEP_MILLIS);

        /**
         * 调用reset方法，将barrier设置到初始化状态。
         * 
         */
        cb.reset();

        /**
         * 下午各个小组自由活动，然后在某个点，比如11点半集合到大巴上。
         */
        tourInTheAfternoon(service, cb);

        /**
         * 下午小组集合完毕后，一天的观光就结束了，将标志位记为true;
         */
        tourOver = true;

        sleep(THREAD_SLEEP_MILLIS);
        service.shutdown();

    }

    /**
     * @return the tourOver
     */
    public static boolean isTourOver() {
        return tourOver;
    }

    /**
     * @param tourOver
     *            the tourOver to set
     */
    public static void setTourOver(boolean tourOver) {
        CyclicBarrierTest.tourOver = tourOver;
    }

    private static void tourInTheMorning(ExecutorService service,
            final CyclicBarrier cb) {
        System.out.println("早上自由玩... ... ");
        for (int groupNumber = 1; groupNumber <= NUMBER_OF_GROUPS; groupNumber++) {
            service.execute(new TeamGroup(cb, groupNumber));
        }
    }

    private static void tourInTheAfternoon(ExecutorService service,
            final CyclicBarrier cb) {
        System.out.println("下午自由玩... ... ");
        for (int groupNumber = 1; groupNumber <= NUMBER_OF_GROUPS; groupNumber++) {
            service.execute(new TeamGroup(cb, groupNumber));
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
```

某次运行的结果如下：

```
用CyclicBarrier辅助工具类模拟旅游过程中小组集合:：
早上自由玩... ... 
第2组完成该地景点浏览，并回到集合点
第1组完成该地景点浏览，并回到集合点
第5组完成该地景点浏览，并回到集合点
第3组完成该地景点浏览，并回到集合点
第6组完成该地景点浏览，并回到集合点
第4组完成该地景点浏览，并回到集合点
下午自由玩... ... 
第2组完成该地景点浏览，并回到集合点
第4组完成该地景点浏览，并回到集合点
第3组完成该地景点浏览，并回到集合点
第6组完成该地景点浏览，并回到集合点
第1组完成该地景点浏览，并回到集合点
第5组完成该地景点浏览，并回到集合点
各个小组都集合到大巴上，准备回家.. ...
```

## **CyclicBarrier与CountDownLatch比较** {#h2_7}

### **相同点** {#h3_8}

> 两者都是用于**线程同步**的辅助工具类，都提供了**await**方法来达到线程等待。

### **不同点** {#h3_9}

#### 从类的实现上看 {#h4_10}

> CountDownLatch通过一个继承AbstractQueuedSynchronizer的内部类Sync来完成同步。
>
> CyclicBarrier通过Condition和Lock来完成同步。

#### 从类的用途上看 {#h4_11}

> CountDownLatch： 一个或者是一部分线程，等待另外一部线程都完成操作。
>
> CyclicBarrier： 所有线程互相等待完成。

#### 从适合场合来看 {#h4_12}

> CountDownLatch中计数是不能被重置的。
>
> 如果需要一个可以重置计数的版本，需要考虑使用CyclicBarrier。

**CountDownLatch适用于一次同步**。当使用CountDownLatch时，任何线程允许多次调用countDown\(\). 那些调用了await\(\)方法的线程将被阻塞，直到那些没有被阻塞线程调用countDown\(\)使计数到达0为止。

![](http://static.oschina.net/uploads/img/201611/06094846_zVFq.jpg)

**相反**，**CyclicBarrier适用于多个同步点。**

例如：一组正在运算的线程，在进入下一个阶段计算之前需要同步。

![](http://static.oschina.net/uploads/img/201611/06094846_R9Kx.jpg)

与CountDownLatch不同，一个处于某个阶段的线程调用了await\(\)方法将会被阻塞，直到所有属于这个阶段的线程都调用了await\(\)方法为止。

在CyclicBarrier中，如果一个线程由于中断，失败或者超时等原因，过早地离开了栅栏点，那么所有在栅栏点等待的其它线程也会通过BrokenBarrierException或者IterupedException异常地离开。

![](http://static.oschina.net/uploads/img/201611/06094846_i91V.jpg)

#### 从关注点上来看 {#h4_13}

> **使用CountDownLatch时，它关注的一个线程或者多个线程需要在其它在一组线程完成操作之后，在去做一些事情**。比如：服务的启动等。
>
> **CyclicBarrier更加关注的是公共的栅栏点\(Common Barrier point\),关注的是这个点上的同步。这个点之前之后的事情并不需要太多的关注。**比如：一个并行计算需要分几个阶段完成，在一个阶段完成进入到下一个阶段之前，需要同步，这时候CyclicBarrier很适合。

### CyclicBarrier类源代码 {#h3_3}

```java
/*
 * @(#)CyclicBarrier.java    1.12 06/03/30
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util.concurrent;
import java.util.concurrent.locks.*;

/**
 * A synchronization aid that allows a set of threads to all wait for
 * each other to reach a common barrier point.  CyclicBarriers are
 * useful in programs involving a fixed sized party of threads that
 * must occasionally wait for each other. The barrier is called
 * [i]cyclic[/i] because it can be re-used after the waiting threads
 * are released.
 *
 * <p>A <tt>CyclicBarrier</tt> supports an optional {@link Runnable} command
 * that is run once per barrier point, after the last thread in the party
 * arrives, but before any threads are released.
 * This [i]barrier action[/i] is useful
 * for updating shared-state before any of the parties continue.
 *
 * <p><b>Sample usage:</b> Here is an example of
 *  using a barrier in a parallel decomposition design:
 * <pre>
 * class Solver {
 *   final int N;
 *   final float[][] data;
 *   final CyclicBarrier barrier;
 *
 *   class Worker implements Runnable {
 *     int myRow;
 *     Worker(int row) { myRow = row; }
 *     public void run() {
 *       while (!done()) {
 *         processRow(myRow);
 *
 *         try {
 *           barrier.await();
 *         } catch (InterruptedException ex) {
 *           return;
 *         } catch (BrokenBarrierException ex) {
 *           return;
 *         }
 *       }
 *     }
 *   }
 *
 *   public Solver(float[][] matrix) {
 *     data = matrix;
 *     N = matrix.length;
 *     barrier = new CyclicBarrier(N,
 *                                 new Runnable() {
 *                                   public void run() {
 *                                     mergeRows(...);
 *                                   }
 *                                 });
 *     for (int i = 0; i < N; ++i)
 *       new Thread(new Worker(i)).start();
 *
 *     waitUntilDone();
 *   }
 * }
 * </pre>
 * Here, each worker thread processes a row of the matrix then waits at the
 * barrier until all rows have been processed. When all rows are processed
 * the supplied {@link Runnable} barrier action is executed and merges the
 * rows. If the merger
 * determines that a solution has been found then <tt>done()</tt> will return
 * <tt>true</tt> and each worker will terminate.
 *
 * <p>If the barrier action does not rely on the parties being suspended when
 * it is executed, then any of the threads in the party could execute that
 * action when it is released. To facilitate this, each invocation of
 * {@link #await} returns the arrival index of that thread at the barrier.
 * You can then choose which thread should execute the barrier action, for
 * example:
 * <pre>  if (barrier.await() == 0) {
 *     // log the completion of this iteration
 *   }</pre>
 *
 * <p>The <tt>CyclicBarrier</tt> uses an all-or-none breakage model
 * for failed synchronization attempts: If a thread leaves a barrier
 * point prematurely because of interruption, failure, or timeout, all
 * other threads waiting at that barrier point will also leave
 * abnormally via {@link BrokenBarrierException} (or
 * {@link InterruptedException} if they too were interrupted at about
 * the same time).
 *
 * <p>Memory consistency effects: Actions in a thread prior to calling
 * {@code await()}
 * [url=package-summary.html#MemoryVisibility]<i>happen-before</i>[/url]
 * actions that are part of the barrier action, which in turn
 * <i>happen-before</i> actions following a successful return from the
 * corresponding {@code await()} in other threads.
 *
 * @since 1.5
 * @see CountDownLatch
 *
 * @author Doug Lea
 */
public class CyclicBarrier {
    /**
     * Each use of the barrier is represented as a generation instance.
     * The generation changes whenever the barrier is tripped, or
     * is reset. There can be many generations associated with threads
     * using the barrier - due to the non-deterministic way the lock
     * may be allocated to waiting threads - but only one of these
     * can be active at a time (the one to which <tt>count</tt> applies)
     * and all the rest are either broken or tripped.
     * There need not be an active generation if there has been a break
     * but no subsequent reset.
     */
    private static class Generation {
        boolean broken = false;
    }

    /** The lock for guarding barrier entry */
    private final ReentrantLock lock = new ReentrantLock();
    /** Condition to wait on until tripped */
    private final Condition trip = lock.newCondition();
    /** The number of parties */
    private final int parties;
    /* The command to run when tripped */
    private final Runnable barrierCommand;
    /** The current generation */
    private Generation generation = new Generation();

    /**
     * Number of parties still waiting. Counts down from parties to 0
     * on each generation.  It is reset to parties on each new
     * generation or when broken.
     */
    private int count;

    /**
     * Updates state on barrier trip and wakes up everyone.
     * Called only while holding lock.
     */
    private void nextGeneration() {
        // signal completion of last generation
        trip.signalAll();
        // set up next generation
        count = parties;
        generation = new Generation();
    }

    /**
     * Sets current barrier generation as broken and wakes up everyone.
     * Called only while holding lock.
     */
    private void breakBarrier() {
        generation.broken = true;
    count = parties;
        trip.signalAll();
    }

    /**
     * Main barrier code, covering the various policies.
     */
    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final Generation g = generation;

            if (g.broken)
                throw new BrokenBarrierException();

            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }

           int index = --count;
           if (index == 0) {  // tripped
               boolean ranAction = false;
               try {
           final Runnable command = barrierCommand;
                   if (command != null)
                       command.run();
                   ranAction = true;
                   nextGeneration();
                   return 0;
               } finally {
                   if (!ranAction)
                       breakBarrier();
               }
           }

            // loop until tripped, broken, interrupted, or timed out
            for (;;) {
                try {
                    if (!timed)
                        trip.await();
                    else if (nanos > 0L)
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        breakBarrier();
            throw ie;
            } else {
            // We're about to finish waiting even if we had not
            // been interrupted, so this interrupt is deemed to
            // "belong" to subsequent execution.
            Thread.currentThread().interrupt();
            }
                }

                if (g.broken)
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a new <tt>CyclicBarrier</tt> that will trip when the
     * given number of parties (threads) are waiting upon it, and which
     * will execute the given barrier action when the barrier is tripped,
     * performed by the last thread entering the barrier.
     *
     * @param parties the number of threads that must invoke {@link #await}
     *        before the barrier is tripped
     * @param barrierAction the command to execute when the barrier is
     *        tripped, or {@code null} if there is no action
     * @throws IllegalArgumentException if {@code parties} is less than 1
     */
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    /**
     * Creates a new <tt>CyclicBarrier</tt> that will trip when the
     * given number of parties (threads) are waiting upon it, and
     * does not perform a predefined action when the barrier is tripped.
     *
     * @param parties the number of threads that must invoke {@link #await}
     *        before the barrier is tripped
     * @throws IllegalArgumentException if {@code parties} is less than 1
     */
    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    /**
     * Returns the number of parties required to trip this barrier.
     *
     * @return the number of parties required to trip this barrier
     */
    public int getParties() {
        return parties;
    }

    /**
     * Waits until all {@linkplain #getParties parties} have invoked
     * <tt>await</tt> on this barrier.
     *
     * <p>If the current thread is not the last to arrive then it is
     * disabled for thread scheduling purposes and lies dormant until
     * one of the following things happens:
     * [list]
     * <li>The last thread arrives; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * one of the other waiting threads; or
     * <li>Some other thread times out while waiting for barrier; or
     * <li>Some other thread invokes {@link #reset} on this barrier.
     * [/list]
     *
     * <p>If the current thread:
     * [list]
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * [/list]
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the barrier is {@link #reset} while any thread is waiting,
     * or if the barrier {@linkplain #isBroken is broken} when
     * <tt>await</tt> is invoked, or while any thread is waiting, then
     * {@link BrokenBarrierException} is thrown.
     *
     * <p>If any thread is {@linkplain Thread#interrupt interrupted} while waiting,
     * then all other waiting threads will throw
     * {@link BrokenBarrierException} and the barrier is placed in the broken
     * state.
     *
     * <p>If the current thread is the last thread to arrive, and a
     * non-null barrier action was supplied in the constructor, then the
     * current thread runs the action before allowing the other threads to
     * continue.
     * If an exception occurs during the barrier action then that exception
     * will be propagated in the current thread and the barrier is placed in
     * the broken state.
     *
     * @return the arrival index of the current thread, where index
     *         <tt>{@link #getParties()} - 1</tt> indicates the first
     *         to arrive and zero indicates the last to arrive
     * @throws InterruptedException if the current thread was interrupted
     *         while waiting
     * @throws BrokenBarrierException if [i]another[/i] thread was
     *         interrupted or timed out while the current thread was
     *         waiting, or the barrier was reset, or the barrier was
     *         broken when {@code await} was called, or the barrier
     *         action (if present) failed due an exception.
     */
    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen;
        }
    }

    /**
     * Waits until all {@linkplain #getParties parties} have invoked
     * <tt>await</tt> on this barrier, or the specified waiting time elapses.
     *
     * <p>If the current thread is not the last to arrive then it is
     * disabled for thread scheduling purposes and lies dormant until
     * one of the following things happens:
     * [list]
     * <li>The last thread arrives; or
     * <li>The specified timeout elapses; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * one of the other waiting threads; or
     * <li>Some other thread times out while waiting for barrier; or
     * <li>Some other thread invokes {@link #reset} on this barrier.
     * [/list]
     *
     * <p>If the current thread:
     * [list]
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * [/list]
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link TimeoutException}
     * is thrown. If the time is less than or equal to zero, the
     * method will not wait at all.
     *
     * <p>If the barrier is {@link #reset} while any thread is waiting,
     * or if the barrier {@linkplain #isBroken is broken} when
     * <tt>await</tt> is invoked, or while any thread is waiting, then
     * {@link BrokenBarrierException} is thrown.
     *
     * <p>If any thread is {@linkplain Thread#interrupt interrupted} while
     * waiting, then all other waiting threads will throw {@link
     * BrokenBarrierException} and the barrier is placed in the broken
     * state.
     *
     * <p>If the current thread is the last thread to arrive, and a
     * non-null barrier action was supplied in the constructor, then the
     * current thread runs the action before allowing the other threads to
     * continue.
     * If an exception occurs during the barrier action then that exception
     * will be propagated in the current thread and the barrier is placed in
     * the broken state.
     *
     * @param timeout the time to wait for the barrier
     * @param unit the time unit of the timeout parameter
     * @return the arrival index of the current thread, where index
     *         <tt>{@link #getParties()} - 1</tt> indicates the first
     *         to arrive and zero indicates the last to arrive
     * @throws InterruptedException if the current thread was interrupted
     *         while waiting
     * @throws TimeoutException if the specified timeout elapses
     * @throws BrokenBarrierException if [i]another[/i] thread was
     *         interrupted or timed out while the current thread was
     *         waiting, or the barrier was reset, or the barrier was broken
     *         when {@code await} was called, or the barrier action (if
     *         present) failed due an exception
     */
    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    /**
     * Queries if this barrier is in a broken state.
     *
     * @return {@code true} if one or more parties broke out of this
     *         barrier due to interruption or timeout since
     *         construction or the last reset, or a barrier action
     *         failed due to an exception; {@code false} otherwise.
     */
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resets the barrier to its initial state.  If any parties are
     * currently waiting at the barrier, they will return with a
     * {@link BrokenBarrierException}. Note that resets [i]after[/i]
     * a breakage has occurred for other reasons can be complicated to
     * carry out; threads need to re-synchronize in some other way,
     * and choose one to perform the reset.  It may be preferable to
     * instead create a new barrier for subsequent use.
     */
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of parties currently waiting at the barrier.
     * This method is primarily useful for debugging and assertions.
     *
     * @return the number of parties currently blocked in {@link #await}
     */
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
}
```



