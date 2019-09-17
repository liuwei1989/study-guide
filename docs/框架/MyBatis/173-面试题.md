#### 1、\#{}和${}的区别是什么？ {#h4_0}

注：这道题是面试官面试我同事的。

答：${}是Properties文件中的变量占位符，它可以用于标签属性值和sql内部，属于静态文本替换，比如${driver}会被静态替换为com.mysql.jdbc.Driver。\#{}是sql的参数占位符，Mybatis会将sql中的\#{}替换为?号，在sql执行前会使用PreparedStatement的参数设置方法，按序给sql的?号占位符设置参数值，比如ps.setInt\(0, parameterValue\)，\#{item.name}的取值方式为使用反射从参数对象中获取item对象的name属性值，相当于param.getItem\(\).getName\(\)。

* \#方式能够很大程度防止sql注入。

* $方式无法防止Sql注入。
* $方式一般用于传入数据库对象，例如传入表名.
* **一般能用\#的就别用$.**



#### 2、Xml映射文件中，除了常见的select\|insert\|updae\|delete标签之外，还有哪些标签？ {#h4_1}

注：这道题是京东面试官面试我时问的。

答：还有很多其他的标签，&lt;resultMap&gt;、&lt;parameterMap&gt;、&lt;sql&gt;、&lt;include&gt;、&lt;selectKey&gt;，加上动态sql的9个标签，trim\|where\|set\|foreach\|if\|choose\|when\|otherwise\|bind等，其中&lt;sql&gt;为sql片段标签，通过&lt;include&gt;标签引入sql片段，&lt;selectKey&gt;为不支持自增的主键生成策略标签。

#### 3、最佳实践中，通常一个Xml映射文件，都会写一个Dao接口与之对应，请问，这个Dao接口的工作原理是什么？Dao接口里的方法，参数不同时，方法能重载吗？ {#h4_2}

注：这道题也是京东面试官面试我时问的。

答：Dao接口，就是人们常说的Mapper接口，接口的全限名，就是映射文件中的namespace的值，接口的方法名，就是映射文件中MappedStatement的id值，接口方法内的参数，就是传递给sql的参数。Mapper接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为key值，可唯一定位一个MappedStatement，举例：com.mybatis3.mappers.StudentDao.findStudentById，可以唯一找到namespace为com.mybatis3.mappers.StudentDao下面id = findStudentById的MappedStatement。在Mybatis中，每一个&lt;select&gt;、&lt;insert&gt;、&lt;update&gt;、&lt;delete&gt;标签，都会被解析为一个MappedStatement对象。

Dao接口里的方法，是不能重载的，因为是全限名+方法名的保存和寻找策略。

Dao接口的工作原理是JDK动态代理，Mybatis运行时会使用JDK动态代理为Dao接口生成代理proxy对象，代理对象proxy会拦截接口方法，转而执行MappedStatement所代表的sql，然后将sql执行结果返回。

#### 4、Mybatis是如何进行分页的？分页插件的原理是什么？ {#h4_3}

注：我出的。

答：Mybatis使用RowBounds对象进行分页，它是针对ResultSet结果集执行的内存分页，而非物理分页，可以在sql内直接书写带有物理分页的参数来完成物理分页功能，也可以使用分页插件来完成物理分页。

分页插件的基本原理是使用Mybatis提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的sql，然后重写sql，根据dialect方言，添加对应的物理分页语句和物理分页参数。

举例：select \* from student，拦截sql后重写为：select t.\* from （select \* from student）t limit 0，10

#### 5、简述Mybatis的插件运行原理，以及如何编写一个插件。 {#h4_4}

注：我出的。

答：Mybatis仅可以编写针对ParameterHandler、ResultSetHandler、StatementHandler、Executor这4种接口的插件，Mybatis使用JDK的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这4种接口对象的方法时，就会进入拦截方法，具体就是InvocationHandler的invoke\(\)方法，当然，只会拦截那些你指定需要拦截的方法。

