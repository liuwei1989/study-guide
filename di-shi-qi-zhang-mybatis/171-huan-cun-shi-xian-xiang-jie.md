### 0.写在前面 

MyBatis

是一个简单，小巧但功能非常强大的ORM开源框架，它的功能强大也体现在它的缓存机制上。

MyBatis

提供了一级缓存、二级缓存 这两个缓存机制，能够很好地处理和维护缓存，以提高系统的性能。本文的目的则是向读者详细介绍

MyBatis

的一级缓存，深入源码，解析

MyBatis

一级缓存的实现原理，并且针对一级缓存的特点提出了在实际使用过程中应该注意的事项。



> 读完本文，你将会学到：
> **1、什么是一级缓存？为什么使用一级缓存？**
>
> **2、MyBatis的一级缓存是怎样组织的？（即SqlSession对象中的缓存是怎样组织的？）**
>
> **3、一级缓存的生命周期有多长？  
> **
>
> **4、Cache接口的设计以及CacheKey的定义**
>
> **5、一级缓存的性能分析以及应该注意的事项  
> **

  


### 1. 什么是一级缓存？ 为什么使用一级缓存？ 

> 每当我们使用**MyBatis**开启一次和[数据库](http://lib.csdn.net/base/mysql)的会话，**MyBatis**会创建出**一个SqlSession对象表示一次数据库会话**。
>
>       在对数据库的一次会话中，我们有可能会反复地执行完全相同的查询语句，如果不采取一些措施的话，每一次查询都会查询一次数据库,而我们在极短的时间内做了完全相同的查询，那么它们的结果极有可能完全相同，由于查询一次数据库的代价很大，这有可能造成很大的资源浪费。
>
>       为了解决这一问题，减少资源的浪费，**MyBatis会在表示会话的SqlSession**对象中建立一个简单的缓存，将每次查询到的结果结果缓存起来，当下次查询的时候，如果判断先前有个完全一样的查询，会直接从缓存中直接将结果取出，返回给用户，不需要再进行一次数据库查询了。
>
> **     如下图所示，MyBatis会在一次会话的表示----一个SqlSession对象中创建一个本地缓存\(local cache\)，对于每一次查询，都会尝试根据查询的条件去本地缓存中查找是否在缓存中，如果在缓存中，就直接从缓存中取出，然后返回给用户；否则，从数据库读取数据，将查询结果存入缓存并返回给用户。**
>
> ![](http://img.blog.csdn.net/20141121213425390)**  
> **
>
> **对于会话（Session）级别的数据缓存，我们称之为一级数据缓存，简称一级缓存。**

### 2. MyBatis中的一级缓存是怎样组织的？（即SqlSession中的缓存是怎样组织的？）

> 由于**MyBatis**使用**SqlSession**对象表示一次数据库的会话，那么，对于会话级别的一级缓存也应该是在SqlSession中控制的。
>
>       实际上,**MyBatis**只是一个**MyBatis**对外的接口，**SqlSession**将它的工作交给了**Executor**执行器这个角色来完成，负责完成对数据库的各种操作。当创建了一个**SqlSession**对象时，**MyBatis**会为这个**SqlSession**对象创建一个新的**Executor**执行器，而缓存信息就被维护在这个**Executor**执行器中，**MyBatis**将缓存和对缓存相关的操作封装成了Cache接口中。**SqlSession**、**Executor**、**Cache**之间的关系如下列类图所示：
>
> ![](http://img.blog.csdn.net/20141120100824184)  
>
>
>       如上述的类图所示，**Executor**接口的实现类**BaseExecutor**中拥有一个**Cache**接口的实现类**PerpetualCache**，则对于**BaseExecutor**对象而言，它将使用**PerpetualCache**对象维护缓存。  
>
>
> 综上，**SqlSession**对象、**Executor**对象、**Cache**对象之间的关系如下图所示：  
>
>
> ![](http://img.blog.csdn.net/20141119164906640)
>
> 由于**Session**级别的一级缓存实际上就是使用**PerpetualCache**维护的，那么**PerpetualCache**是怎样实现的呢？
>
> **PerpetualCache**实现原理其实很简单，其内部就是通过一个简单的**HashMap&lt;k,v&gt;**来实现的，没有其他的任何限制。如下是**PerpetualCache**的实现代码：

```
package org.apache.ibatis.cache.impl;  
  
import java.util.HashMap;  
import java.util.Map;  
import java.util.concurrent.locks.ReadWriteLock;  
  
import org.apache.ibatis.cache.Cache;  
import org.apache.ibatis.cache.CacheException;  
  
/** 
 * 使用简单的HashMap来维护缓存 
 * @author Clinton Begin 
 */  
public class PerpetualCache implements Cache {  
  
  private String id;  
  
  private Map<Object, Object> cache = new HashMap<Object, Object>();  
  
  public PerpetualCache(String id) {  
    this.id = id;  
  }  
  
  public String getId() {  
    return id;  
  }  
  
  public int getSize() {  
    return cache.size();  
  }  
  
  public void putObject(Object key, Object value) {  
    cache.put(key, value);  
  }  
  
  public Object getObject(Object key) {  
    return cache.get(key);  
  }  
  
  public Object removeObject(Object key) {  
    return cache.remove(key);  
  }  
  
  public void clear() {  
    cache.clear();  
  }  
  
  public ReadWriteLock getReadWriteLock() {  
    return null;  
  }  
  
  public boolean equals(Object o) {  
    if (getId() == null) throw new CacheException("Cache instances require an ID.");  
    if (this == o) return true;  
    if (!(o instanceof Cache)) return false;  
  
    Cache otherCache = (Cache) o;  
    return getId().equals(otherCache.getId());  
  }  
  
  public int hashCode() {  
    if (getId() == null) throw new CacheException("Cache instances require an ID.");  
    return getId().hashCode();  
  }  
  
}  
```

### 3.一级缓存的生命周期有多长？ 

> **a.MyBatis**在开启一个数据库会话时，会 创建一个新的**SqlSession**对象，**SqlSession**对象中会有一个新的**Executor**对象，**Executor**对象中持有一个新的**PerpetualCache**对象；当会话结束时，**SqlSession**对象及其内部的**Executor**对象还有**PerpetualCache**对象也一并释放掉。
>
> **b.**如果**SqlSession**调用了**close\(\)**方法，会释放掉一级缓存**PerpetualCache**对象，一级缓存将不可用；
>
> **c.**如果**SqlSession**调用了**clearCache\(\)**，会清空**PerpetualCache**对象中的数据，但是该对象仍可使用；
>
> **d.SqlSession**中执行了任何一个**update**操作\(**update\(\)、delete\(\)、insert\(\)**\) ，都会清空**PerpetualCache**对象的数据，但是该对象可以继续使用；  
>
>
> ![](http://img.blog.csdn.net/20141120104257906)

### **4. SqlSession 一级缓存的工作流程：**

> 1.对于某个查询，根据**statementId,params,rowBounds**来构建一个**key**值，根据这个**key**值去缓存**Cache**中取出对应的**key**值存储的缓存结果；
>
> 2. 判断从**Cache**中根据特定的**key**值取的数据数据是否为空，即是否命中；
>
> 3. 如果命中，则直接将缓存结果返回；
>
> 4. 如果没命中：
>
>         4.1  去数据库中查询数据，得到查询结果；
>
>         4.2  将key和查询到的结果分别作为**key**,**value**对存储到**Cache**中；
>
>         4.3. 将查询结果返回；
>
> 5. 结束。
> _**\[关于上述工作过程中 key值的构建，我们将在第下一节中重点探讨，这也是MyBatis缓存机制中非常重要的一个概念。\]**_

![](http://img.blog.csdn.net/20141120133247125)  


### 5. Cache接口的设计以及CacheKey的定义（非常重要） 

> 如下图所示，**MyBatis**定义了一个**org.apache.ibatis.cache.Cache**接口作为其**Cache**提供者的**SPI\(Service Provider Interface\)**，所有的**MyBatis**内部的**Cache**缓存，都应该实现这一接口。**MyBatis**定义了一个**PerpetualCache**实现类实现了**Cache**接口，**实际上，在SqlSession对象里的Executor对象内维护的Cache**类型实例对象，就是**PerpetualCache**子类创建的。
>
>     （**MyBatis**内部还有很多**Cache**接口的实现，一级缓存只会涉及到这一个**PerpetualCache**子类，**Cache**的其他实现将会放到二级缓存中介绍）。
>
> ![](http://img.blog.csdn.net/20141120134402285)  
>
>
> 我们知道，**Cache最核心的实现其实就是一个Map，将本次查询使用的特征值作为key，将查询结果作为value存储到Map**中。
>
> 现在最核心的问题出现了：**怎样来确定一次查询的特征值？**  
>
>
> 换句话说就是：**怎样判断某两次查询是完全相同的查询？**
>
> 也可以这样说：**如何确定Cache**中的key值？
>
> **MyBatis**认为，对于两次查询，如果以下条件都完全一样，那么就认为它们是完全相同的两次查询：  
>
>
> > _**1. 传入的statementId  
> > **_
> >
> > _**2. 查询时要求的结果集中的结果范围 （结果的范围通过rowBounds.offset和rowBounds.limit表示）；**_
> >
> > _**3. 这次查询所产生的最终要传递给JDBC**_[_**Java**_](http://lib.csdn.net/base/java)_**.sql.Preparedstatement的Sql语句字符串（boundSql.getSql\(\)）**_
> >
> > _**4. 传递给java.sql.Statement要设置的参数值**_
>
> 现在分别解释上述四个条件：
>
> > 1. 传入的_**statementId**_，对于**MyBatis**而言，你要使用它，必须需要一个_**statementId**_，它代表着你将执行什么样的**Sql**；
> >
> > 2.**MyBatis**自身提供的分页功能是通过**RowBounds**来实现的，它通过_**rowBounds.offset**_和_**rowBounds.limit**_来过滤查询出来的结果集，这种分页功能是基于查询结果的再过滤，而不是进行数据库的物理分页；
> >
> > 由于**MyBatis**底层还是依赖于**JDBC**实现的，那么，对于两次完全一模一样的查询，**MyBatis**要保证对于底层**JDBC**而言，也是完全一致的查询才行。而对于**JDBC**而言，两次查询，只要传入给**JDBC**的**SQL**语句完全一致，传入的参数也完全一致，就认为是两次查询是完全一致的。
> >
> > 上述的第3个条件正是要求保证传递给**JDBC**的**SQL**语句完全一致；第4条则是保证传递给**JDBC**的参数也完全一致；  
> >
> >
> > 3、4讲的有可能比较含糊，举一个例子：

```
<select id="selectByCritiera" parameterType="java.util.Map" resultMap="BaseResultMap">  
      select employee_id,first_name,last_name,email,salary  
      from louis.employees  
      where  employee_id = #{employeeId}  
      and first_name= #{firstName}  
      and last_name = #{lastName}  
      and email = #{email}  
</select>  
```

如果使用上述的"selectByCritiera"进行查询，那么，MyBatis会将上述的SQL中的\#{} 都替换成 ? 如下：

```
select employee_id,first_name,last_name,email,salary  
from louis.employees  
where  employee_id = ?  
and first_name= ?  
and last_name = ?  
and email = ?  
```

MyBatis最终会使用上述的SQL字符串创建JDBC的java.sql.PreparedStatement对象，对于这个PreparedStatement对象，还需要对它设置参数，调用setXXX\(\)来完成设值，第4条的条件，就是要求对设置JDBC的PreparedStatement的参数值也要完全一致。

即3、4两条MyBatis最本质的要求就是：调用JDBC的时候，传入的SQL语句要完全相同，传递给JDBC的参数值也要完全相同。



综上所述,CacheKey由以下条件决定：

**statementId  + rowBounds  + 传递给JDBC的SQL  + 传递给JDBC的参数值**  




**CacheKey的创建**

对于每次的查询请求，**Executor**都会根据传递的参数信息以及动态生成的**SQL**语句，将上面的条件根据一定的计算规则，创建一个对应的**CacheKey**对象。

我们知道创建**CacheKey**的目的，就两个：

    1. 根据**CacheKey**作为**key**,去**Cache缓存**中查找缓存结果；

    2. 如果查找缓存命中失败，则通过此**CacheKey**作为**key**，将**从数据库查询到的结果**作为**value**，组成**key**,**value**对存储到**Cache**缓存中。

**CacheKey**的构建被放置到了**Executor**接口的实现类**BaseExecutor**中，定义如下：

```
/** 
 * 所属类:  org.apache.ibatis.executor.BaseExecutor 
 * 功能   :   根据传入信息构建CacheKey 
 */  
public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {  
  if (closed) throw new ExecutorException("Executor was closed.");  
  CacheKey cacheKey = new CacheKey();  
  //1.statementId  
  cacheKey.update(ms.getId());  
  //2. rowBounds.offset  
  cacheKey.update(rowBounds.getOffset());  
  //3. rowBounds.limit  
  cacheKey.update(rowBounds.getLimit());  
  //4. SQL语句  
  cacheKey.update(boundSql.getSql());  
  //5. 将每一个要传递给JDBC的参数值也更新到CacheKey中  
  List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();  
  TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();  
  for (int i = 0; i < parameterMappings.size(); i++) { // mimic DefaultParameterHandler logic  
    ParameterMapping parameterMapping = parameterMappings.get(i);  
    if (parameterMapping.getMode() != ParameterMode.OUT) {  
      Object value;  
      String propertyName = parameterMapping.getProperty();  
      if (boundSql.hasAdditionalParameter(propertyName)) {  
        value = boundSql.getAdditionalParameter(propertyName);  
      } else if (parameterObject == null) {  
        value = null;  
      } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {  
        value = parameterObject;  
      } else {  
        MetaObject metaObject = configuration.newMetaObject(parameterObject);  
        value = metaObject.getValue(propertyName);  
      }  
      //将每一个要传递给JDBC的参数值也更新到CacheKey中  
      cacheKey.update(value);  
    }  
  }  
  return cacheKey;  
}      
```

CacheKey的hashcode生成算法

刚才已经提到，Cache接口的实现，本质上是使用的HashMap&lt;k,v&gt;,而构建CacheKey的目的就是为了作为HashMap&lt;k,v&gt;中的key值。而HashMap是通过key值的hashcode 来组织和存储的，那么，构建CacheKey的过程实际上就是构造其hashCode的过程。下面的代码就是CacheKey的核心hashcode生成[算法](http://lib.csdn.net/base/datastructure)，感兴趣的话可以看一下：

```
 public void update(Object object) {  
   if (object != null && object.getClass().isArray()) {  
     int length = Array.getLength(object);  
     for (int i = 0; i < length; i++) {  
       Object element = Array.get(object, i);  
       doUpdate(element);  
     }  
   } else {  
     doUpdate(object);  
   }  
 }  
  
 private void doUpdate(Object object) {  
  
//1. 得到对象的hashcode;    
   int baseHashCode = object == null ? 1 : object.hashCode();  
   //对象计数递增  
   count++;  
   checksum += baseHashCode;  
   //2. 对象的hashcode 扩大count倍  
   baseHashCode *= count;  
   //3. hashCode * 拓展因子（默认37）+拓展扩大后的对象hashCode值  
   hashcode = multiplier * hashcode + baseHashCode;  
   updateList.add(object);  
 }  
```

一级缓存的性能分析

我将从两个 一级缓存的特性来讨论SqlSession的一级缓存性能问题：

1.MyBatis对会话（Session）级别的一级缓存设计的比较简单，就简单地使用了HashMap来维护，并没有对HashMap的容量和大小进行限制。

读者有可能就觉得不妥了：如果我一直使用某一个SqlSession对象查询数据，这样会不会导致HashMap太大，而导致 java.lang.OutOfMemoryError错误啊？ 读者这么考虑也不无道理，不过MyBatis的确是这样设计的。

MyBatis这样设计也有它自己的理由：

a.  一般而言SqlSession的生存时间很短。一般情况下使用一个SqlSession对象执行的操作不会太多，执行完就会消亡；

b.  对于某一个SqlSession对象而言，只要执行update操作（update、insert、delete），都会将这个SqlSession对象中对应的一级缓存清空掉，所以一般情况下不会出现缓存过大，影响JVM内存空间的问题；

c.  可以手动地释放掉SqlSession对象中的缓存。

2.  一级缓存是一个粗粒度的缓存，没有更新缓存和缓存过期的概念

      MyBatis的一级缓存就是使用了简单的HashMap，MyBatis只负责将查询数据库的结果存储到缓存中去， 不会去判断缓存存放的时间是否过长、是否过期，因此也就没有对缓存的结果进行更新这一说了。



根据一级缓存的特性，在使用的过程中，我认为应该注意：

1、对于数据变化频率很大，并且需要高时效准确性的数据要求，我们使用SqlSession查询的时候，要控制好SqlSession的生存时间，SqlSession的生存时间越长，它其中缓存的数据有可能就越旧，从而造成和真实数据库的误差；同时对于这种情况，用户也可以手动地适时清空SqlSession中的缓存；

2、对于只执行、并且频繁执行大范围的select操作的SqlSession对象，SqlSession对象的生存时间不应过长。









