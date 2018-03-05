## Spring AOP代理对象的生成

Spring提供了两种方式来生成代理对象: JdkProxy和[**Cglib**](https://github.com/cglib/cglib)，具体使用哪种方式生成由AopProxyFactory根据AdvisedSupport对象的配置来决定。默认的策略是如果目标类是接口，则使用JDK动态代理技术，否则使用Cglib来生成代理。

![](http://upload-images.jianshu.io/upload_images/1401055-5bce642faaec45a7.png?imageMogr2/auto-orient/strip|imageView2/2/w/1240)

具体逻辑在 `org.springframework.aop.framework.DefaultAopProxyFactory`类中，源码如下：

```
/**
 * Spring AOP代理工厂
 */
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

    @Override
    public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
        if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass == null) {
                throw new AopConfigException("TargetSource cannot determine target class: " +
                        "Either an interface or a target is required for proxy creation.");
            }
            //如果目标类是接口, 使用JDK动态代理来生成代理类及代理类实例对象
            if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
                return new JdkDynamicAopProxy(config);
            }
            //使用Cglib生成代理类及代理类实例对象
            return new ObjenesisCglibAopProxy(config);
        }
        else {
            return new JdkDynamicAopProxy(config);
        }
    }

    private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
        Class<?>[] ifcs = config.getProxiedInterfaces();
        return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
    }

}
```

### JDK动态代理技术生成代理类及实例对象

下面我们来研究一下Spring如何使用JDK来生成代理对象，具体的生成代码放在`org.springframework.aop.framework.JdkDynamicAopProxy` 这个类中，直接上相关代码：

```
/**
 * JDK动态代理实现类
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    /** use serialVersionUID from Spring 1.2 for interoperability */
    private static final long serialVersionUID = 5531744639992436476L;

    /** We use a static Log to avoid serialization issues */
    private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

    /** Config used to configure this proxy */
    private final AdvisedSupport advised;

    /**
     * Is the {@link #equals} method defined on the proxied interfaces?
     */
    private boolean equalsDefined;

    /**
     * Is the {@link #hashCode} method defined on the proxied interfaces?
     */
    private boolean hashCodeDefined;


    /**
     * Construct a new JdkDynamicAopProxy for the given AOP configuration.
     * @param config the AOP configuration as AdvisedSupport object
     * @throws AopConfigException if the config is invalid. We try to throw an informative
     * exception in this case, rather than let a mysterious failure happen later.
     */
    public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
            throw new AopConfigException("No advisors and no TargetSource specified");
        }
        this.advised = config;
    }

    /**
     * 使用JDK动态代理生成代理类
     */
    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
        }
        Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
        findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
        return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
    }

    /**
     * Finds any {@link #equals} or {@link #hashCode} method that may be defined
     * on the supplied set of interfaces.
     * @param proxiedInterfaces the interfaces to introspect
     */
    private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
        for (Class<?> proxiedInterface : proxiedInterfaces) {
            Method[] methods = proxiedInterface.getDeclaredMethods();
            for (Method method : methods) {
                if (AopUtils.isEqualsMethod(method)) {
                    this.equalsDefined = true;
                }
                if (AopUtils.isHashCodeMethod(method)) {
                    this.hashCodeDefined = true;
                }
                if (this.equalsDefined && this.hashCodeDefined) {
                    return;
                }
            }
        }
    }
}
```

JdkDynamicAopProxy 同时实现了AopProxy和InvocationHandler接口，InvocationHandler是JDK动态代理的核心，生成的代理对象的方法调用都会委托到InvocationHandler.invoke\(\)方法。下面我们就通过分析这个类中实现的invoke\(\)方法来具体看下Spring AOP是如何织入切面的。

```
@Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodInvocation invocation;
        Object oldProxy = null;
        boolean setProxyContext = false;

        TargetSource targetSource = this.advised.targetSource;
        Class<?> targetClass = null;
        Object target = null;

        try {
            if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {    //目标类没有实现eqauls()方法
                // The target does not implement the equals(Object) method itself.
                return equals(args[0]);
            }
            else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {    //目标类没有实现hashCode()方法
                // The target does not implement the hashCode() method itself.
                return hashCode();
            }
            else if (method.getDeclaringClass() == DecoratingProxy.class) {
                // There is only getDecoratedClass() declared -> dispatch to proxy config.
                return AopProxyUtils.ultimateTargetClass(this.advised);
            }
            else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
                    method.getDeclaringClass().isAssignableFrom(Advised.class)) {
                // Service invocations on ProxyConfig with the proxy config...
                return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
            }

            Object retVal;

            if (this.advised.exposeProxy) {
                // Make invocation available if necessary.
                oldProxy = AopContext.setCurrentProxy(proxy);
                setProxyContext = true;
            }

            // May be null. Get as late as possible to minimize the time we "own" the target,
            // in case it comes from a pool.
            target = targetSource.getTarget();
            if (target != null) {
                targetClass = target.getClass();
            }

            // Get the interception chain for this method.
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

            // Check whether we have any advice. If we don't, we can fallback on direct
            // reflective invocation of the target, and avoid creating a MethodInvocation.
            if (chain.isEmpty()) {
                // We can skip creating a MethodInvocation: just invoke the target directly
                // Note that the final invoker must be an InvokerInterceptor so we know it does
                // nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
                Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
            }
            else {
                // We need to create a method invocation...
                invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
                // Proceed to the joinpoint through the interceptor chain.
                retVal = invocation.proceed();
            }

            // Massage return value if necessary.
            Class<?> returnType = method.getReturnType();
            if (retVal != null && retVal == target &&
                    returnType != Object.class && returnType.isInstance(proxy) &&
                    !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
                // Special case: it returned "this" and the return type of the method
                // is type-compatible. Note that we can't help if the target sets
                // a reference to itself in another returned object.
                retVal = proxy;
            }
            else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                throw new AopInvocationException(
                        "Null return value from advice does not match primitive return type for: " + method);
            }
            return retVal;
        }
        finally {
            if (target != null && !targetSource.isStatic()) {
                // Must have come from TargetSource.
                targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
                // Restore old proxy.
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }
```

主流程可以简述为：获取可以应用到此方法上的通知链（Interceptor Chain），如果有，则应用通知，并执行joinpoint；如果通知链为空，则直接反射执行joinpoint。

而这里的关键是通知链是如何获取的以及它又是如何执行的，下面逐一分析下。

首先，从上面的代码可以看到，通知链是通过AdvisedSupport.getInterceptorsAndDynamicInterceptionAdvice\(\)这个方法来获取的,我们来看下这个方法的实现:

```
public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
        MethodCacheKey cacheKey = new MethodCacheKey(method);
        List<Object> cached = this.methodCache.get(cacheKey);
        if (cached == null) {
            cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
                    this, method, targetClass);
            this.methodCache.put(cacheKey, cached);
        }
        return cached;
    }
```

可以看到实际的获取工作其实是由`org.springframework.aop.framework.AdvisorChainFactory`的 getInterceptorsAndDynamicInterceptionAdvice\(\)这个方法来完成的，获取到的结果会被缓存。  
AdvisorChainFactory接口只有一个默认实现类 ：`DefaultAdvisorChainFactory`，下面来分析下这个类的实现：

```
package org.springframework.aop.framework;

@SuppressWarnings("serial")
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
            Advised config, Method method, Class<?> targetClass) {

        // This is somewhat tricky... We have to process introductions first,
        // but we need to preserve order in the ultimate list.
        List<Object> interceptorList = new ArrayList<Object>(config.getAdvisors().length);
        Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
        //查看是否包含IntroductionAdvisor  
        boolean hasIntroductions = hasMatchingIntroductions(config, actualClass);

        //用于将Advisor转化成MethodInterceptor
        AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();

        for (Advisor advisor : config.getAdvisors()) {
            if (advisor instanceof PointcutAdvisor) {
                // Add it conditionally.
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
                    //将Advisor转化成Interceptor  
                    MethodInterceptor[] interceptors = registry.getInterceptors(advisor);

                    //检查当前advisor的pointcut是否可以匹配当前方法  
                    MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
                    if (MethodMatchers.matches(mm, method, actualClass, hasIntroductions)) {
                        if (mm.isRuntime()) {
                            // Creating a new object instance in the getInterceptors() method
                            // isn't a problem as we normally cache created chains.
                            for (MethodInterceptor interceptor : interceptors) {
                                interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
                            }
                        }
                        else {
                            interceptorList.addAll(Arrays.asList(interceptors));
                        }
                    }
                }
            }
            else if (advisor instanceof IntroductionAdvisor) {
                IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
                if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
                    Interceptor[] interceptors = registry.getInterceptors(advisor);
                    interceptorList.addAll(Arrays.asList(interceptors));
                }
            }
            else {
                Interceptor[] interceptors = registry.getInterceptors(advisor);
                interceptorList.addAll(Arrays.asList(interceptors));
            }
        }

        return interceptorList;
    }

    /**
     * Determine whether the Advisors contain matching introductions.
     */
    private static boolean hasMatchingIntroductions(Advised config, Class<?> actualClass) {
        for (int i = 0; i < config.getAdvisors().length; i++) {
            Advisor advisor = config.getAdvisors()[i];
            if (advisor instanceof IntroductionAdvisor) {
                IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
                if (ia.getClassFilter().matches(actualClass)) {
                    return true;
                }
            }
        }
        return false;
    }

}
```

这个方法执行完成后，Advised中配置能够应用到连接点或者目标类的Advisor全部被转化成了MethodInterceptor.

接下来，回到 `JdkDynamicAopProxy`的 invoke方法中，如下：

```
// 获取这个方法上的拦截器链
    List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

    if (chain.isEmpty()) {
        //直接调用
        Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
        retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
    }
    else {
        // 创建MethodInvocation
        invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
        // Proceed to the joinpoint through the interceptor chain.
        retVal = invocation.proceed();
    }
```

从这段代码可以看出，如果得到的拦截器链为空，则直接反射调用目标方法，否则创建ReflectiveMethodInvocation，调用其proceed方法，触发拦截器链的执行，来看下 `ReflectiveMethodInvocation` 的proceed\(\) 方法源码：

```
@Override
    public Object proceed() throws Throwable {
        //    We start with an index of -1 and increment early.
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return invokeJoinpoint();
        }

        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
            // Evaluate dynamic method matcher here: static part will already have
            // been evaluated and found to match.
            InterceptorAndDynamicMethodMatcher dm =
                    (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
            if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
                return dm.interceptor.invoke(this);
            }
            else {
                // Dynamic matching failed.
                // Skip this interceptor and invoke the next in the chain.
                return proceed();
            }
        }
        else {
            // It's an interceptor, so we just invoke it: The pointcut will have
            // been evaluated statically before this object was constructed.
            return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
        }
    }

    /**
     * Invoke the joinpoint using reflection.
     * Subclasses can override this to use custom invocation.
     * @return the return value of the joinpoint
     * @throws Throwable if invoking the joinpoint resulted in an exception
     */
    protected Object invokeJoinpoint() throws Throwable {
        return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
    }
```

### Cglib生成代理类及实例对象

接下来的分析会涉及到Cglib 使用，对Cglib不熟悉的同学，先看看 \[Cglib Tutorial\] \([https://github.com/cglib/cglib/wiki/Tutorial\)。](https://github.com/cglib/cglib/wiki/Tutorial%29。)

Spring AOP中使用Cglib生成动态代理的类是  
`org.springframework.aop.framework.ObjenesisCglibAopProxy` ，它继承自 `org.springframework.aop.framework.CglibAopProxy`，我们首先来看看CglibAopProxy 的源码：

```
/**
 * Cglib动态代理实现类
 */
class CglibAopProxy implements AopProxy, Serializable {

    // Constants for CGLIB callback array indices
    private static final int AOP_PROXY = 0;
    private static final int INVOKE_TARGET = 1;
    private static final int NO_OVERRIDE = 2;
    private static final int DISPATCH_TARGET = 3;
    private static final int DISPATCH_ADVISED = 4;
    private static final int INVOKE_EQUALS = 5;
    private static final int INVOKE_HASHCODE = 6;


    /** Logger available to subclasses; static to optimize serialization */
    protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

    /** Keeps track of the Classes that we have validated for final methods */
    private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<Class<?>, Boolean>();


    /** The configuration used to configure this proxy */
    protected final AdvisedSupport advised;

    protected Object[] constructorArgs;

    protected Class<?>[] constructorArgTypes;

    /** Dispatcher used for methods on Advised */
    private final transient AdvisedDispatcher advisedDispatcher;

    private transient Map<String, Integer> fixedInterceptorMap;

    private transient int fixedInterceptorOffset;

    public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
            throw new AopConfigException("No advisors and no TargetSource specified");
        }
        this.advised = config;
        this.advisedDispatcher = new AdvisedDispatcher(this.advised);
    }

        @Override
    public Object getProxy() {
        return getProxy(null);
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating CGLIB proxy: target source is " + this.advised.getTargetSource());
        }

        try {
            Class<?> rootClass = this.advised.getTargetClass();
            Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");

            Class<?> proxySuperClass = rootClass;
            if (ClassUtils.isCglibProxyClass(rootClass)) {
                proxySuperClass = rootClass.getSuperclass();
                Class<?>[] additionalInterfaces = rootClass.getInterfaces();
                for (Class<?> additionalInterface : additionalInterfaces) {
                    this.advised.addInterface(additionalInterface);
                }
            }

            // Validate the class, writing log messages as necessary.
            validateClassIfNecessary(proxySuperClass, classLoader);

            // Cglib Enhancer配置
            Enhancer enhancer = createEnhancer();
            if (classLoader != null) {
                enhancer.setClassLoader(classLoader);
                if (classLoader instanceof SmartClassLoader &&
                        ((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
                    enhancer.setUseCache(false);
                }
            }
            enhancer.setSuperclass(proxySuperClass);
            enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));

            // Cglib 动态代理核心
            Callback[] callbacks = getCallbacks(rootClass);
            Class<?>[] types = new Class<?>[callbacks.length];
            for (int x = 0; x < types.length; x++) {
                types[x] = callbacks[x].getClass();
            }
            // fixedInterceptorMap only populated at this point, after getCallbacks call above
            enhancer.setCallbackFilter(new ProxyCallbackFilter(
                    this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset));
            enhancer.setCallbackTypes(types);

            // 生成动态代理类和代理类实例
            return createProxyClassAndInstance(enhancer, callbacks);
        }
        catch (CodeGenerationException ex) {
            throw new AopConfigException("Could not generate CGLIB subclass of class [" +
                    this.advised.getTargetClass() + "]: " +
                    "Common causes of this problem include using a final class or a non-visible class",
                    ex);
        }
        catch (IllegalArgumentException ex) {
            throw new AopConfigException("Could not generate CGLIB subclass of class [" +
                    this.advised.getTargetClass() + "]: " +
                    "Common causes of this problem include using a final class or a non-visible class",
                    ex);
        }
        catch (Exception ex) {
            // TargetSource.getTarget() failed
            throw new AopConfigException("Unexpected AOP exception", ex);
        }
    }

    protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        enhancer.setInterceptDuringConstruction(false);
        //设置方法回调
        enhancer.setCallbacks(callbacks);
        return (this.constructorArgs != null ?
                enhancer.create(this.constructorArgTypes, this.constructorArgs) :
                enhancer.create());
    }
}
```

CglibAopProxy 和 JdkDynamicAopProxy类一样 实现了 `org.springframework.aop.framework.AopProxy`接口。

既然使用Cglib来生成代理类，那么其生成的代理对象的方法调用都会委托到Callback，我们来看一下 getCallbacks\(\) 方法，源码如下：

```
private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
        // Parameters used for optimisation choices...
        boolean exposeProxy = this.advised.isExposeProxy();
        boolean isFrozen = this.advised.isFrozen();
        boolean isStatic = this.advised.getTargetSource().isStatic();

        // Choose an "aop" interceptor (used for AOP calls).
        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

        // Choose a "straight to target" interceptor. (used for calls that are
        // unadvised but can return this). May be required to expose the proxy.
        Callback targetInterceptor;
        if (exposeProxy) {
            targetInterceptor = isStatic ?
                    new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
        }
        else {
            targetInterceptor = isStatic ?
                    new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
                    new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
        }

        // Choose a "direct to target" dispatcher (used for
        // unadvised calls to static targets that cannot return this).
        Callback targetDispatcher = isStatic ?
                new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

        Callback[] mainCallbacks = new Callback[] {
                aopInterceptor,  // for normal advice
                targetInterceptor,  // invoke target without considering advice, if optimized
                new SerializableNoOp(),  // no override for methods mapped to this
                targetDispatcher, this.advisedDispatcher,
                new EqualsInterceptor(this.advised),
                new HashCodeInterceptor(this.advised)
        };

        Callback[] callbacks;

        // If the target is a static one and the advice chain is frozen,
        // then we can make some optimisations by sending the AOP calls
        // direct to the target using the fixed chain for that method.
        if (isStatic && isFrozen) {
            Method[] methods = rootClass.getMethods();
            Callback[] fixedCallbacks = new Callback[methods.length];
            this.fixedInterceptorMap = new HashMap<String, Integer>(methods.length);

            // TODO: small memory optimisation here (can skip creation for methods with no advice)
            for (int x = 0; x < methods.length; x++) {
                List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
                fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
                        chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
                this.fixedInterceptorMap.put(methods[x].toString(), x);
            }

            // Now copy both the callbacks from mainCallbacks
            // and fixedCallbacks into the callbacks array.
            callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
            System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
            System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
            this.fixedInterceptorOffset = mainCallbacks.length;
        }
        else {
            callbacks = mainCallbacks;
        }
        return callbacks;
    }
```

ObjenesisCglibAopProxy 类源码如下：

```
class ObjenesisCglibAopProxy extends CglibAopProxy {

    private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

    private static final SpringObjenesis objenesis = new SpringObjenesis();


    /**
     * Create a new ObjenesisCglibAopProxy for the given AOP configuration.
     * @param config the AOP configuration as AdvisedSupport object
     */
    public ObjenesisCglibAopProxy(AdvisedSupport config) {
        super(config);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
        Class<?> proxyClass = enhancer.createClass();
        Object proxyInstance = null;

        if (objenesis.isWorthTrying()) {
            try {
                proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
            }
            catch (Throwable ex) {
                logger.debug("Unable to instantiate proxy using Objenesis, " +
                        "falling back to regular proxy construction", ex);
            }
        }

        if (proxyInstance == null) {
            // Regular instantiation via default constructor...
            try {
                proxyInstance = (this.constructorArgs != null ?
                        proxyClass.getConstructor(this.constructorArgTypes).newInstance(this.constructorArgs) :
                        proxyClass.newInstance());
            }
            catch (Throwable ex) {
                throw new AopConfigException("Unable to instantiate proxy using Objenesis, " +
                        "and regular proxy instantiation via default constructor fails as well", ex);
            }
        }

        ((Factory) proxyInstance).setCallbacks(callbacks);
        return proxyInstance;
    }

}
```

ObjenesisCglibAopProxy 重写了父类 `CglibAopProxy` 中的createProxyClassAndInstance方法，使用 [Objenesis](http://objenesis.org/)来生成代理类实例对象。

SpringObjenesis 源码如下：

```
package org.springframework.objenesis;

import org.springframework.core.SpringProperties;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.objenesis.instantiator.ObjectInstantiator;
import org.springframework.objenesis.strategy.InstantiatorStrategy;
import org.springframework.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.util.ConcurrentReferenceHashMap;

public class SpringObjenesis implements Objenesis {
    public static final String IGNORE_OBJENESIS_PROPERTY_NAME = "spring.objenesis.ignore";
    private final InstantiatorStrategy strategy;
    private final ConcurrentReferenceHashMap<Class<?>, ObjectInstantiator<?>> cache;
    private volatile Boolean worthTrying;

    public SpringObjenesis() {
        this((InstantiatorStrategy)null);
    }

    public SpringObjenesis(InstantiatorStrategy strategy) {
        this.cache = new ConcurrentReferenceHashMap();
        this.strategy = (InstantiatorStrategy)(strategy != null?strategy:new StdInstantiatorStrategy());
        if(SpringProperties.getFlag("spring.objenesis.ignore")) {
            this.worthTrying = Boolean.FALSE;
        }

    }

    public boolean isWorthTrying() {
        return this.worthTrying != Boolean.FALSE;
    }

    public <T> T newInstance(Class<T> clazz, boolean useCache) {
        return !useCache?this.newInstantiatorOf(clazz).newInstance():this.getInstantiatorOf(clazz).newInstance();
    }

    public <T> T newInstance(Class<T> clazz) {
        return this.getInstantiatorOf(clazz).newInstance();
    }

    public <T> ObjectInstantiator<T> getInstantiatorOf(Class<T> clazz) {
        ObjectInstantiator instantiator = (ObjectInstantiator)this.cache.get(clazz);
        if(instantiator == null) {
            ObjectInstantiator newInstantiator = this.newInstantiatorOf(clazz);
            instantiator = (ObjectInstantiator)this.cache.putIfAbsent(clazz, newInstantiator);
            if(instantiator == null) {
                instantiator = newInstantiator;
            }
        }

        return instantiator;
    }

    protected <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> clazz) {
        Boolean currentWorthTrying = this.worthTrying;

        try {
            ObjectInstantiator err = this.strategy.newInstantiatorOf(clazz);
            if(currentWorthTrying == null) {
                this.worthTrying = Boolean.TRUE;
            }

            return err;
        } catch (ObjenesisException var5) {
            if(currentWorthTrying == null) {
                Throwable cause = var5.getCause();
                if(cause instanceof ClassNotFoundException || cause instanceof IllegalAccessException) {
                    this.worthTrying = Boolean.FALSE;
                }
            }

            throw var5;
        } catch (NoClassDefFoundError var6) {
            if(currentWorthTrying == null) {
                this.worthTrying = Boolean.FALSE;
            }

            throw new ObjenesisException(var6);
        }
    }
}
```

到此，关于Spring AOP内部实现原理分析就结束了。

