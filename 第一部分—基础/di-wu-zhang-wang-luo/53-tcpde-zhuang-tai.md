在TCP层，有个FLAGS字段，这个字段有以下几个标识：SYN, FIN, ACK, PSH, RST, URG.

其中，对于我们日常的分析有用的就是前面的五个字段。

它们的含义是：

SYN表示建立连接，

FIN表示关闭连接，

ACK表示响应，  
PSH表示有 DATA数据传输，

RST表示连接重置。

其中，ACK是可能与SYN，FIN等同时使用的，比如SYN和ACK可能同时为1，它表示的就是建立连接之后的响应，如果只是单个的一个SYN，它表示的只是建立连接。

TCP的几次握手就是通过这样的ACK表现出来的。

但SYN与FIN是不会同时为1的，因为前者表示的是建立连接，而后者表示的是断开连接。

RST一般是在FIN之后才会出现为1的情况，表示的是连接重置。

一般地，当出现FIN包或RST包时，我们便认为客户端与服务器端断开了连接；而当出现SYN和SYN＋ACK包时，我们认为客户端与服务器建立了一个连接。

PSH为1的情况，一般只出现在 DATA内容不为0的包中，也就是说PSH为1表示的是有真正的TCP数据包内容被传递。

TCP的连接建立和连接关闭，都是通过请求－响应的模式完成的。

概念补充-TCP三次握手：

TCP\(Transmission Control Protocol\)传输控制协议

TCP是主机对主机层的传输控制协议，提供可靠的连接服务，采用三次握手确认建立一个连接：

位码即tcp标志位，有6种标示：SYN\(synchronous建立联机\) ACK\(acknowledgement 确认\) PSH\(push传送\) FIN\(finish结束\) RST\(reset重置\) URG\(urgent紧急\)Sequence number\(顺序号码\) Acknowledge number\(确认号码\)

第一次握手：主机A发送位码为syn＝1，随机产生seq number=1234567的数据包到服务器，主机B由SYN=1知道，A要求建立联机；

第二次握手：主机B收到请求后要确认联机信息，向A发送ack number=\(主机A的seq+1\)，syn=1，ack=1，随机产生seq=7654321的包；

第三次握手：主机A收到后检查ack number是否正确，即第一次发送的seq number+1，以及位码ack是否为1，若正确，主机A会再发送ack number=\(主机B的seq+1\)，ack=1，主机B收到后确认seq值与ack=1则连接建立成功。

完成三次握手，主机A与主机B开始传送数据。

在TCP/IP协议中，TCP协议提供可靠的连接服务，采用三次握手建立一个连接。

第一次握手：建立连接时，客户端发送syn包\(syn=j\)到服务器，并进入SYN\_SEND状态，等待服务器确认；

第二次握手：服务器收到syn包，必须确认客户的SYN（ack=j+1），同时自己也发送一个SYN包（syn=k），即SYN+ACK包，此时服务器进入SYN\_RECV状态；

第三次握手：客户端收到服务器的SYN＋ACK包，向服务器发送确认包ACK\(ack=k+1\)，此包发送完毕，客户端和服务器进入ESTABLISHED状态，完成三次握手。完成三次握手，客户端与服务器开始传送数据.

