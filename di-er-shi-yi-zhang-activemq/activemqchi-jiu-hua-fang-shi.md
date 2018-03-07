消息持久性对于可靠消息传递来说应该是一种比较好的方法，有了消息持久化，即使发送者和接受者不是同时在线或者消息中心在发送者发送消息后宕机了，在消息中心重新启动后仍然可以将消息发送出去，如果把这种持久化和ReliableMessaging结合起来应该是很好的保证了消息的可靠传送。

消息持久性的原理很简单，就是在发送者将消息发送出去后，消息中心首先将消息存储到本地数据文件、内存数据库或者远程数据库等，然后试图将消息发送给接收者，发送成功则将消息从存储中删除，失败则继续尝试。消息中心启动以后首先要检查制定的存储位置，如果有未发送成功的消息，则需要把消息发送出去。

ActiveMQ持久化方式：AMQ、KahaDB、JDBC、LevelDB。

1、AMQ

AMQ是一种文件存储形式，它具有写入速度快和容易恢复的特点。消息存储在一个个文件中，文件的默认大小为32M，如果一条消息的大小超过了32M，那么这个值必须设置大一点。当一个存储文件中的消息已经全部被消费，那么这个文件将被标识为可删除，在下一个清除阶段，这个文件被删除。AMQ适用于ActiveMQ5.3之前的版本。默认配置如下：

```
<persistenceAdapter>
   <amqPersistenceAdapter directory="activemq-data"maxFileLength="32mb"/>
</persistenceAdapter>
```



属性如下：

| 属性名称 | 默认值 | 描述 |
| :--- | :--- | :--- |
| directory | activemq-data | 消息文件和日志的存储目录 |
| useNIO | true | 使用NIO协议存储消息 |
| syncOnWrite | false | 同步写到磁盘，这个选项对性能影响非常大 |
| maxFileLength | 32Mb | 一个消息文件的大小 |
| persistentIndex | true | 消息索引的持久化，如果为false，那么索引保存在内存中 |
| maxCheckpointMessageAddSize | 4kb | 一个事务允许的最大消息量 |
| cleanupInterval | 30000 | 清除操作周期，单位ms |
| indexBinSize | 1024 | 索引文件缓存页面数，缺省为1024，当amq扩充或者缩减存储时，会锁定整个broker，导致一定时间的阻塞，所以这个值应该调整到比较大，但是代码中实现会动态伸缩，调整效果并不理想。 |
| indexKeySize | 96 | 索引key的大小，key是消息ID |
| indexPageSize | 16kb | 索引的页大小 |
| directoryArchive | archive | 存储被归档的消息文件目录 |
| archiveDataLogs | false | 当为true时，归档的消息文件被移到directoryArchive,而不是直接删除　　　　　　　　　　　　　　　　　　　　 |

2、KahaDB

KahaDB是基于文件的本地数据库储存形式，虽然没有AMQ的速度快，但是它具有强扩展性，恢复的时间比AMQ短，从5.4版本之后KahaDB做为默认的持久化方式。默认配置如下：

```
<persistenceAdapter>
   <kahaDB directory="activemq-data"journalMaxFileLength="32mb"/>
</persistenceAdapter>
```



KahaDB的属性如下：

| 属性名称 | 默认值 | 描述 |
| :--- | :--- | :--- |
| directory | activemq-data | 消息文件和日志的存储目录 |
| indexWriteBatchSize | 1000 | 一批索引的大小，当要更新的索引量到达这个值时，更新到消息文件中 |
| indexCacheSize | 10000 | 内存中，索引的页大小 |
| enableIndexWriteAsync | false | 索引是否异步写到消息文件中 |
| journalMaxFileLength | 32mb | 一个消息文件的大小 |
| enableJournalDiskSyncs | true | 是否讲非事务的消息同步写入到磁盘 |
| cleanupInterval | 30000 | 清除操作周期，单位ms |
| checkpointInterval | 5000 | 索引写入到消息文件的周期，单位ms |
| ignoreMissingJournalfiles | false | 忽略丢失的消息文件，false，当丢失了消息文件，启动异常 |
| checkForCorruptJournalFiles | false | 检查消息文件是否损坏，true，检查发现损坏会尝试修复 |
| checksumJournalFiles | false | 产生一个checksum，以便能够检测journal文件是否损坏。 |
| 5.4版本之后有效的属性: |  |  |
| archiveDataLogs | false | 当为true时，归档的消息文件被移到directoryArchive,而不是直接删除 |
| directoryArchive | null | 存储被归档的消息文件目录 |
| databaseLockedWaitDelay | 10000 | 在使用负载时，等待获得文件锁的延迟时间，单位ms |
| maxAsyncJobs | 10000 | 同个生产者产生等待写入的异步消息最大量 |
| concurrentStoreAndDispatchTopics | false | 当写入消息的时候，是否转发主题消息 |
| concurrentStoreAndDispatchQueues | true | 当写入消息的时候，是否转发队列消息 |
| 5.6版本之后有效的属性: |  |  |
| archiveCorruptedIndex | false | 是否归档错误的索引 |

每个KahaDB的实例都可以配置单独的适配器，如果没有目标队列提交给filteredKahaDB，那么意味着对所有的队列有效。如果一个队列没有对应的适配器，那么将会抛出一个异常。配置如下：

