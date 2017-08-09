### 前言

对于Spring MVC项目搭建相信大家按照网上教程来做基本都会，但更多时候我们应该多问几个为什么，多思考实现原理，当你懂了工作原理之后对于很多问题的可能就知道是那里出错了，同时也可以更好地改进项目。

### Spring MVC简介

Spring是一个开源框架，Spring是于2003 年兴起的一个轻量级的Java 开发框架。它是为了解决企业应用开发的复杂性而创建的。框架的主要优势之一就是其分层架构，分层架构允许使用者选择使用哪一个组件，同时为 J2EE 应用程序开发提供集成的框架。Spring使用基本的JavaBean来完成以前只可能由EJB完成的事情。然而，Spring的用途不仅限于服务器端的开发。从简单性、可测试性和松耦合的角度而言，任何Java应用都可以从Spring中受益。Spring的核心是控制反转（IoC）和面向切面（AOP）。简单来说，Spring是一个分层的JavaSE/EEfull-stack\(一站式\) 轻量级开源框架。

### 补充知识点

**Spring Web MVC**框架是以请求为驱动，围绕中央Servlet设计，将请求发送给控制器，并提供了其他促进Web应用程序开发的功能。  
DispatcherServlet就是这个中央Servlet，DispatcherServlet是一个真正的Servlet。DispatcherServlet的功能很强大，它与Spring IOC容器完全兼容，因此可以使用任何Spring拥护的特征。

DispatcherServlet拥有一些特别的beans来处理请求和呈现适当的视图：

| bean类型 | 描述 |
| :--- | :--- |
| _HandlerMapping_ | 用于handlers映射请求和一系列的对于拦截器的前处理和后处理，大部分时候使用`@controller`注解，也可以使用其他接口。 |
| _HandlerAdapter_ | 帮助DispatcherServlet处理映射请求处理程序，而不管实际调用的是那个处理程序。具体些就是HandlerAdapter是隐藏在DispatcherServlet之下的。 一般情况下调用的处理器是`@Controller`注解的处理器。 |
| _HandlerExceptionResolver_ | 处理映射异常。 |
| _ViewResolver_ | 根据实际配置解析实际的View类型 |
| _LocaleResolver & LocaleContextResolver_ | 解决客户端正在使用的的区域设置以及可能的时区，以便能够提供国际化视野。 |
| _ThemeResolver_ | 解决Web应用程序可以使用的主题，例如提供个性化布局。 |
| _MultipartResolver_ | 解析多部分请求，以支持从HTML表单中上传文件 |
| _FlashMapManager_ | 存储并检索可用于将一个请求属性传递到另一个请求的input和output的FlashMap，通常用于在重定向中 |

### Spring MVC工作流程

Spring的MVC框架主要由DispatcherServlet、处理器映射、处理器\(控制器\)、视图解析器、视图组成。  
其工作原理图如下所示：

