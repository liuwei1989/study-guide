## 关于LSM树

LSM树，即日志结构合并树(Log-Structured Merge-Tree)。其实它并不属于一个具体的数据结构，它更多是一种数据结构的设计思想。大多NoSQL数据库核心思想都是基于LSM来做的，只是具体的实现不同。

## LSM树诞生背景
传统关系型数据库使用btree或一些变体作为存储结构，能高效进行查找。但保存在磁盘中时它也有一个明显的缺陷，那就是逻辑上相离很近但物理却可能相隔很远，这就可能造成大量的磁盘随机读写。随机读写比顺序读写慢很多，为了提升IO性能，我们需要一种能将随机操作变为顺序操作的机制，于是便有了LSM树。LSM树能让我们进行顺序写磁盘，从而大幅提升写操作，作为代价的是牺牲了一些读性能。

## 关于磁盘IO
磁盘读写时涉及到磁盘上数据查找，地址一般由柱面号、盘面号和块号三者构成。也就是说移动臂先根据柱面号移动到指定柱面，然后根据盘面号确定盘面的磁道，最后根据块号将指定的磁道段移动到磁头下，便可开始读写。

整个过程主要有三部分时间消耗，查找时间(seek time) +等待时间(latency time)+传输时间(transmission time) 。分别表示定位柱面的耗时、将块号指定磁道段移到磁头的耗时、将数据传到内存的耗时。整个磁盘IO最耗时的地方在查找时间，所以减少查找时间能大幅提升性能。

## LSM树原理
LSM树由两个或以上的存储结构组成，比如在论文中为了方便说明使用了最简单的两个存储结构。一个存储结构常驻内存中，称为C0 tree，具体可以是任何方便健值查找的数据结构，比如红黑树、map之类，甚至可以是跳表。另外一个存储结构常驻在硬盘中，称为C1 tree，具体结构类似B树。C1所有节点都是100%满的，节点的大小为磁盘块大小。

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1335918197,131868783&fm=173&app=25&f=JPEG?w=550&h=197&s=5AA8346207FA58230AF5C5DA0000C0B1)

## 插入步骤
大体思路是：插入一条新纪录时，首先在日志文件中插入操作日志，以便后面恢复使用，日志是以append形式插入，所以速度非常快；将新纪录的索引插入到C0中，这里在内存中完成，不涉及磁盘IO操作；当C0大小达到某一阈值时或者每隔一段时间，将C0中记录滚动合并到磁盘C1中；对于多个存储结构的情况，当C1体量越来越大就向C2合并，以此类推，一直往上合并Ck。

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1252199397,485300143&fm=173&app=25&f=JPEG?w=550&h=170&s=09AA7C329FA840031E7CB0CA0000A0B1)

## 合并步骤
合并过程中会使用两个块：emptying block和filling block。

1. 从C1中读取未合并叶子节点，放置内存中的emptying block中。
2. 从小到大找C0中的节点，与emptying block进行合并排序，合并结果保存到filling block中，并将C0对应的节点删除。
3. 不断执行第2步操作，合并排序结果不断填入filling block中，当其满了则将其追加到磁盘的新位置上，注意是追加而不是改变原来的节点。合并期间如故宫emptying block使用完了则再从C1中读取未合并的叶子节点。
4. C0和C1所有叶子节点都按以上合并完成后即完成一次合并。

## 关于优化措施
本文用图阐述LSM的基本原理，但实际项目中其实有很多优化策略，而且有很多针对LSM树优化的paper。比如使用布隆过滤器快速判断key是否存在，还有做一些额外的索引以帮助更快找到记录等等。

## 插入操作

向LSM树中插入A E L R U，首先会插入到内存中的C0树上，这里使用AVL树，插入“A”，先项磁盘日志文件追加记录，然后再插入C0，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=2033269763,2620018998&fm=173&app=25&f=JPEG?w=550&h=309&s=47B1EC6C1AE7997A1A76E80E0300B0C9)

插入“E”，同样先追加日志再写内存，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=2247497468,2054704171&fm=173&app=25&f=JPEG?w=550&h=321&s=57B1EC6C9AE68F6C4A76EC0F030070CB)
继续插入“L”，旋转后如下，

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=693301584,3912259745&fm=173&app=25&f=JPEG?w=550&h=319&s=4FB1EC4C1AE78F6C5A76E80F030020CB)

插入“R”“U”，旋转后最终如下。

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1015735956,1143074971&fm=173&app=25&f=JPEG?w=550&h=318&s=0FB5EC481AE7996C5A76E80F030020C9)

