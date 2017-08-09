iptables -F

iptables -X

iptables -F -t mangle

iptables -t mangle -X

iptables -F -t nat

iptables -t nat -X

首先，把三个表清空，把自建的规则清空。



iptables -P INPUT DROP

iptables -P OUTPUT DROP

iptables -P FORWARD ACCEPT

设定INPUT、OUTPUT的默认策略为DROP，FORWARD为ACCEPT。



iptables -A INPUT -i lo -j ACCEPT

iptables -A OUTPUT -o lo -j ACCEPT

先把“回环”打开，以免有不必要的麻烦。



iptables -A INPUT -i eth+ -p icmp --icmp-type 8 -j ACCEPT

iptables -A OUTPUT -o eth+ -p icmp --icmp-type 0 -j ACCEPT

在所有网卡上打开ping功能，便于维护和检测。



iptables -A INPUT -i eth0 -s 192.168.100.250 -d 192.168.100.1 -p tcp --dport 22 -j ACCEPT

iptables -A OUTPUT -o eth0 -d 192.168.100.250 -s 192.168.100.1 -p tcp --sport 22 -j ACCEPT

打开22端口，允许远程管理。（设定了很多的附加条件：管理机器IP必须是250，并且必须从eth0网卡进入）



iptables -A INPUT -i eth0 -s 192.168.100.0/24 -p tcp --dport 3128 -m state --state NEW,ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth0 -d 192.168.100.0/24 -p tcp --sport 3128 -m state --state ESTABLISHED -j ACCEPT

iptables -A INPUT -i eth1 -s 192.168.168.0/24 -p tcp --dport 3128 -m state --state NEW,ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth1 -d 192.168.168.0/24 -p tcp --sport 3128 -m state --state ESTABLISHED -j ACCEPT

iptables -A INPUT -i eth2 -p tcp --dport 32768:61000 -m state --state ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth2 -p tcp --sport 32768:61000 -m state --state NEW,ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth2 -p udp --dport 53 -j ACCEPT

iptables -A INPUT -i eth2 -p udp --sport 53 -j ACCEPT

上面这几句是比较头痛的，我做逐一解释。

iptables -A INPUT -i eth0 -s 192.168.100.0/24 -p tcp --dport 3128 -m state --state NEW,ESTABLISHED -j ACCEPT

允许192.168.100.0/24网段的机器发送数据包从eth0网卡进入。如果数据包是tcp协议，而且目的端口是3128（因为REDIRECT已经把80改为3128了。nat表的PREROUTING是在filter表的INPUT前面的。）的，再而且，数据包的状态必须是NEW或者ESTABLISHED的（NEW代表tcp三段式握手的“第一握”，换句话说就是，允许客户端机器向服务器发出链接申请。ESTABLISHED表示通过握手已经建立起链接），通过。



iptables -A OUTPUT -o eth2 -p tcp --sport 32768:61000 -m state --state NEW,ESTABLISHED -j ACCEPT

我们先来看这一句。现在你的数据包已经进入到linux服务器防火墙上来了。squid需要代替你去访问，所以这时，服务器就成了客户端的角色，所以它要使用32768到61000的私有端口进行访问。（大家会奇怪应该是1024到65535吧。其实CentOS版的linux所定义的私有端口是32768到61000的，你可以通过cat /proc/sys/net/ipv4/ip\_local\_port\_range，查看一下。）再次声明：这里是squid以客户端的身份去访问其他的服务器，所以这里的源端口是32768:61000，而不是3128！



iptables -A INPUT -i eth2 -p tcp --dport 32768:61000 -m state --state ESTABLISHED -j ACCEPT

当然了，数据有去就有回。



iptables -A OUTPUT -o eth0 -d 192.168.100.0/24 -p tcp --sport 3128 -m state --state ESTABLISHED -j ACCEPT

数据包还得通过服务器，转到内网网卡上。请注意，这里，是squid帮你去访问了你想要访问的网站。所以在内网中，你的机器是客户端角色，而squid是服务器角色。这与刚才对外访问的过程是不同的。所以在这里，源端口是3128，而不是32768:61000。



iptables -A OUTPUT -o eth2 -p udp --dport 53 -j ACCEPT

iptables -A INPUT -i eth2 -p udp --sport 53 -j ACCEPT

当然，DNS是不可缺少的。



iptables -A INPUT -i eth+ -p tcp --dport 80 -j LOG --log-prefix "iptables\_80\_alert" --log-level info

