# JVM命令参数大全

JVM命令行选项主要有3类：标准选项（eg：-client）、非标准选项（eg：-Xmxsize）、非稳定选项（eg：-XX:+AggressiveOpts） 选项使用说明:  -XX:+option 启用选项  -XX:-option 不启用选项  -XX:option=number 给选项设置一个数字类型值，可跟单位，例如 128k, 256m, 1g  -XX:option=string 给选项设置一个字符串值，例如-XX:HeapDumpPath=./dump.core

| 参数选项 | 含义 |
| :--- | :--- |
| -Xmssize | 设置初始化堆内存大小，这个值的大小必须是1024的倍数，并且大于1M，可以指定单位k\(K\),m\(M\),g\(G\)。例如 -Xms6m。如果没有设置这个值，那么它的初始化大小就是年轻代和老年代的和。等价于-XX:InitialHeapSize。 |
| -Xmxsize | 设置最大堆内存大小，这个值的大小必须是1024的倍数，并且大于2M，可以指定单位k\(K\),m\(M\),g\(G\)。默认值是根据运行时的系统配置来确定的。一般服务器部署时，把-Xms和-Xmx的值设置成相同的大小。-Xmx选项和-XX:MaxHeapSize相同。 |
| -Xmnsize | 设置年轻代大小。可以指定单位k\(K\),m\(M\),g\(G\) .例如-Xmn256m。还可以通过其他两个选项来代替这个选项来指定年轻代最小和最大内存：-XX:NewSize指定初始化大小,-XX:MaxNewSize指定最大内存大小 |
| -XX:NewSize=&lt;n&gt;\[g\|m\|k\] | 年轻代的初始值 |
| -XX:MaxNewSize=&lt;n&gt;\[g\|m\|k\] | 年轻代的最大值 |
| -Xsssize | 设置线程栈的大小。可以指定单位k\(K\),m\(M\),g\(G\)。默认值根据内存而定。 这个选项和-XX:ThreadStackSize相同 |
| -XX:ThreadStackSize=size | 设置线程栈大小。默认值依赖于机器内存。这个选项和-Xss选项的功能相同 |
| -XX:MetaspaceSize=size | 设置元数据空间初始大小 |
| -XX:MaxMetaspaceSize=size | 设置元数据空间最大值 |
| -XX:NewRatio | 设置老生代和新生代大小的比例，比如-XX:NewRatio=2表示1/3的Heap是新生代，2/3的Heap是老生代 |
| -XX:SurvivorRatio | 用来设置新生代中Eden和Survivor空间大小的比例，需要注意的是有两个Survivor。比如-XX:SurvivorRatio=8表示Eden区域在新生代的8/10，两个Survivor分别占1/10 |
| --- | --- |
| -Xnoclassgc | 禁止类的垃圾回收，这个以节省一些GC时间，缩短应用运行时的中断时间。当设置了这个选项的时候，类对象在GC时不会受到任何影响，它会被认为是一直存活的。这就使得更多的内存是永久保留的。如果使用不慎的话就会导致内存溢出的异常。 |
| -Xint | 设置jvm以解释模式运行，所有的字节码将被直接执行，而不会编译成本地码 |
| -Dproperty=value | 设置系统属性值，property表示系统变量的名称，value代表变量的值，如果value中间有空格，那么value需要使用引号括起来\(比如： -Dfoo=”foo bar”\) |
| -verbose:class | 展示出每一个类被加载的信息 |
| -verbose:gc | 展示出每一次垃圾回收事件 |
| -verbose:jni | 展示本地方法的使用，以及本地接口的活动 |
| -Xloggc:filename | 把GC信息输出到文件中，和verbose:gc的内容是一样的。如果这两个命令一起使用的话，Xloggc会覆盖verbose命令 |
| -Xmaxjitcodesize=size | 指定JIT编译代码的最大缓存，单位是字节。 也可以指定单位k\(K\)或m\(M\)。默认的最大缓存是240M。如果禁止分层编译的话，默认大小就是48M。-Xmaxjitcodesize=240m这个选项和-XX:ReservedCodeCacheSize类似 |
| -Xmixed | 使用混合模式运行代码：解释模式和编译模式 |
| -Xverify:mode | 设置字节码验证模式。字节码验证可以帮助我们找到一些问题。mode的参数如下：none—不进行验证。这回节省应用启动时间，同时也减少了java提供的保护。remote—验证那些不是被引导类加载器加载的类。这是默认的设置。all—验证所有的类 |
| -XX:MaxTenuringThreshold=threshold | 在新生代中对象存活次数\(经过Minor GC的次数\)后仍然存活，就会晋升到旧生代 |
| -XX:InitialTenuringThreshold | 表示对象被移到老年代的年龄阈值的初始值 |
| -XX:MaxTenuringThreshold | 表示对象被移到老年代的年龄阈值的最大值 |
| -XX:TargetSurvivorRatio | 表示MinorGC结束了Survivor区域中占用空间的期望比例 |
| --- | --- |
| -XX:+UseParallelGC | 使用 Parallel收集器 |
| -XX:+UseParallelOldGC | 使用 ParallelOld垃圾回收器 |
| -XX:+UseParNewGC | 使用ParNew垃圾回收器 |
| -XX:+UseSerialGC | 使用Serial垃圾回收器 |
| -XX:+UseConcMarkSweepGC | 使用cms垃圾回收器 |
| -XX:+CMSParallelFullGC | 默认关闭。开启后，当应用使用CMS算法时候，如果触发FullGC，可以大幅降低FullGC的停机时间，FullGC线程数可以通过-XX:ParallelGCThreads=x来指定，和CMS的minor gc线程数一致 |
| -XX:+CMSClassUnloadingEnabled | 当使用CMS垃圾收集器时，允许类卸载。默认开启。禁止类卸载可以使用-XX:-CMSClassUnloadingEnabled |
| -XX:CMSExpAvgFactor=percent | 指定垃圾收集消耗的时间百分比。默认这个数是25% |
| -XX:CMSInitiatingOccupancyFraction=percent | 设置CMS收集开始的百分比，当老年代使用率达到该值后开始垃圾收集。默认值是-1，任何的负值表示会使用-XX:CMSTriggerRatio选项来定义这个百分比数 |
| -XX:+UesCMSInitiatingOccupancyOnly | 总是使用CMSInitiatingOccupancyFraction值作为启动CMS周期的阈值 |
| -XX:+CMSScavengeBeforeRemark | 在CMS重新标记之前执行清除操作，默认这个选项是关闭的 |
| -XX:CMSTriggerRatio=percent | 设置由-XX:MinHeapFreeRatio指定值的百分比的值。默认是80% |
| -XX:ConcGCThreads=threads | 发GC的线程数量。默认值根据cpu的数量而定 |
| -XX:+UseG1GC | 使用G1垃圾回收器 |
| -XX:G1HeapRegionSize=size | 当使用G1收集器时，设置java堆被分割的大小。这个大小范围在1M到32M之间。例如把这个值设置成了16M：-XX:G1HeapRegionSize=16m |
| -XX:MaxGCPauseMillis=time | 设置GC最大暂停时间 |
| -XX:G1ReservePercent=percent | 使用g1收集器时，设置保留java堆大小，防止晋升失败。范围是0到50.默认设置是10% |
| -XX:InitialHeapSize=size | 初始化堆大小 |
| -XX:MaxHeapSize=size | 最大堆大小 |
| -XX:MaxNewSize=size | 新生代最大大小 |
| -XX:MaxHeapFreeRatio=percent | 设置堆垃圾回收后最大空闲空间比例。默认是70% |
| -XX:InitialSurvivorRatio=ratio | 设置幸存区的比例 |
| -XX:InitiatingHeapOccupancyPercent=percent | 设置进行垃圾回收的堆占用的百分比 |
| -XX:+UseGCOverheadLimit | 限制GC的运行时间 |
| -XX:+DisableExplicitGC | 这个选项控制显式GC，也就是调用System.gc\(\)，默认在调用这个方法的时候就会发生gc，如果不允许显式gc，那么调用这个方法的时候，就不会发生gc行为。开启该选项后，GC的触发时机将由Garbage Collector全权掌控。注意：你熟悉的代码里没调用System.gc\(\)，不代表你依赖的框架工具没在使用。例如RMI就在多数用户毫不知情的情况下，显示地调用GC来防止自身OOM |
| -XX:+ExplicitGCInvokesConcurrent | 当调用System.gc\(\)的时候，执行并行gc。默认是不开启的，只有使用-XX:+UseConcMarkSweepGC选项的时候才能开启这个选项 |
| -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses | 当调用System.gc\(\)的时候， 执行并行gc。并在垃圾回收的周期内卸载类。 只有使用-XX:+UseConcMarkSweepGC选项的时候才能开启这个选项 |
| --- | --- |
| -XX:+UseTLAB | 启用线程本地缓存区（Thread Local）。1.4.2以前和使用-client选项时，默认不启用，其余版本默认启用 |
| -XX:CompileThreshold=10000 | 通过JIT编译器，将方法编译成机器码的触发阀值，可以理解为调用方法的次数，例如调1000次，将方法编译为机器码，默认1000 |
| -XX:LargePageSizeInBytes=4m | 默认4m，设置堆内存的内存页大小 |
| -XX:+UseBiasedLocking | 启用偏向锁。默认启用 |
| -XX:+UseLargePages | 启用大内存分页，默认启用 |
| -XX:+UseFastAccessorMethods | 优化原始类型的getter方法性能，默认启用 |
| -XX:+UseSpinning | 启用多线程自旋锁优化。默认已启用 |
| -XX:PreBlockSpin=10 | 控制多线程自旋锁优化的自旋次数。-XX:+UseSpinning必须先启用，默认启用，默认自旋10次。关联选项：-XX:+UseSpinning |
| --- | --- |
| -Xloggc:filename | 把GC信息输出到文件中，和verbose:gc的内容是一样的。如果这两个命令一起使用的话，Xloggc会覆盖verbose命令 |
| -XX:+PrintGC | 打印GC信息 |
| -XX:+PrintGCDetails | 打印gc详细信息 |
| -XX:+PrintGCApplicationConcurrentTime | 打印自从上次gc停顿到现在过去了多少时间 |
| -XX:+PrintGCApplicationStoppedTime | 打印gc一共停顿了多长时间 |
| -XX:+PrintGCDateStamps | 打印gc时间 |
| -XX:+PrintGCTimeStamps | 打印gc时间戳 |
| -XX:+PrintGCTaskTimeStamps | 为每个独立的gc线程打印时间戳 |
| -XX:+PrintStringDeduplicationStatistics | 打印字符串去重统计信息 |
| -XX:+PrintTenuringDistribution | 打印各代信息 |
| -XX:+TraceClassLoading | 跟踪类的加载信息,当类加载的时候输入该类，默认关闭 |
| -XX:+TraceClassLoadingPreorder | 按照引用顺序跟踪类加载。默认关闭 |
| -XX:+TraceClassResolution | 跟踪常量池，默认关闭 |
| -XX:+TraceClassUnloading | 跟踪类的卸载信息，默认关闭 |
| -XX:+TraceLoaderConstraints | 跟踪类加载器约束的相关信息，默认关闭 |
| -XX:-PrintCommandLineFlags | 输出JVM设置的选项和值，比如：堆大小、垃圾回收器等。默认这个选项是关闭的 |
| -XX:-CITime | 打印消耗在JIT编译的时间 |
| -XX:+G1PrintHeapRegions | 打印G1收集器收集的区域。默认这个选项是关闭的 |
| -XX:ErrorFile=./hs\_err\_pid.log | 如果JVM crashed，将错误日志输出到指定文件路径 |
| -XX:-HeapDumpOnOutOfMemoryError | 在OOM时，输出一个dump.core文件，记录当时的堆内存快照 |
| -XX:HeapDumpPath=./java\_pid.hprof | 堆内存快照的存储文件路径 |
| -XX:-PrintCompilation | 打印方法被JIT编译时的信息 |
| -XX:+UseGCLogFileRotation | GC日志滚动输出，默认关闭 |
| -XX:NumberOfGCLogFiles=&lt;num\_of\_files&gt; | 指定要滚动输出的GC日志文件个数 |
| -XX:GCLogFileSize=&lt;num\_of\_size&gt; | GC日志文件大小 |