假设此时触发合并，则因为C1还没有树，所以emptying block为空，直接从C0树中依次找最小的节点。filling block长度为4，这里假设磁盘块大小为4。

开始找最小的节点，并放到filling block中，

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1288115043,3198674279&fm=173&app=25&f=JPEG?w=550&h=317&s=47B0EC6C9AE7836C0A76E80F0300A0C9)

继续找第二个节点，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1087860701,1614172228&fm=173&app=25&f=JPEG?w=550&h=312&s=57B1EC6C1AE7DF7E5E56EC0B030070CB)

以此类推，填满filling block，

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=436737379,1624943161&fm=173&app=25&f=JPEG?w=550&h=315&s=47B1EC6C1AE39F6C4A76CC0F0300A0C9)

开始写入磁盘，C1树，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=3195205922,3762229222&fm=173&app=25&f=JPEG?w=550&h=316&s=47B1EC6C9AE3CF6C5A76C80F030020C9)

继续插入B F N T，先分别写日志，然后插入到内存的C0树中，

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=2022919156,4105082633&fm=173&app=25&f=JPEG?w=550&h=313&s=4FB1EC481AE7896E5A76E80F0300F0C9)

假如此时进行合并，先加载C1的最左边叶子节点到emptying block，

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=229964256,2798073394&fm=173&app=25&f=JPEG?w=550&h=320&s=07B5EC6C1AE5DF7E5A76E80F030020CB)

接着对C0树的节点和emptying block进行合并排序，首先是“A”进入filling block，

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=1373216494,1819114912&fm=173&app=25&f=JPEG?w=550&h=315&s=4FB1EC4C1AE78B6E5A76EC0F0300A0CB)

然后是“B”，

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1757749125,2440533772&fm=173&app=25&f=JPEG?w=550&h=311&s=5FB5EC4C1AE68D6C5A76C80F030060CB)

合并排序最终结果为，

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=3600878794,1442988143&fm=173&app=25&f=JPEG?w=550&h=324&s=0FB5EC4C1AE7976C4A72E80F030070C9)


将filling block追加到磁盘的新位置，将原来的节点删除掉，

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=1428319108,3586384198&fm=173&app=25&f=JPEG?w=550&h=314&s=47B1EC681AE68F6C4A76E80F0300F0C9)

继续合并排序，再次填满filling block，

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3523514046,1798547171&fm=173&app=25&f=JPEG?w=550&h=323&s=5FB5EC4C1AE78B7C5A76E80E030070CB)

将filling block追加到磁盘的新位置，上一层的节点也要以磁盘块（或多个磁盘块）大小写入，尽量避开随机写。另外由于合并过程可能会导致上层节点的更新，可以暂时保存在内存，后面在适当时机写入。

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3928421280,1105253966&fm=173&app=25&f=JPEG?w=550&h=286&s=47B5EC6C1AA78B6C1E72C4030300B0C9)

## 查找操作
查找总体思想是先找内存的C0树，找不到则找磁盘的C1树，然后是C2树，以此类推。

假如要找“B”，先找C0树，没找到。

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=3172258544,3991786599&fm=173&app=25&f=JPEG?w=550&h=295&s=47B1EC681AA6AB7C1A72CC0E030070C9)

接着找C1树，从根节点开始，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=4046943404,3609915015&fm=173&app=25&f=JPEG?w=550&h=287&s=57B5EC6C1AE089781E72CC0B0300B0CB)

找到“B”。

![](https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=4083708814,2898464590&fm=173&app=25&f=JPEG?w=550&h=276&s=47B1EC681AA18B7E5272CC0B030070CB)

## 删除操作
删除操作为了能快速执行，主要是通过标记来实现，在内存中将要删除的记录标记一下，后面异步执行合并时将相应记录删除。

比如要删除“U”，假设标为#的表示删除，则C0树的“U”节点变为，

![](https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1054955816,1453669578&fm=173&app=25&f=JPEG?w=550&h=298&s=57B1EC6C1AA68B6C1872CC0E030070C9)

而如果C0树不存在的记录，则在C0树中生成一个节点，并标为#，查找时就能在内存中得知该记录已被删除，无需去磁盘找了。比如要删除“B”，那么没有必要去磁盘执行删除操作，直接在C0树中插入一个“B”节点，并标为#。

![](https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1284947658,3312508502&fm=173&app=25&f=JPEG?w=550&h=287&s=47B1EC681AA6AB7C1A76CC0E030070C9)

[参考文章](https://baijiahao.baidu.com/s?id=1613810327967900833&wfr=spider&for=pc)