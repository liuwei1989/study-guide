Mybatis中的Sql命令，在枚举类SqlCommandType中定义的。  


```
public enum SqlCommandType {
  UNKNOWN, INSERT, UPDATE, DELETE, SELECT, FLUSH;
}
```

下面，我们以Mapper接口中的一个方法作为例子，看看Sql命令的执行完整流程。  


```
public interface StudentMapper {
	List<Student> findAllStudents(Map<String, Object> map, RowBounds rowBounds, ResultSetHandler rh);	
}
```

参数RowBounds和ResultSetHandler是可选参数，表示分页对象和自定义结果集处理器，一般不需要。  


一个完整的Sql命令，其执行的完整流程图如下：

![](http://static.oschina.net/uploads/space/2016/0505/181036_SgDu_2727738.jpg)

（Made In Edrawmax）

对于上面的流程图，如果看过前面的博文的话，大部分对象我们都比较熟悉了。一个图，就完整展示了其执行流程。

MapperProxy的功能：

1. 因为Mapper接口不能直接实例化，MapperProxy的作用，就是使用JDK动态代理功能，间接实例化Mapper的proxy对象。可参看系列博文的第二篇。

2. 缓存MapperMethod对象。

```
private final Map<Method, MapperMethod> methodCache;
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
    // 投鞭断流
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }

  // 缓存MapperMethod
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }
```

  


MapperMethod的功能：

1. 解析Mapper接口的方法，并封装成MapperMethod对象。

2. 将Sql命令，正确路由到恰当的SqlSession的方法上。

```
public class MapperMethod {

  // 保存了Sql命令的类型和键id
  private final SqlCommand command;
  // 保存了Mapper接口方法的解析信息
  private final MethodSignature method;

  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, method);
  }

  // 根据解析结果，路由到恰当的SqlSession方法上
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    if (SqlCommandType.INSERT == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
    } else if (SqlCommandType.UPDATE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
    } else if (SqlCommandType.DELETE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
    } else if (SqlCommandType.SELECT == command.getType()) {
      if (method.returnsVoid() && method.hasResultHandler()) {
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
        result = executeForMap(sqlSession, args);
      } else {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = sqlSession.selectOne(command.getName(), param);
      }
    } else if (SqlCommandType.FLUSH == command.getType()) {
        result = sqlSession.flushStatements();
    } else {
      throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
  // ...
```

org.apache.ibatis.binding.MapperMethod.SqlCommand。  


```
public static class SqlCommand {
    // full id, 通过它可以找到MappedStatement
    private final String name;
    private final SqlCommandType type;
// ...
```

org.apache.ibatis.binding.MapperMethod.MethodSignature。

```
public static class MethodSignature {

    private final boolean returnsMany;
    private final boolean returnsMap;
    private final boolean returnsVoid;
    private final Class<?> returnType;
    private final String mapKey;
    private final Integer resultHandlerIndex;
    private final Integer rowBoundsIndex;
    private final SortedMap<Integer, String> params;
    private final boolean hasNamedParameters;

    public MethodSignature(Configuration configuration, Method method) {
      this.returnType = method.getReturnType();
      this.returnsVoid = void.class.equals(this.returnType);
      this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
      this.mapKey = getMapKey(method);
      this.returnsMap = (this.mapKey != null);
      this.hasNamedParameters = hasNamedParams(method);
      // 分页参数
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      // 自定义ResultHandler
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      this.params = Collections.unmodifiableSortedMap(getParams(method, this.hasNamedParameters));
    }
```

以上是对MapperMethod的补充说明。  


本节的重点，是上面的那个Sql命令完整执行流程图。如果不是使用Mapper接口调用，而是直接调用SqlSession的方法，那么，流程图从SqlSession的地方开始即可，后续都是一样的。