实现Mybatis的Interceptor接口并复写intercept\(\)方法，然后在给插件编写注解，指定要拦截哪一个接口的哪些方法即可，记住，别忘了在配置文件中配置你编写的插件。

#### 6、Mybatis执行批量插入，能返回数据库主键列表吗？ {#h4_5}

注：我出的。

答：能，JDBC都能，Mybatis当然也能。

#### 7、Mybatis动态sql是做什么的？都有哪些动态sql？能简述一下动态sql的执行原理不？ {#h4_6}

注：我出的。

答：Mybatis动态sql可以让我们在Xml映射文件内，以标签的形式编写动态sql，完成逻辑判断和动态拼接sql的功能，Mybatis提供了9种动态sql标签trim\|where\|set\|foreach\|if\|choose\|when\|otherwise\|bind。

其执行原理为，使用OGNL从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，以此来完成动态sql的功能。

#### 8、Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？ {#h4_7}

注：我出的。

答：第一种是使用&lt;resultMap&gt;标签，逐一定义列名和对象属性名之间的映射关系。第二种是使用sql列的别名功能，将列别名书写为对象属性名，比如T\_NAME AS NAME，对象属性名一般是name，小写，但是列名不区分大小写，Mybatis会忽略列名大小写，智能找到与之对应对象属性名，你甚至可以写成T\_NAME AS NaMe，Mybatis一样可以正常工作。

有了列名与属性名的映射关系后，Mybatis通过反射创建对象，同时使用反射给对象的属性逐一赋值并返回，那些找不到映射关系的属性，是无法完成赋值的。

#### 9、Mybatis能执行一对一、一对多的关联查询吗？都有哪些实现方式，以及它们之间的区别。 {#h4_8}

注：我出的。

答：能，Mybatis不仅可以执行一对一、一对多的关联查询，还可以执行多对一，多对多的关联查询，多对一查询，其实就是一对一查询，只需要把selectOne\(\)修改为selectList\(\)即可；多对多查询，其实就是一对多查询，只需要把selectOne\(\)修改为selectList\(\)即可。

关联对象查询，有两种实现方式，一种是单独发送一个sql去查询关联对象，赋给主对象，然后返回主对象。另一种是使用嵌套查询，嵌套查询的含义为使用join查询，一部分列是A对象的属性值，另外一部分列是关联对象B的属性值，好处是只发一个sql查询，就可以把主对象和其关联对象查出来。

那么问题来了，join查询出来100条记录，如何确定主对象是5个，而不是100个？其去重复的原理是&lt;resultMap&gt;标签内的&lt;id&gt;子标签，指定了唯一确定一条记录的id列，Mybatis根据&lt;id&gt;列值来完成100条记录的去重复功能，&lt;id&gt;可以有多个，代表了联合主键的语意。

同样主对象的关联对象，也是根据这个原理去重复的，尽管一般情况下，只有主对象会有重复记录，关联对象一般不会重复。

举例：下面join查询出来6条记录，一、二列是Teacher对象列，第三列为Student对象列，Mybatis去重复处理后，结果为1个老师6个学生，而不是6个老师6个学生。

       t\_id    t\_name           s\_id

\|          1 \| teacher      \|      38 \|  
\|          1 \| teacher      \|      39 \|  
\|          1 \| teacher      \|      40 \|  
\|          1 \| teacher      \|      41 \|  
\|          1 \| teacher      \|      42 \|  
\|          1 \| teacher      \|      43 \|

#### 10、Mybatis是否支持延迟加载？如果支持，它的实现原理是什么？ {#h4_9}

注：我出的。

答：Mybatis仅支持association关联对象和collection关联集合对象的延迟加载，association指的就是一对一，collection指的就是一对多查询。在Mybatis配置文件中，可以配置是否启用延迟加载lazyLoadingEnabled=true\|false。

