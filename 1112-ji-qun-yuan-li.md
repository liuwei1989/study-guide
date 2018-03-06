Mysqlcluster数据节点组内主从同步采用的是同步复制，来保证组内节点数据的一致性。一般通过两阶段提交协议来实现，一般工作过程如下：

a\)Master执行提交语句时，事务被发送到slave，slave开始准备事务的提交。

b\)每个slave都要准备事务，然后向master发送OK\(或ABORT\)消息，表明事务已经准备好（或者无法准备该事务）。

c\)Master等待所有Slave发送OK或ABORT消息

如果Master收到所有Slave的OK消息，它就会向所有Slave发送提交消息，告诉Slave提交该事务；

如果Master收到来自任何一个Slave的ABORT消息，它就向所有Slave发送ABORT消息，告诉Slave去中止事务。

e\)每个Slave等待来自Master的OK或ABORT消息。

如果Slave收到提交请求，它们就会提交事务，并向Master发送事务已提交的确认；

如果Slave收到取消请求,它们就会撤销所有改变并释放所占有的资源，从而中止事务，然后向Masterv送事务已中止的确认。

f\)当Master收到来自所有Slave的确认后，就会报告该事务被提交（或中止），然后继续进行下一个事务处理。

由于同步复制一共需要4次消息传递，故mysql  cluster的数据更新速度比单机mysql要慢。所以mysql cluster要求运行在千兆以上的局域网内，节点可以采用双网卡，节点组之间采用直连方式。