iptables -A INPUT -i eth+ -p tcp --dport 21 -j LOG --log-prefix "iptables\_21\_alert" --log-level info

iptables -A INPUT -i eth+ -p tcp --dport 22 -j LOG --log-prefix "iptables\_22\_alert" --log-level info

iptables -A INPUT -i eth+ -p tcp --dport 25 -j LOG --log-prefix "iptables\_25\_alert" --log-level info

iptables -A INPUT -i eth+ -p icmp --icmp-type 8 -j LOG --log-prefix "iptables\_icmp8\_alert" --log-level info

当然了，来点日志记录会对网管员有所帮助。



iptables 基本命令使用举例

一、链的基本操作

1、清除所有的规则。

1）清除预设表filter中所有规则链中的规则。

\# iptables -F

2）清除预设表filter中使用者自定链中的规则。

\#iptables -X

\#iptables -Z

2、设置链的默认策略。一般有两种方法。

1）首先允许所有的包，然后再禁止有危险的包通过放火墙。

\#iptables -P INPUT ACCEPT

\#iptables -P OUTPUT ACCEPT

\#iptables -P FORWARD ACCEPT

2）首先禁止所有的包，然后根据需要的服务允许特定的包通过防火墙。

\#iptables -P INPUT DROP

\#iptables -P OUTPUT DROP

\#iptables -P FORWARD DROP

3、列出表/链中的所有规则。默认只列出filter表。

\#iptables -L

4、向链中添加规则。下面的语句用于开放网络接口：

\#iptables -A INPUT -i lo -j ACCEPT

\#iptables -A OUTPUT -o lo -j ACCEPT

\#iptables -A INPUT -i eth0 -j ACEPT

\#iptables -A OUTPUT -o eth1 -j ACCEPT

\#iptables -A FORWARD -i eth1 -j ACCEPT

\#iptables -A FORWARD -0 eth1 -j ACCEPT

注意:由于本地进程不会经过FORWARD链，因此回环接口lo只在INPUT和OUTPUT两个链上作用。

5、使用者自定义链。

\#iptables -N custom

\#iptables -A custom -s 0/0 -d 0/0 -p icmp -j DROP

\#iptables -A INPUT -s 0/0 -d 0/0 -j DROP

二、设置基本的规则匹配

1、指定协议匹配。

1）匹配指定协议。

\#iptables -A INPUT -p tcp

2）匹配指定协议之外的所有协议。

\#iptables -A INPUT -p !tcp

2、指定地址匹配。

1）指定匹配的主机。

\#iptables -A INPUT -s 192.168.0.18

2）指定匹配的网络。

\#iptables -A INPUT -s 192.168.2.0/24

3）匹配指定主机之外的地址。

\#iptables -A FORWARD -s !192.168.0.19

4）匹配指定网络之外的网络。

\#iptables -A FORWARD -s ! 192.168.3.0/24

3、指定网络接口匹配。

1）指定单一的网络接口匹配。

\#iptables -A INPUT -i eth0

\#iptables -A FORWARD -o eth0

2）指定同类型的网络接口匹配。

\#iptables -A FORWARD -o ppp+

4、指定端口匹配。

1）指定单一端口匹配。

\#iptables -A INPUT -p tcp --sport www

\#iptables -A INPUT -p udp –dport 53

2）匹配指定端口之外的端口。

\#iptables -A INPUT -p tcp –dport !22

3）匹配端口范围。

\#iptables -A INPUT -p tcp –sport 22:80

4）匹配ICMP端口和ICMP类型。

\#iptables -A INOUT -p icmp –icimp-type 8

5）指定ip碎片。

每

个网络接口都有一个MTU（最大传输单元），这个参数定义了可以通过的数据包的最大尺寸。如果一个数据包大于这个参数值时，系统会将其划分成更小的数据包

（称为ip碎片）来传输，而接受方则对这些ip碎片再进行重组以还原整个包。这样会导致一个问题：当系统将大数据包划分成ip碎片传输时，第一个碎片含有

完整的包头信息（IP+TCP、UDP和ICMP），但是后续的碎片只有包头的部分信息（如源地址、目的地址）。因此，检查后面的ip碎片的头部（象有

TCP、UDP和ICMP一样）是不可能的。假如有这样的一条规则：

\#iptables -A FORWARD -p tcp -s 192.168.1.0/24 -d 192.168.2.100 –dport 80 -j ACCEPT

