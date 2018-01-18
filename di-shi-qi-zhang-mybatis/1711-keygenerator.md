在Mybatis中，执行insert操作时，如果我们希望返回数据库生成的自增主键值，那么就需要使用到KeyGenerator对象。

需要注意的是，KeyGenerator的作用，是返回数据库生成的自增主键值，而不是生成数据库的自增主键值。返回的主键值放到哪儿呢？放到parameter object的主键属性上。

下面看看其接口定义。

```
public interface KeyGenerator {
  void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
  void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
}
```

接口定义还是比较简单的，就是在insert前、insert后，策略处理主键值。

![](http://static.oschina.net/uploads/space/2016/0511/161009_hmaI_2727738.jpg)

（Made In IntelliJ IDEA IDE）

Jdbc3KeyGenerator：用于处理数据库支持自增主键的情况，如MySQL的auto\_increment。

NoKeyGenerator：空实现，不需要处理主键。

SelectKeyGenerator：用于处理数据库不支持自增主键的情况，比如Oracle的sequence序列。

上面都比较泛泛而谈，我们来点实际的，看看它们都是如何工作的。

**1. JDBC实现insert后，返回自增主键值的原理**

```
Class.forName("com.mysql.jdbc.Driver");

Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "123");
conn.setAutoCommit(false);
PreparedStatement pstm = conn.prepareStatement("insert into students(name, email) values(?, ?)",
Statement.RETURN_GENERATED_KEYS);

pstm.setString(1, "name1");
pstm.setString(2, "email1");
pstm.addBatch();
pstm.setString(1, "name2");
pstm.setString(2, "email2");
pstm.addBatch();
pstm.executeBatch();
// 返回自增主键值
ResultSet rs = pstm.getGeneratedKeys();
while (rs.next()) {
		Object value = rs.getObject(1);
		System.out.println(value);
	}
conn.commit();
rs.close();
pstm.close();
conn.close();

output：
246
247
```

以上代码，仅作为演示使用。Mybatis是对JDBC的封装，其Jdbc3KeyGenerator类，就是使用上面的原理，来返回数据库生成的主键值的。

**2. Jdbc3KeyGenerator源码解读**

```
public class Jdbc3KeyGenerator implements KeyGenerator {

  @Override
  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    // do nothing
  }

  @Override
  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    processBatch(ms, stmt, getParameters(parameter));
  }

  public void processBatch(MappedStatement ms, Statement stmt, Collection<Object> parameters) {
    ResultSet rs = null;
    try {
      // 获得返回的主键值结果集
      rs = stmt.getGeneratedKeys();
      final Configuration configuration = ms.getConfiguration();
      final TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
      final String[] keyProperties = ms.getKeyProperties();
      final ResultSetMetaData rsmd = rs.getMetaData();
      TypeHandler<?>[] typeHandlers = null;
      if (keyProperties != null && rsmd.getColumnCount() >= keyProperties.length) {
        // 给参数object对象的属性赋主键值（批量插入，可能是多个）
        for (Object parameter : parameters) {
          // there should be one row for each statement (also one for each parameter)
          if (!rs.next()) {
            break;
          }
          final MetaObject metaParam = configuration.newMetaObject(parameter);
          if (typeHandlers == null) {
            typeHandlers = getTypeHandlers(typeHandlerRegistry, metaParam, keyProperties, rsmd);
          }
          // 赋值
          populateKeys(rs, metaParam, keyProperties, typeHandlers);
        }
      }
    } catch (Exception e) {
      throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e, e);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
private void populateKeys(ResultSet rs, MetaObject metaParam, String[] keyProperties, TypeHandler<?>[] typeHandlers) throws SQLException {
    // 主键字段，可能是多个（一般情况下，是一个）
    for (int i = 0; i < keyProperties.length; i++) {
      TypeHandler<?> th = typeHandlers[i];
      if (th != null) {
        Object value = th.getResult(rs, i + 1);
       // 反射赋值
        metaParam.setValue(keyProperties[i], value);
      }
    }
  }
//...
```

Mapper.Xml配置方式。

```
<insert id="insertStudents" useGeneratedKeys="true" keyProperty="studId" parameterType="Student">
```

**3. NoKeyGenerator源码解读**

完全是空实现，没啥可说的。

**4. SelectKeyGenerator的原理**

```
<insert id="insertStudent" parameterType="Student" >
		<selectKey keyProperty="studId" resultType="int" order="BEFORE"> 
			SELECT ELEARNING.STUD_ID_SEQ.NEXTVAL FROM DUAL 
		</selectKey>
		INSERT INTO
		STUDENTS(STUD_ID, NAME, EMAIL, DOB, PHONE)
		VALUES(#{studId}, #{name},
		#{email}, #{dob}, #{phone})
	</insert>
```

在执行insert之前，先发起一个sql查询，将返回的序列值赋值给Student的stuId属性，然后再执行insert操作，这样表中的stud\_id字段就有值了。order="BEFORE"表示insert前执行，比如取sequence序列值；order="AFTER"表示insert之后执行，比如使用触发器给主键stud\_id赋值。比较简单，我就不再贴源码了。

注意：由于selectKey本身返回单个序列主键值，也就无法支持批量insert操作并返回主键id列表了。如果要执行批量insert，请选择使用for循环执行多次插入操作。

**5. KeyGenerator的创建过程**

每一个MappedStatement，都有一个非空的KeyGenerator引用。

org.apache.ibatis.mapping.MappedStatement.Builder.Builder\(\)构造方法赋初始值源码。

```
mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
```

org.apache.ibatis.builder.xml.XMLStatementBuilder.parseStatementNode\(\)覆盖KeyGenerator初始值的源码。

```
String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
if (configuration.hasKeyGenerator(keyStatementId)) {
      // 表示存在selectKey获取主键值方式
      keyGenerator = configuration.getKeyGenerator(keyStatementId);
    } else {
      keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
          configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
          ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
    }
```

org.apache.ibatis.builder.xml.XMLStatementBuilder.parseSelectKeyNode\(\)解析&lt;selectKey&gt;元素，构建SelectKeyGenerator的源码。

```
MappedStatement keyStatement = configuration.getMappedStatement(id, false);
configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
```

因此，只有SelectKeyGenerator会保存至Configuration对象的Map&lt;String, KeyGenerator&gt; keyGenerators属性当中。&lt;selectKey&gt;元素，会被Mybatis解析为一个MappedStatement对象，并作为构造参数传递至SelectKeyGenerator内保存起来。

```
public class SelectKeyGenerator implements KeyGenerator {
  
  public static final String SELECT_KEY_SUFFIX = "!selectKey";
  private boolean executeBefore;
  private MappedStatement keyStatement;
//...
```

Map&lt;String, KeyGenerator&gt; keyGenerators的存储结构如下。

```
{insertStudent!selectKey=org.apache.ibatis.executor.keygen.SelectKeyGenerator@59d016c9, 
com.mybatis3.mappers.StudentMapper.insertStudent!selectKey=org.apache.ibatis.executor.keygen.SelectKeyGenerator@59d016c9}
```

至此，每一个MappedStatement对象，都恰当的绑定了一个KeyGenerator对象，就可以开始工作了。

**6. KeyGenerator的使用过程**

keyGenerator.processBefore\(\)方法调用时机。

org.apache.ibatis.executor.statement.BaseStatementHandler.BaseStatementHandler\(\)构造方法源码。

```
if (boundSql == null) {
      // 调用keyGenerator.processBefore()方法
      generateKeys(parameterObject);
      boundSql = mappedStatement.getBoundSql(parameterObject);
    }
// ...

 protected void generateKeys(Object parameter) {
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processBefore(executor, mappedStatement, null, parameter);
  }
```

即，创建StatementHandler对象时，就会执行keyGenerator.processBefore\(\)方法。keyGenerator.processAfter\(\)方法，自然就是Statement执行后执行了。

org.apache.ibatis.executor.statement.SimpleStatementHandler.update\(Statement\)方法源码。其他的StatementHandler都是类似的。

```
@Override
  public int update(Statement statement) throws SQLException {
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    int rows;
    if (keyGenerator instanceof Jdbc3KeyGenerator) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else if (keyGenerator instanceof SelectKeyGenerator) {
      statement.execute(sql);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else {
      statement.execute(sql);
      rows = statement.getUpdateCount();
    }
    return rows;
  }
```

**7. 批量插入，返回主键id列表**

```
for (Student student : students) {
	studentMapper.insertStudent(student);
}
```

对的，你没看错，就是像上面这样for循环逐一insert操作的，此时，如果你考虑性能的话，可以使用BatchExecutor来完成，当然了，其他的Executor也是可以的。

如果文章就像上面这样写，那么就完全失去了写文章的价值，上面的for循环，谁都懂这么操作可以实现，但是，很多人想要的并不是这个例子，而是另外一种批量插入操作，返回主键id列表。那么，看第8条。

**8. Mybatis批量插入，返回主键id列表为null**

```
<insert id="insertStudents" useGeneratedKeys="true" keyProperty="studId" parameterType="java.util.ArrayList">
		INSERT INTO
		STUDENTS(STUD_ID, NAME, EMAIL, DOB, PHONE)
		VALUES
	<foreach collection="list" item="item" index="index" separator=","> 
        	(#{item.studId},#{item.name},#{item.email},#{item.dob}, #{item.phone}) 
    	</foreach> 
</insert>
```

很多同学，包括开源中国社区，都遇到使用上面的批量insert操作，返回的主键id列表是null的问题，很多人得出结论：Mybatis不支持这种形式的批量插入并返回主键id列表。真是这样吗？

我必须明确的跟大家说，Mybatis是支持上述形式的批量插入，且可以正确返回主键id列表的。之所以返回null值，是Mybatis框架的一个bug，下一篇将具体讲述产生这个bug的原因，以及如何修复它。

