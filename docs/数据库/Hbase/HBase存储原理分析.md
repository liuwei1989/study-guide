# HBase

HBase 是一个构建在 HDFS 上的分布式列族存储系统，其内部管理的文件全部存储在 HDFS 中。

HDFS 特点：

- 有良好的容错性和扩展性，都可以扩展到成百上千个节点
- 适合批处理场景
- 不支持数据随机查找，不适合增量数据处理并且不支持数据更新

HBase 特点：

- 容量大：HBase单表可以有百亿行、百万列，数据矩阵横向和纵向两个纬度所支持的数据量级都非常具有弹性；
- 面向列：列是可以动态增加的，不需要指定列，面向列的存储和权限控制，并支持独立检索；
- 多版本：每一个列的数据存储有个多Version；
- 稀疏性：为空的列不占用存储空间；
- 扩展性：底层依赖于HDFS，空间不够的时候只需要横向扩展即可；
- 高可靠性：副本机制保证了数据的可靠性；
- 高性能：写入性能高，底层使用LSM数据结构和RowKey有序排序等架构上的独特设计；读性能高，使用region切分、主键索引和缓存机制使得具备随机读取性能高。

## 一、HBase 存储模式

HBase 是构建在 HDFS 分布式的列族式存储系统。HBase 内部管理的文件全部存储在 HDFS 中。HDFS 特点：

### 行式存储和列式存储

> **什么是行式存储和列式存储**

行式存储：以行为存储基准的存储方式称为行式存储，一行的数据聚合存储在一块。

列式存储：以列为存储基准的存储方式称为列式存储，保证每一列的数据存储在一块。

[](./pics/h_1.png)

> **行式存储与列式存储各自的特点**

|      |                        行式存储                         |                           列式存储                           |
| :--: | :-----------------------------------------------------: | :----------------------------------------------------------: |
| 优点 |  存在索引，随机读的效率很高<br/>对事务的处理能力非常高  | 根据同一列数据的相似性原理，有利于对数据进行压缩，<br/>其压缩效率远高于行式存储，存储成本比较低；<br/>可并行查询，查询效率高 |
| 缺点 | 维护大量索引，存储成本高；<br/>不能线性扩展，压缩效率低 |                          不支持事务                          |

> **行式存储和列式存储的应用环境**

- 行式存储的应用环境：

如果需要关系查询，那么行式存储很好。

行式存储最大的优点是关系之间的解决方案，表与表之间很大的关联关系并且数据量不大，那么行式存储就是很好的选择。记住因为它的线性扩展性不高，需要保证数据量不能特别大，控制在千万级与以下。

- 列式存储的应用环境：

如果数据量非常大，使用列式存储。

在大数据，利于压缩和扩展的肯定要选择列式存储，如果事务使用率不高，那么也最好使用列式存储，随机更新更些行的频率不高，也可以使用列式存储。

### HBase 的列族式存储

**列族**指多个数据列的组合，HBase 中的每个列都归属于一个列族，列族是表 schema 的一部分。

> **HBase Table 组成**：

`Table = RowKey(行键) + Family(列族) + Column(列) + TimeStamp(版本) + Value(数据值)`

> **数据存储模式**：

`(Table,RowKey,Family,Column,TimeStamp) -> Value `

如下图，上面的表示传统的关系型数据库的表结构，下面就是 HBase 的表结构：

[](./pics/h_2.jpg)

> **列数据属性**：

HBase 中默认一列数据可以保存三个版本，比如对于聊天数据，可标记为已读、未读等属性。

[](./pics/h_3.jpg)

> **数据存储原型**：

HBase 是一个稀疏的、分布式、持久、多维、排序的映射，它以行键（RowKey），列键（Column）和时间戳（TimeStamp）为索引。

Hbase 在存储数据的时候，有两个 SortedMap，首先按照 Rowkey 进行字典排序，然后再对 Column 进行字典排序：

`SortedMap<RowKey,List<SortedMap<Column,List<Value,TimeStamp>>>>`

第一个 SortedMap 代表那个表，包含一个列族的 List。列族中包含了另一个 SortedMap 存储列和相应的值。这些值在最后的 List 中，存储了值和该值被设置的时间戳。

### HBase 存储示例

- 示例表：

| 表名 | 列族 |         列          |
| :--: | :--: | :-----------------: |
| test |  cf  | a,b,c,month，day... |

- 示例表数据：

[](./pics/h_5.jpg)

HBase 的所有操作均是基于 RowKey 的。支持 CRUD（Create、Read、Update 和 Delete）和 Scan 操作：

