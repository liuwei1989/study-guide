我们经常在linux要查找某个文件，但不知道放在哪里了，可以使用下面的一些命令来搜索。这些是从网上找到的资料\(参考资料1\)，因为有时很长时间不会用到，当要用的时候经常弄混了，所以放到这里方便使用。 

which       查看可执行文件的位置 

whereis    查看文件的位置 

locate       配合数据库查看文件位置 

find          实际搜寻硬盘查询文件名称 \(find也可以根据文件大小-size 时间-atime 正则表达式-regex\)



1、which 

语法：  

\[root @redhat ~\]\# which 可执行文件名称  

例如：  

\[root @redhat ~\]\# which passwd  

/usr/bin/passwd  

which是通过 PATH环境变量 到该路径内查找可执行文件，所以基本的功能是寻找可执行文件  



2、whereis 

语法：  

\[root @redhat ~\]\# whereis \[-bmsu\] 文件或者目录名称  

参数说 明：  

-b ： 只找二进制文件  

-m： 只找在说明文件manual路径下的文件  

-s ： 只找source源文件  

-u ： 没有说明文档的文件  

例如：  

\[root @redhat ~\]\# whereis passwd  

passwd: /usr/bin/passwd /etc/passwd /usr/share/man/man1/passwd.1.gz /usr/share/man/man5/passwd.5.gz  

将和passwd文件相关的文件都查找出来  



\[root @redhat ~\]\# whereis -b passwd  

passwd: /usr/bin/passwd /etc/passwd  

只将二进制文件查找出来  



和find相比，whereis查找的速度非常快，这是因为linux系统会将系统内的所有文件都记录在一个 数据库文件 \( 参考资料1以及大多数文章中都是这样描述的，whereis会在一个数据库文件中查找，在参考资料2中找到这个数据库文件目录 /var/lib/slocate/slocate.db，我在服务器中并没有找到这个目录，原因应该是我没有装locate命令，那么whereis到底是怎么查找的呢？ 找了很久没有，从参考资料3中有一种个人比较相信的答案，从 /{bin,sbin,etc}   /usr{lib,bin,old,new,local,games,include,etc,src,man,sbin,X386,TeX,g++-include} 

/usr/local/{X386,TeX,X11,include,lib,man,etc,bin,games,emacs} 中查找，也没有去看whereis的源码，如果有确定的可以交流一下 \)中，当使用whereis和下面即将介绍的locate时，会从数据库中查找数据，而不是像find命令那样，通过遍历硬盘来查找，效率自然会很高。  

但是 该数据库文件并不是实时更新，默认情况下时一星期更新一次，因此，我们在用whereis和locate 查找文件时，有时会找到已经被删除的数据，或者刚刚建立文件，却无法查找到，原因就是因为数据库文件没有被更新。  



3、 locate  

语法：  

\[root@redhat ~\]\# locate 文件或者目录名称  

例 如：  

\[root@redhat ~\]\# locate passwd  

/home/weblogic/bea/user\_projects/domains/zhanggongzhe112/myserver/stage/\_appsdir\_DB\_war/DB.war/jsp/as/user/passwd.jsp  

/home/weblogic/bea/user\_projects/domains/zhanggongzhe112/myserver/stage/\_appsdir\_admin\_war/admin.war/jsp/platform/passwd.jsp  

/lib/security/pam\_unix\_passwd.so  

/lib/security/pam\_passwdqc.so  

/usr/include/rpcsvc/yppasswd.x  

/usr/include/rpcsvc/yppasswd.h  

/usr/lib/perl5/5.8.5/i386-linux-thread-multi/rpcsvc/yppasswd.ph  

/usr/lib/kde3/kded\_kpasswdserver.la  

/usr/lib/kde3/kded\_kpasswdserver.so  

/usr/lib/ruby/1.8/webrick/httpauth/htpasswd.rb  

/usr/bin/vncpasswd  

/usr/bin/userpasswd  

/usr/bin/yppasswd  

…………  



4、 find  

语法：  

\[root@redhat ~\]\# find 路径 参数  

参 数说明：  

时间查找参数：  

-atime n :将n\*24小时内存取过的的文件列出来  

-ctime n :将n\*24小时内改变、新增的文件或者目录列出来  

-mtime n :将n\*24小时内修改过的文件或者目录列出来  

-newer file ：把比file还要新的文件列出来  

名称查找参数：  

-gid n       ：寻找群组ID为n的文件  

-group name  ：寻找群组名称为name的文件  

-uid n       ：寻找拥有者ID为n的文件  

-user name   ：寻找用户者名称为name的文件  

-name file   ：寻找文件名为file的文件（可以使用通配符）  

例 如：  

\[root@redhat ~\]\# find / -name zgz  

/home/zgz  

/home/zgz/zgz  

/home/weblogic/bea/user\_projects/domains/zgz  

/home/oracle/product/10g/cfgtoollogs/dbca/zgz  

/home/oracle/product/10g/cfgtoollogs/emca/zgz  

/home/oracle/oradata/zgz  



\[root@redhat ~\]\# find / -name '\*zgz\*'  

/home/zgz  

/home/zgz/zgz1  

/home/zgz/zgzdirzgz  

/home/zgz/zgz  

/home/zgz/zgzdir  

/home/weblogic/bea/user\_projects/domains/zgz  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log00006  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log00002  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log00004  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log00008  

/home/weblogic/bea/user\_projects/domains/zgz/zgz.log00005  



当我们用whereis和locate无法查找到我们需要的文件时，可以使用find，但是find是在硬盘上遍历查找，因此非常消耗硬盘的资源，而且效率也非常低，因此建议大家优先使用whereis和locate。  

locate 是在数据库里查找，数据库大至每天更新一次。  

whereis 可以找到可执行命令和man page  

find 就是根据条件查找文件。  

which 可以找到可执行文件和别名\(alias\) 