![](http://upload-images.jianshu.io/upload_images/4290189-ea34c7220f7f978e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Spring MVC工作流程图

#### Spring MVC工作流程说明

从图中可以看出，Spring Web MVC框架是围绕DispatcherServlet设计的，DispathcerServlet相当于与一个调度器，将请求根据处理流程一步一步分发给相应的解析器解析处理。

1. 客户端将请求发送给后台（一般都是通过浏览器进行发送的），请求到了后台都是交给DispatcherServlet处理，所有相当于是直接发送给了DispatcherServlet。
2. DispatcherServlet根据请求的信息（URL、Http方法、请求报文头、请求参数Cookie等）调用HandlerMapping解析器解析该请求对应的handler。
3. 解析到对应的handler后，DispatcherServlet将控制权移交给HandlerAdapter来处理请求，HandlerAdapter将处理器包装为适配器，从而支持多种类型的处理器。
 
   4.HandlerAdapter会根据实际需要的Handler调用真正的处理器来处理请求，处理器根据要求处理业务逻辑。
4. 处理器处理完业务逻辑之后会返回一个ModelAndView对象，这里的Model是处理器处理完成之后的数据，View是一个逻辑上的View。
5. ViewResolver会根据逻辑View查找实际对应的是那个View。
6. DispatcherServlet最后将5中返回的Model放入到6中解析的正真的View中生成结果，并返回给请求者。

#### DispatcherServlet介绍

在Web MVC框架中，每个DispatcherServlet都拥有自己的WebApplicationContext，它继承了在root WebApplicationContextContext中定义的所有beans。root WebApplicationContext包含了在其上下文和Sevlet实例之间共享的所有基础框架beans。  
DispatcherServlet的上下文继承如图所示：

![](http://upload-images.jianshu.io/upload_images/4290189-d9baece193672866.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

  


Spring Web MVC 一般情况的上下文继承图

对于上下文的继承，特殊情况是可以把Controller、ViewResolver、HandlerMapping放在Root WebApplicationContext中。

DispatcherServlet就像一个“Front Controller”设计模式，Spring farmwork文档中给出DispathcerServlet的处理流程如下图所示：

![](http://upload-images.jianshu.io/upload_images/4290189-e4a9db925012bf94.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

  


DispatcherServlet处理流程图

在初始化DispatherServlet时，在初始化DispatcherServlet时，Spring MVC会查找一个在WEB-INF下名为_\[servlet-name\]-servlet.xml_的文件且在这里创建beans。例如在web.xml中声明servlet-name如下图所示：

```
<web-app>
        <servlet>
                <servlet-name>golfing</servlet-name>
                <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
                <load-on-startup>1</load-on-startup>
        </servlet>
        <servlet-mapping>
                <servlet-name>golfing</servlet-name>
                <url-pattern>/golfing/*</url-pattern>
        </servlet-mapping>
</web-app>
```

则必须在/WEB-INF/下建立文件_golfing-servlet.xml_文件，这个文件会包含所有beans。现在比较流行注解方式，使用注解方式在项目初始化的时候会把注解类注入到Servlet WebApplicationContext中，所以DispatcherServlet也可以读取到注解的bean。

**DispatcherServlet的对请求的处理流程**：  
● 在请求中搜索并绑定WebApplicationContext作为controlelr和程序中其他元素可以使用的属性。  
● 当处理请求时，将区域解析器（Locale resolver\)绑定到请求中，以便于在进程中的元素可以解决区域问题。区域解析器是可选的。  
● 主题解析器（Theme resolver）被绑定到请求中，用于诸如视图之类的元素使用哪种主题的请求。可选。  
● 检查请求中是否指定多部分文件解析器（multipart file resolver\)，若有则使用使用MultipartHttpServletRequest作为处理程序。  
● 查找合适的handler，如果找到合适的handler，则执行与处理程序（预处理程序、后处理程序和Controller）关联的执行链，以便准备模型或呈现。  
● 如果model返回，则view就呈现；如果model没有返回，就没有view呈现。

#### HandlerMappings

在以前的版本的Spring中，为寻找合适的handlers，我们需要在web application context中定义一个或多个HandlerMapping beans去匹配web 请求。而现在的注解控制器，不需要这样做了，因为RequestMappingHandlerMapping 会自动地寻找在所有`@Controller` beans的`@RequestMapping`注解来寻找合适的handlers。

由于HandlerMapping是从AbstractHandlerMapping中扩展而来，因此HandlerMapping拥有以下特性：  
● 有一系列的拦截器可以使用。  
● 当handler mapping没有匹配到handler时，会默认使用defaultHandler 。  
● 匹配的顺序是基于order属性的，Spirng会对所有在环境中可用的handler mapping进行排序，并且使用第一个匹配的到handler。  
● 如果设置alwaysUseFullPath为true，Spring将会使用完整路径去匹配合适的handler；否则会使用相对路径。例如，如果Servlet的映射是使用`/testing/*`且alwaysUseFullPath属性是true，当使用_/testing/viewPage.html_，不管这个属性是真是假，都返回_/viewPage.html_。  
● urlDecode默认是true，如果选择比较encoded的路径，需要设置该标识为false。注：当比较encoded路径是不会匹配Servlet路径的。

一共有三种拦截器：  
● preHandle\(\)：在真正的handler执行之前调用。  
● postHandle\(\)：在handler执行之后调用。  
● afterCompletion\(\)：在完成请求之后调用。

preHandle\(\)`方法返回一个布尔值。当返回的是true时，handler执行链才会继续执行；当返回的false时，DispatcherServlet假设拦截器本身可以处理请求（例如呈现视图），并且不会执行在执行链中的其他拦截器和handler。可以使用`preHandle\(\)\`\`\`来筛选、预处理请求。  
使用拦截器的例子如下：

```
<beans>
        <bean id="handlerMapping"
                        class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping">
                <property name="interceptors">
                        <list>
                                <ref bean="officeHoursInterceptor"/>
                        </list>
                </property>
        </bean>

        <bean id="officeHoursInterceptor"
                        class="samples.TimeBasedAccessInterceptor">
                <property name="openingTime" value="9"/>
                <property name="closingTime" value="18"/>
        </bean>
</beans>
```

```
package samples;

public class TimeBasedAccessInterceptor extends HandlerInterceptorAdapter {

        private int openingTime;
        private int closingTime;

        public void setOpeningTime(int openingTime) {
                this.openingTime = openingTime;
        }

        public void setClosingTime(int closingTime) {
                this.closingTime = closingTime;
        }

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                        Object handler) throws Exception {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(HOUR_OF_DAY);
                if (openingTime <= hour && hour < closingTime) {
                        return true;
                }
                response.sendRedirect("http://host.com/outsideOfficeHours.html");
                return false;
        }
}