- 单行操作：Put 、Get 和 Scan。
- 多行操作：Scan 和 MultiPut。

## 二、HBase 数据表分析

### 建表语句解析

```html
create 'demo:user',
{NAME=>'b',VERSIONS=>'3',COMPERSSION=>'SNAPPY',COMPRESSION_COMPACT=>'SNAPPY',
REPLICATION_SCOPE=>1},
{NAME=>'o',REPLICATION_SCOPE=>1,COMPERSSION=>'SNAPPY',COMPRESSION_COMPACT=>'SNAPPY'}
```

`create 'demo:user'` 中 demo 是命名空间，user 是表名。

`NAME`列族名；

`VERSION`数据版本数，设置一列的数据版本数量，默认值为 3；

`REPLICATION_SCOPE` 复制机制，主从复制。通过预写日志和 Hlog 实现的，当请求发送给 Master 的时，log 日志放入 hdfs 的同时，会进入 REPLICATION 这个队列中，Slave 通过 Zookeeper 去获取，并写入 Slave 的表中；

`COMPERSSION`：数据压缩的配置。

注：Snappy 是一种压缩特性，虽然压缩率较低，但其编解码速率更高。

### HBase 存储目录解析

在 hbase-site.xml 文件中设置数据存储目录:

```xml
<property>
	<name>hbase.rootdir</name>
    <value>/home/hbase_data</value>
</property>
```

- .tmp：当对表进行创建和删除操作的时候，会将表移动到该目录下，然后再进行操作。它是一个临时存储当前需要修改的数据结构。
- WALs：存储预写日志。
- archive：存储表的归档和快照，由Master上的一个任务定时进行处理。
- corrupt：用于存储损坏的日志文件，一般为空。
- data：存储数据的核心目录，系统表和用户表均存储在这个目录下。data 目录集体如下：

[](./pics/h_6.jpg)

- hbase.id：hbase：集群中的唯一id，用于标识hbase进程。
- hbase.version：表明了文件版本信息。
- oldWALs：当log已经持久化以后，WALs中的日志文件会移动到该目录下。

### HBase 元信息表（系统表）

元信息表同样是一张 Hbase 表，同样拥有 RowKey 和列族这样的概念。存储在 Region Server 上，位于Zookeeper 上，用户查询数据的时候，需要先到 Zookeeper 上获取到元数据表的数据再进行相应用户的查找。

[](./pics/h_7.jpg)

RowKey：格式化的 region key

Value：保存着region server的地址，其中最重要的一个列族就是 info，其中最重要的数据列是 server，包含region server 的地址和端口号。

元信息表的值当 region 进行分割、disable、enable、drop 或 balance 等操作，或 region server 挂掉都会导致元信息表值的变化，Master 就需要重新分配 region，元信息表就会及时更新。

元信息表相当于 Hbase 的第一级索引，是 Hbase 中最重要的系统表。

[](./pics/h_8.jpg)

## 三、HBase 物理模型

### LSM 存储思想

LSM 树（即日志结构合并树）思想：

把一棵大树拆分成 N 棵小树，它首先写入内存中，随着小树越来越大，内存中的小树会 flush 到磁盘中，磁盘中的树定期可以做 merge 操作，合并成一棵大树，以优化读性能。

下图是一个 LSM 树的简易模型：

- C0：所有数据均存储在内存
- C1：所有数据均存储在磁盘

当一条新的记录插入的时候，先从 C0 中插入，当达到 C0 的阈值以后，就将 C0 中的某些数据片段迁移到 C1 中并合并到 C1 树上。由于合并排序算法是批量的、并且是顺序存储的，所以速度十分快。

[](./pics/h_10.jpg)

LSM 树思想在 HBase 中的实现（三层存储结构）：

- Level 0：日志/内存，为了加速随机写的速度，先写入日志和内存中，由于内存中的数据是不稳定的，日志是为了保障高可用。当达到阈值，会有异步线程将部分数据 flush 到硬盘上；
- Level 1：日志/内存；
- Level 2：合并，由于不断地刷写会产生大量小文件，这样不利于管理和查询，需要在合适的时机启动线程进行合并操作会生成一个大文件（多路归并算法）。

[](./pics/h_9.jpg)

### 数据存储模块简介

`RegionServer = Region + Store  + MemStore + StoreFile + HFile + HLog`

[](./pics/h_11.jpg)