```
<persistenceAdapter>

  <mKahaDBdirectory="${activemq.base}/data/kahadb">

    <filteredPersistenceAdapters>

      <!-- match all queues -->

      <filteredKahaDBqueue=">">

        <persistenceAdapter>

          <kahaDBjournalMaxFileLength="32mb"/>

        </persistenceAdapter>

      </filteredKahaDB>

      

      <!-- match all destinations -->

      <filteredKahaDB>

        <persistenceAdapter>

          <kahaDBenableJournalDiskSyncs="false"/>

        </persistenceAdapter>

      </filteredKahaDB>

    </filteredPersistenceAdapters>

  </mKahaDB>

</persistenceAdapter>
```



如果filteredKahaDB的perDestination属性设置为true，那么匹配的目标队列将会得到自己对应的KahaDB实例。配置如下：

```
<persistenceAdapter>

  <mKahaDBdirectory="${activemq.base}/data/kahadb">

    <filteredPersistenceAdapters>

      <!-- kahaDB per destinations -->

      <filteredKahaDB perDestination="true">

        <persistenceAdapter>

          <kahaDBjournalMaxFileLength="32mb"/>

        </persistenceAdapter>

      </filteredKahaDB>

    </filteredPersistenceAdapters>

  </mKahaDB>

</persistenceAdapter>
```



3、JDBC

可以将消息存储到数据库中，例如：Mysql、SQL Server、Oracle、DB2。

配置JDBC适配器：

```
<persistenceAdapter>

    <jdbcPersistenceAdapterdataSource="#mysql-ds"
createTablesOnStartup="false"
/>

</persistenceAdapter>

```



dataSource指定持久化数据库的bean，createTablesOnStartup是否在启动的时候创建数据表，默认值是true，这样每次启动都会去创建数据表了，一般是第一次启动的时候设置为true，之后改成false。

```
Mysql持久化bean：

<bean
id="mysql-ds"
class="org.apache.commons.dbcp.BasicDataSource"
destroy-method="close">

    <property
name="driverClassName"
value="com.mysql.jdbc.Driver"/>

    <property
name="url"
value="jdbc:mysql://localhost/activemq?relaxAutoCommit=true"/>

    <property
name="username"
value="activemq"/>

    <property
name="password"
value="activemq"/>

    <property
name="poolPreparedStatements"
value="true"/>

</bean>

SQL Server持久化bean：

<bean
id="mssql-ds"
class="net.sourceforge.jtds.jdbcx.JtdsDataSource"
destroy-method="close">

   <property
name="serverName"
value="SERVERNAME"/>

   <property
name="portNumber"
value="PORTNUMBER"/>

   <property
name="databaseName"
value="DATABASENAME"/>

   <property
name="user"
value="USER"/>

   <property
name="password"
value="PASSWORD"/>

</bean>

Oracle持久化bean：

<bean
id="oracle-ds"
class="org.apache.commons.dbcp.BasicDataSource"
destroy-method="close">

    <property
name="driverClassName"
value="oracle.jdbc.driver.OracleDriver"/>

    <property
name="url"
value="jdbc:oracle:thin:@10.53.132.47:1521:activemq"/>

    <property
name="username"
value="activemq"/>

    <property
name="password"
value="activemq"/>

    <property
name="maxActive"
value="200"/>

    <property
name="poolPreparedStatements"
value="true"/>

</bean>

DB2持久化bean：

<bean
id="db2-ds"
class="org.apache.commons.dbcp.BasicDataSource" 
destroy-method="close">

      <property
name="driverClassName"
value="com.ibm.db2.jcc.DB2Driver"/>

      <property
name="url"
value="jdbc:db2://hndb02.bf.ctc.com:50002/activemq"/>

      <property
name="username"
value="activemq"/>

      <property
name="password"
value="activemq"/>

      <property
name="maxActive"
value="200"/>

      <property
name="poolPreparedStatements"
value="true"/>

  </bean>
```



4、LevelDB

这种文件系统是从ActiveMQ5.8之后引进的，它和KahaDB非常相似，也是基于文件的本地数据库储存形式，但是它提供比KahaDB更快的持久性。与KahaDB不同的是，它不是使用传统的B-树来实现对日志数据的提前写，而是使用基于索引的LevelDB。

默认配置如下：

```

<persistenceAdapter>

      <levelDBdirectory="activemq-data"/>

</persistenceAdapter>

```



属性如下：

| 属性名称 | 默认值 | 描述 |
| :--- | :--- | :--- |
| directory | "LevelDB" | 数据文件的存储目录 |
| readThreads | 10 | 系统允许的并发读线程数量 |
| sync | true | 同步写到磁盘 |
| logSize | 104857600 \(100 MB\) | 日志文件大小的最大值 |
| logWriteBufferSize | 4194304 \(4 MB\) | 日志数据写入文件系统的最大缓存值 |
| verifyChecksums | false | 是否对从文件系统中读取的数据进行校验 |
| paranoidChecks | false | 尽快对系统内部发生的存储错误进行标记 |
| indexFactory | org.fusesource.leveldbjni.JniDBFactory, org.iq80.leveldb.impl.Iq80DBFactory | 在创建LevelDB索引时使用 |
| indexMaxOpenFiles | 1000 | 可供索引使用的打开文件的数量 |
| indexBlockRestartInterval | 16 | Number keys between restart points for delta encoding of keys. |
| indexWriteBufferSize | 6291456 \(6 MB\) | 内存中索引数据的最大值 |
| indexBlockSize | 4096 \(4 K\) | 每个数据块的索引数据大小 |
| indexCacheSize | 268435456 \(256 MB\) | 使用缓存索引块允许的最大内存 |
| indexCompression | snappy | 适用于索引块的压缩类型 |
| logCompression | none | 适用于日志记录的压缩类型 |

  


