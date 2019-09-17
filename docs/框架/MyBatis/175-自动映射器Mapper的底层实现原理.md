**动态代理的功能：通过拦截器方法回调，对目标target方法进行增强。**

言外之意就是为了增强目标target方法。上面这句话没错，但也不要认为它就是真理，殊不知，动态代理还有**投鞭断流**的霸权，连目标target都不要的科幻模式。

注：本文默认认为，读者对动态代理的原理是理解的，如果不明白target的含义，难以看懂本篇文章，建议先理解动态代理。  


## **1. 自定义JDK动态代理之投鞭断流实现自动映射器Mapper** {#h2_0}

首先定义一个pojo。

```
public class User {
	private Integer id;
	private String name;
	private int age;

	public User(Integer id, String name, int age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}
	// getter setter
}
```

再定义一个接口UserMapper.java。

```
public interface UserMapper {
	public User getUserById(Integer id);	
}
```

接下来我们看看如何使用动态代理之**投鞭断流**，实现实例化接口并调用接口方法返回数据的。  


自定义一个InvocationHandler。

```
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MapperProxy implements InvocationHandler {

	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> clz) {
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[] { clz }, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			try {
				// 诸如hashCode()、toString()、equals()等方法，将target指向当前对象this
				return method.invoke(this, args);
			} catch (Throwable t) {
			}
		}
		// 投鞭断流
		return new User((Integer) args[0], "zhangsan", 18);
	}
}
```

上面代码中的target，在执行Object.java内的方法时，target被指向了this，target已经变成了傀儡、象征、占位符。在**投鞭断流**式的拦截时，已经没有了target。  


写一个测试代码：

```
public static void main(String[] args) {
	MapperProxy proxy = new MapperProxy();

	UserMapper mapper = proxy.newInstance(UserMapper.class);
	User user = mapper.getUserById(1001);

	System.out.println("ID:" + user.getId());
	System.out.println("Name:" + user.getName());
	System.out.println("Age:" + user.getAge());

	System.out.println(mapper.toString());
}
```

output：

```
ID:1001
Name:zhangsan
Age:18
x.y.MapperProxy@6bc7c054
```

这便是Mybatis自动映射器Mapper的底层实现原理。  


可能有读者不禁要问：你怎么把代码写的像初学者写的一样？没有结构，且缺乏美感。  


必须声明，作为一名经验老道的高手，能把程序写的像初学者写的一样，那必定是高手中的高手。这样可以让初学者感觉到亲切，舒服，符合自己的Style，让他们或她们，感觉到大牛写的代码也不过如此，自己甚至写的比这些大牛写的还要好，从此自信满满，热情高涨，认为与大牛之间的差距，仅剩下三分钟。

## **2. Mybatis自动映射器Mapper的源码分析** {#h2_1}

首先编写一个测试类：  


```
   public static void main(String[] args) {
		SqlSession sqlSession = MybatisSqlSessionFactory.openSession();
		try {
			StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
			List<Student> students = studentMapper.findAllStudents();
			for (Student student : students) {
				System.out.println(student);
			}
		} finally {
			sqlSession.close();
		}
	}
```

Mapper长这个样子：  


```
public interface StudentMapper {
	List<Student> findAllStudents();
	Student findStudentById(Integer id);
	void insertStudent(Student student);
}
```

org.apache.ibatis.binding.MapperProxy.java部分源码。

```
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

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
  // ...
```

org.apache.ibatis.binding.MapperProxyFactory.java部分源码。

```
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }
```

这便是Mybatis使用动态代理之**投鞭断流**。  


## **3. 接口Mapper内的方法能重载（overLoad）吗？（重要）** {#h2_2}

类似下面：

```
public User getUserById(Integer id);
public User getUserById(Integer id, String name);
```

**Answer：不能。**  


原因：在**投鞭断流**时，Mybatis使用package+Mapper+method全限名作为key，去xml内寻找唯一sql来执行的。类似：key=x.y.UserMapper.getUserById，那么，重载方法时将导致矛盾。对于Mapper接口，Mybatis禁止方法重载（overLoad）。