- Region ：对于一个 RegionServer 可以包含很多 Region，并且每一个 Region  包含的数据都是互斥的，存储有用户各个行的数据。
- Store  ：对应表中的列族，即有多少个列族，就有多少个 Store。
- MemStore ：是一个**内存式的数据结构**，用户数据进入 Region 之后会先写入 MemStore 当满了之后，再 flush 到 StoreFile 中，在 StoreFile 中将数据封装成 HFile 再 flush 到 HDFS 上 。
- HLog：对于一个 RegionServer 只有一个 HLog 实例。

HLog 和 MemStore 构成了 Level 0，保证了**数据的高可用**和**性能的低延迟**。

StoreFile 和 HFile 构成了Level 1，实现了**不可靠数据的持久化**，真正地将 HBase 变成了高可用的数据库系统。

### HBase Region 解析

[](./pics/h_12.png)

Hbase 的 Table 中的所有行都按照 RowKey的字典序排列。Table 在行的方向上分割为多个 Region。Region 是按大小分割的，每个表开始只有一个 Region，随着数据增多，Region 不断增大，当增大到一个阈值的时候， Region 就会等分会两个新的 Region，之后会有越来越多的 Region。

[](./pics/h_13.png)

Region 是 HBase 中**分布式存储和负载均衡的最小单元**。 

每个 Region 只能被一个 RegionServer 服务；RegionServer 可同时服务多个 Region。

[](./pics/h_14.png)

Region 虽然是**分布式存储的最小单元，但不是存储的最小单元**。Region 由一个或者多个 Store 组成，每个 Store保存一个列族。每个 Store 又由一个 memStore和多个 StoreFile 组成。memStore 存储在内存中，StoreFile 存储在 HDFS 上。

### HBase HFile 解析

[](./pics/h_17.png)



HFile 分为六个部分：

- DataBlocks：保存表中的数据，这部分可以被压缩。
- MetaBlocks（可选的）：保存用户自定义的键值对，可以被压缩。
- FileInfo：HFile 的元信息，不被压缩，用户也可以在这一部分添加自己的元信息。
- DataIndex：DataBlock 的索引。每条索引的 key 是被索引的 block 的第一条记录的 key。
- MetaBlock(可选的)：MetaBlock 的索引。
- Trailer：定长。保存了每一段的偏移量，读取一个 HFile 时，会首先读取 Trailer，Trailer 保存了每个段的起始位置(段的 Magic Number 用来做安全 check)，然后，DataBlock Index（DataBlock 的索引）会被读取到内存中，这样，当检索某个 key 时，不需要扫描整个 HFile，而只需从内存中找到 key 所在的 block，通过一次磁盘 I / O将整个 block 读取到内存中，再找到需要的 key。DataBlock Index采用 LRU （最近最久未访问）机制淘汰。

[](./pics/h_18.png)

HFile 的 Data Block，Meta Block 通常采用压缩方式存储，压缩之后可以大大减少网络 I / O 和磁盘 I / O，随之而来的开销当然是需要花费 CPU 资源进行压缩和解压缩。

目标 HFile 的压缩支持两种方式：Gzip，Lzo。

### HBase WAL 解析

WAL（Write Ahead Log）用来做灾难恢复，HLog 记录数据的所有变更,一旦数据修改，就可以从 log 中进行恢复。

**每个 Region Server 维护一个 HLog，而不是每个 Region 维护一个 HLog**。这样不同 Region （来自不同 table）的日志会混在一起，这样做的目的是不断追加单个文件相对于同时写多个文件而言，可以减少磁盘寻址次数，可以提高对 table 的写性能。带来的麻烦是，如果一台 Region Server 失效，为了恢复其上的 Region，需要将 Region server上的 log 进行拆分，然后分发到其它 Region Server 上进行恢复。

[](./pics/h_19.jpg)

HLog 文件就是一个普通的 Hadoop Sequence File，Sequence File 的 Key 是 HLogKey 对象，HLogKey 中记录了写入数据的归属信息，除了 table 和 Region 名字外，同时还包括 Sequence Number 和 WriteTime（写入时间），Sequence Number 的起始值为 0，或者是最近一次存入文件系统中的 Sequence Number。

HLog Sequece File 的 Value 是 HBase 的 KeyValue 对象，即对应 HFile 中的 KeyValue。

### HBase Compaction 解析

Compaction 会从Region Store 中选择一些 HFile 文件进行合并，合并就是指将一些待合并的文件中的 K-V 对进行排序重新生成一个新的文件取代原来的待合并的文件。由于系统不断地进行刷写会产生大量小文件，这样不利于数据查找。

那么将这些小文件合并成一些大文件，这样使得查找过程的 I / O 次数减少，提高了查找效率。

