Mybatis对数据库的操作，都将委托给执行器Executor来完成，所以，在Mybatis这部电影当中，Executor是绝对的领衔主演。

在Mybatis中，SqlSession对数据库的操作，将委托给执行器Executor来完成，而Executor由五鼠组成，分别是：**简单鼠SimpleExecutor**、**重用鼠ReuseExecutor**、**批量鼠BatchExecutor**、**缓存鼠CachingExecutor**、**无用鼠ClosedExecutor**。

## **1. Executor接口设计与类结构图** {#h2_0}

```
public interface Executor {
  ResultHandler NO_RESULT_HANDLER = null;
  int update(MappedStatement ms, Object parameter) throws SQLException;
  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;
  <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;
  List<BatchResult> flushStatements() throws SQLException;
  void commit(boolean required) throws SQLException;
  void rollback(boolean required) throws SQLException;
  CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);
  boolean isCached(MappedStatement ms, CacheKey key);
  void clearLocalCache();
  void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);
  Transaction getTransaction();
  void close(boolean forceRollback);
  boolean isClosed();
  void setExecutorWrapper(Executor executor);
}
```

这些接口方法相对都比较见名知意，但其中的**flushStatements\(\)**方法，给人很多疑惑，它是我们关注的重点方法。

类结构图：

