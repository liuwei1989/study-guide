资源（File、URL、Classpath 等等）是众多框架使用和运行的基础，Spring 当然也不例外，在具体探究 Spring 容器的设计实现之前，我们先来做一些准备工作，对于容器运行基础的资源描述做一个简单的了解。

### **一. 资源的抽象声明** {#h3_0}

在 java 中，资源被抽象成 URL，通过注册相应的 handler 来处理不同资源的操作逻辑，而 Spring 则采用 Resource 来对各种资源进行统一抽象，`org.springframework.core.io.Resource`是一个接口，定义了资源的基本操作，包括是否存在、是否可读、是否已经打开等等。

Resource 接口声明如下：

```
public interface InputStreamSource {
    /** 返回一个新的输入流 */
    InputStream getInputStream() throws IOException;
}
```

```
public interface Resource extends InputStreamSource {
    /** 资源是否存在 */
    boolean exists();
    /** 资源是否可读 */
    boolean isReadable();
    /** 资源流是否打开 */
    boolean isOpen();
    /** 返回资源的URL */
    URL getURL() throws IOException;
    /** 返回资源的URI */
    URI getURI() throws IOException;
    /** 返回资源的File对象 */
    File getFile() throws IOException;
    /** 返回文件的长度 */
    long contentLength() throws IOException;
    /** 返回文件上次被修改的时间戳 */
    long lastModified() throws IOException;
    /** 依据当前资源创建一个相对的资源，并返回资源对象 */
    Resource createRelative(String relativePath) throws IOException;
    /** 返回资源的文件名 */
    String getFilename();
    /** 返回资源的描述信息 */
    String getDescription();
}
```

由继承关系可以看到 Resource 继承了`org.springframework.core.io.InputStreamSource`，InputStreamSource 封装任何可以返回 InputStream 的类，并仅声明了一个方法：getInputStream\(\)，用于返回一个新的 InputStream 对象。

Resource 本身则抽象了资源的基本操作，Spring 也针对不同的资源定义了相应的类实现，比如：文件（FileSystemResource）；Byte数组资源（ByteArrayResource）；ClassPath资源（ClassPathResource）；URL资源（UrlResource）等等，具体如下图：