--- \| --- -XX:+FlightRecorder \| Java飞行记录（JFR）。这是一个商业特性，需要和-XX:+UnlockCommercialFeatures选项一起使用 -XX:FlightRecorderOptions=parameter=value \| 设置JFR的参数，来控制JFR的行为， 这是一个商业特性。这个选项仅仅当JFR可用的时候才能使用，也就是需要开启-XX:+FlightRecorder选项 -XX:+UnlockCommercialFeatures \| 开启商业特性，默认关闭 -XX:+UseAppCDS \| 启动应用类数据共享（AppCDS），为了使用AppCDS，必须制定-XX:SharedClassListFile和-XX:SharedArchiveFile两个选项。 属于商业特性，需要指定-XX:+UnlockCommercialFeatures选项

下面的列表展示了所有可用的JFR参数（多个参数用,分隔）：

1. defaultrecording={true\|false}  指定是否在后台一直记录还是只运行一段时间。默认这个参数的值是false，也就是只运行一段时间。如果要一直运行就设置这个参数的值为true。
2. disk={true\|false}  指定JFR是否持续地把记录写到硬盘，默认这个参数值是false（不会持续）。
3. maxchunksize=size  设置数据最大块的大小，可以使用单位后缀k\(K\),m\(M\),g\(G\)。默认值是12M。
4. maxage=time  设置数据最大保留时间，s是秒，m是分钟，h是小时，d是天，默认最大保留时间是15分钟。 仅仅当disk=true 的时候，此选项可用。
5. maxsize=size  设置数据在硬盘的最大容量，可以使用单位后缀k\(K\),m\(M\),g\(G\)。默认容量没有限制。仅仅当disk=true 的时候，此选项可用。
6. repository=path  设置临时仓库，默认使用系统临时路径。
7. dumponexit={true\|false}  指定是否在JVM终止的时候记录JFR数据。默认是false。
8. dumponexitpath=path  指定JVM终止时记录的JFR数据的存储路径。只有设置了defaultrecording=true时这个路径才有意义。如果指定的是一个目录，JVM会把当前的日期和时间作为文件名，如果指定的是一个文件名，而且这个文件已存在，那么就会加上时间后缀。
9. globalbuffersize=size  指定保留数据的总大小。可以使用单位后缀k\(K\),m\(M\),g\(G\)。默认大小是462848 个字节。
10. loglevel={quiet\|error\|warning\|info\|debug\|trace}  指定JFR的日志级别，默认是info。
11. samplethreads={true\|false}  设置是否进行线程抽样，默认这个选项是true；
12. settings=path  设置事件配置文件，默认使用default.jfc.这个文件在JAVA\_HOME/jre/lib/jfr
13. stackdepth=depth  栈追踪的深度，默认深度是64个方法调用，最大是2048，最小是1。
14. threadbuffersize=size  指定每个线程的本地缓冲小大，可以使用单位后缀k\(K\),m\(M\),g\(G\)。值越大就说明在写磁盘之前可以容纳更多的数据。默认大小是5K。



