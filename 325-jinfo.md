jinfo可以输出java进程、core文件或远程debug服务器的配置信息。这些配置信息包括JAVA系统参数及命令行参数,如果进程运行在64位虚拟机上，需要指明`-J-d64`参数，如：`jinfo -J-d64 -sysprops pid`

另外，Java7的官方文档指出，这一命令在后续的版本中可能不再使用。笔者使用的版本\(jdk8\)中已经不支持该命令\(笔者翻阅了[java8中该命令的文档](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/jinfo.html)，其中已经明确说明不再支持\)。提示如下：

```
HollisMacBook-Air:test-workspace hollis$ jinfo 92520
Attaching to process ID 92520, please wait...
^@

Exception in thread "main" java.lang.reflect.InvocationTargetException
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:606)
    at sun.tools.jinfo.JInfo.runTool(JInfo.java:97)
    at sun.tools.jinfo.JInfo.main(JInfo.java:71)
Caused by: sun.jvm.hotspot.runtime.VMVersionMismatchException: Supported versions are 24.79-b02. Target VM is 25.40-b25
    at sun.jvm.hotspot.runtime.VM.checkVMVersion(VM.java:234)
    at sun.jvm.hotspot.runtime.VM.<init>(VM.java:297)
    at sun.jvm.hotspot.runtime.VM.initialize(VM.java:368)
    at sun.jvm.hotspot.bugspot.BugSpotAgent.setupVM(BugSpotAgent.java:598)
    at sun.jvm.hotspot.bugspot.BugSpotAgent.go(BugSpotAgent.java:493)
    at sun.jvm.hotspot.bugspot.BugSpotAgent.attach(BugSpotAgent.java:331)
    at sun.jvm.hotspot.tools.Tool.start(Tool.java:163)
    at sun.jvm.hotspot.tools.JInfo.main(JInfo.java:128)
    ... 6 more
```

由于打印jvm常用信息可以使用[Jps](http://www.hollischuang.com/archives/105)命令，并且在后续的java版本中可能不再支持，所以这个命令笔者就不详细介绍了。下面给出help信息，读者可自行阅读使用。（这就好像上高中，老师讲到一些难点的时候说，不明白也不要紧，知道有这么一回事就可以了！）

### 用法摘要

以键值对的形式打印出JAVA系统参数及命令行参数的名称和内容。

```
-flag name
prints the name and value of the given command line flag.
-flag [+|-]name
enables or disables the given boolean command line flag.
-flag name=value
sets the given command line flag to the specified value.
-flags
prints command line flags passed to the JVM. pairs.
-sysprops
prints Java System properties as name, value pairs.
-h
prints a help message
-help
prints a help message
```

### 参考资料

[jinfo](http://docs.oracle.com/javase/7/docs/technotes/tools/share/jinfo.html)