并且这时的FORWARD的policy为DROP时，系统只会让第一个ip碎片通过，而余下的碎片因为包头信息不完整而无法通过。可以通过—fragment/-f 选项来指定第二个及以后的ip碎片解决上述问题。

\#iptables -A FORWARD -f -s 192.168.1.0/24 -d 192.168.2.100 -j ACCEPT

注意现在有许多进行ip碎片攻击的实例，如DoS攻击，因此允许ip碎片通过是有安全隐患的，对于这一点可以采用iptables的匹配扩展来进行限制。

三、设置扩展的规则匹配（举例已忽略目标动作）

1、多端口匹配。

1）匹配多个源端口。

\#iptables -A INPUT -p tcp -m multiport –sport 22,53,80,110

2）匹配多个目的端口。

\#iptables -A INPUT -p tcp -m multiport –dpoort 22,53,80

3）匹配多端口\(无论是源端口还是目的端口）

\#iptables -A INPUT -p tcp -m multiport –port 22,53,80,110

2、指定TCP匹配扩展

使用 –tcp-flags 选项可以根据tcp包的标志位进行过滤。

\#iptables -A INPUT -p tcp –tcp-flags SYN,FIN,ACK SYN

\#iptables -A FROWARD -p tcp –tcp-flags ALL SYN,ACK

上实例中第一个表示SYN、ACK、FIN的标志都检查，但是只有SYN匹配。第二个表示ALL（SYN，ACK，FIN，RST，URG，PSH）的标志都检查，但是只有设置了SYN和ACK的匹配。

\#iptables -A FORWARD -p tcp --syn

选项—syn相当于”--tcp-flags SYN,RST,ACK SYN”的简写。

3、limit速率匹配扩展。

1）指定单位时间内允许通过的数据包个数，单位时间可以是/second、/minute、/hour、/day或使用第一个子母。

\#iptables -A INPUT -m limit --limit 300/hour

2 \)指定触发事件的阀值。

\#iptables -A INPUT -m limit –limit-burst 10

用来比对一次同时涌入的封包是否超过10个，超过此上限的包将直接丢弃。

3）同时指定速率限制和触发阀值。

\#iptables -A INPUT -p icmp -m limit –-limit 3/m –limit-burst 3

表示每分钟允许的最大包数量为限制速率（本例为3）加上当前的触发阀值burst数。任何情况下，都可保证3个数据包通过，触发阀值burst相当于允许额外的包数量。

4）基于状态的匹配扩展（连接跟踪）

每个网络连接包括以下信息：源地址、目标地址、源端口、目的端口，称为套接字对（socket pairs）；协议类型、连接状态（TCP协议）

和超时时间等。防火墙把这些信息称为状态（stateful）。状态包过滤防火墙能在内存中维护一个跟踪状态的表，比简单包过滤防火墙具有更大的安全性，命令格式如下：

iptables -m state –-state \[!\]state \[,state,state,state\]

其中，state表是一个逗号分割的列表，用来指定连接状态，4种：

&gt;NEW: 该包想要开始一个新的连接（重新连接或连接重定向）

&gt;RELATED:该包是属于某个已经建立的连接所建立的新连接。举例：

FTP的数据传输连接和控制连接之间就是RELATED关系。

&gt;ESTABLISHED：该包属于某个已经建立的连接。

&gt;INVALID:该包不匹配于任何连接，通常这些包被DROP。

例如：

（1）在INPUT链添加一条规则，匹配已经建立的连接或由已经建立的连接所建立的新连接。即匹配所有的TCP回应包。

\#iptables -A INPUT -m state –state RELATED,ESTABLISHED

（2）在INPUT链链添加一条规则，匹配所有从非eth0接口来的连接请求包。

\#iptables -A INPUT -m state -–state NEW -i !eth0

又如，对于ftp连接可以使用下面的连接跟踪：

（1）被动（Passive）ftp连接模式。

\#iptables -A INPUT -p tcp --sport 1024: --dport 1024: -m state –-state ESTABLISHED -j ACCEPT

\#iptables -A OUTPUT -p tcp --sport 1024: --dport 1024: -m

state -–state ESTABLISHED,RELATED -j ACCEPT

（2）主动（Active）ftp连接模式

\#iptables -A INNPUT -p tcp --sport 20 -m state –-state ESTABLISHED,RELATED -j ACCEPT

\#iptables -A OUTPUT -p tcp –OUTPUT -p tcp –dport 20 -m state --state ESTABLISHED -j ACCEPT