![](https://static.oschina.net/uploads/img/201706/04152217_0DJa.png "spring-resource")

### **二. 资源的具体定义** {#h3_1}

下面来分别看看每一种 resource 的定义，我们按照层次由上到下一层层来看：

#### **2.1 第一层** {#h4_2}

这一层主要涉及如下 5 个类或接口：

> 1. org.springframework.web.servlet.resource.EncodedResource
> 2. org.springframework.web.servlet.resource.VersionedResource
> 3. org.springframework.core.io.WritableResource
> 4. org.springframework.core.io.AbstractResource
> 5. org.springframework.core.io.ContextResource

这些资源接口，有些已经派生出了多个类，有些则还没有具体的实现，留给以后的版本，以及用户去自定义扩展。

> * org.springframework.web.servlet.resource.EncodedResource

EncodedResource 在 4.1 版本中引入，正如其名，是与编码相关的资源定义，该接口仅声明了一个方法 getContentEncoding\(\)，用来获取资源内容编码，编码形式可以参考 HTTP/1.1 对**Content-Encoding**首部的定义。

> Content-Encoding
>
> 该首部用于说明是否对某对象进行了编码，并告知客户端对对象进行了哪种或哪些类型的编码，客户端可以据此进行适当的解码操作。
>
> * 类型：实体首部
> * 基本语法：Content-Encoding: 1\# content-coding

上面是对 HTTP/1.1 中的 Content-Encoding 首部的简单介绍。

> * org.springframework.web.servlet.resource.VersionedResource

VersionedResource 在 4.2.5 版本引入，用于对资源的版本进行描述，该接口仅声明了一个方法 getVersion\(\)，用于获取版本信息。

> * org.springframework.core.io.WritableResource

WritableResource 接口用于描述一个资源是否支持可写的特性，在基本资源接口定义 Resource 中，仅描述了一个资源是否可读，因为可读相对于可写是更加基本的特性，而对于可读又可写的文件来说，WritableResource 接口进一步完善描述了其资源特性。该接口声明了两个方法 isWritable\(\) 和 getOutputStream\(\)，前者用于返回文件是否可写，后者则返回可写文件的 OutputStream。

> * org.springframework.core.io.AbstractResource

AbstractResource 不是对某一特性的描述，而是一种编程技巧，Resource 中声明了资源的多种操作，如果我们直接去实现 Resource 接口，势必要提供针对每一个方法的实现，而这些方法并不是全部都需要提供支持，所以 AbstractResource 对所有方法提供了默认实现，通过继承 AbstractResource，我们可以针对性的选择实现我们需要的方法。

> * org.springframework.core.io.ContextResource

ContextResource 是在 2.5 版本引入的一个扩展接口，用于描述从上下文环境中加载的资源，该接口仅声明了一个方法 getPathWithinContext\(\)，用于获取上下文环境的相对路径。

#### **2.2 第二层** {#h4_3}

第二层主要派生自 AbstractResource，包括：

> 1. org.springframework.core.io.FileSystemResource
> 2. org.springframework.core.io.PathResource
> 3. org.springframework.core.io.ByteArrayResource
> 4. org.springframework.core.io.VfsResource
> 5. org.springframework.core.io.InputStreamResource
> 6. org.springframework.core.io.DescriptiveResource
> 7. org.springframework.beans.factory.support.BeanDefinitionResource
> 8. org.springframework.core.io.AbstractFileResolvingResource

这些资源类全部继承自 AbstractResource，并依据自己的特性，对 Resource 中声明的方法做了选择性的实现。

> * org.springframework.core.io.FileSystemResource

FileSystemResource 是对文件系统类型资源的描述，这也是 Spring 中典型的资源类型（这个类由 Spring 共同创始人 Juergen Hoeller 写于2003年12月28号），该类继承自 AbstractResource，并实现了 WritableResource 接口。

FileSystemResource 提供了两个构造方法分别由`java.io.File`对象和文件路径来构造资源对象，对于传入的路径 path，由于输入的不确定性，会利用 StringUtils.cleanPath\(path\) 对其进行格式化。FileSystemResource 实现的资源接口如下：

```
public class FileSystemResource extends AbstractResource implements WritableResource {
    /** 文件对象 */
    private final File file;
    /** 文件路径 */
    private final String path;

    // 构造函数省略

    public final String getPath();
    public boolean exists();
    public boolean isReadable();
    public InputStream getInputStream() throws IOException;
    public boolean isWritable();
    public OutputStream getOutputStream() throws IOException;
    public URL getURL() throws IOException;
    public URI getURI() throws IOException;
    public File getFile();
    public long contentLength() throws IOException;
    public Resource createRelative(String relativePath);
    public String getFilename();
    public String getDescription();
}
```

FileSystemResource 中的函数实现几乎都依赖于`java.io.File`的方法。这里提一下 createRelative\(String relativePath\)，该方法基于当前路径创建相对资源，方法源码如下：

```
public Resource createRelative(String relativePath) {
    String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
    return new FileSystemResource(pathToUse);
}
```

方法先利用 StringUtils.applyRelativePath\(this.path, relativePath\) 方法来创建资源绝对路径，主要操作是截取 path 的最后一个文件分隔符 ‘/’ 前面的内容与 relativePath 拼接，然后基于新的路径构造资源对象。

> org.springframework.core.io.PathResource

PathResource 在 4.0 版本引入，是基于 jdk1.7 NIO 2.0 中的类`java.nio.file.Path`所实现的资源类型。NIO 2.0 针对本地 I/O 引入了许多新的类（如下所示），用来改变 java 在 I/O 方面一直被人诟病的慢特性。

> import java.nio.file.DirectoryStream;  
> import java.nio.file.FileSystem;  
> import java.nio.file.FileSystems;  
> import java.nio.file.Files;  
> import java.nio.file.Path;  
> import java.nio.file.Paths;  
> import java.nio.file.attribute.FileAttribute;  
> import java.nio.file.attribute.PosixFilePermission;  
> import java.nio.file.attribute.PosixFilePermissions;

所以 PathResource 也表示 Spring 由 BIO 向 NIO 的迈进。

> * org.springframework.core.io.ByteArrayResource

ByteArrayResource 利用字节数组作为资源存储的标的，jdk 原生也提供了字节数组式的 I/O 流，所以二者在设计思想是想通的。

> * org.springframework.core.io.VfsResource

VfsResource 对[JBoss Virtual File System \(VFS\)](https://developer.jboss.org/wiki/VFS3UserGuide)提供了支持，针对 JBoss VFS 的说明，官网简介如下：

> The Virtual File System \(VFS\) framework is an abstraction layer designed to simplify the programmatic access to file system resources. One of the key benefits of VFS is to hide certain file system details and allow for file system layouts that are not required to reflect a real file system. This allows for great flexibility and makes it possible to navigate arbitrary structures \(ex. archives\) as though they are part of a single file system.

具体没用过，不多做解释。

> * org.springframework.core.io.InputStreamResource

InputStreamResource 基于给定的 InputStream 来创建资源，流是一般文件的更低一层，程序设计的共性就是越往底层走需要考虑的问题就越多，所以 Spring 明确表示，如果有相应的上层实现则不推荐直接使用 InputStreamResource。

> * org.springframework.core.io.DescriptiveResource

DescriptiveResource 资源并不是一个真实可读的资源，而是对文件的一种描述，所以这类资源的 exists\(\) 方法始终返回 false。这类资源的作用是在必要的时候用来占坑，比如文档所说的，当一个方法需要你传递一个资源对象，但又不会在方法中真正读取该对象的时候，如果没有合适的资源对象作为参数，就创建一个 DescriptiveResource 资源做参数吧。

> * org.springframework.beans.factory.support.BeanDefinitionResource

BeanDefinitionResource 是对 BeanDefinition 对象的一个包装，BeanDefinition 对象是 Spring 核心类之一，是对 bean 定义在内存中进行描述的数据结构，我们在配置文件中定义的 bean，经过加载之后都会以 BeanDefinition 对象的形式存储在容器中，具体加载过程后续会有专门文章讲解。

这里的 BeanDefinitionResource 仅仅是对 BeanDefinition 对象的持有，并提供返回的 get 方法，而一般资源操作方法几乎都不支持。

> * org.springframework.core.io.AbstractFileResolvingResource

AbstractFileResolvingResource 是解析 URL 所指代的文件为`java.io.File`对象的抽象资源类，具体的实现典型的有 UrlResource 和 ClassPathResource。AbstractFileResolvingResource的接口定义如下：

```
public abstract class AbstractFileResolvingResource extends AbstractResource {
    /** 解析 url 获取 File 对象 */
    public File getFile() throws IOException;
    /** 解析指代底层文件的 url 为 File 对象，比如压缩包中的文件 */
    protected File getFileForLastModifiedCheck() throws IOException;
    /** 解析 URI 所指代的 File 对象 */
    protected File getFile(URI uri) throws IOException;
    public boolean exists()
    public boolean isReadable();
    public long contentLength() throws IOException;
    public long lastModified() throws IOException;
}
```

我们来看一下 getFile\(\) 和 getFileForLastModifiedCheck\(\) 函数的实现，getFile\(URI uri\) 类似于 getFile\(\)：

```
public File getFile() throws IOException {
    URL url = this.getURL(); // 获取 url
    if (url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
        // 如果是 JBoss VFS 文件
        return VfsResourceDelegate.getResource(url).getFile();
    }
    return ResourceUtils.getFile(url, getDescription());
}
```

getFile\(\) 解析 url 为 File 对象，方法首先调用 getURL\(\) 获取 url 对象，然后检查当前 url 是不是 JBoss VFS 文件，如果是则走 VFS 文件解析策略，否则就调用工具类`org.springframework.util.ResourceUtils`的 getFile\(URL resourceUrl, String description\) 进行解析，解析过程如下：

```
public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
    Assert.notNull(resourceUrl, "Resource URL must not be null");
    if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
        // url 不是 file 协议，说明不是指代文件的 url
        throw new FileNotFoundException(
                description + " cannot be resolved to absolute file path because it does not reside in the file system: " + resourceUrl);
    }
    try {
        // 由 url 对象 构造 File 对象返回
        return new File(toURI(resourceUrl).getSchemeSpecificPart());
    } catch (URISyntaxException ex) {
        // Fallback for URLs that are not valid URIs (should hardly ever happen).
        return new File(resourceUrl.getFile());
    }
}
```

getFileForLastModifiedCheck\(\) 相对于 getFile\(\) 提供了对压缩文件 url 路径的解析：

```
protected File getFileForLastModifiedCheck() throws IOException {
    URL url = this.getURL();
    if (ResourceUtils.isJarURL(url)) {
        // 如果 URL 的协议是 jar, zip, vfszip, wsjar 之一
        URL actualUrl = ResourceUtils.extractArchiveURL(url);  // 解析 url
        if (actualUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
            // 如果是 JBoss VFS 文件
            return VfsResourceDelegate.getResource(actualUrl).getFile();
        }
        return ResourceUtils.getFile(actualUrl, "Jar URL");
    } else {
        // 不是 jar url
        return this.getFile();
    }
}
```

方法首先获取 url 对象，然后判断是不是 jar url，如果不是就走之前的 getFile\(\) 进行常规解析，如果是，即当前 url 的协议是jar, zip, vfszip, wsjar 之一，则首先解析 url 得到常规 url 对象，然后执行和 getFile\(\) 相同的逻辑。

#### **2.3 第三层** {#h4_4}

> 1. org.springframework.web.servlet.resource.TransformedResource
> 2. org.springframework.core.io.UrlResource
> 3. org.springframework.core.io.ClassPathResource
> 4. org.springframework.web.portlet.context.PortletContextResource
> 5. org.springframework.web.context.support.ServletContextResource

第三层主要是对第二层对象的进一步实现，包括 ByteArrayResource、AbstractFileResolvingResource，以及 ContextResource。

> * org.springframework.web.servlet.resource.TransformedResource

TransformedResource 继承自 ByteArrayResource，在 4.1 版本引入，相对于 ByteArrayResource 增加了对文件名和上次修改时间戳的描述。

> * org.springframework.core.io.UrlResource

UrlResource 是对 AbstractFileResolvingResource 的实现，这里的 URL 是常规 URL 对象的一个子集，主要考虑的是 “**file:**” 协议。UrlResource 提供了多种构造方法：

```
public UrlResource(URI uri) throws MalformedURLException;
public UrlResource(URL url);
public UrlResource(String path) throws MalformedURLException;
public UrlResource(String protocol, String location) throws MalformedURLException;
public UrlResource(String protocol, String location, String fragment) throws MalformedURLException;
```

> * org.springframework.core.io.ClassPathResource

ClassPathResource 是比较常用的一种资源，继承自 AbstractFileResolvingResource，是对类上下文环境内资源的一种描述，基于 ClassLoader 或 Class 来定位加载资源。

整个类的核心在于如何依据 ClassLoader 或 Class 进行资源定位，这归功于 resolveURL\(\) 方法，源码如下：

```
protected URL resolveURL() {
    if (this.clazz != null) {
        return this.clazz.getResource(this.path);
    } else if (this.classLoader != null) {
        return this.classLoader.getResource(this.path);
    } else {
        return ClassLoader.getSystemResource(this.path);
    }
}
```

方法的实现还是比较简单的，主要是基于类加载器去检索资源路径。

> * org.springframework.web.portlet.context.PortletContextResource

PortletContextResource 继承自 AbstractFileResolvingResource，实现了 ContextResource 接口，提供了对`javax.portlet.PortletContext`的支持。

> * org.springframework.web.context.support.ServletContextResource

ServletContextResource 同样继承自 AbstractFileResolvingResource，实现了 ContextResource 接口，提供了对`javax.servlet.ServletContext`的支持。

### **三. 资源的加载** {#h3_5}

Spring 为资源定义了一套专门的资源加载接口（类继承关系见下图），ResourceLoader 接口是整个继承体系的基础，该接口声明了两个方法（如下所示），getResource 用于获取指定的地址的资源对象，getClassLoader 则返回当前 ResourceLoader 所使用的类加载器，一些时候我们可能需要基于该类加载器执行一些相对定位操作：

```
public interface ResourceLoader {

    Resource getResource(String location);

    ClassLoader getClassLoader();

}
```

我们可以将所有的接口分为加载器和解析器两大类，加载器的作用不言而喻，对于解析器而言，由前面的分析我们知道 Spring 定义了多种资源类型，Spring 支持对于特定资源类型的解析并加载返回对应的资源对象。

![](https://static.oschina.net/uploads/img/201706/04152105_k5eh.png "spring-reaource-loader")

在日常使用过程中，我们通常都是以 Ant 风格来配置资源路径，这给我们的配置带来了极大的灵活性，而正是 PathMatchingResourcePatternResolver 为 Ant 风格的配置提供了支持，路径的解析本质上依赖于各种规则，Ant 风格也不例外，有兴趣的同学可以自己阅读一下 PathMatchingResourcePatternResolver 的路径解析过程。