它的原理是，使用CGLIB创建目标对象的代理对象，当调用目标方法时，进入拦截器方法，比如调用a.getB\(\).getName\(\)，拦截器invoke\(\)方法发现a.getB\(\)是null值，那么就会单独发送事先保存好的查询关联B对象的sql，把B查询上来，然后调用a.setB\(b\)，于是a的对象b属性就有值了，接着完成a.getB\(\).getName\(\)方法的调用。这就是延迟加载的基本原理。

当然了，不光是Mybatis，几乎所有的包括Hibernate，支持延迟加载的原理都是一样的。

#### 11、Mybatis的Xml映射文件中，不同的Xml映射文件，id是否可以重复？ {#h4_10}

注：我出的。

答：不同的Xml映射文件，如果配置了namespace，那么id可以重复；如果没有配置namespace，那么id不能重复；毕竟namespace不是必须的，只是最佳实践而已。

原因就是namespace+id是作为Map&lt;String, MappedStatement&gt;的key使用的，如果没有namespace，就剩下id，那么，id重复会导致数据互相覆盖。有了namespace，自然id就可以重复，namespace不同，namespace+id自然也就不同。

#### 12、Mybatis中如何执行批处理？ {#h4_11}

注：我出的。

答：使用BatchExecutor完成批处理。

#### 13、Mybatis都有哪些Executor执行器？它们之间的区别是什么？ {#h4_12}

注：我出的

答：Mybatis有三种基本的Executor执行器，**SimpleExecutor、ReuseExecutor、BatchExecutor。**

**SimpleExecutor：**每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。

**ReuseExecutor：**执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，而是放置于Map&lt;String, Statement&gt;内，供下一次使用。简言之，就是重复使用Statement对象。

**BatchExecutor：**执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch\(\)），等待统一执行（executeBatch\(\)），它缓存了多个Statement对象，每个Statement对象都是addBatch\(\)完毕后，等待逐一执行executeBatch\(\)批处理。与JDBC批处理相同。

作用范围：Executor的这些特点，都严格限制在SqlSession生命周期范围内。

#### 14、Mybatis中如何指定使用哪一种Executor执行器？ {#h4_13}

注：我出的

答：在Mybatis配置文件中，可以指定默认的ExecutorType执行器类型，也可以手动给DefaultSqlSessionFactory的创建SqlSession的方法传递ExecutorType类型参数。

#### 15、Mybatis是否可以映射Enum枚举类？ {#h4_14}

注：我出的

答：Mybatis可以映射枚举类，不单可以映射枚举类，Mybatis可以映射任何对象到表的一列上。映射方式为自定义一个TypeHandler，实现TypeHandler的setParameter\(\)和getResult\(\)接口方法。TypeHandler有两个作用，一是完成从javaType至jdbcType的转换，二是完成jdbcType至javaType的转换，体现为setParameter\(\)和getResult\(\)两个方法，分别代表设置sql问号占位符参数和获取列查询结果。

#### 16、Mybatis映射文件中，如果A标签通过include引用了B标签的内容，请问，B标签能否定义在A标签的后面，还是说必须定义在A标签的前面？ {#h4_15}

注：我出的

答：虽然Mybatis解析Xml映射文件是按照顺序解析的，但是，被引用的B标签依然可以定义在任何地方，Mybatis都可以正确识别。

原理是，Mybatis解析A标签，发现A标签引用了B标签，但是B标签尚未解析到，尚不存在，此时，Mybatis会将A标签标记为未解析状态，然后继续解析余下的标签，包含B标签，待所有标签解析完毕，Mybatis会重新解析那些被标记为未解析的标签，此时再解析A标签时，B标签已经存在，A标签也就可以正常解析完成了。

#### 17、简述Mybatis的Xml映射文件和Mybatis内部数据结构之间的映射关系？ {#h4_16}

注：我出的