```

任何请求都会被TimeBasedAccessInterceptor处理。如果当前时间不是工作时间，用户将会重定向到一个指向静态HTML的文件，只能在工作时间使用该网站。

注：postHandle 方法并不是特别适用于`@ResponseBody` 和 ResponseEntity 方法中，`postHandle()`是在HttpMessageConverter写入并提交响应之后执行，因此postHandle有可能会改变响应。

#### HandlerAdapter

**HandlerAdapter用于调用具体的handler来处理业务逻辑**。HandlerAdapter实现了每种不同类型的handler，DispatcherServlet将控制权交给HandlerAdapter并通过HandlerAdapter接口访问所有已安装的处理程序，允许DispatcherServlet无限扩展，这也意味着HandlerAdapter不包含任何特定于处理程序的特定代码，相当于一个中间件，用于调用具体的Handler。

从spring3.1版本开始，废除了AnnotationMethodHandlerAdapter的使用，推荐使用RequestMappingHandlerAdapter完成注解式处理器适配来对标记`@ResquestMapping`的方法进行适配。  
springmvc使用`<mvc:annotation-driven>`自动加载RequestMappingHandlerMapping和RequestMappingHandlerAdapter，可用在springmvc.xml配置文件中使用&lt;mvc:annotation-driven&gt;替代注解处理器和适配器的配置。

#### Handler

**默认的Handler是基于`@Controller` 和 `@RequestMapping`注解的**，提供了广泛的灵活handling 方法。而`@Controller`又会调用Service和Dao来完成一些业务逻辑处理。`@RequestMapping`注解用于去匹配URL。

**新特性**：  
在Spring4.3还有`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`等直接筛选请求类型的注解。  
例如`@GetMapping("/owners/{ownerId}")`可以在请求路径上设置参数，在函数参数中使用`@PathVariable` String owner获取参数。  
在RFC 3986规范中还可以使用Matrix Variables。还可以使用Consumable Media Types来限制media的类型。

#### Model And View

ModelAndView对象是连接业务逻辑层与view展示层的桥梁，对spring MVC来说它也是连接Handler与view的桥梁。ModelAndView对象顾名思义会持有一个ModelMap对象和一个View对象或者View的名称。这个View的名称知识一个逻辑上的View名称，怎么理解呢，假设我们最后返回一个jsp对象，那么只需在controller中返回success或error，即可，这个是逻辑上的，而实际上的就是返回_/WEB-INF/jsp/success.jsp_或_/WEB-INF/jsp/error.jsp_才是真实的View。

#### ViewResolver

所有的handler都必须明确返回一个ModelAndView对象，其中的View是一个逻辑上的视图，比如明确返回String,View,or ModelAndView或隐式地（基于约定）。逻辑视图由ViewResolver解析，以下是Spring中的拥有的视图解析器：

| ViewResolver | 描述 |
| :--- | :--- |
| _AbstractCachingViewResolver_ | 抽象视图解析器器即caches视图。通常views在使用之前需要准备，扩展此视图解析器是提供缓存。 |
| _XmlViewResolver_ | 实现ViewResolver，它接受XML编写的配置文件（与Spring XML bean相同的DTD），默认的配置的文件是/WEB-INF/view.xml。 |
| _ResourceBundleViewResolver_ | 实现ViewResolver，它使用ResourceBundle中的bean定义，由bundle基本名称指定。默认的文件名是在classpath中的views.properties。 |
| _UrlBasedViewResolver_ | 简单地实现ViewResolver接口，它影响了逻辑视图名到URL的直接解析，而不需要明确的映射定义。适用于逻辑名称和视图资源的直接名称匹配，这种方式不需要映射。 |
| _InternalResourceViewResolver_ | 是UrlBasedViewResolver 的子类，支持InternalResourceView（Servlets和JSP）和子类，例如JstlView和TilesView。也可以公国setViewClass来具体制定那种视图类。 |
| _FreeMarkerViewResolver_ | UrlBasedViewResolver 的子类，支持FreeMarkerView和它的常规子类。 |
| _ContentNegotiatingViewResolver_ | 实现ViewResolver接口，它解析基于请求文件名或Accept 头的视图。 |

例如常见的返回JSP视图的配置如下：

```
<bean id="viewResolver"
                class="org.springframework.web.servlet.view.UrlBasedViewResolver">
        <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
