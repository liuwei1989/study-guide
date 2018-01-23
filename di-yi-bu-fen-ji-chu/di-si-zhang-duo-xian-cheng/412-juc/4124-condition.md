## 一、Condition接口简介 {#一condition接口简介}

在上述两篇文章中讲解AQS的时候，我们已经知道了同步队列AQS的内部类ConditionObject实现了Condition接口，使用ReentrantLock和ReentrantReadWriteLock的内部类Sync我们可以通过`newCondition()`方法创建一个或多个ConditionObject对象。

在使用synchronized作为同步的时候，可以使用任意的Java对象作为锁，这是因为任意的一个Java对象，都拥有一组监视器方法，这些监视器方法是定义在超类Object中的，主要包括：wait、notify、notifyAll这些方法，这些方法与synchronized关键字配合实现等待/通知模式。

Condition接口也提供了类似object的监视器方法，与Lock配合使用也可以实现等待/通知模式，虽然如此，但是两者在使用方式以及功能功能上还是有些许差别的，主要差别如下：

![](http://img.blog.csdn.net/20171029110546219?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

Condition接口定义：

![](http://img.blog.csdn.net/20171029110945381?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

可以看到基本和Object超类中定义的差不多。

各接口的含义如下：

![](http://img.blog.csdn.net/20171029111033172?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

## 二、Condition接口实现原理 {#三condition接口实现原理}

ConditionObject实现了Condition接口，是AQS的内部类，因为Condition的操作需要获取相关联的锁，所以作为同步器的内部类是一个比较合理的方式。每一个Condition对象都包含一个等待队列，该队列是Condition实现等待通知机制的关键。

和synchronized一样，在调用wait和notify等方法之前都必须要先获取锁，同样使用Condition对象的await和signal方法的时候也是要先获取到锁！

**1、等待队列**

等待队列是一个FIFO的队列，在队列中的每一个节点都包含一个线程的引用，该线程就是在Condition对象上等待的线程，如果一个线程调用了`Condition.await()`方法，那么该线程将会释放锁，构造成节点加入等待队列并进入等待状态。这里的节点Node使用的是AQS中定义的Node。也就是说AQS中的同步队列和Condition的等待队列使用的节点类型都是AQS中定义的Node内部类（AbstractQueuedSynchronizer.Node）。

一个Condition对象包含一个等待队列，Condition拥有首节点和尾节点。当前线程调用`Condition.await()`方法，将会以当前线程构造节点，并将该节点从尾部加入到等待队列，等待队列的基本结构如下图：

![](http://img.blog.csdn.net/20171029124535955?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

如上图可知，Condition拥有首尾节点的引用，而新增节点只需要将原有的尾节点nextWaiter指向它，并且更新尾节点即可。上述节点引用更新的过程并没有使用到CAS保证，这是因为当前线程调用`await（）`方法的时候必定是获取了锁的线程，也就是说该过程是由锁来保证线程安全的。

我们知道在使用synchronized的时候，是使用的对象监视器模型的，即在Object的监视器模型上，一个对象拥有一个同步队列和等待队列，而Lock可以拥有一个同步队列和多个等待队列，这是因为通过`lock.newCondition()`可以创建多个Condition条件，而这多个Condition对象都是在同一个锁的基础上创建的，在同一时刻也只能由一个线程获取到该锁。

Lock模式下同步队列和等待队列的对应关系如下图：

![](http://img.blog.csdn.net/20171029125241833?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

又因为Condition的实现是AQS的内部类，因此每个Condition对象都可以访问AQS同步器提供的方法，相当于每个Condition都拥有所属同步器AQS的引用。

**2、等待的实现**

当前线程调用`Condition.await()`方法的时候，相当于将当前线程从**同步队列**的首节点移动到Condition的等待队列中，并释放锁，同时线程变为等待状态。

当前线程加入到等待队列的过程如下：

![](http://img.blog.csdn.net/20171029125836532?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

可以看出同步队列的首节点并不是直接加入到等待队列的尾节点，而是封装成等待队列的节点才插入到等待队列的尾部的。

**3、通知的实现**

调用当前线程的`Condition.signal()`方法，将会唤醒在等待队列中等待时间最长的节点也就是首节点，在唤醒节点之前，会将该节点移到同步队列中。

节点从等待队列加入到同步队列的过程如下：

![](http://img.blog.csdn.net/20171029131125563?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGxnZW4xNTczODc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

通过调用同步器的方法将等待队列中的头结点线程安全的移到同步队列的尾节点，当前线程在使用LockSupport唤醒该节点的线程。

被唤醒后的线程，将会从`await（）`方法中的while循环中退出，进而调用同步器的方法加入到获取同步状态的竞争中。

成功获取同步状态之后，被唤醒的线程从先前调用的await饭发个返回，此时该线程已经成功的获取了锁。

Condition的`signalAll()`方法，相当于对等待队列中的每一个节点均执行一次`signal（）`方法，效果就是将等待队列中的所有节点全部移到同步队列中，并唤醒每个节点的线程。



参考文章：

1、部分内容和截图来自《Java并发编程的艺术》

2、[http://blog.csdn.net/ghsau/article/details/7481142](http://blog.csdn.net/ghsau/article/details/7481142)

3、[http://ifeve.com/understand-condition/](http://ifeve.com/understand-condition/)

4、[http://www.cnblogs.com/zhengbin/p/6420984.html](http://www.cnblogs.com/zhengbin/p/6420984.html)