答：Mybatis将所有Xml配置信息都封装到All-In-One重量级对象Configuration内部。在Xml映射文件中，&lt;parameterMap&gt;标签会被解析为ParameterMap对象，其每个子元素会被解析为ParameterMapping对象。&lt;resultMap&gt;标签会被解析为ResultMap对象，其每个子元素会被解析为ResultMapping对象。每一个&lt;select&gt;、&lt;insert&gt;、&lt;update&gt;、&lt;delete&gt;标签均会被解析为MappedStatement对象，标签内的sql会被解析为BoundSql对象。

#### 18、为什么说Mybatis是半自动ORM映射工具？它与全自动的区别在哪里？ {#h4_17}

注：我出的

答：Hibernate属于全自动ORM映射工具，使用Hibernate查询关联对象或者关联集合对象时，可以根据对象关系模型直接获取，所以它是全自动的。而Mybatis在查询关联对象或关联集合对象时，需要手动编写sql来完成，所以，称之为半自动ORM映射工具。

面试题看似都很简单，但是想要能正确回答上来，必定是研究过源码且深入的人，而不是仅会使用的人或者用的很熟的人，以上所有面试题及其答案所涉及的内容，在我的Mybatis系列博客中都有详细讲解和原理分析。





1.Mybatis比IBatis比较大的几个改进是什么

        a.有接口绑定,包括注解绑定sql和xml绑定Sql ,

        b.动态sql由原来的节点配置变成OGNL表达式,

        c. 在一对一,一对多的时候引进了association,在一对多的时候引入了collection

           节点,不过都是在resultMap里面配置



2.什么是MyBatis的接口绑定,有什么好处

        接口映射就是在IBatis中任意定义接口,然后把接口里面的方法和SQL语句绑定,

        我们直接调用接口方法就可以,这样比起原来了SqlSession提供的方法我们可以有更加灵活的选择和设置.



3.接口绑定有几种实现方式,分别是怎么实现的?

        接口绑定有两种实现方式,一种是通过注解绑定,就是在接口的方法上面加上

        @Select@Update等注解里面包含Sql语句来绑定,另外一种就是通过xml里面写SQL来绑定,

        在这种情况下,要指定xml映射文件里面的namespace必须为接口的全路径名.



4.什么情况下用注解绑定,什么情况下用xml绑定

        当Sql语句比较简单时候,用注解绑定,

        当SQL语句比较复杂时候,用xml绑定,一般用xml绑定的比较多



5.MyBatis实现一对一有几种方式?具体怎么操作的

        有联合查询和嵌套查询,联合查询是几个表联合查询,只查询一次,

        通过在resultMap里面配置association节点配置一对一的类就可以完成;



        嵌套查询是先查一个表,根据这个表里面

        的结果的外键id,去再另外一个表里面查询数据,也是通过association配置,但另外一个表

        的查询通过select属性配置



6.MyBatis实现一对多有几种方式,怎么操作的

        有联合查询和嵌套查询,联合查询是几个表联合查询,只查询一次,通过在resultMap里面配

        置collection节点配置一对多的类就可以完成;



        嵌套查询是先查一个表,根据这个表里面的

        结果的外键id,去再另外一个表里面查询数据,也是通过配置collection,但另外一个表的

        查询通过select节点配置



7.MyBatis里面的动态Sql是怎么设定的?用什么语法?

        MyBatis里面的动态Sql一般是通过if节点来实现,通过OGNL语法来实现,但是如果要写的完

        整,必须配合where,trim节点,where节点是判断包含节点有内容就插入where,否则不插

        入,trim节点是用来判断如果动态语句是以and 或or开始,那么会自动把这个and或者or取

        掉 



8.IBatis和MyBatis在核心处理类分别叫什么

        IBatis里面的核心处理类交SqlMapClient,

        MyBatis里面的核心处理类叫做SqlSession 



