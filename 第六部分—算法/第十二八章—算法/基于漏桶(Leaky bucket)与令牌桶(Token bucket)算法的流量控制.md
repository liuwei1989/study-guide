# 基于漏桶\(Leaky bucket\)与令牌桶\(Token bucket\)算法的流量控制

互联网服务赖以生存的根本是流量, 产品和运营会经常通过各种方式来为应用倒流,比如淘宝的双十一等,如何让系统在处理高并发的同时还是保证自身系统的稳定,通常在最短时间内提高并发的做法就是加机器,但是如果机器不够怎么办?那就需要做业务降级或系统限流,流量控制中用的比较多的两个算法就是漏桶和令牌桶.

##### 漏桶算法\(Leaky bucket\) {#漏桶算法(Leaky bucket)}

漏桶算法强制一个常量的输出速率而不管输入数据流的突发性,当输入空闲时，该算法不执行任何动作.就像用一个底部开了个洞的漏桶接水一样,水进入到漏桶里,桶里的水通过下面的孔以固定的速率流出,当水流入速度过大会直接溢出,可以看出漏桶算法能强行限制数据的传输速率.如下图所示:

[![](http://chinageek-wordpress.stor.sinaapp.com/uploads/2015/11/leaky-bucket.png "leaky bucket")](http://chinageek-wordpress.stor.sinaapp.com/uploads/2015/11/leaky-bucket.png)

##### 令牌桶\(Token bucket\) {#令牌桶(Token bucket)}

令牌桶算法的基本过程如下:

1. 每秒会有 r 个令牌放入桶中，或者说，每过 1/r 秒桶中增加一个令牌
2. 桶中最多存放 b 个令牌，如果桶满了，新放入的令牌会被丢弃
3. 当一个 n 字节的数据包到达时，消耗 n 个令牌，然后发送该数据包
4. 如果桶中可用令牌小于 n，则该数据包将被缓存或丢弃

[![](http://chinageek-wordpress.stor.sinaapp.com/uploads/2015/11/token-bucket.jpg "token bucket")](http://chinageek-wordpress.stor.sinaapp.com/uploads/2015/11/token-bucket.jpg)

##### 漏桶和令牌桶比较 {#漏桶和令牌桶比较}

“漏桶算法”能够强行限制数据的传输速率，而“令牌桶算法”在能够限制数据的平均传输数据外，还允许某种程度的突发传输。在“令牌桶算法”中，只要令牌桶中存在令牌，那么就允许突发地传输数据直到达到用户配置的上限，因此它适合于具有突发特性的流量。

#####  RateLimiter {# RateLimiter}

我们可以使用 Guava 的 RateLimiter 来实现基于令牌桶的流量控制。RateLimiter 令牌桶算法的单桶实现,RateLimiter 对简单的令牌桶算法做了一些工程上的优化，具体的实现是 SmoothBursty。需要注意的是，RateLimiter 的另一个实现 SmoothWarmingUp，就不是令牌桶了，而是漏桶算法。

SmoothBursty 有一个可以放 N 个时间窗口产生的令牌的桶，系统空闲的时候令牌就一直攒着，最好情况下可以扛 N 倍于限流值的高峰而不影响后续请求,就像三峡大坝一样能扛千年一遇的洪水.



