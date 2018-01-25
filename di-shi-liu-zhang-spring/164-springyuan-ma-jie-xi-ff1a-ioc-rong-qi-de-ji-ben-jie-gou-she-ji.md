IoC（Inversion of Control），即控制反转，是 Spring 的重要基础特性之一，也是面向对象程序设计中的重要法则，其目的是用来降低程序之间的耦合度。控制反转一般分为两种类型，**依赖注入（Dependency Injection，简称 DI）**和**依赖查找（Dependency Lookup）**，不过依赖注入应用更加广泛，所以大部分时候，依赖注入等同于控制反转。

在面向对象程序设计中，对象一般用于承载数据和处理数据，不同对象之间的相互依赖、合作构成了我们的软件系统。设想在大型系统设计中，需要大量的对象通过相互依赖来实现需求，如果这些依赖关系由对象自己去控制和管理，那么耦合度将会很高，不易于系统的扩展和维护，这个时候我们可以将对象的依赖注入交给 IoC 容器来实现，将对象的新建、引用赋值等操作交由 IoC 容器统一完成，而对象只需要专心负责承载和处理数据即可。这样的设计可以降低系统在实现上的复杂性和耦合度，让系统更加灵活，满足“开-闭”原则，并易于扩展和维护。

Spring IoC 容器是 IoC 设计原则的轻量化实现，如果将 Spring IoC 容器类的 UML 图呈现在面前（如下图），你会发现 Spring IoC 容器的设计可以分为两个主要的路线：一个是实现了 BeanFactory 接口的简单容器；另外一个是以 ApplicationContext 应用上下文为核心的高级容器，也是我们广泛使用的容器类型，相对于 BeanFactory 而言，高级容器增加了许多面向实际应用的功能，让原本在 BeanFactory 中需要编码实现的功能，简化到用配置即可完成。

