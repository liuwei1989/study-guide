1.说到数据库事务，人们脑海里自然不自然的就会浮现出事务的四大特性、四大隔离级别、七大传播特性。四大还好说，问题是七大传播特性是哪儿来的？是Spring在当前线程内，处理多个数据库操作方法事务时所做的一种事务应用策略。事务本身并不存在什么传播特性，不要混淆事务本身和Spring的事务应用策略。（当然，找工作面试时，还是可以巧妙的描述传播特性的）  


2.一说到事务，人们可能又会想起create、begin、commit、rollback、close、suspend。可实际上，只有commit、rollback是实际存在的，剩下的create、begin、close、suspend都是虚幻的，是业务层或数据库底层应用语意，而非JDBC事务的真实命令。

create（事务创建）：不存在。

begin（事务开始）：姑且认为存在于DB的命令行中，比如Mysql的start transaction命令，以及其他数据库中的begin transaction命令。JDBC中不存在。

close（事务关闭）：不存在。应用程序接口中的close\(\)方法，是为了把connection放回数据库连接池中，供下一次使用，与事务毫无关系。

suspend（事务挂起）：不存在。Spring中事务挂起的含义是，需要新事务时，将现有的connection1保存起来（它还有尚未提交的事务），然后创建connection2，connection2提交、回滚、关闭完毕后，再把connection1取出来，完成提交、回滚、关闭等动作，保存connection1的动作称之为事务挂起。在JDBC中，是根本不存在事务挂起的说法的，也不存在这样的接口方法。

  


因此，记住事务的三个真实存在的方法，不要被各种事务状态名词所迷惑，它们分别是：**conn.setAutoCommit\(\)**、**conn.commit\(\)**、**conn.rollback\(\)**。

conn.close\(\)含义为关闭一个数据库连接，这已经不再是事务方法了。



---

  


## **1. Mybaits中的事务接口Transaction** {#h2_0}

```
public interface Transaction {
    Connection getConnection() throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;
    void close() throws SQLException;
}
```

有了文章开头的分析，当你再次看到close\(\)方法时，千万别再认为是关闭一个事务了，而是关闭一个conn连接，或者是把conn连接放回连接池内。  


事务类层次结构图：  


