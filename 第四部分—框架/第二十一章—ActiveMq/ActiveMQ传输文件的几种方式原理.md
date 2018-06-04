本文讨论ActiveMQ传输文件的几种方法的原理及其利弊，作为消息发送、直接传输文件、使用ftp或http中转。最后介绍扩展ActiveMQ实现自定义文件传输方式，讨论如何实现高效的文件传输。by kimmking

# 作为消息发送

按照JMS规范，为了保证可靠性，所有的消息都应该是发送到broker，然后交由broker来投递的。也即是说其实JMS是不建议或不支持传输文件的。

对于比较小的文件，简单的处理方式是先读取所有的文件成byte\[\]，然后使用ByteMessage，把文件数据发送到broker，像正常的message一样处理。对于大文件，例如1GB以上的文件，这么搞直接把client或是broker给oom掉了。

这种方式仅仅适用于小文件的传输。特别是如果broker端使用数据库作为存储，message序列化以后存放于blob字段，文件传输频繁或是稍微有点大，写入效率极低。

# 直接传输文件

为了解决传输大文件的问题，ActiveMQ在jms规范之外引入了jms streams的概念。PTP模式下，连到同一个destination的两端，可以通过broker中转来传输大文件。

发送端使用connection.createOutputStream打开一个输出流，往流里写文件。

OutputStream out =connection.createOutputStream\(destination\);

接收端则简单的使用connection.createInputStream拿到一个输入流，从中读取文件数据即可。

```
InputStream in = connection.createInputStream(destination)
```

详见：[http://activemq.apache.org/jms-streams.html](http://activemq.apache.org/jms-streams.html)

使用非常简单。ActiveMQ在中间做了什么事情呢？

其实过程蛮曲折的，发送端拿到文件后，首先分片，默认64K文件数据为一个byte message，然后依次把所有的message发送到broker，broker转发给接收端，最后发送一个空消息作为结束符。

![](http://img.my.csdn.net/uploads/201212/20/1356006414_7378.jpg)  


connection上提供了两个创建OutputStream的方法，一个是createOutputStream创建的是持久化的消息集合，这些数据会写到磁盘或是数据库（对大文件来说慢消费也是一件可怕的事儿）；一个是createNonPersistOutputStream创建的是非持久化消息集合，不会写到磁盘上，如果没有及时消费掉就惨了。

文件片段的byte message的TTL设置为0，就是不会超时进入DLQ。

**优势**：简单直接，处理非常小（不大于64K）的文件非常方便。

**劣势**：对大文件，简直就是噩梦。

# 文件中转方式

使用消息的方式来传递大文件，明显不是一个有效率的办法。文件应该就是按文件的方式去处理。

## 自己处理中转

如果自己处理文件的话，一个简单方式是使用共享或ftp、dfs等方式，先把文件发送到一个大家都可以拿到的地方，然后发送message，payload或properties中包含文件的路径信息。这样，consumer拿到文件路径后去指定的地方，按照给定的方式去获取文件数据即可。

**优势**：这种方式可以用来处理大数据，并且不需要client或broker在内存中持有文件数据本身，非常的节省资源。而且文件是通过额外的方式处理，跟ActiveMQ本身无关，所以符合jms协议、处理的效率也相对比较高。

**劣势**：需要自己处理很多文件相关的操作。

## BlobMessage对文件中转的封装

幸运的是，ActiveMQ把上面繁复的文件处理工作进行了封装，屏蔽掉文件中转的整个处理过程，使得我们可以使用类似jms规范的API来简单操作文件传输。

举个例子来说，典型的使用步骤：

发送端：

1.        启动ActiveMQ时，也启动jetty\(即activemq.xml中有import jetty.xml\)，此时jetty中运行了一个ActiveMQ自带的http文件服务器

2.        使用tcp://localhost:61616?jms.blobTransferPolicy.defaultUploadUrl=http://localhost:8161/fileserver/创建connection，然后创建session和producer

3.        使用如下代码发送文件：

BlobMessageblobMessage = session.createBlobMessage\(file\); 

blobMessage.setStringProperty\("FILE.NAME",file.getName\(\)\); 

blobMessage.setLongProperty\("FILE.SIZE",file.length\(\)\); 

producer.send\(blobMessage\); 

接收端比较简单，正常的使用jms接收到消息：

InputStream inputStream = blobMessage.getInputStream\(\);

然后直接读取文件数据即可。文件名和文件大小可以从message的属性中拿到。



这个过程中ActiveMQ做了什么呢？

发送端：producer.send的时候，把文件通过http协议的PUT方法发到jetty中的fileserver（默认128K走http的chunk分片传输）。然后把http的url写入消息中。再把消息发送到broker。

接收端：接收到消息以后，发现是BlobMessage，拿到url，直接使用GET方法获取文件数据。处理完毕后，使用DELETE方法从fileserver删除文件。

![](http://img.my.csdn.net/uploads/201212/20/1356006452_4547.jpg)  


BlobMessage支持3种文件中转方式：

**FILE**

         要求client和broker在同一个机器或者使用同一个共享存储。发送文件的时候，把文件从本地写入到指定路径。接收文件的时候，把文件从此路径读出来。

**HTTP**

         使用http的fileserver，PUT/GET/DELETE方法。ActiveMQ自带了简单的实现。就是前面场景中使用的方式。

**FTP**

         使用一个独立的ftpserver作为文件中转方式。发送文件的时候，把文件发送到ftp服务器。接收文件的时候，从ftp把文件读取下来。



详见：[http://activemq.apache.org/blob-messages.html](http://activemq.apache.org/blob-messages.html)



**优势**：消息处理与文件处理传输分开，极大的提高了文件传输的效率。而且可以使用类似jms协议的方式来处理文件发送。

**劣势**：FILE方式不太实用。HTTP和FTP方式都需要额外的fileserver。



# 自定义文件传输方式

ActiveMQ实现BlobMessage的三种文件中转方式时，使用了Façade和Strategy模式。ActiveMQBlobMessage需要在发送消息时使用blobUploader上传文件、接收消息时使用blobDownloader下载文件。每种中转方式的这两个操作分别使用BlobUploadStrategy和BlobDownloadStrategy封装。

所以，我们也可以根据ActiveMQBlobMessage文件传送的原理，实现自己的定制方式：

1、  给ActiveMQBlobMessage添加自己的blobDownloader和blobUploader来实现文件的处理。

2、  扩展这个文件中转机制，实现BlobUploadStrategy和BlobDownloadStrategy。



一个更高效且不需要额外fileserver的实现思路是：broker上再打开一个tcp的监听端口，用来接收和转发文件，当发送和接收端都在时，broker仅仅作为一个传输代理。接收端不在时，broker把数据存为本地临时文件，处理完毕后删除掉。之所以使用一个新的端口来传输文件数据而不是已有的transport，是为了避免jms streams这种命令和数据混合的模式。可以参考ftp协议，作为一种高效的文件传输协议，它有一个很大的特点就是命令的处理，和数据传输的处理使用不同的端口和连接。而ActiveMQ使用的openwire协议其实就是一个个的操作命令。文件分片、包装、序列化，到另一头再反向这个过程，无疑是效率很低下的。

