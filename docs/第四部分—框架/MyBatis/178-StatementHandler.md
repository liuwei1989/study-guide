## **1. 数据库操作invoke时序图**  {#h2_0}

![](http://static.oschina.net/uploads/space/2016/0430/100649_wVZ2_2727738.png)

（Made In Visual Paradigm）

本文重点分析StatementHandler和ParameterHandler是如何与Executor共襄盛举的。（**上图中的execute\(\)失误画错了，应该是executeQuery\(\)**）

## **2. Executor内使用StatementHandler模板** {#h2_1}

```
Statement stmt;
StatementHandler handler;
// 判断缓存内是否存在stmt
if (...) {
   // 不存在，就创建一个Statement（可能是Statement、PrepareStatement、CallableStatement）
    stmt = handler.prepare(connection);
}
handler.parameterize(stmt);
```

无论是何种Executor实现类，都使用上面的模板方法调用，所以，StatementHandler隐藏了创建Statement对象和parameterize初始化参数的秘密。  


## 3. StatementHandler接口设计与类结构图 {#h2_2}

```
public interface StatementHandler {
  Statement prepare(Connection connection)
      throws SQLException;
  void parameterize(Statement statement)
      throws SQLException;
  void batch(Statement statement)
      throws SQLException;
  int update(Statement statement)
      throws SQLException;
  <E> List<E> query(Statement statement, ResultHandler resultHandler)
      throws SQLException;
  BoundSql getBoundSql();
  ParameterHandler getParameterHandler();
}
```

类结构图。  


![](http://static.oschina.net/uploads/space/2016/0430/102312_5ZnF_2727738.png)

（Made In Intellij Idea IDE）

SimpleStatementHandler：用于处理**Statement**对象的数据库操作。

PreparedStatementHandler：用于处理**PreparedStatement**对象的数据库操作。

CallableStatementHandler：用于处理**CallableStatement**对象的数据库操作。（存储过程）

  


RoutingStatementHandler：用于创建上面三种Handler的策略类。（**简直是装饰设计模式的极大错误示范，别以为长的帅，我就不抨击它，根本没有存在的理由**）



## 4. 基类BaseStatementHandler源码解析 {#h2_3}

```
protected abstract Statement instantiateStatement(Connection connection) throws SQLException;
```

对于BaseStatementHandler，完成了Sql相关信息的保存工作，也就是把通用食材准备好了。我们重点关注上面的抽象方法即可，也就是创建什么样的Statement对象，具体由子类去实现，子类相当于厨师，面对相同的食材，厨师对其烹饪的手法略有不同。  


### 4.1. SimpleStatementHandler源码解析 {#h3_4}

```
 @Override
  public void batch(Statement statement) throws SQLException {
    String sql = boundSql.getSql();
    statement.addBatch(sql);
  }
  
  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    String sql = boundSql.getSql();
    statement.execute(sql);
    return resultSetHandler.<E>handleResultSets(statement);
  }
  
  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    if (mappedStatement.getResultSetType() != null) {
      return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.createStatement();
    }
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    // N/A
  }
```

创建了一个**Statement**对象，由于Statement对象不支持“?”参数，所以，**parameterize\(\)**是空实现。  
  


### 4.2. PreparedStatementHandler源码解析 {#h3_5}

```
@Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
      String[] keyColumnNames = mappedStatement.getKeyColumns();
      if (keyColumnNames == null) {
        return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      } else {
        return connection.prepareStatement(sql, keyColumnNames);
      }
    } else if (mappedStatement.getResultSetType() != null) {
      return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.prepareStatement(sql);
    }
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    parameterHandler.setParameters((PreparedStatement) statement);
  }
```

创建了一个**PrepareStatement**对象，**parameterize\(\)**则委托给**ParameterHandler**去设置。  




### 4.3. CallableStatementHandler源码解析 {#h3_6}

```
  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (mappedStatement.getResultSetType() != null) {
      return connection.prepareCall(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.prepareCall(sql);
    }
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    registerOutputParameters((CallableStatement) statement);
    parameterHandler.setParameters((CallableStatement) statement);
  }
```

创建了一个**CallableStatement**对象，**parameterize\(\)**则委托给**ParameterHandler**去设置。  


## 5. DefaultParameterHandler源码解析 {#h2_7}

对于ParameterHandler，它只有一个唯一的实现类：DefaultParameterHandler.setParameter\(\)。  


```
@Override
  public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
            // ...
          try {
            typeHandler.setParameter(ps, i + 1, value, jdbcType);
          } catch (TypeException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          } catch (SQLException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }
  }
```

请看方法参数，参数要求是一个PreparedStatement对象，当然可以处理PreparedStatement。然而，为何它还可以处理CallableStatement对象呢？  


原因在这里，请看JDK中有关java.sql.CallableStatement的接口描述。

```
IN parameter values are set using the set methods inherited from  PreparedStatement
```

对于存储过程的IN参数来说，CallableStatement继承自PreparedStatement，传递进来的CallableStatement对象，其实也是PreparedStatement对象。  


## 6. StatementHandler的创建时机和创建策略控制 {#h2_9}

```
@Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.update(stmt);
  }
```

Executor每执行一个query或update动作，都会创建一个StatementHandler对象。  


StatementHandler创建策略有三种。（默认为PREPARED）

```
public enum StatementType {
  STATEMENT, PREPARED, CALLABLE
}
```

创建策略到底如何控制？可以在**Mapper.xml**内配置**statementType**属性。  


```
<select id="findAllStudents" resultMap="StudentResult" statementType="STATEMENT">
	SELECT * FROM STUDENTS
</select>
```

获取StatementType的源码如下。  


org.apache.ibatis.builder.xml.XMLStatementBuilder.parseStatementNode\(\)。

```
StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
```

默认值StatementType.PREPARED。  
  


至此，一个Sql命令，经过马拉松式的长跑（SqlSession--&gt;Executor--&gt;StatementHandler--&gt;Statement--&gt;DB）,终于如愿以偿的到达了终点。  


