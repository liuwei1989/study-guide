RejectedExecutionHandler是一个接口：

```
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
```

里面只有一个方法。当要创建的线程数量大于线程池的最大线程数的时候，新的任务就会被拒绝，就会调用这个接口里的这个方法。

可以自己实现这个接口，实现对这些超出数量的任务的处理。

ThreadPoolExecutor自己已经提供了四个拒绝策略，分别是`CallerRunsPolicy`,`AbortPolicy`,`DiscardPolicy`,`DiscardOldestPolicy`

这四个拒绝策略其实一看实现方法就知道很简单。

## AbortPolicy

ThreadPoolExecutor中默认的拒绝策略就是AbortPolicy。直接抛出异常。

```
private static final RejectedExecutionHandler defaultHandler =
    new AbortPolicy();
```

下面是他的实现：

```
public static class AbortPolicy implements RejectedExecutionHandler {
    public AbortPolicy() { }
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                             " rejected from " +
                                             e.toString());
    }
}
```

很简单粗暴，直接抛出个RejectedExecutionException异常，也不执行这个任务了。

## 测试

先自定义一个Runnable,给每个线程起个名字，下面都用这个Runnable

```
static class MyThread implements Runnable {
        String name;
        public MyThread(String name) {
            this.name = name;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程:"+Thread.currentThread().getName() +" 执行:"+name +"  run");
        }
    }
```

然后构造一个核心线程是1，最大线程数是2的线程池。拒绝策略是AbortPolicy

```
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0, 
        TimeUnit.MICROSECONDS, 
        new LinkedBlockingDeque<Runnable>(2), 
        new ThreadPoolExecutor.AbortPolicy());
```

```
for (int i = 0; i < 6; i++) {
    System.out.println("添加第"+i+"个任务");
    executor.execute(new MyThread("线程"+i));
    Iterator iterator = executor.getQueue().iterator();
    while (iterator.hasNext()){
        MyThread thread = (MyThread) iterator.next();
        System.out.println("列表："+thread.name);
    }
}
```

输出是：

![](http://img0.tuicool.com/vURRBfa.png!web)

分析一下过程。

1. 添加第一个任务时，直接执行，任务列表为空。
2. 添加第二个任务时，因为采用的LinkedBlockingDeque，，并且核心线程正在执行任务，所以会将第二个任务放在队列中，队列中有 线程2.
3. 添加第三个任务时，也一样会放在队列中，队列中有 线程2，线程3.
4. 添加第四个任务时，因为核心任务还在运行，而且任务队列已经满了，所以胡直接创建新线程执行第四个任务，。这时线程池中一共就有两个线程在运行了，达到了最大线程数。任务队列中还是有线程2， 线程3.
5. 添加第五个任务时，再也没有地方能存放和执行这个任务了，就会被线程池拒绝添加，执行拒绝策略的rejectedExecution方法，这里就是执行AbortPolicy的rejectedExecution方法直接抛出异常。
6. 最终，只有四个线程能完成运行。后面的都被拒绝了。

## CallerRunsPolicy

CallerRunsPolicy在任务被拒绝添加后，会调用当前线程池的所在的线程去执行被拒绝的任务。

下面说他的实现：

```
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    public CallerRunsPolicy() { }
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            r.run();
        }
    }
}
```

也很简单，直接run。

## 测试

```
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 30,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<Runnable>(2),
        new ThreadPoolExecutor.AbortPolicy());
```

按上面的运行，输出

![](http://img2.tuicool.com/MJRrqer.png!web)

注意在添加第五个任务，任务5 的时候，同样被线程池拒绝了，因此执行了CallerRunsPolicy的rejectedExecution方法，这个方法直接执行任务的run方法。因此可以看到任务5是在main线程中执行的。

从中也可以看出，因为第五个任务在主线程中运行，所以主线程就被阻塞了，以至于当第五个任务执行完，添加第六个任务时，前面两个任务已经执行完了，有了空闲线程，因此线程6又可以添加到线程池中执行了。

这个策略的缺点就是可能会阻塞主线程。

## DiscardPolicy

这个策略的处理就更简单了，看一下实现就明白了：

```
public static classDiscardPolicyimplementsRejectedExecutionHandler{
    publicDiscardPolicy(){ }
    publicvoidrejectedExecution(Runnable r, ThreadPoolExecutor e){
    }
}

```

这个东西什么都没干。

因此采用这个拒绝策略，会让被线程池拒绝的任务直接抛弃，不会抛异常也不会执行。

## 测试

```
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 30,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<Runnable>(2),
        new ThreadPoolExecutor.DiscardPolicy());
```

输出：

![](http://img0.tuicool.com/VNB3Evu.png!web)

可以看到 后面添加的任务5和6根本不会执行，什么反应都没有，直接丢弃。

## DiscardOldestPolicy

DiscardOldestPolicy策略的作用是，当任务呗拒绝添加时，会抛弃任务队列中最旧的任务也就是最先加入队列的，再把这个新任务添加进去。

```
public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    public DiscardOldestPolicy() { }
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            e.getQueue().poll();
            e.execute(r);
        }
    }
}
```

在rejectedExecution先从任务队列总弹出最先加入的任务，空出一个位置，然后再次执行execute方法把任务加入队列。

## 测试

```
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 30,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<Runnable>(2),
        new ThreadPoolExecutor.DiscardOldestPolicy());
```

输出是：

![](http://img0.tuicool.com/uI7RBj7.png!web)

可以看到，

1. 在添加第五个任务时，会被线程池拒绝。这时任务队列中有 任务2，任务3
2. 这时，拒绝策略会让任务队列中最先加入的任务弹出，也就是任务2.
3. 然后把被拒绝的任务5添加人任务队列，这时任务队列中就成了 任务3，任务5.
4. 添加第六个任务时会因为同样的过程，将队列中的任务3抛弃，把任务6加进去，任务队列中就成了 任务5，任务6
5. 因此，最终能被执行的任务只有1，4，5，6. 任务2和任务3倍抛弃了，不会执行。

## 自定义拒绝策略

通过看前面的系统提供的四种拒绝策略可以看出，拒绝策略的实现都非常简单。自己写亦一样

比如现在想让被拒绝的任务在一个新的线程中执行，可以这样写：

```
static class MyRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        new Thread(r,"新线程"+new Random().nextInt(10)).start();
    }
}
```

然后正常使用：

```
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 30,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<Runnable>(2),
        new MyRejectedExecutionHandler());
```

输出：

![](http://img1.tuicool.com/UvEbaye.png!web)

发现被拒绝的任务5和任务6都在新线程中执行了。