</bean>
```

在controller中返回的逻辑视图名为test，而最终视图的地址为：前缀+逻辑视图名+后缀，所以RequestDispatcher的视图解析器最后返回/WEB-INF/jsp/test.jsp。  
Spring支持多视图解析器，可以在程序上下文中添加多个视图解析器，还可以设置“order”属性来指定匹配优先级。Spring会找到合适的第一个视图解析器来返回结果。如果没有找到视图，则返回null。

#### View

现在业务逻辑处理的数据存放在Model中，返回的View也确定了，最后一步只需将Model中的数据项View中填充即可，最后将数据返回给请求者。

View可以是支持：字符串、JSON Mapping View、JSP & JSTL、XML Marshalling View、Document views \(PDF/Excel\)、Thymeleaf、Groovy Markup Templates、FreeMarker、Script templates、Tiles、Feed View多种格式的视图，DispatcherServlet根据不同handler的不同配置选择不同的View返回。

### 总结

学习Spring MVC先学习工作原理，然后再将工作流程中的细节部分单独深入学习理解，这种学习方式更能深刻掌握Spring MVC。

### 参考：

[springMVC 的工作原理和机制](http://www.cnblogs.com/zbf1214/p/5265117.html)  
[SpringMVC工作原理](http://blog.csdn.net/cswhale/article/details/16941281)  
[SpringMVC 框架介绍](http://www.360doc.com/content/15/0326/14/18924983_458195050.shtml)  
[实例详解Spring MVC入门使用](http://blog.csdn.net/kkdelta/article/details/7274708)

