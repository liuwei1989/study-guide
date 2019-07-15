### ZooKeeper是干啥的？

ZooKeeper是一个开源的分布式协调服务，他为分布式应用提供了高效且可靠的分布式协调服务，提供了诸如统一命名空间服务，配置服务和分布式锁等分布式基础服务。

### ZooKeeper基本概念

#### 集群角色

和Paxos算法中的集群角色类型，ZooKeeper中包含Leader、Follower和Observer三个角色；

通过一次选举过程，被选举的机器节点被称为Leader，Leader机器为客户端提供读和写服务；

Follower和Observer是集群中的其他机器节点，唯一的区别就是：Observer不参与Leader的选举过程，也不参与写操作的过半写成功策略。

一个典型的ZooKeeper集群如下：

![](https://images2015.cnblogs.com/blog/581813/201706/581813-20170626010114336-754141744.jpg)

#### 会话

会话就是一个客户端与服务器之间的一个TCP长连接。客户端和服务器的一切交互都是通过这个长连接进行的；

会话会在客户端与服务器断开链接后，如果经过了设点的sessionTimeout时间内没有重新链接后失效。

#### 节点

节点在ZeeKeeper中包含两层含义：

1. 集群中的一台机器，我们成为机器节点；
2. ZooKeeper数据模型中的数据单元，我们成为数据节点（ZNode）。

ZooKeeper的数据模型是内存中的一个ZNode数，由斜杠\(/\)进行分割的路径，就是一个ZNode，每个ZNode上除了保存自己的数据内容，还保存一系列属性信息；

ZooKeeper中的数据节点分为两种：持久节点和临时节点。

所谓的持久节点是指一旦这个ZNode创建成功，除非主动进行ZNode的移除操作，节点会一直保存在ZooKeeper上；而临时节点的生命周期是跟客户端的会话相关联的，一旦客户端会话失效，这个会话上的所有临时节点都会被自动移除。

#### 版本

ZooKeeper为每一个ZNode节点维护一个叫做Stat的数据结构，在Stat中维护了节点相关的三个版本：

1. 当前ZNode的版本 version
2. 当前ZNode子节点的版本 cversion
3. 当前ZNode的ACL\(Access Control Lists\)版本 aversion

#### 监听器Watcher

ZooKeeper允许用户在指定节点上注册一些Watcher，并且在一些特定事件触发的时候，ZooKeeper会通过事件通知到感兴趣的客户端上。

#### ACL（Access Control Lists）

ZooKeeper中定义了5中控制权限：

1. CREATE：创建子节点的权限
2. READ：获取节点数据和子节点列表的权限
3. WRITE：跟新节点数据的权限
4. DELETE：删除子节点的权限
5. ADMIN：设置节点ACL的权限。

其中CREATE和DELETE这两种权限都是针对子节点的权限控制。

### ZooKeeper的数据模型

上面有提到ZooKeeper的数据模型是一个ZNode节点树，是一个类型与标准文件系统的层次结构，也是使用斜杠\(/\)进行分割，如下图：

![](https://images2015.cnblogs.com/blog/581813/201706/581813-20170626005312757-1275647224.jpg)

在ZooKeeper中每一个节点都可以使用其路径唯一标识，如节点p\_1的标识为：/app1/p\_1

每个ZNode节点都可以存储自己的数据，还可以拥有自己的子节点目录。

### 参考

[http://zookeeper.apache.org/doc/trunk/zookeeperOver.html](http://zookeeper.apache.org/doc/trunk/zookeeperOver.html)

《从Paxos到ZooKeeper分布式一致性原理与实践》

[https://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/](https://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/)

[http://www.cnblogs.com/sunddenly/p/4138580.html](http://www.cnblogs.com/sunddenly/p/4138580.html)