![](http://static.oschina.net/uploads/space/2016/0426/210148_pTo6_2727738.png)

                        \(Made In Intellij Idea IDE\)

JdbcTransaction：单独使用Mybatis时，默认的事务管理实现类，就和它的名字一样，它就是我们常说的JDBC事务的极简封装，和编程使用mysql-connector-java-5.1.38-bin.jar事务驱动没啥差别。其极简封装，仅是让connection支持连接池而已。

ManagedTransaction：含义为托管事务，空壳事务管理器，皮包公司。仅是提醒用户，在其它环境中应用时，把事务托管给其它框架，比如托管给Spring，让Spring去管理事务。

org.apache.ibatis.transaction.jdbc.JdbcTransaction.java部分源码。

```
@Override
  public void close() throws SQLException {
    if (connection != null) {
      resetAutoCommit();
      if (log.isDebugEnabled()) {
        log.debug("Closing JDBC Connection [" + connection + "]");
      }
      connection.close();
    }
  }
```

面对上面这段代码，我们不禁好奇，connection.close\(\)之前，居然调用了一个resetAutoCommit\(\)，含义为重置autoCommit属性值。connection.close\(\)含义为销毁conn，既然要销毁conn，为何还多此一举的调用一个resetAutoCommit\(\)呢？消失之前多喝口水，真的没有必要。

其实，原因是这样的，connection.close\(\)不意味着真的要销毁conn，而是要把conn放回连接池，供下一次使用，既然还要使用，自然就需要重置AutoCommit属性了。通过生成connection代理类，来实现重回连接池的功能。如果connection是普通的Connection实例，那么代码也是没有问题的，双重支持。

## **2. 事务工厂TransactionFactory** {#h2_1}

![](http://static.oschina.net/uploads/space/2016/0426/213147_2njQ_2727738.png)

顾名思义，一个生产JdbcTransaction实例，一个生产ManagedTransaction实例。两个毫无实际意义的工厂类，除了new之外，没有其他代码。

```
<transactionManager type="JDBC" />
```

mybatis-config.xml配置文件内，可配置事务管理类型。  


## 3. Transaction的用法 {#h2_2}

无论是SqlSession，还是Executor，它们的事务方法，最终都指向了Transaction的事务方法，即都是由Transaction来完成事务提交、回滚的。

配一个简单的时序图。

![](http://static.oschina.net/uploads/space/2016/0426/215117_P9mi_2727738.png)

                    （Made In Visual Paradigm）

代码样例：

```
public static void main(String[] args) {
		SqlSession sqlSession = MybatisSqlSessionFactory.openSession();
		try {
			StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
			
			Student student = new Student();
			student.setName("yy");
			student.setEmail("email@email.com");
			student.setDob(new Date());
			student.setPhone(new PhoneNumber("123-2568-8947"));
			
			studentMapper.insertStudent(student);
			sqlSession.commit();
		} catch (Exception e) {
			sqlSession.rollback();
		} finally {
			sqlSession.close();
		}
	}
```

注：Executor在执行insertStudent\(student\)方法时，与事务的提交、回滚、关闭毫无瓜葛（方法内部不会提交、回滚事务），需要像上面的代码一样，手动显示调用commit\(\)、rollback\(\)、close\(\)等方法。

因此，后续在分析到类似insert\(\)、update\(\)等方法内部时，需要忘记事务的存在，不要试图在insert\(\)等方法内部寻找有关事务的任何方法。

## 4. 你可能关心的有关事务的几种特殊场景表现（重要） {#h2_3}

### 1. 一个conn生命周期内，可以存在无数多个事务。  {#h3_4}

```
// 执行了connection.setAutoCommit(false)，并返回
            SqlSession sqlSession = MybatisSqlSessionFactory.openSession();
		try {
			StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
			
			Student student = new Student();
			student.setName("yy");
			student.setEmail("email@email.com");
			student.setDob(new Date());
			student.setPhone(new PhoneNumber("123-2568-8947"));
			
			studentMapper.insertStudent(student);
			// 提交
			sqlSession.commit();
			
			studentMapper.insertStudent(student);
			// 多次提交
			sqlSession.commit();
		} catch (Exception e) {
		        // 回滚，只能回滚当前未提交的事务
			sqlSession.rollback();
		} finally {
			sqlSession.close();
		}
```

对于JDBC来说，autoCommit=false时，是自动开启事务的，执行commit\(\)后，该事务结束。以上代码正常情况下，开启了2个事务，向数据库插入了2条数据。JDBC中不存在Hibernate中的session的概念，在JDBC中，insert了几次，数据库就会有几条记录，切勿混淆。而rollback\(\)，只能回滚当前未提交的事务。  


### 2. autoCommit=false，没有执行commit\(\)，仅执行close\(\)，会发生什么？ {#h3_5}

```
try {
    studentMapper.insertStudent(student);
} finally {
    sqlSession.close();
}
```

就像上面这样的代码，没有commit\(\)，固执的程序员总是好奇这样的特例。

insert后，close之前，如果数据库的事务隔离级别是read uncommitted，那么，我们可以在数据库中查询到该条记录。

接着执行sqlSession.close\(\)时，经过SqlSession的判断，决定执行rollback\(\)操作，于是，事务回滚，数据库记录消失。

下面，我们看看org.apache.ibatis.session.defaults.DefaultSqlSession.java中的close\(\)方法源码。

```
 @Override
  public void close() {
    try {
      executor.close(isCommitOrRollbackRequired(false));
      dirty = false;
    } finally {
      ErrorContext.instance().reset();
    }
  }
```

事务是否回滚，依靠isCommitOrRollbackRequired\(false\)方法来判断。  


```
 private boolean isCommitOrRollbackRequired(boolean force) {
    return (!autoCommit && dirty) || force;
  }
```

在上面的条件判断中，!autoCommit=true（取反当然是true了），force=false，最终是否回滚事务，只有dirty参数了，dirty含义为是否是脏数据。  


```
@Override
  public int insert(String statement, Object parameter) {
    return update(statement, parameter);
  }

  @Override
  public int update(String statement, Object parameter) {
    try {
      dirty = true;
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.update(ms, wrapCollection(parameter));
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```

源码很明确，只要执行update操作，就设置dirty=true。insert、delete最终也是执行update操作。  


只有在执行完commit\(\)、rollback\(\)、close\(\)等方法后，才会再次设置dirty=false。

```
  @Override
  public void commit(boolean force) {
    try {
      executor.commit(isCommitOrRollbackRequired(force));
      dirty = false;
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```

**因此，得出结论：autoCommit=false，但是没有手动commit，在sqlSession.close\(\)时，Mybatis会将事务进行rollback\(\)操作，然后才执行conn.close\(\)关闭连接，当然数据最终也就没能持久化到数据库中了。**  


### 3. autoCommit=false，没有commit，也没有close，会发生什么？** ** {#h3_6}

```
studentMapper.insertStudent(student);
```

干脆，就这一句话，即不commit，也不close。

**结论：insert后，jvm结束前，如果事务隔离级别是read uncommitted，我们可以查到该条记录。jvm结束后，事务被rollback\(\)，记录消失。通过断点debug方式，你可以看到效果。**  


**这说明JDBC驱动实现，已经考虑到这样的特例情况，底层已经有相应的处理机制了。这也超出了我们的探究范围。**



**警告：请按正确的try-catch-finally编程方式处理事务，若不从，本人概不负责后果。**



**注：无参的openSession\(\)方法，会自动设置autoCommit=false。**



总结：Mybatis的JdbcTransaction，和纯粹的Jdbc事务，几乎没有差别，它仅是扩展支持了连接池的connection。另外，需要明确，无论你是否手动处理了事务，只要是对数据库进行任何update操作（update、delete、insert），都一定是在事务中进行的，这是数据库的设计规范之一。读完本篇文章，是否颠覆了你心中目前对事务的理解呢？欢迎点评。

