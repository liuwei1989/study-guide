#### AtomicInteger源码分析——基于CAS的乐观锁实现

##### 1. 悲观锁与乐观锁

我们都知道，cpu是时分复用的，也就是把cpu的时间片，分配给不同的thread/process轮流执行，时间片与时间片之间，需要进行cpu切换，也就是会发生进程的切换。切换涉及到清空寄存器，缓存数据。然后重新加载新的thread所需数据。当一个线程被挂起时，加入到阻塞队列，在一定的时间或条件下，在通过notify\\(\\)，notifyAll\\(\\)唤醒回来。在某个资源不可用的时候，就将cpu让出，把当前等待线程切换为阻塞状态。等到资源\\(比如一个共享数据）可用了，那么就将线程唤醒，让他进入runnable状态等待cpu调度。这就是典型的悲观锁的实现。独占锁是一种悲观锁，synchronized就是一种独占锁，它假设最坏的情况，并且只有在确保其它线程不会造成干扰的情况下执行，会导致其它所有需要锁的线程挂起，等待持有锁的线程释放锁。

但是，由于在进程挂起和恢复执行过程中存在着很大的开销。当一个线程正在等待锁时，它不能做任何事，所以悲观锁有很大的缺点。举个例子，如果一个线程需要某个资源，但是这个资源的占用时间很短，当线程第一次抢占这个资源时，可能这个资源被占用，如果此时挂起这个线程，可能立刻就发现资源可用，然后又需要花费很长的时间重新抢占锁，时间代价就会非常的高。

所以就有了乐观锁的概念，他的核心思路就是，每次不加锁而是假设没有冲突而去完成某项操作，如果因为冲突失败就重试，直到成功为止。在上面的例子中，某个线程可以不让出cpu,而是一直while循环，如果失败就重试，直到成功为止。所以，当数据争用不严重时，乐观锁效果更好。比如CAS就是一种乐观锁思想的应用。

##### 2.   java中CAS的实现

CAS就是Compare and Swap的意思，比较并操作。很多的cpu直接支持CAS指令。CAS是项乐观锁技术，当多个线程尝试使用CAS同时更新同一个变量时，只有其中一个线程能更新变量的值，而其它线程都失败，失败的线程并不会被挂起，而是被告知这次竞争中失败，并可以再次尝试。CAS有3个操作数，内存值V，旧的预期值A，要修改的新值B。当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做。

JDK1.5中引入了底层的支持，在int、long和对象的引用等类型上都公开了CAS的操作，并且JVM把它们编译为底层硬件提供的最有效的方法，在运行CAS的平台上，运行时把它们编译为相应的机器指令。在java.util.concurrent.atomic包下面的所有的原子变量类型中，比如AtomicInteger，都使用了这些底层的JVM支持为数字类型的引用类型提供一种高效的CAS操作。

在CAS操作中，会出现ABA问题。就是如果V的值先由A变成B，再由B变成A，那么仍然认为是发生了变化，并需要重新执行算法中的步骤。有简单的解决方案：不是更新某个引用的值，而是更新两个值，包括一个引用和一个版本号，即使这个值由A变为B，然后为变为A，版本号也是不同的。AtomicStampedReference和AtomicMarkableReference支持在两个变量上执行原子的条件更新。AtomicStampedReference更新一个“对象-引用”二元组，通过在引用上加上“版本号”，从而避免ABA问题，AtomicMarkableReference将更新一个“对象引用-布尔值”的二元组。

##### 3.  AtomicInteger的实现。

AtomicInteger 是一个支持原子操作的 Integer 类，就是保证对AtomicInteger类型变量的增加和减少操作是原子性的，不会出现多个线程下的数据不一致问题。如果不使用 AtomicInteger，要实现一个按顺序获取的 ID，就必须在每次获取时进行加锁操作，以避免出现并发时获取到同样的 ID 的现象。

接下来通过源代码来看AtomicInteger具体是如何实现的原子操作。

首先看incrementAndGet\(\) 方法，下面是具体的代码。

```
public final int incrementAndGet() {  
    for (;;) {  
        int current = get();  
        int next = current + 1;  
        if (compareAndSet(current, next))  
            return next;  
    }  
}
```

通过源码，可以知道，这个方法的做法为先获取到当前的 value 属性值，然后将 value 加 1，赋值给一个局部的 next 变量，然而，这两步都是非线程安全的，但是内部有一个死循环，不断去做compareAndSet操作，直到成功为止，也就是修改的根本在compareAndSet方法里面，compareAndSet\(\)方法的代码如下：

```
public final boolean compareAndSet(int expect, int update) {  
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);  
}
```

compareAndSet\(\)方法调用的compareAndSwapInt\(\)方法的声明如下，是一个native方法。

publicfinal native boolean compareAndSwapInt\(Object var1, long var2, int var4, intvar5\);

compareAndSet 传入的为执行方法时获取到的 value 属性值，next 为加 1 后的值， compareAndSet所做的为调用 Sun 的 UnSafe 的 compareAndSwapInt 方法来完成，此方法为 native 方法，compareAndSwapInt 基于的是CPU 的 CAS指令来实现的。所以基于 CAS 的操作可认为是无阻塞的，一个线程的失败或挂起不会引起其它线程也失败或挂起。并且由于 CAS 操作是 CPU 原语，所以性能比较好。

类似的，还有decrementAndGet\\(\\)方法。它和incrementAndGet\\(\\)的区别是将 value 减 1，赋值给next 变量。

AtomicInteger中还有getAndIncrement\\(\\) 和getAndDecrement\\(\\) 方法，他们的实现原理和上面的两个方法完全相同，区别是返回值不同，前两个方法返回的是改变之后的值，即next。而这两个方法返回的是改变之前的值，即current。