9.IBatis和MyBatis在细节上的不同有哪些

        在sql里面变量命名有原来的\#变量\# 变成了\#{变量}  

        原来的$变量$变成了${变量},

        原来在sql节点里面的class都换名字交type 

        原来的queryForObject queryForList 变成了selectOne selectList  

        原来的别名设置在映射文件里面放在了核心配置文件里



10.讲下MyBatis的缓存

        MyBatis的缓存分为一级缓存和二级缓存,

        一级缓存放在session里面,默认就有,二级缓存放在它的命名空间里,默认是打开的,

        使用二级缓存属性类需要实现Serializable序列化接

        口\(可用来保存对象的状态\),可在它的映射文件中配置&lt;cache/&gt;



11.MyBatis\(IBatis\)的好处是什么

        ibatis把sql语句从Java源程序中独立出来，

        放在单独的XML文件中编写，给程序的维护带来了很大便利。

        ibatis封装了底层JDBC API的调用细节，并能自动将结果集转换成Java Bean对象，

        大大简化了Java数据库编程的重复工作。



        因为Ibatis需要程序员自己去编写sql语句，

        程序员可以结合数据库自身的特点灵活控制sql语句，

        因此能够实现比hibernate等全自动orm框架更高的查询效率，能够完成复杂查询。.







1.JDBC编程有哪些不足之处，MyBatis是如何解决这些问题的？

① 数据库链接创建、释放频繁造成系统资源浪费从而影响系统性能，如果使用数据库链接池可解决此问题。

解决：在SqlMapConfig.xml中配置数据链接池，使用连接池管理数据库链接。

② Sql语句写在代码中造成代码不易维护，实际应用sql变化的可能较大，sql变动需要改变java代码。

解决：将Sql语句配置在XXXXmapper.xml文件中与java代码分离。

③ 向sql语句传参数麻烦，因为sql语句的where条件不一定，可能多也可能少，占位符需要和参数一一对应。

解决： Mybatis自动将java对象映射至sql语句。

④ 对结果集解析麻烦，sql变化导致解析代码变化，且解析前需要遍历，如果能将数据库记录封装成pojo对象解析比较方便。

解决：Mybatis自动将sql执行结果映射至java对象。



2.MyBatis编程步骤是什么样的？

① 创建SqlSessionFactory 

② 通过SqlSessionFactory创建SqlSession 

③ 通过sqlsession执行数据库操作 

④ 调用session.commit\(\)提交事务 

⑤ 调用session.close\(\)关闭会话



3.MyBatis与Hibernate有哪些不同？

    Mybatis和hibernate不同，它不完全是一个ORM框架，因为MyBatis需要程序员自己编写Sql语句，不过mybatis可以通过XML或注解方式灵活配置要运行的sql语句，并将java对象和sql语句映射生成最终执行的sql，最后将sql执行的结果再映射生成java对象。 

    Mybatis学习门槛低，简单易学，程序员直接编写原生态sql，可严格控制sql执行性能，灵活度高，非常适合对关系数据模型要求不高的软件开发，例如互联网软件、企业运营类软件等，因为这类软件需求变化频繁，一但需求变化要求成果输出迅速。但是灵活的前提是mybatis无法做到数据库无关性，如果需要实现支持多种数据库的软件则需要自定义多套sql映射文件，工作量大。 

    Hibernate对象/关系映射能力强，数据库无关性好，对于关系模型要求高的软件（例如需求固定的定制化软件）如果用hibernate开发可以节省很多代码，提高效率。但是Hibernate的缺点是学习门槛高，要精通门槛更高，而且怎么设计O/R映射，在性能和对象模型之间如何权衡，以及怎样用好Hibernate需要具有很强的经验和能力才行。 

总之，按照用户的需求在有限的资源环境下只要能做出维护性、扩展性良好的软件架构都是好架构，所以框架只有适合才是最好。



4.使用MyBatis的mapper接口调用时有哪些要求？

①  Mapper接口方法名和mapper.xml中定义的每个sql的id相同 

