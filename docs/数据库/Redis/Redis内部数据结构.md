## Redis内部数据结构
### 字符串

Redis的字符串叫做[SDS]，即Simple Dynamic String。它的结构是一个带长度信息的字节数组

```c++
struct SDS<T> {
T capacity; // 数组容量
T len; // 数组长度
byte flags; // 特殊标识位，不理睬它
byte[] content; // 数组内容
}
```

1. content存储真正的字符串内容，capacity表示所分配数组的长度，len表示字符串的实际长度

2. SDS使泛型用T，是因为当字符串比较短时，len和capacity可以使用byte和short来表示
3. Redis的字符串有两种存储方式，当长度特别短使用`EMB`形式存储，当长度超过44时，使用`raw`形式存储

4. 字符串在长度小于 1M 之前，扩容空间采用加倍策略，也就是保留 100% 的冗余空间。当长度超过 1M 之后，为了避免加倍后的冗余空间过大而导致浪费，每次扩容只会多分配 1M 大小的冗余空间。

### 字典

字典，是一种用于保存键值对的抽象数据结构，Redis中的hash结构、zset中value和score值的映射关系、Redis所有的key和value、带过期时间的key都是使用字典（dict）这个数据结构。

![](pics/redis-dict.png)

字典使用哈希表来作为底层实现，每个字典带有两个哈希表，一个平时使用，另外一个仅在进行渐进式搬迁时使用，这时候两个 hashtable 存储的分别是旧的 hashtable 和新的 hashtable。待搬迁结束后，旧的 hashtable 被删除，新的 hashtable 取而代之。



扩容：

1. 如果服务器没有正在执行bgsave令，并且哈希表中的元素个数大于等于一维数据的长度，自动开始对dict进行扩容扩容至2倍
2. 如果服务器正在执行bgsave命令，并且哈希表中的元素个数大于等于一维数据的长度的5倍，才进行强制扩容

缩容：

1. 当元素个数低于数组长度的 10%，Redis 会对 hash 表进行缩容来减少 hash 表的第一维数组空间占用。缩容不会考虑 Redis 是否正在做 bgsave。


SAVE和BGSAVE的区别：

1. SAVE  保存是阻塞主进程，客户端无法连接redis，等SAVE完成后，主进程才开始工作，客户端可以连接

2. BGSAVE  是fork一个save的子进程，在执行save过程中，不影响主进程，客户端可以正常链接redis，等子进程fork执行save完成后，通知主进程，子进程关闭。很明显BGSAVE方式比较适合线上的维护操作，两种方式的使用一定要了解清楚在谨慎选择。

### 压缩列表

zset 和 hash 容器对象在元素个数较少的时候，采用压缩列表 (ziplist) 进行存储。压缩列表是一块连续的内存空间，元素之间紧挨着存储，没有任何冗余空隙。

![](pics/redis-ziplist.png)

- zlbytes：4字节，记录整个压缩列表占用内存的字节数
- zltail：4字节，记录压缩列表尾部节点距离起始地址的偏移量
- zllen：2字节，记录压缩列表包含的节点数量
- entry：不定，列表中的每个节点
- zlend：1字节，特殊值0xFF，标记压缩列表的结束



增加元素：

1. 因为 ziplist 都是紧凑存储，没有冗余空间 。意味着插入一个新的元素就需要调用 realloc 扩展内存。取决于内存分配器算法和当前的 ziplist 内存大小，realloc 可能会重新分配新的内存空间，并将之前的内容一次性拷贝到新的地址，也可能在原有的地址上进行扩展，这时就不需要进行旧内容的内存拷贝。
2. 如果 ziplist 占据内存太大，重新分配内存和拷贝内存就会有很大的消耗。所以 ziplist 不适合存储大型字符串，存储的元素也不宜过多。

级联更新：

1. 当前某个 entry 之前的节点 从小于254字节，变成大于等于254字节， 那么当前entry 的 previous_entry_length 从1字节变成5字节。如果因为从1字节变成5字节，使自己跨越了从小于254字节，到过了254字节这条线，就又会引起下一个节点的扩容。
2. 最坏的情况是：所有entry都是刚好处于250-253字节之间，然后在链表头插入一个大于等于254字节的entry，此时会触发全链级联更新。
3. 删除中间的某个节点也可能会导致级联更新



### 快速列表

考虑到链表的附加空间相对太高，prev 和 next 指针就要占去 16 个字节 (64bit 系统的指针是 8 个字节)，另外每个节点的内存都是单独分配，会加剧内存的碎片化，影响内存管理效率。后续版本对列表数据结构进行了改造，使用 quicklist 代替了 ziplist 和 linkedlist。

```c++
typedef struct quicklist {
    quicklistNode *head;        // 指向quicklist的头部
    quicklistNode *tail;        // 指向quicklist的尾部
    unsigned long count;        // 列表中所有数据项的个数总和
    unsigned int len;           // quicklist节点的个数，即ziplist的个数
    int fill : 16;              // ziplist大小限定，由list-max-ziplist-size给定
    unsigned int compress : 16; // 节点压缩深度设置，由list-compress-depth给定
} quicklist;

```



quicklist 是 ziplist 和 linkedlist 的混合体，它将 linkedlist 按段切分，每一段使用 ziplist 来紧凑存储，多个 ziplist 之间使用双向指针串接起来。

![](pics/redis-quicklist.png)

1. quicklist 内部默认单个 ziplist 长度为 8k 字节，超出了这个字节数，就会新起一个 ziplist。ziplist 的长度由配置参数list-max-ziplist-size决定。
2. quicklist 默认的压缩深度是 0，也就是不压缩。压缩的实际深度由配置参数list-compress-depth决定。为了支持快速的 push/pop 操作，quicklist 的首尾两个 ziplist 不压缩，此时深度就是 1。如果深度为 2，就表示 quicklist 的首尾第一个 ziplist 以及首尾第二个 ziplist 都不压缩。


###  跳跃列表
Redis 的 zset 是一个复合结构，一方面它需要一个 hash 结构来存储 value 和 score 的对应关系，另一方面需要提供按照 score 来排序的功能，还需要能够指定 score 的范围来获取 value 列表的功能，这就需要另外一个结构「跳跃列表」。

![](pics/redis-skiplist.png)

图中只画了四层，Redis 的跳跃表共有 64 层，意味着最多可以容纳 2^64 次方个元素。每一个 kv 块对应的结构如下面的代码中的zslnode结构，kv header 也是这个结构，只不过 value 字段是 null 值——无效的，score 是 Double.MIN_VALUE，用来垫底的。kv 之间使用指针串起来形成了双向链表结构，它们是 有序 排列的，从小到大。不同的 kv 层高可能不一样，层数越高的 kv 越少。同一层的 kv 会使用指针串起来。每一个层元素的遍历都是从 kv header 出发。

[跳跃表原理](数据库/Redis/102-跳跃表原理.md)