![](http://static.oschina.net/uploads/space/2016/0427/190909_WGDg_2727738.png)

\(Made In Intellij Idea IDE\)

不是说有五鼠吗？怎么就看见四鼠？其实，还有一个**无用鼠ClosedExecutor是**静态内部类：private static final class ClosedExecutor extends BaseExecutor。

接下来，我们看看这五鼠都有哪些本领，能闹得起东京。

**简单鼠SimpleExecutor**：每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。（可以是Statement或PrepareStatement对象）

**重用鼠ReuseExecutor**：执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，而是放置于Map&lt;String, Statement&gt;内，供下一次使用。（可以是Statement或PrepareStatement对象）

**批量鼠BatchExecutor**：执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch\(\)），等待统一执行（executeBatch\(\)），它缓存了多个Statement对象，每个Statement对象都是addBatch\(\)完毕后，等待逐一执行executeBatch\(\)批处理的；BatchExecutor相当于维护了多个桶，每个桶里都装了很多属于自己的SQL，就像苹果蓝里装了很多苹果，番茄蓝里装了很多番茄，最后，再统一倒进仓库。（可以是Statement或PrepareStatement对象）

**缓存鼠CachingExecutor**：装饰设计模式典范，先从缓存中获取查询结果，存在就返回，不存在，再委托给Executor delegate去数据库取，delegate可以是上面任一的SimpleExecutor、ReuseExecutor、BatchExecutor。

**无用鼠ClosedExecutor**：毫无用处，读者可自行查看其源码，仅作为一种标识，和Serializable标记接口作用相当。

**作用范围：以上这五鼠的作用范围，都严格限制在SqlSession生命周期范围内。**

## **2. 基类BaseExecutor源码解析 ** {#h2_1}

org.apache.ibatis.executor.BaseExecutor.java部分源码。

```
 @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    clearLocalCache();
    return doUpdate(ms, parameter);
  }
    protected abstract int doUpdate(MappedStatement ms, Object parameter)
      throws SQLException;

  protected abstract List<BatchResult> doFlushStatements(boolean isRollback)
      throws SQLException;

  protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
      throws SQLException;
```

经典的模板设计模式，不同的实现类，分别实现自己的三个抽象方法即可：doUpdate\(\)、doQuery\(\)、doFlushStatement\(\)。五鼠闹东京的本领，就看各自对这三个方法的江湖修炼情况了。

  


### **2.1. 简单鼠SimpleExecutor源码解析** {#h3_2}

**简单鼠SimpleExecutor**，真的非常简单，几乎没啥可说的。

```
  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.<E>query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }
```

随建随关Statement。

  


### **2.2. 重用鼠ReuseExecutor源码解析** {#h3_3}

```
public class ReuseExecutor extends BaseExecutor {
  // key=sql, value=Statement，不同的sql，对应不同的Statement
  private final Map<String, Statement> statementMap = new HashMap<String, Statement>();
  
    @Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.update(stmt);
  }
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    BoundSql boundSql = handler.getBoundSql();
    String sql = boundSql.getSql();
    // sql是key，不同的sql，将产生不同的Statement
    if (hasStatementFor(sql)) {
    // 从statementMap中获取Statement
      stmt = getStatement(sql);
    } else {
      Connection connection = getConnection(statementLog);
      stmt = handler.prepare(connection);
      // 将Statement放到statementMap中
      putStatement(sql, stmt);
    }
    handler.parameterize(stmt);
    return stmt;
  }
  // ...
    @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    for (Statement stmt : statementMap.values()) {
      closeStatement(stmt);
    }
    statementMap.clear();
    return Collections.emptyList();
  }
```

**重用鼠ReuseExecutor**就是依赖Map&lt;String, Statement&gt;来完成对Statement的重用的（用完不关）。

总不能一直不关吧？到底什么时候关闭这些Statement对象的？问的非常好。

方法**flushStatements\(\)**就是用来处理这些Statement对象的。

**在执行commit、rollback等动作前，将会执行flushStatements\(\)方法，将Statement对象逐一关闭。读者可参看BaseExecutor源码。**

**  
**

### **2.3. 批量鼠BatchExecutor原理及源码解析（重点难点）** {#h3_4}

**批量鼠BatchExecutor**是我们重点分析的对象，也是我们重点学习的对象，因为它略微难一点点，不过，我会使出我的九阳神功，将它描述的简单易懂。

```
// 缓存多个Statement对象，每个Statement都是addBatch()后，等待执行
private final List<Statement> statementList = new ArrayList<Statement>();
// 对应的结果集（主要保存了update结果的count数量）
private final List<BatchResult> batchResultList = new ArrayList<BatchResult>();
// 当前保存的sql，即上次执行的sql
private String currentSql;

  @Override
  public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final Configuration configuration = ms.getConfiguration();
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final BoundSql boundSql = handler.getBoundSql();
    // 本次执行的sql
    final String sql = boundSql.getSql();
    final Statement stmt;
    // 要求当前的sql和上一次的currentSql相同，同时MappedStatement也必须相同
    if (sql.equals(currentSql) && ms.equals(currentStatement)) {
     // 已经存在Statement，取出最后一个Statement，有序
      int last = statementList.size() - 1;
      stmt = statementList.get(last);
     handler.parameterize(stmt);//fix Issues 322
      BatchResult batchResult = batchResultList.get(last);
      batchResult.addParameterObject(parameterObject);
    } else {
    // 尚不存在，新建Statement
      Connection connection = getConnection(ms.getStatementLog());
      stmt = handler.prepare(connection);
      handler.parameterize(stmt);    //fix Issues 322
      currentSql = sql;
      currentStatement = ms;
      // 放到Statement缓存
      statementList.add(stmt);
      batchResultList.add(new BatchResult(ms, sql, parameterObject));
    }
  // 将sql以addBatch()的方式，添加到Statement中（该步骤由StatementHandler内部完成）
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
  }
```

需要注意的是sql.equals\(currentSql\)和statementList.get\(last\)，充分说明了其有序逻辑：AABB，将生成2个Statement对象；AABBAA，将生成3个Statement对象，而不是2个。因为，只要sql有变化，将导致生成新的Statement对象。

**缓存了这么多Statement批处理对象，何时执行它们？在doFlushStatements\(\)方法中完成执行stmt.executeBatch\(\)，随即关闭这些Statement对象。读者可自行查看。**  


这里所说的Statement，可以是Statement或Preparestatement。

**注：对于批处理来说，JDBC只支持update操作（update、insert、delete等），不支持select查询操作。**

BatchExecutor和JDBC批处理的区别。  


JDBC中Statement的批处理原理图。

![](http://static.oschina.net/uploads/space/2016/0427/211258_IEzi_2727738.png)

\(Made In Windows 画图板\)

对于Statement来说，只要SQL不同，就会产生新编译动作，Statement不支持问号“?”参数占位符。

  


JDBC中PrepareStatement的批处理原理图。

![](http://static.oschina.net/uploads/space/2016/0427/211508_KS1e_2727738.png)

\(Made In Windows 画图板\)

对于PrepareStatement，只要SQL相同，就只会编译一次，如果SQL不同呢？此时和Statement一样，会编译多次。PrepareStatement的优势在于支持问号“?”参数占位符，SQL相同，参数不同时，可以减少编译次数至一次，大大提高效率；另外可以防止SQL注入漏洞。



BatchExecutor的批处理原理图。

![](http://static.oschina.net/uploads/space/2016/0427/211635_3apZ_2727738.png)

\(Made In Windows 画图板\)

BatchExecutor的批处理，和JDBC的批处理，主要区别就是BatchExecutor维护了一组Statement批处理对象，它有自动路由功能，SQL1、SQL2、SQL3代表不同的SQL。（Statement或Preparestatement）



### **2.4. 缓存鼠CachingExecutor原理及源码分析** {#h3_5}

**缓存鼠CachingExecutor**使用了经典的装饰器设计模式，先从缓存中取查询结果，有则返回，如果没有，再委托给Executor delegate从数据库中查询。

```
private Executor delegate;
```

delegate可以是上面任一的SimpleExecutor、ReuseExecutor、BatchExecutor。

比较简单，没什么可说的，有关Mybatis的缓存原理，将单独开启一篇缓存原理的文章。

  


### **2.5. 无用鼠ClosedExecutor** {#h3_6}

**无用鼠ClosedExecutor**，无内容可介绍，它仅作为一种标识，和Serializable标记接口作用相当。

  


## **3. Executor的创建时机和创建策略** {#h2_7}

Executor的创建时机是，创建DefaultSqlSession实例时，作为构造参数传递进去。（见DefaultSqlSessionFactory.java内）  


```
Executor executor = configuration.newExecutor(tx, execType);
return new DefaultSqlSession(configuration, executor, autoCommit);
```

Executor有三种创建策略。  


```
public enum ExecutorType {
  SIMPLE, REUSE, BATCH
}
```

那么，Mybatis怎么知道，应该创建这三种策略中的哪一种呢？依据是什么？  


有两种手段来指定Executor创建策略。

第一种，configuration配置文件中，配置默认ExecutorType类型。（当不配置时，默认为ExecutorType.SIMPLE）

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <setting name="defaultExecutorType" value="REUSE" />
    </settings>
</configuration>
```

第二种，手动给DefaultSqlSessionFactory.java的创建SqlSession的方法传递ExecutorType参数。

```
@Override
  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return openSessionFromDataSource(execType, null, autoCommit);
  }
```

至此，关于Executor从哪儿来，要到哪里去，能干什么，都已经打听清楚了。



[參考文章](https://my.oschina.net/zudajun/blog/667214)

