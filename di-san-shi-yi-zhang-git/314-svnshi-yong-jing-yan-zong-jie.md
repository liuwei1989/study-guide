1.关于目录说明,一个项目下一般会建立3个目录，trunk、branches和tags。

trunk通常是放主干程序的，这个目录下最好保留的是一份最近最新的可编译执行的代码。

Branches中存放该项目的一些分支，通常是一些新功能的添加或者bug修复打出的分支。最终分支的程序会merge到trunk中。

Tags一般只有增加权限，没有删除修改权限，tags中存放一些比较稳定的已经或者曾经上线的版本。

注意：Trunk下不需要再添加个项目名的目录，直接放代码，而branches和tags下面都需要通过一定的规则根据项目名生成目录（通常通过目录就知道为什么打这个分支或者tag），然后再目录中存放代码



2.如何将代码从trunk拷贝到branches或者tags。很多人都是将代码从trunk中下载下来，然后删除.svn信息，最后提交到tags或者branches中，但这样做不利于版本管理，

应该通过svn cp命令，将trunk中的代码拷贝到branches或者tags中，因为通过svn cp命令只是对代码打了个标记，再处理merge操作时，方面追根朔源。

例如：当对一个项目做下一版本开发时，先通过svn cp[http://xxx.xxx.xxx/svn/project/app/xxx/trunk](http://xxx.xxx.xxx/svn/project/app/xxx/trunk)[http://xxx.xxx.xxx/svn/project/app/xxx/branches/xxx.forexamples](http://xxx.xxx.xxx/svn/project/app/xxx/branches/xxx.forexamples)命名，

在branches中打出个分支，然后再分支中进行开发。这样可以确保多人开发时trunk代码不冲突。开发完成后，经过测试后，可以svn merge到trunk中。

当对一个项目进行上线操作时，通过命令svn cp[http://xxx.xxx.xxx/svn/project/app/xxx/trunk](http://xxx.xxx.xxx/svn/project/app/xxx/trunk)[http://xxx.xxx.xxx/svn/project/app/xxx/tags/xxx.online.20111115.a](http://xxx.xxx.xxx/svn/project/app/xxx/tags/xxx.online.20111115.a)对代码打个tag，

因为tags是不能删除的，所以可以保障代码的一致性和稳定性。除了问题也好排查。



3.项目测试完了，怎么样merge会主干呢？这也是一个非常头痛的问题，我也见过有人通过删除trunk，然后把branches的代码拷贝到trunk的情况，

因为merge时经常会出现冲突，更改太麻烦了，其实这些麻烦事都是自己给自己找的，如果大家都按照一定的规范进行打分支，merge，一般不会出现不一致的现象。

Svn中给出merge用法如下：

文本框: merge:将两个源差异应用至工作副本。

用法: 1. merge sourceURL1\[@N\] sourceURL2\[@M\] \[WCPATH\] 2. merge sourceWCPATH1@N sourceWCPATH2@M \[WCPATH\] 3. merge \[-c M\[,N...\] \| -r N:M ...\] SOURCE\[@REV\] \[WCPATH\]

1、第一种形式中，源URL的版本N与M作为比较的来源。如果没有指定版本，默认

为HEAD。

2、在第二种形式中，两个源工作副本路径对应的版本库中的URL作为比较的来源。这

里必须指定版本。

3、第三种形式中，SOURCE可为URL或工作副本中的路径\(后者会使用版本库中对应

的URL\)。比较版本为REV的SOURCE，就像它在版本N到M存在一样。如果没

有给出REV，默认为HEAD。选项“-c M”等价于“-r:M”，“-c -M”与之

相反，等价于“-r M:”。如果没有指定版本范围，默认为0:REV。可以指定

多个“-c”或“-r”，并且可以混合使用向前范围或向后范围。

