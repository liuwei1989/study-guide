本篇将对定义在 XML 文件中的 bean，从静态配置到变成可以使用的对象的过程，即 bean 的加载和初始化过程进行一个整体的梳理，不去深究，点到为止，只求对简单容器的实现有一个整体的感知，具体实现细节留到后面用针对性的篇章进行讲解。

首先我们来引入一个 Spring 入门使用示例，假设我们现在定义了一个类`org.zhenchao.framework.MyBean`，我们希望利用 Spring 来管理类对象，这里我们利用 Spring 经典的 XML 配置文件形式进行配置：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- bean的基本配置 -->
    <bean name="myBean" class="org.zhenchao.framework.MyBean"/>

</beans>
```

我们将上面的配置文件命名为 spring-core.xml，则对象的最原始的获取和使用示例如下：

```
// 1. 定义资源
Resource resource = new ClassPathResource("spring-core.xml");
// 2. 利用XmlBeanFactory解析并注册bean定义
XmlBeanFactory beanFactory = new XmlBeanFactory(resource);
// 3. 从IOC容器加载获取bean
MyBean myBean = (MyBean) beanFactory.getBean("myBean");
// 4. 使用bean
myBean.sayHello();
```

上面 demo 虽然简单，但麻雀虽小，五脏俱全，完整的让 Spring 执行了一遍加载配置文件，创建并初始化 bean 实例的过程。虽然从 Spring 3.1 开始 XmlBeanFactory 已经被置为`Deprecated`，但是 Spring 并没有定义出更加高级的基于 XML 加载 bean 的 BeanFactory，而是推荐采用更加原生的方式，即组合使用 DefaultListableBeanFactory 和 XmlBeanDefinitionReader 来完成上述过程：

```
Resource resource = new ClassPathResource("spring-core.xml");
DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
reader.loadBeanDefinitions(resource);
MyBean myBean = (MyBean) beanFactory.getBean("myBean");
myBean.sayHello();
```

后面的分析你将会看到 XmlBeanFactory 实际上是对 DefaultListableBeanFactory 和 XmlBeanDefinitionReader 组合使用方式的封装，并没有增加新的处理逻辑，所以我们仍将继续基于 XmlBeanFactory 来分析加载 bean 的过程。

Bean 的加载过程整体上可以分成两步走，第一步完成由静态配置到内存表示 BeanDefinition 的转换，第二步是基于 BeanDefinition 实例创建 bean 实例，并初始化 bean 的过程。我们将第一步称为 Bean 的解析和注册的过程，解析配置并注册到容器，而第二步则可以看做是 Bean 实例的创建和初始化的过程。

### **一. Bean 的解析与注册** {#h3_0}

![](https://static.oschina.net/uploads/img/201706/06235756_wbZ2.png "spring-bean-loading")

在当前阶段中，主要是对配置文件的解析，并注册 BeanDefinition 实例的过程，上图是本阶段执行过程的时序图，当我们`new XmlBeanFactory(resource)`的时候，已经完成将配置文件包装成了 Spring 定义的资源，并开始执行解析和注册。`new XmlBeanFactory(resource)`调用的构造方法如下：

```
public XmlBeanFactory(Resource resource) throws BeansException {
    this(resource, null);
}
```

这个构造方法本质上还是继续调用了：

```
public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
    super(parentBeanFactory);
    // 加载xml资源
    this.reader.loadBeanDefinitions(resource);
}
```

在这个构造方法里面先是调用了父类构造函数，即 DefaultListableBeanFactory 类，这是一个非常核心的类，它包含了基本 IoC 容器所具有的重要功能，是一个 IoC 容器的基本实现。然后是调用了 reader.loadBeanDefinitions\(resource\)，从这里开始加载配置。

Spring 在设计上采用了许多程序设计的基本原则，比如迪米特法则、开闭原则，以及接口隔离原则等等，这样的设计为后续的扩展提供了极大的灵活性，也增强了模块的复用性，这也是我看 Spring 源码的动力之一，希望通过阅读学习的过程来提升自己系统接口设计的能力。Spring 使用了专门的资源加载器对资源进行加载，这里的 reader 就是 XmlBeanDefinitionReader 对象，用来加载基于 XML 文件配置的 bean。这里的加载过程可以概括如下：

> 1. 利用 EncodedResource 二次包装资源文件
> 2. 获取资源输入流，并构造 InputSource 对象
> 3. 获取 XML 文件的实体解析器和验证模式
> 4. 加载 XML 文件，获取对应的 Document 对象
> 5. 由 Document 对象解析并注册 bean

#### **1.1 利用 EncodedResource 二次包装资源文件** {#h4_1}

采用 EncodedResource 对resource 进行二次封装，EncodedResource 类的说明可以参考本系列第二篇 “Spring源码解析：资源描述与加载”。

#### **1.2 获取资源输入流，并构造 InputSource 对象** {#h4_2}

对资源进行编码封装之后，开始真正进入 loadBeanDefinitions\(new EncodedResource\(resource\)\) 的过程，该方法源码如下：

```
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
    Assert.notNull(encodedResource, "EncodedResource must not be null");
    if (logger.isInfoEnabled()) {
        logger.info("Loading XML bean definitions from " + encodedResource.getResource());
    }

    // 标记正在加载的资源，防止循环引用
    Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
    if (currentResources == null) {
        currentResources = new HashSet<EncodedResource>(4);
        this.resourcesCurrentlyBeingLoaded.set(currentResources);
    }
    if (!currentResources.add(encodedResource)) {
        throw new BeanDefinitionStoreException("Detected cyclic loading of " + encodedResource + " - check your import definitions!");
    }

    try {
        // 获取资源的输入流
        InputStream inputStream = encodedResource.getResource().getInputStream();
        try {
            // 构造InputSource对象
            InputSource inputSource = new InputSource(inputStream);
            if (encodedResource.getEncoding() != null) {
                inputSource.setEncoding(encodedResource.getEncoding());
            }
            // 真正开始从XML文件中加载Bean定义
            return this.doLoadBeanDefinitions(inputSource, encodedResource.getResource());
        } finally {
            inputStream.close();
        }
    } catch (IOException ex) {
        throw new BeanDefinitionStoreException("IOException parsing XML document from " + encodedResource.getResource(), ex);
    } finally {
        currentResources.remove(encodedResource);
        if (currentResources.isEmpty()) {
            this.resourcesCurrentlyBeingLoaded.remove();
        }
    }
}
```

需要知晓的是`org.xml.sax.InputSource`不是 Spring 中定义的类，这个类来自 jdk，是 java 对 XML 实体提供的原生支持。这个方法主要还是做了一些准备工作，按照 Spring 方法的命名相关，真正干活的方法一般都是以 “do” 开头的，这里的 doLoadBeanDefinitions\(inputSource, encodedResource.getResource\(\)\) 才是真正开始加载 XML 的入口，该方法源码如下：

```
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
    try {

        // 1. 加载xml文件，获取到对应的Document（包含获取xml文件的实体解析器和验证模式）
        Document doc = this.doLoadDocument(inputSource, resource);

        // 2. 解析Document对象，并注册bean
        return this.registerBeanDefinitions(doc, resource);

    } catch (BeanDefinitionStoreException ex) {
        // 这里是连环catch，省略
    }
}
```

方法逻辑还是很清晰的，第一步获取`org.w3c.dom.Document`对象，第二步由该对象解析得到 BeanDefinition 对象，并注册到 IoC 容器中。

#### **1.3 获取 XML 文件的实体解析器和验证模式** {#h4_3}

doLoadDocument\(inputSource, resource\) 包含了获取实体解析器、验证模式，以及 Document 对象的逻辑，源码如下：

```
protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
    return this.documentLoader.loadDocument(
            inputSource,
            this.getEntityResolver(),  // 获取实体解析器
            this.errorHandler,
            this.getValidationModeForResource(resource),  // 获取验证模式
            this.isNamespaceAware());
}
```

XML 是半结构化数据，XML 的验证模式用于保证结构的正确性，常见的验证模式有 DTD 和 XSD 两种，获取验证模式的源码如下：

```
protected int getValidationModeForResource(Resource resource) {
    int validationModeToUse = this.getValidationMode();
    if (validationModeToUse != VALIDATION_AUTO) {
        // 手动指定了验证模式
        return validationModeToUse;
    }

    // 没有指定验证模式，则自动检测
    int detectedMode = this.detectValidationMode(resource);
    if (detectedMode != VALIDATION_AUTO) {
        return detectedMode;
    }

    // 检测验证模式失败，默认采用XSD验证
    return VALIDATION_XSD;
}
```

上面源码描述了获取验证模式的执行流程，如果没有手动指定，那么 Spring 会去自动检测。对于 XML 文件的解析，SAX 首先会读取 XML 文件头声明，以获取相应验证文件地址，并下载对应的文件，如果网络不正常，则会影响下载过程，这个时候可以通过注册一个实体解析器来实现寻找验证文件的过程。

#### **1.4 加载 XML 文件，获取对应的 Document 对象** {#h4_4}

完成获取对应的验证模式和解析器，就可以开始加载 Document 对象了，这里本质上调用的是 DefaultDocumentLoader 的 loadDocument\(\) 方法，源码如下：

```
public Document loadDocument(InputSource inputSource, EntityResolver entityResolver,
                             ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception {

    DocumentBuilderFactory factory = this.createDocumentBuilderFactory(validationMode, namespaceAware);
    if (logger.isDebugEnabled()) {
        logger.debug("Using JAXP provider [" + factory.getClass().getName() + "]");
    }
    DocumentBuilder builder = this.createDocumentBuilder(factory, entityResolver, errorHandler);
    return builder.parse(inputSource);
}
```

整个过程类似于我们平常解析 XML 文件的流程。

#### **1.5.由 Document 对象解析并注册 bean** {#h4_5}

完成了对 XML 文件到 Document 对象的构造，我们终于可以解析 Document 对象并注册 bean 了，这一过程发生在 registerBeanDefinitions\(doc, resource\) 中，源码如下：

```
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    // 使用DefaultBeanDefinitionDocumentReader构造
    BeanDefinitionDocumentReader documentReader = this.createBeanDefinitionDocumentReader();

    // 记录之前已经注册的BeanDefinition个数
    int countBefore = this.getRegistry().getBeanDefinitionCount();

    // 加载并注册bean
    documentReader.registerBeanDefinitions(doc, createReaderContext(resource));

    // 返回本次加载的bean的数量
    return getRegistry().getBeanDefinitionCount() - countBefore;
}
```

其作用是创建对应的 BeanDefinitionDocumentReader，并计算返回了过程中新注册的 bean 的数量，而具体的注册过程，则是由 BeanDefinitionDocumentReader 来完成的，具体的实现位于子类 DefaultBeanDefinitionDocumentReader 中：

```
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    logger.debug("Loading bean definitions");

    // 获取文档的root结点
    Element root = doc.getDocumentElement();

    this.doRegisterBeanDefinitions(root);
}
```

还是按照 Spring 命名习惯，doRegisterBeanDefinitions 才是真正干活的地方，这也是真正开始解析配置的核心所在：

```
protected void doRegisterBeanDefinitions(Element root) {
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = this.createDelegate(getReaderContext(), root, parent);

    if (this.delegate.isDefaultNamespace(root)) {
        // 处理profile标签（其作用类比pom.xml中的profile）
        String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
        if (StringUtils.hasText(profileSpec)) {
            String[] specifiedProfiles =
                    StringUtils.tokenizeToStringArray(profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            if (!this.getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec + "] not matching: " + getReaderContext().getResource());
                }
                return;
            }
        }
    }

    // 解析预处理，留给子类实现
    this.preProcessXml(root);

    // 解析并注册BeanDefinition
    this.parseBeanDefinitions(root, this.delegate);

    // 解析后处理，留给子类实现
    this.postProcessXml(root);

    this.delegate = parent;
}
```

方法中显示处理了 &lt;profile/&gt; 标签，这个属性在 Spring 中不是很常用，不过在 maven 的 pom.xml 中则很常见，意义也是相同的，就是在配置多套环境时，可以根据部署的具体环境来选择使用哪一套配置。方法中会先去检测是否配置了 profile，如果是就需要从上下文环境中确认当前激活了哪一套 profile。

该方法在解析并注册 BeanDefinition 前后各设置一个模板方法，留给子类扩展实现，并在 parseBeanDefinitions\(root, this.delegate\) 中执行解析和注册的逻辑：

```
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        // 解析默认标签
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (delegate.isDefaultNamespace(ele)) {
                    // 解析默认标签
                    this.parseDefaultElement(ele, delegate);
                } else {
                    // 解析自定义标签
                    delegate.parseCustomElement(ele);
                }
            }
        }
    } else {
        // 解析自定义标签
        delegate.parseCustomElement(root);
    }
}
```

方法中判断当前标签是默认标签还是自定义标签，并按照不同的策略去解析，这是一个复杂的过程，后面会用文章进行针对性讲解，这里不在往下细究。

到这里我们已经完成了静态配置到动态 BeanDefinition 的解析，并注册到容器中，接下去将探究如何创建并初始化 bean 实例的过程。

### **二. Bean 实例的创建和初始化** {#h3_6}

在完成了 Bean 的加载过程之后，我们可以调用 beanFactory.getBean\("myBean"\) 方法来获取目标对象，这里本质上调用的是 AbstractBeanFactory 的 getBean\(String name\) 方法：

```
public Object  throws BeansException {
    return this.doGetBean(name, null, null, false);
}
```

方法中调用了 doGetBean\(name, null, null, false\) 来实现具体逻辑，也符合我们的预期，该方法可以看做是获取 bean 实例的整体框架，一个函数完成了整个过程的模块调度，还是挺复杂的：

```
protected <T> T doGetBean(
            final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly) throws BeansException {
    /*
     * 获取name对应的真正beanName
     *
     * 因为传入的参数可以是alias，也可能是FactoryBean的name，所以需要进行解析，包含以下内容：
     * 1. 如果是FactoryBean，则去掉修饰符“&”
     * 2. 沿着引用链获取alias对应的最终name
     */
    final String beanName = this.transformedBeanName(name);
    Object bean;
    /*
     * 检查缓存或者实例工厂中是否有对应的单例
     *
     * 在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候为了避免循环依赖
     * Spring创建bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提前曝光（将对应的ObjectFactory加入到缓存）
     * 一旦下一个bean创建需要依赖上一个bean，则直接使用ObjectFactory对象
     */
    Object sharedInstance = this.getSingleton(beanName); // 获取单例
    if (sharedInstance != null && args == null) {
        // 实例已经存在
        if (logger.isDebugEnabled()) {
            if (this.isSingletonCurrentlyInCreation(beanName)) {
                logger.debug("Returning eagerly cached instance of singleton bean '" + beanName + "' that is not fully initialized yet - a consequence of a circular reference");
            } else {
                logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
            }
        }
        // 返回对应的实例
        bean = this.getObjectForBeanInstance(sharedInstance, name, beanName, null);
    } else {
        // 单例实例不存在
        if (this.isPrototypeCurrentlyInCreation(beanName)) {
            /*
             * 只有在单例模式下才会尝试解决循环依赖问题
             * 对于原型模式，如果存在循环依赖，也就是满足this.isPrototypeCurrentlyInCreation(beanName)，抛出异常
             */
            throw new BeanCurrentlyInCreationException(beanName);
        }
        // 获取parentBeanFactory实例
        BeanFactory parentBeanFactory = this.getParentBeanFactory();
        // 如果在beanDefinitionMap中（即所有已经加载的类中）不包含目标bean，则尝试从parentBeanFactory中获取
        if (parentBeanFactory != null && !this.containsBeanDefinition(beanName)) {
            String nameToLookup = this.originalBeanName(name);  // 获取name对应的真正beanName，如果是factoryBean，则加上“&”前缀
            if (args != null) {
                // 递归到BeanFactory中寻找
                return (T) parentBeanFactory.getBean(nameToLookup, args);
            } else {
                return parentBeanFactory.getBean(nameToLookup, requiredType);
            }
        }
        // 如果不仅仅是做类型检查，标记bean的状态已经创建，即将beanName加入alreadyCreated集合中
        if (!typeCheckOnly) {
            this.markBeanAsCreated(beanName);
        }
        try {
            /*
             * 将存储XML配置的GenericBeanDefinition实例转换成RootBeanDefinition实例，方便后续处理
             * 如果存在父bean，则同时合并父bean的相关属性
             */
            final RootBeanDefinition mbd = this.getMergedLocalBeanDefinition(beanName);
            // 检查bean是否是抽象的，如果是则抛出异常
            this.checkMergedBeanDefinition(mbd, beanName, args);
            // 加载当前bean依赖的bean
            String[] dependsOn = mbd.getDependsOn();
            if (dependsOn != null) {
                // 存在依赖，递归实例化依赖的bean
                for (String dep : dependsOn) {
                    if (this.isDependent(beanName, dep)) {
                        // 检查dep是否依赖beanName，从而导致循环依赖
                        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                    }
                    // 缓存依赖调用
                    this.registerDependentBean(dep, beanName);
                    this.getBean(dep);
                }
            }
            // 完成加载依赖的bean后，实例化mbd自身
            if (mbd.isSingleton()) {
                // scope == singleton
                sharedInstance = this.getSingleton(beanName, new ObjectFactory<Object>() {
                    @Override
                    public Object getObject() throws BeansException {
                        try {
                            return createBean(beanName, mbd, args);
                        } catch (BeansException ex) {
                            // 清理工作，从单例缓存中移除
                            destroySingleton(beanName);
                            throw ex;
                        }
                    }
                });
                bean = this.getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
            } else if (mbd.isPrototype()) {
                // scope == prototype
                Object prototypeInstance;
                try {
                    // 设置正在创建的状态
                    this.beforePrototypeCreation(beanName);
                    // 创建bean
                    prototypeInstance = this.createBean(beanName, mbd, args);
                } finally {
                    this.afterPrototypeCreation(beanName);
                }
                // 返回对应的实例
                bean = this.getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
            } else {
                // 其它scope
                String scopeName = mbd.getScope();
                final Scope scope = this.scopes.get(scopeName);
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                }
                try {
                    Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
                        @Override
                        public Object getObject() throws BeansException {
                            beforePrototypeCreation(beanName);
                            try {
                                return createBean(beanName, mbd, args);
                            } finally {
                                afterPrototypeCreation(beanName);
                            }
                        }
                    });
                    // 返回对应的实例
                    bean = this.getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                } catch (IllegalStateException ex) {
                    throw new BeanCreationException(beanName, "Scope '" + scopeName + "' is not active for the current thread; consider defining a scoped proxy for this bean if you intend to refer to it from a singleton", ex);
                }
            }
        } catch (BeansException ex) {
            cleanupAfterBeanCreationFailure(beanName);
            throw ex;
        }
    }
    // 检查需要的类型是否符合bean的实际类型，对应getBean时指定的requireType
    if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
        try {
            // 执行类型转换，转换成期望的类型
            return this.getTypeConverter().convertIfNecessary(bean, requiredType);
        } catch (TypeMismatchException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to convert bean '" + name + "' to required type '" + ClassUtils.getQualifiedName(requiredType) + "'", ex);
            }
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
        }
    }
    return (T) bean;
}
```

整个方法的过程可以概括为：

> 1. 获取参数 name 对应的真正的 beanName
> 2. 检查缓存或者实例工厂中是否有对应的单例，若存在则进行实例化并返回对象，否则继续往下执行
> 3. 执行 prototype 类型依赖检查，防止循环依赖
> 4. 如果当前 beanFactory 中不存在需要的 bean，则尝试从 parentBeanFactory 中获取
> 5. 将之前解析过程返得到的 GenericBeanDefinition 对象合并为 RootBeanDefinition 对象，便于后续处理
> 6. 如果存在依赖的 bean，则递归加载依赖的 bean
> 7. 依据当前 bean 的作用域对 bean 进行实例化
> 8. 如果对返回 bean 类型有要求，则进行检查，按需做类型转换
> 9. 返回 bean 实例