![](https://static.oschina.net/uploads/img/201706/05233931_kAgu.png "spring-ioc")

如上图，如果以一条直线从右上至左下做分割的话，那么简单容器结构主要分布在左上半部分，继承路径为：

> BeanFactory → HierarchicalBeanFactory → ConfigurableBeanFactory → ConfigurableListableBeanFactory → DefaultListableBeanFactory

而高级容器结构主要分布在右下半部分，当然高级容器的实现是建立在简单容器基础之上的，继承路径为：

> BeanFactory → ListableBeanFactory → ApplicationContext → ConfigurableApplicationContext 和 WebApplicationContext

### **一. Bean的内存表示** {#h3_0}

现实中的容器都是用来装物品的，Spring 的容器也不例外，这里的物品就是 bean。我通常对于 bean 的印象是一个个躺在配置文件中的`<bean/>`标签，或者是被注解的类，但是这些都是 bean 的静态表示，是还没有放入容器的物料，最终（加载完配置，且在 getBean 之前）加载到容器中的是一个个 BeanDefinition 实例。

BeanDefinition 的继承关系如下图，RootBeanDefinition、ChildBeanDefinition，以及 GenericBeanDefinition 是三个主要的实现。有时候我们需要在配置时，通过 parent 属性指定 bean 的父子关系，这个时候父 bean 则用 RootBeanDefinition 表示，而子 bean 则用 ChildBeanDefinition 表示。GenericBeanDefinition 自 2.5 版本引入，是对于一般的 bean 定义的一站式服务中心。

![](https://static.oschina.net/uploads/img/201706/05234024_VufM.png "spring-bean-definition")

这三个类都是由 AbstractBeanDefinition 派生而来，该抽象类中包含了 bean 的所有配置项和一些支持程序运行的属性，如下：

```
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor implements BeanDefinition, Cloneable {

    // 常量定义略

    /** bean 对应的类实例 */
    private volatile Object beanClass;
    /** bean的作用域，对应scope属性 */
    private String scope = SCOPE_DEFAULT;
    /** 是否是抽象类，对应abstract属性 */
    private boolean abstractFlag = false;
    /** 是否延迟加载，对应lazy-init属性 */
    private boolean lazyInit = false;
    /** 自动装配模式，对应autowire属性 */
    private int autowireMode = AUTOWIRE_NO;
    /** 依赖检查，对应dependency-check属性 */
    private int dependencyCheck = DEPENDENCY_CHECK_NONE;
    /** 对应depends-on，表示一个bean实例化前置依赖另一个bean */
    private String[] dependsOn;
    /** 对应autowire-candidate属性，设置为false时表示取消当前bean作为自动装配候选者的资格 */
    private boolean autowireCandidate = true;
    /** 对应primary属性，当自动装配存在多个候选者时，将其作为首选 */
    private boolean primary = false;
    /** 对应qualifier属性 */
    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<String, AutowireCandidateQualifier>(0);
    /** 非配置项：表示允许访问非公开的构造器和方法，由程序设置 */
    private boolean nonPublicAccessAllowed = true;
    /**
     * 非配置项：表示是否允许以宽松的模式解析构造函数，由程序设置
     *
     * 例如：如果设置为true，则在下列情况时不会抛出异常（示例来源于《Spring源码深度解析》）
     * interface ITest{}
     * class ITestImpl implements ITest {}
     * class Main {
     * Main(ITest i){}
     * Main(ITestImpl i){}
     * }
     */
    private boolean lenientConstructorResolution = true;
    /** 对应factory-bean属性 */
    private String factoryBeanName;
    /** 对应factory-method属性 */
    private String factoryMethodName;
    /** 记录构造函数注入属性，对应<construct-arg/>标签 */
    private ConstructorArgumentValues constructorArgumentValues;
    /** 记录<property/>属性集合 */
    private MutablePropertyValues propertyValues;
    /** 记录<lookup-method/>和<replaced-method/>标签配置 */
    private MethodOverrides methodOverrides = new MethodOverrides();
    /** 对应init-method属性 */
    private String initMethodName;
    /** 对应destroy-method属性 */
    private String destroyMethodName;
    /** 非配置项：是否执行init-method，由程序设置 */
    private boolean enforceInitMethod = true;
    /** 非配置项：是否执行destroy-method，由程序设置 */
    private boolean enforceDestroyMethod = true;
    /** 非配置项：表示是否是用户定义，而不是程序定义的，创建AOP时为true,由程序设置 */
    private boolean synthetic = false;
    /**
     * 非配置项：定义bean的应用场景，由程序设置，角色如下：
     * ROLE_APPLICATION：用户
     * ROLE_INFRASTRUCTURE：完全内部使用
     * ROLE_SUPPORT：某些复杂配置的一部分
     */
    private int role = BeanDefinition.ROLE_APPLICATION;
    /** bean的描述信息，对应description标签 */
    private String description;
    /** bean定义的资源 */
    private Resource resource;

    // 方法定义略
}
```

BeanDefinition 是容器对于 bean 配置的内部表示，Spring 将各个 bean 的 BeanDefinition 实例注册记录在 BeanDefinitionRegistry 中，该接口定义了对 BeanDefinition 的各种增删查操作，类似于内存数据库，其实现类 SimpleBeanDefinitionRegistry 主要以 Map 作为存储标的。

### **二. 简单容器基本结构** {#h3_1}

BeanFactory 是 Spring IoC 容器设计的基础，定义了容器应该具备的最基本的方法，源码如下：

```
public interface BeanFactory {
    /**
     * 用户使用容器时，可以使用转义符“&”来得到FactoryBean本身
     * 用来区分通过容器获取的FactoryBean产生的对象和获取FactoryBean本身
     * 例如：
     * 如果myBean是一个FactoryBean，那么使用“&myBean”得到的是FactoryBean，而不是myBean这个FactoryBean产生出来的对象
     */
    String FACTORY_BEAN_PREFIX = "&";

    /** 根据bean的名字获取对应的bean实例 */
    Object getBean(String name) throws BeansException;

    /** 根据bean的名字获取对应的bean实例，增加了对象类型检查 */
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    /** 根据类型获取对应的bean实例 */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /** 根据bean的名字获取对应的bean实例，可以指定构造函数的参数或者工厂方法的参数 */
    Object getBean(String name, Object... args) throws BeansException;

    /**
     * 根据类型获取对应的bean实例，可以指定构造函数的参数或者工厂方法的参数
     * @since 4.1
     */
    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

    /** 判断容器是否持有指定名称的bean实例 */
    boolean containsBean(String name);

    /** 是不是单例 */
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

    /** 是不是原型对象 */
    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

    /**
     * 判断name对应的bean实例是不是指定Class类型
     * @since 4.2
     */
    boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

    /** 判断name对应的bean实例是不是指定Class类型 */
    boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

    /** 获取bean实例的Class类型 */
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    /** 获取指定bean的所有别名 */
    String[] getAliases(String name);
}
```

BeanFactory 中定义的各种方法如上面方法注释，整个设计还是比较简洁、直观的，其中将近一半是获取 bean 对象的各种方法，另外就是对 bean 属性的获取和判定，该接口仅仅是定义了 IoC 容器的最基本基本形式，具体实现都交由子类来实现，后面我们会列举说明。

#### **2.1 FactoryBean 和 BeanFactory** {#h4_2}

Spring 在上述源码中定义了一个属性`FACTORY_BEAN_PREFIX = "&"`，用来获取**FactoryBean**对象，这个要与我们本节所讨论的**BeanFactory**相区分开来，虽然两者在名字上很相似，但却是完全不同的两个类。BeanFactory 以 Factory 结尾，所以它是一个工厂，用来管理 bean 对象，而 FactoryBean 则以 Bean 结尾，说明它本质上还是一个 bean，只是比我们通常所见的 bean 稍微特殊了一点。

FactoryBean 在实际开发中笔者用的并不是很多，这个主要是用来构造一些复杂对象，如果一个对象的配置十分复杂，那么这种时候通过编码实现可能是更好的选择。FactoryBean 的源码实现十分简单，定义了 3 个方法：

```
public interface FactoryBean<T> {

    /** 获取由 FactoryBean 创建的目标 bean 实例*/
    T getObject() throws Exception;

    /** 返回目标 bean 类型 */
    Class<?> getObjectType();

    /** 是否是单实例 */
    boolean isSingleton();
}
```

下面对 FactoryBean 的用法举例说明，假设我们定义了一个类`org.zhenchao.bean.MyBean`，如下：

```
public class MyBean implements BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {

    private long id;
    private String username;
    private String password;

    public MyBean() {}

    public MyBean(long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
```

我们需要实现一个实现了 FactoryBean 接口的类，我们在这个类里面实现了构造 MyBean 实例的逻辑：

```
public class MyFactoryBean implements FactoryBean<MyBean> {

    private String info;

    @Override
    public MyBean getObject() throws Exception {
        MyBean myBean = new MyBean();
        if (StringUtils.isNotBlank(info)) {
            String[] elements = info.split(",");
            myBean = new MyBean(Long.valueOf(elements[0]), elements[1], elements[2]);
        }
        return myBean;
    }

    @Override
    public Class<?> getObjectType() {
        return MyBean.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getInfo() {
        return info;
    }

    public MyFactoryBean setInfo(String info) {
        this.info = info;
        return this;
    }
}
```

然后 XML 配置如下：

```
<bean id="my-factory-bean" class="org.zhenchao.bean.MyFactoryBean" p:info="10001,zhenchao,123456"/>
```

当我们 getBean\("my-factory-bean"\) 时，返回的将不是 MyFactoryBean 对象，而是 MyBean 对象，如果我们希望获取 MyFactoryBean 对象，则可以在获取的 name 前面加 “&” ，即 getBean\("&my-factory-bean"\)，也就是我们之前介绍的定义在 BeanFactory 中的唯一的一个属性。下面的 UT 将会显示漂亮的 Green Bar：

```
Assert.assertTrue(beanFactory.getBean("my-factory-bean") instanceof MyBean);  // true
Assert.assertFalse(beanFactory.getBean("&my-factory-bean") instanceof MyBean); // false
Assert.assertTrue(beanFactory.getBean("&my-factory-bean") instanceof FactoryBean); // true
```

#### **2.2 基本结构设计** {#h4_3}

BeanFactory 定义了容器的基本形式，Spring 又在此基础上逐层扩展来丰富容器的特性（如下图），在简单容器中，除了 BeanFactory，还主要包含如下类和接口：

> 1. org.springframework.beans.factory.HierarchicalBeanFactory
> 2. org.springframework.beans.factory.ListableBeanFactory
> 3. org.springframework.beans.factory.config.AutowireCapableBeanFactory
> 4. org.springframework.beans.factory.config.ConfigurableBeanFactory
> 5. org.springframework.beans.factory.support.DefaultListableBeanFactory

![](https://static.oschina.net/uploads/img/201706/05234143_JBjp.png "spring-ioc-simple")

下面来逐个对其特性进行介绍：

> * org.springframework.beans.factory.HierarchicalBeanFactory

HierarchicalBeanFactory 译为中文是“分层的”，它相对于 BeanFactory 增加了对父 BeanFactory 的获取，子容器可以通过接口方法访问父容器，让容器的设计具备了层次性。这种层次性增强了容器的扩展性和灵活性，我们可以通过编程的方式为一个已有的容器添加一个或多个子容器，从而实现一些特殊功能。层次容器有一个特点就是子容器对于父容器来说是透明的，而子容器则能感知到父容器的存在。典型的应用场景就是 Spring MVC，控制层的 bean 位于子容器中，并将业务层和持久层的 bean 所在的容器设置为父容器，这样的设计可以让控制层的 bean 访问业务层和持久层的 bean，反之则不行，从而在容器层面对三层软件结构设计提供支持。

> * org.springframework.beans.factory.ListableBeanFactory

ListableBeanFactory 引入了获取容器中 bean 的配置信息的若干方法，比如获取容器中 bean 的个数，获取容器中所有 bean 的名称列表，按照目标类型获取 bean 名称，以及检查容器中是否包含指定名称的 bean 等等。Listable 中文译为“可列举的”，对于容器而言，bean 的定义和属性是可以列举的对象。

> * org.springframework.beans.factory.config.AutowireCapableBeanFactory

AutowireCapableBeanFactory 提供了创建 bean、自动注入，初始化以及应用 bean 的后置处理器等功能。自动注入让配置变得更加简单，也让注解配置成为可能，Spring 提供了四种自动注入类型：

> 1. **byName**
>    ，根据名称自动装配，假设 bean A 有一个名为 b 的属性，如果容器中刚好存在一个 bean 的名称为 b，则将该 bean 装配给 bean A 的 b 属性。
> 2. **byType**
>    ，根据类型自动匹配，假设 bean A 有一个类型为 B 的属性，如果容器中刚好有一个 B 类型的 bean，则使用该 bean 装配 A 的对应属性。
> 3. **constructor**
>    ，仅针对构造方法注入而言，类似于 byType，如果 bean A 有一个构造方法，构造方法包含一个 B 类型的入参，如果容器中有一个 B 类型的 bean，则使用该 bean 作为入参，如果找不到，则抛出异常。
> 4. **autodetect**
>    ，根据 bean 的自省机制决定采用 byType 还是 constructor 进行自动装配，如果 bean 提供了默认的构造函数，则采用 byType，否则采用 constructor。

&lt;beans/&gt; 元素标签中的 default-autowire 属性可以配置全局自动匹配，default-autowire 默认值为 no，表示不启用自动装配。在实际开发中，XML 配置方式很少启用自动装配功能，而基于注解的配置方式默认采用 byType 自动装配策略。

> * org.springframework.beans.factory.config.ConfigurableBeanFactory

ConfigurableBeanFactory 提供配置 Factory 的各种方法，增强了容器的可定制性，定义了设置类装载器、属性编辑器、容器初始化后置处理器等方法。

> * org.springframework.beans.factory.support.DefaultListableBeanFactory

DefaultListableBeanFactory 是一个非常重要的类，它包含了 IoC 容器所应该具备的重要功能，是容器完整功能的一个基本实现，XmlBeanFactory 是一个典型的由该类派生出来的 Factory，并且只是增加了加载 XML 配置资源的逻辑，而容器相关的特性则全部由 DefaultListableBeanFactory 来实现，如下是 XmlBeanFactory 的源码：

```
public class XmlBeanFactory extends DefaultListableBeanFactory {

    private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    public XmlBeanFactory(Resource resource) throws BeansException {
        this(resource, null);
    }

    public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
        super(parentBeanFactory);
        // 加载xml资源
        this.reader.loadBeanDefinitions(resource);
    }
}
```

整个代码实现上非常简单，我将在下一篇章中分析 XmlBeanFactory 加载 bean 的基本过程。Spring 在 3.1 版本之后将 XmlBeanFactory 置为了`deprecated`，并推荐使用更加原生的方式，即组合使用 DefaultListableBeanFactory 和 XmlBeanDefinitionReader 来完成 XmlBeanFactory 的功能。

### **三. 高级容器基本结构** {#h3_4}

ApplicationContext 是 Spring 为开发者提供的高级容器形式，也是我们初始化 Spring 容器的常用方式，除了简单容器所具备的功能外，ApplicationContext 还提供了许多额外功能来降低开发人员的开发量，提升框架的使用效率。这些额外的功能主要包括：

* **国际化支持**

ApplicationContext 实现了`org.springframework.context.MessageSource`接口，该接口为容器提供国际化消息访问功能，支持具备多语言版本需求的应用开发，并提供了多种实现来简化国际化资源文件的装载和获取。

* **发布应用上下文事件**

ApplicationContext 实现了`org.springframework.context.ApplicationEventPublisher`接口，该接口让容器拥有发布应用上下文事件的功能，包括容器启动、关闭事件等，如果一个 bean 需要接收容器事件，则只需要实现 ApplicationListener 接口即可，Spring 会自动扫描对应的监听器配置，并注册成为主题的观察者。

* **丰富的资源获取的方式**

ApplicationContext 实现了`org.springframework.core.io.support.ResourcePatternResolver`接口，ResourcePatternResolver 的实现类 PathMatchingResourcePatternResolver 让我们可以采用 Ant 风格的资源路径去加载配置文件。

基于 ApplicationContext 派生出了众多的扩展实现，如下图

![](https://static.oschina.net/uploads/img/201706/05234236_S69F.png "spring-ioc-advance")

ConfigurableApplicationContext 和 WebApplicationContext 是直接实现 ApplicationContext 的两个接口。

**ConfigurableApplicationContext**中主要增加了 refresh 和 close 两个方法，从而为应用上下文提供了启动、刷新和关闭的能力。其中 refresh 方法是高级容器的核心方法，方法中概括了高级容器初始化的主要流程（包含简单的容器的全部功能，以及高级容器特有的扩展功能），比如我们通常会采用如下方式启动高级容器：

```
ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-core.xml");
```

这里我们的在`new ClassPathXmlApplicationContext("spring-core.xml")`时，本质上就是在触发 refresh 方法，如下：

```
// new关键调用的构造方法
public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
    this(new String[] {configLocation}, true, null);
}

// 本质上是调用本构造方法
public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent) throws BeansException {
    super(parent);
    // 支持多个配置文件以数组形式传入
    this.setConfigLocations(configLocations);
    if (refresh) {
        // refresh几乎包含了ApplicationContext包含的全部功能
        this.refresh();
    }
}
```

在调用 refresh 方法之前，Spring 会先去解析配置文件的路径并存储到一个字符串数组中，然后就开始调用 refresh 方法，执行容器的初始化逻辑，这里的实现位于 AbstractApplicationContext 中：

```
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // 1. 初始化 refresh 的上下文环境
        this.prepareRefresh();
        // 2. 初始化 BeanFactory，加载并解析配置
        ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
        /* ---至此，完成了简单容器的所有功能，下面开始对简单容器进行增强--- */
        // 3. 对 BeanFactory 进行功能增强
        this.prepareBeanFactory(beanFactory);
        try {
            // 4. 后置处理 beanFactory，交由子类实现
            this.postProcessBeanFactory(beanFactory);
            // 5. 调用已注册的 BeanFactoryPostProcessor
            this.invokeBeanFactoryPostProcessors(beanFactory);
            // 6. 注册 BeanPostProcessor，仅仅是注册，调用在getBean的时候
            this.registerBeanPostProcessors(beanFactory);
            // 7. 初始化国际化资源
            this.initMessageSource();
            // 8. 初始化事件广播器
            this.initApplicationEventMulticaster();
            // 9. 留给子类实现的模板方法
            this.onRefresh();
            // 10. 注册事件监听器
            this.registerListeners();
            // 11. 实例化所有非延迟加载的单例
            this.finishBeanFactoryInitialization(beanFactory);
            // 12. 完成刷新过程，发布应用事件
            this.finishRefresh();
        } catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + ex);
            }
            // Destroy already created singletons to avoid dangling resources.
            this.destroyBeans();
            // Reset 'active' flag.
            this.cancelRefresh(ex);
            // Propagate exception to caller.
            throw ex;
        } finally {
            // Reset common introspection caches in Spring's core, since we
            // might not ever need metadata for singleton beans anymore...
            this.resetCommonCaches();
        }
    }
}
```

以后的篇章中，将详细分析整个过程的源码实现，这里只需要了解整个初始化大概流程即可。

**WebApplicationContext**是为 WEB 应用定制的上下文，可以基于 WEB 容器来实现配置文件的加载，以及初始化工作。对于非 WEB 应用而言，bean 只有 singleton 和 prototype 两种作用域，而在 WebApplicationContext 中则新增了 request、session、globalSession，以及 application 四种作用域。

WebApplicationContext 将整个应用上下文对象以属性的形式放置到 ServletContext 中，所以在 WEB 应用中，我们可以通过 WebApplicationContextUtils 的 getWebApplicationContext\(ServletContext sc\) 方法，从 ServletContext 中获取到 ApplicationContext 实例。为了支持这一特性，WebApplicationContext 定义了一个常量：

> ROOT\_WEB\_APPLICATION\_CONTEXT\_ATTRIBUTE = WebApplicationContext.class.getName\(\) + ".ROOT"

并在初始化应用上下文时以该常量为 key，将 WebApplicationContext 实例存放到 ServletContext 的属性列表中，当我们在调用 WebApplicationContextUtils 的 getWebApplicationContext\(ServletContext sc\) 方法时，本质上是在调用 ServletContext 的 getAttribute\(String name\) 方法，只不过 Spring 会对获取的结果做一些校验。

Spring IoC 容器的基本设计主要分为 BeanFactory 和 ApplicationContext 两大部分，在本篇文章中我们对整个容器按照这样的分类进行了简单的讲解，从而在整体上对 Spring IoC 建立一个感官上认识，后面的文章中我们将逐类展开，详细探讨。