②  Mapper接口方法的输入参数类型和mapper.xml中定义的每个sql 的parameterType的类型相同 

③  Mapper接口方法的输出参数类型和mapper.xml中定义的每个sql的resultType的类型相同 

④  Mapper.xml文件中的namespace即是mapper接口的类路径。



5.SqlMapConfig.xml中配置有哪些内容？

SqlMapConfig.xml中配置的内容和顺序如下： 

properties（属性）

settings（配置）

typeAliases（类型别名）

typeHandlers（类型处理器）

objectFactory（对象工厂）

plugins（插件）

environments（环境集合属性对象）

environment（环境子属性对象）

transactionManager（事务管理）

dataSource（数据源）

mappers（映射器）



6.简单的说一下MyBatis的一级缓存和二级缓存？

Mybatis首先去缓存中查询结果集，如果没有则查询数据库，如果有则从缓存取出返回结果集就不走数据库。Mybatis内部存储缓存使用一个HashMap，key为hashCode+sqlId+Sql语句。value为从查询出来映射生成的java对象

Mybatis的二级缓存即查询缓存，它的作用域是一个mapper的namespace，即在同一个namespace中查询sql可以从缓存中获取数据。二级缓存是可以跨SqlSession的。



7.Mapper编写有哪几种方式？

①接口实现类继承SqlSessionDaoSupport

使用此种方法需要编写mapper接口，mapper接口实现类、mapper.xml文件



1、在sqlMapConfig.xml中配置mapper.xml的位置

&lt;mappers&gt;

    &lt;mapper resource="mapper.xml文件的地址" /&gt;

    &lt;mapper resource="mapper.xml文件的地址" /&gt;

&lt;/mappers&gt;

2、定义mapper接口

3、实现类集成SqlSessionDaoSupport

mapper方法中可以this.getSqlSession\(\)进行数据增删改查。

4、spring 配置

&lt;bean id=" " class="mapper接口的实现"&gt;

    &lt;property name="sqlSessionFactory" ref="sqlSessionFactory"&gt;&lt;/property&gt;

&lt;/bean&gt;

②使用org.mybatis.spring.mapper.MapperFactoryBean



1、在sqlMapConfig.xml中配置mapper.xml的位置

如果mapper.xml和mappre接口的名称相同且在同一个目录，这里可以不用配置



&lt;mappers&gt;

    &lt;mapper resource="mapper.xml文件的地址" /&gt;

    &lt;mapper resource="mapper.xml文件的地址" /&gt;

&lt;/mappers&gt;



2、定义mapper接口



注意



1、mapper.xml中的namespace为mapper接口的地址



2、mapper接口中的方法名和mapper.xml中的定义的statement的id保持一致



3、 Spring中定义



&lt;bean id="" class="org.mybatis.spring.mapper.MapperFactoryBean"&gt;

    &lt;property name="mapperInterface"   value="mapper接口地址" /&gt; 

    &lt;property name="sqlSessionFactory" ref="sqlSessionFactory" /&gt; 

&lt;/bean&gt;

③使用mapper扫描器



1、mapper.xml文件编写，



注意：



mapper.xml中的namespace为mapper接口的地址



mapper接口中的方法名和mapper.xml中的定义的statement的id保持一致



如果将mapper.xml和mapper接口的名称保持一致则不用在sqlMapConfig.xml中进行配置 



2、定义mapper接口



注意mapper.xml的文件名和mapper的接口名称保持一致，且放在同一个目录



3、配置mapper扫描器



 



&lt;bean class="org.mybatis.spring.mapper.MapperScannerConfigurer"&gt;

    &lt;property name="basePackage" value="mapper接口包地址"&gt;&lt;/property&gt;

    &lt;property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/&gt; 

&lt;/bean&gt;

4、使用扫描器后从spring容器中获取mapper的实现对象

 



扫描器将接口通过代理方法生成实现对象，要spring容器中自动注册，名称为mapper 接口的名称。