其中可以分为以下两类：

- MinorCompaction：选取一些小的相邻的 Store File 进行合并一个更大的 Store File，生成多个较大的 Store File。
- MajorCompaction：将所有的 Store File 合并成一个 Store File，这个过程中**将清理被删除的数据、TTL 过期的数据、版本号超过设定版本号的数据**。操作过程的时间比较长，消耗的资源也比较多。

[](./pics/h_21.jpg)

HBase 中 Compaction 的触发时机的因素很多，最主要的有三种：

- MemStore Flush ：每次执行完 Flush 操作以后，都会进行判断是否超过阈值，若超过就触发一个合并。
- 后台线程周期性的检查：Compaction Checker 会定期触发检查是否需要合并，这个线程会优先检查文件数是否大于阈值，一旦大于就会触发合并，若不满足会继续检查是否满足 MajorCompaction。简单来说，就是如果当前 Store 中最早更新时间早于某个值，这个值成为 mc time，就会触发大的合并，HBase 通过这种方式来删除过期的数据，其浮动区间为 `[7-7 * 0.2, 7+7 * 0.2]`。默认在七天会进行一 次MajorCompaction。
- 手动触发：通常是为了执行 MajorCompaction，因为很多业务担心自动的 MajorCompaction 会影响性能。选择在低峰期手动触发；用户在进行完 alter 操作以后，希望立即生效；管理员在发现硬盘容量不够的时候会手动触发，这样可以删除大量的过期数据。

## 四、HBase 系统架构

### 组件

[](./pics/h_15.png)

HBase 构建在 HDFS 的基础上：

#### 1、Client

包含访问 HBase 的接口，并维护 cache 来加快对 HBase 的访问，比如 Region 的位置信息。

#### 2、Zookeeper

- 保证任何时段，集群中只有一个 Master
- 存储所有 Region 的寻址地址
- 实时监控 Region Server 的线上线下信息，并且通知给 Master
- 存储 HBase 的 schema 和 table 的元数据
- 默认情况下，HBase 管理 Zookeeper 实例，Master 与 RegionServers 启动时会向 Zookeeper 注册，Zookeeper 的引入使得 Master 不再是单点故障。

[](./pics/h_20.png)

#### 3、HMaster

- 为 RegionServer 分配 Region 和 RegionServer 的负载均衡
- 发现失效的 Region Server 并重新分配其上的 Region
- 管理用户对 table 的增删改查操作

#### 4、HRegionServer

- HRegionServer 负责维护 Region，处理对这些 Region 的 I / O 请求
- HRegionServer 负责切分在运行过程中变得过大的 Region

### 容错机制

#### Master 容错

Zookeeper 重新选择一个新的 Master，无 Master 过程中：

- 数据读取仍照常进行；
- Region切分、负载均衡等无法进行；

#### RegionServer 容错

定时向 Zookeeper 汇报心跳，如果一段时间内未出现心跳，Master 将该 RegionServer 上的 Region 重新分配到其他 RegionServer 上，失效服务器上“预写”日志由主服务器进行分割并派送给新的 RegionServer。

- RegionServer容错：定时向Zookeeper汇报心跳，如果一旦时间内未出现心跳，Master将该RegionServer上的Region重新分配到其他RegionServer上，失效服务器上“预写”日志由主服务器进行分割并派送给新的RegionServer

#### Zookeeper 容错

Zookeeper 是一个可靠的服务，一般配置 3 或 5 个 Zookeeper 实例。

## 五、HBase 数据存取解析

[](./pics/h_16.png)

###  数据存取流程

#### 1、数据存储

> **客户端**

提交之前会先请求 Zookeeper 来确定元数据表所在的 Region Server 的地址，再根据 RowKey 确定归属的 Region Server，之后用户提交 Put/Delete 这样的请求，HBase 客户端会将 Put 请求添加到本地的 buffer 中，符合一定的条件就会通过异步批量提交。

HBase 默认设置 auto flush（自动刷写）为 true，表示 Put 请求会直接提交给服务器进行处理，用户可以设置 auto flush 为 false，这样 Put 请求会首先放入本地的 buffer 中，等到buffer 的大小达到一定阈值（默认是2M）后再提交。

[](./pics/h_22.jpg)

> **服务端**

当数据到达 Region Server 的某个 Region 后，首先获取 RowLock（行锁），之后再日志和写入缓存，此时并不会同步日志，操作完释放行锁，随后再将同步（sync）到 HDFS 上，如果同步失败进行回滚操作将缓存中已写入的数据删除掉，代表插入失败。

