## **目标** {#id-服务器JVMGC规范配置-目标}

**规范线上jvm gc相关配置，打印gc log，服务OOM时dump，方便线上问题排查**



-verbose:gc -Xloggc:/opt/web/logs/${module}/${module}.gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/web/logs/${module}/dump

其中${module}为服务包名，可以自定义，能标识自己的服务即可

**Demo：**

-server -Xms1g -Xmx1g -Xmn512m -Xss1024K -XX:PermSize=256m -XX:MaxPermSize=512m -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:SurvivorRatio=4 -XX:MaxTenuringThreshold=10 -XX:CMSInitiatingOccupancyFraction=80 -verbose:gc -Xloggc:/opt/web/logs/tomcat7\_server/tomcat7\_server.gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=/opt/web/logs/tomcat7\_server/dump



**备注:一开始需要创建jvm log目录：/opt/web/logs/${module}/dump**

