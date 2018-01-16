CopyOnWriteArrayList是Java并发包中提供的一个并发容器，它是个线程安全且读操作无锁的ArrayList，写操作则通过创建底层数组的新副本来实现，是一种读写分离的并发策略，我们也可以称这种容器为"写时复制器"，Java并发包中类似的容器还有CopyOnWriteSet。本文会对CopyOnWriteArrayList的实现原理及源码进行分析。

**实现原理**

我们都知道，集合框架中的ArrayList是非线程安全的，Vector虽是线程安全的，但由于简单粗暴的锁同步机制，性能较差。而CopyOnWriteArrayList则提供了另一种不同的并发处理策略（当然是针对特定的并发场景）。

很多时候，我们的系统应对的都是**读多写少**的并发场景。CopyOnWriteArrayList容器允许并发读，读操作是无锁的，性能较高。至于写操作，比如向容器中添加一个元素，**则首先将当前容器复制一份，然后在新副本上执行写操作，结束之后再将原容器的引用指向新容器。**

[![](http://files.jb51.net/file_images/article/201705/2017522154036319.jpg?2017422154045)](http://files.jb51.net/file_images/article/201705/2017522154036319.jpg?2017422154045)

**优缺点分析**

了解了CopyOnWriteArrayList的实现原理，分析它的优缺点及使用场景就很容易了。

**优点：**

读操作性能很高，因为无需任何同步措施，比较适用于读多写少的并发场景。Java的list在遍历时，若中途有别的线程对list容器进行修改，则会抛出ConcurrentModificationException异常。而CopyOnWriteArrayList由于其"读写分离"的思想，遍历和修改操作分别作用在不同的list容器，所以在使用迭代器进行遍历时候，也就不会抛出ConcurrentModificationException异常了

**缺点：**

缺点也很明显，一是内存占用问题，毕竟每次执行写操作都要将原容器拷贝一份，数据量大时，对内存压力较大，可能会引起频繁GC；二是无法保证实时性，Vector对于读写操作均加锁同步，可以保证读和写的强一致性。而CopyOnWriteArrayList由于其实现策略的原因，写和读分别作用在新老不同容器上，在写操作执行过程中，读不会阻塞但读取到的却是老容器的数据。

**源码分析**

添加操作

```java
public boolean add(E e) {
  //ReentrantLock加锁，保证线程安全
  final ReentrantLock lock = this.lock;
  lock.lock();
  try {
   Object[] elements = getArray();
   int len = elements.length;
   //拷贝原容器，长度为原容器长度加一
   Object[] newElements = Arrays.copyOf(elements, len + 1);
   //在新副本上执行添加操作
   newElements[len] = e;
   //将原容器引用指向新副本
   setArray(newElements);
   return true;
  } finally {
   //解锁
   lock.unlock();
  }
 }
```

添加的逻辑很简单，先将原容器copy一份，然后在新副本上执行写操作，之后再切换引用。当然此过程是要加锁的。

**删除操作**

```java
public E remove(int index) {
  //加锁
  final ReentrantLock lock = this.lock;
  lock.lock();
  try {
   Object[] elements = getArray();
   int len = elements.length;
   E oldValue = get(elements, index);
   int numMoved = len - index - 1;
   if (numMoved == 0)
    //如果要删除的是列表末端数据，拷贝前len-1个数据到新副本上，再切换引用
    setArray(Arrays.copyOf(elements, len - 1));
   else {
    //否则，将除要删除元素之外的其他元素拷贝到新副本中，并切换引用
    Object[] newElements = new Object[len - 1];
    System.arraycopy(elements, 0, newElements, 0, index);
    System.arraycopy(elements, index + 1, newElements, index,
         numMoved);
    setArray(newElements);
   }
   return oldValue;
  } finally {
   //解锁
   lock.unlock();
  }
}
```

删除操作同理，将除要删除元素之外的其他元素拷贝到新副本中，然后切换引用，将原容器引用指向新副本。同属写操作，需要加锁。

读操作

```java
public E get(int index) {
  return get(getArray(), index);
 }
　　直接读取即可，无需加锁

 private E get(Object[] a, int index) {
  return (E) a[index];
 }
```

总结

并发优化的ArrayList。基于不可变对象策略，在修改时先复制出一个数组快照来修改，改好了，再让内部指针指向新数组。

因为对快照的修改对读操作来说不可见，所以读读之间不互斥，读写之间也不互斥，只有写写之间要加锁互斥。但复制快照的成本昂贵，典型的适合读多写少的场景。

虽然增加了addIfAbsent（e）方法，会遍历数组来检查元素是否已存在，性能可以想像的不会太好。



[参考文章](http://yikun.github.io/2015/04/28/Java-CopyOnWriteArrayList%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86%E5%8F%8A%E5%AE%9E%E7%8E%B0/)