当缓存达到一定阈值（默认是64M）后，启动异步线程将数据刷写到硬盘上形成多个 StoreFile，当 StoreFile 数量达到一定阈值后会触发合并操作。当单个 StoreFile 的大小达到一定大小的时候会触发一个 split 操作，将当前的Region 切分为两个 Region，再同步到 HMater 上，原有 Region 会下线，子 HRegion 会被 HMaster 分配到相应的Region Server 上。

[](./pics/h_23.jpg)

#### 2、数据获取

> **客户端**

这里同数据存储的过程类似。

[](./pics/h_24.jpg)

> **服务端**

Region Server 在接收到客户端的 Get / Scan 请求之后，首先 HBase 在确定的 RegionServer 上构造一个RegionScanner 准备为当前定位的 Scan 做检索。

RegionScanner 会根据列族构建 StoreScanner，有多少个列族就会构建多少个 StoreScanner。每个 StoreScanner 会为当前 Store 中的每个 HFile 构建一个 StoreFileScanner，用于实际执行对应的文件检索。同时会对对应的 Mem 构建对应的 MemStoreScanner，用于检索 MemStore 中的数据。

构建两类 Scanner 的原因在于，数据可能还没有完全刷写到硬盘上，部分数据还存储于内存之中。检索完之后，就能够找到对应的 K-V，再经过简单地封装就形成了 ResultSet，就可以直接返回给客户端。

[](./pics/h_25.jpg)

### 数据存取优化

#### 布隆过滤器

布隆过滤器使用 BitSet 存储数据，但是它进行了一定的改进，从而解除了 BitSet 要求数据的范围不大的限制。

**在存储时，它要求数据先经过 k 个哈希函得到 k 个位置，并将 BitSet 中对应位置设置为 1。在查找时，也需要先经过 k 个哈希函数得到 k 个位置，如果所有位置上都为 1，那么表示这个数据存在**。

由于哈希函数的特点，两个不同的数通过哈希函数得到的值可能相同。如果两个数通过 k 个哈希函数得到的值都相同，那么使用布隆过滤器会将这两个数判为相同。

可以知道，令 k （k 个哈希函数）和 m （m 是 BitSet 的位数）都大一些会使得误判率降低，但是这会带来更高的时间和空间开销。

布隆过滤器会误判，也就是将一个不存在的数判断为已经存在，这会造成一定的问题。例如在垃圾邮件过滤系统中，会将一个邮件误判为垃圾邮件，那么就收不到这个邮件。可以使用白名单的方式进行补救。

[](./pics/h_26.png)

#### 1、数据存储优化

HBase 通过 MemStore 和 WAL 两种机制，实现**数据顺序快速的插入**，极大降低了数据存储的延迟。

#### 2、数据获取优化

HBase 使用布隆过滤器来提高**随机读**的性能，布隆过滤器是列族级别的配置。HBase 中的每个 HFile 都有对应的**位数组**，K-V 在写入 HFile 时，会经过几个哈希函数的映射并写入对应的位数组里面。HFile 中的位数组，就是布隆过滤器中存储的值。HFile 越大位数组也会越大，太大就不适合放入内存中了，因此 HFile 将位数组按照 RowKey 进行拆分，一部分连续的 RowKey 使用一个位数组，HFile 会有多个位数组，在查询的时候，首先会定位到某个位数组再将该位数组加载到内存中进行过滤就行，这样减少了内存的开支。

HBase 中存在两种布隆过滤器：

- Row：根据 RowKey 来过滤 StoreFile，这种情况可以针对列族和列都相同，只有 RowKey 不同的情况；
- RowCol：根据 RowKey + ColumnQualifier（列描述符）来过滤 StoreFile，这种情况是针对列族相同，列和RowKey 不同的情况。

## 参考资料

- [Hbase存储模式](https://www.jianshu.com/p/9d373efcc336)
- [hbase 表存储结构的详细理解，各个模块的作用介绍](https://blog.csdn.net/maketubu7/article/details/80612930)
- [HBase 存储原理剖析](https://www.imooc.com/learn/996)
- [Hbase的应用场景、原理及架构分析](https://blog.csdn.net/xiangxizhishi/article/details/75388971)

- [Hbase原理、基本概念、基本架构](https://blog.csdn.net/woshiwanxin102213/article/details/17584043)
- [HBase 超详细介绍](https://blog.csdn.net/FrankieWang008/article/details/41965543)

- [HBase详解（很全面](https://blog.csdn.net/lukabruce/article/details/80624619)