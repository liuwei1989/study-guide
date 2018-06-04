# Nginx {#nginx}

首先要明白，Nginx 采用的是多进程（单线程） & 多路IO复用模型。使用了 I/O 多路复用技术的 Nginx，就成了”并发事件驱动“的服务器。

![](http://img.blog.csdn.net/20170303170640322?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU1RGUEhQ/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "这里写图片描述")

## 多进程的工作模式 {#多进程的工作模式}

```
1、Nginx 在启动后，会有一个 master 进程和多个相互独立的 worker 进程。
2、接收来自外界的信号，向各worker进程发送信号，每个进程都有可能来处理这个连接。
3、 master 进程能监控 worker 进程的运行状态，当 worker 进程退出后(异常情况下)，会自动启动新的 worker 进程。
```

注意 worker 进程数，一般会设置成机器 cpu 核数。因为更多的worker 数，只会导致进程相互竞争 cpu，从而带来不必要的上下文切换。

使用多进程模式，不仅能提高并发率，而且进程之间相互独立，一个 worker 进程挂了不会影响到其他 worker 进程。

## 惊群现象 {#惊群现象}

主进程（master 进程）首先通过 socket\(\) 来创建一个 sock 文件描述符用来监听，然后fork生成子进程（workers 进程），子进程将继承父进程的 sockfd（socket 文件描述符），之后子进程 accept\(\) 后将创建已连接描述符（connected descriptor）），然后通过已连接描述符来与客户端通信。

那么，由于所有子进程都继承了父进程的 sockfd，那么当连接进来时，所有子进程都将收到通知并“争着”与它建立连接，这就叫“惊群现象”。大量的进程被激活又挂起，只有一个进程可以accept\(\) 到这个连接，这当然会消耗系统资源。

## Nginx对惊群现象的处理： {#nginx对惊群现象的处理}

Nginx 提供了一个 accept\_mutex 这个东西，这是一个加在accept上的一把互斥锁。即每个 worker 进程在执行 accept 之前都需要先获取锁，获取不到就放弃执行 accept\(\)。有了这把锁之后，同一时刻，就只会有一个进程去 accpet\(\)，这样就不会有惊群问题了。accept\_mutex 是一个可控选项，我们可以显示地关掉，默认是打开的。

## worker进程工作流程 {#worker进程工作流程}

当一个 worker 进程在 accept\(\) 这个连接之后，就开始读取请求，解析请求，处理请求，产生数据后，再返回给客户端，最后才断开连接，一个完整的请求。一个请求，完全由 worker 进程来处理，而且只能在一个 worker 进程中处理。

这样做带来的好处：

1、节省锁带来的开销。每个 worker 进程都是独立的进程，不共享资源，不需要加锁。同时在编程以及问题查上时，也会方便很多。

2、独立进程，减少风险。采用独立的进程，可以让互相之间不会影响，一个进程退出后，其它进程还在工作，服务不会中断，master 进程则很快重新启动新的 worker 进程。当然，worker 进程的也能发生意外退出。

多进程模型每个进程/线程只能处理一路IO，那么 Nginx是如何处理多路IO呢？

如果不使用 IO 多路复用，那么在一个进程中，同时只能处理一个请求，比如执行 accept\(\)，如果没有连接过来，那么程序会阻塞在这里，直到有一个连接过来，才能继续向下执行。

而多路复用，允许我们只在事件发生时才将控制返回给程序，而其他时候内核都挂起进程，随时待命。

## 核心：Nginx采用的 IO多路复用模型epoll {#核心nginx采用的-io多路复用模型epoll}

epoll通过在Linux内核中申请一个简易的文件系统（文件系统一般用什么数据结构实现？B+树），其工作流程分为三部分：

```
1、调用 int epoll_create(int size)建立一个epoll对象，内核会创建一个eventpoll结构体，用于存放通过epoll_ctl()向epoll对象中添加进来的事件，这些事件都会挂载在红黑树中。
2、调用 int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event) 在 epoll 对象中为 fd 注册事件，所有添加到epoll中的事件都会与设备驱动程序建立回调关系，也就是说，当相应的事件发生时会调用这个sockfd的回调方法，将sockfd添加到eventpoll 中的双链表。
3、调用 int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout) 来等待事件的发生，timeout 为 -1 时，该调用会阻塞知道有事件发生
```

这样，注册好事件之后，只要有 fd 上事件发生，epoll\_wait\(\) 就能检测到并返回给用户，用户就能”非阻塞“地进行 I/O 了。

epoll\(\) 中内核则维护一个链表，epoll\_wait 直接检查链表是不是空就知道是否有文件描述符准备好了。（epoll 与 select 相比最大的优点是不会随着 sockfd 数目增长而降低效率，使用 select\(\) 时，内核采用轮训的方法来查看是否有fd 准备好，其中的保存 sockfd 的是类似数组的数据结构 fd\_set，key 为 fd，value 为 0 或者 1。）

能达到这种效果，是因为在内核实现中 epoll 是根据每个 sockfd 上面的与设备驱动程序建立起来的回调函数实现的。那么，某个 sockfd 上的事件发生时，与它对应的回调函数就会被调用，来把这个 sockfd 加入链表，其他处于“空闲的”状态的则不会。在这点上，epoll 实现了一个”伪”AIO。但是如果绝大部分的 I/O 都是“活跃的”，每个 socket 使用率很高的话，epoll效率不一定比 select 高（可能是要维护队列复杂）。

可以看出，因为一个进程里只有一个线程，所以一个进程同时只能做一件事，但是可以通过不断地切换来“同时”处理多个请求。

例子：Nginx 会注册一个事件：“如果来自一个新客户端的连接请求到来了，再通知我”，此后只有连接请求到来，服务器才会执行 accept\(\) 来接收请求。又比如向上游服务器（比如 PHP-FPM）转发请求，并等待请求返回时，这个处理的 worker 不会在这阻塞，它会在发送完请求后，注册一个事件：“如果缓冲区接收到数据了，告诉我一声，我再将它读进来”，于是进程就空闲下来等待事件发生。

这样，基于**多进程+epoll**， Nginx 便能实现高并发。

使用 epoll 处理事件的一个框架，代码转自：[http://www.cnblogs.com/fnlingnzb-learner/p/5835573.html](http://www.cnblogs.com/fnlingnzb-learner/p/5835573.html)

```
for( ; ; )  //  无限循环
      {
          nfds = epoll_wait(epfd,events,20,500);  //  最长阻塞 500s
          for(i=0;i<nfds;++i)
          {
              if(events[i].data.fd==listenfd) //有新的连接
              {
                  connfd = accept(listenfd,(sockaddr *)&clientaddr, &clilen); //accept这个连接
                  ev.data.fd=connfd;
                 ev.events=EPOLLIN|EPOLLET;
                 epoll_ctl(epfd,EPOLL_CTL_ADD,connfd,&ev); //将新的fd添加到epoll的监听队列中
             }
             else if( events[i].events&EPOLLIN ) //接收到数据，读socket
             {
                 n = read(sockfd, line, MAXLINE)) < 0    //读
                 ev.data.ptr = md;     //md为自定义类型，添加数据
                 ev.events=EPOLLOUT|EPOLLET;
                 epoll_ctl(epfd,EPOLL_CTL_MOD,sockfd,&ev);//修改标识符，等待下一个循环时发送数据，异步处理的精髓
             }
             else if(events[i].events&EPOLLOUT) //有数据待发送，写socket
             {
                 struct myepoll_data* md = (myepoll_data*)events[i].data.ptr;    //取数据
                 sockfd = md->fd;
                 send( sockfd, md->ptr, strlen((char*)md->ptr), 0 );        //发送数据
                 ev.data.fd=sockfd;
                 ev.events=EPOLLIN|EPOLLET;
                 epoll_ctl(epfd,EPOLL_CTL_MOD,sockfd,&ev); //修改标识符，等待下一个循环时接收数据
             }
             else
             {
                 //其他的处理
             }
         }
     }
```

## Nginx 与 多进程模式 Apache 的比较： {#nginx-与-多进程模式-apache-的比较}

事件驱动适合于I/O密集型服务，多进程或线程适合于CPU密集型服务：  
1、Nginx 更主要是作为反向代理，而非Web服务器使用。其模式是事件驱动。  
2、事件驱动服务器，最适合做的就是这种 I/O 密集型工作，如反向代理，它在客户端与WEB服务器之间起一个数据中转作用，纯粹是 I/O 操作，自身并不涉及到复杂计算。因为进程在一个地方进行计算时，那么这个进程就不能处理其他事件了。  
3、Nginx 只需要少量进程配合事件驱动，几个进程跑 libevent，不像 Apache 多进程模型那样动辄数百的进程数。  
5、Nginx 处理静态文件效果也很好，那是因为读写文件和网络通信其实都是 I/O操作，处理过程一样。



[参考文章1](http://blog.csdn.net/stfphp/article/details/52936490)

[参考文章2](https://www.cnblogs.com/linguoguo/p/5511293.html)

