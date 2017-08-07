**Sql语句优化和索引**

**1.Innerjoin和左连接，右连接，子查询**

A.     inner join内连接也叫等值连接是，left/rightjoin是外连接。

SELECT A.id,A.name,B.id,B.name FROM A LEFT JOIN B ON A.id =B.id;

SELECT A.id,A.name,B.id,B.name FROM A RIGHT JOIN ON B A.id= B.id;

SELECT A.id,A.name,B.id,B.name FROM A INNER JOIN ON A.id =B.id;

经过来之多方面的证实inner join性能比较快，因为inner join是等值连接，或许返回的行数比较少。但是我们要记得有些语句隐形的用到了等值连接，如：

SELECT A.id,A.name,B.id,B.name FROM A,B WHERE A.id = B.id;

推荐：能用inner join连接尽量使用inner join连接

B．子查询的性能又比外连接性能慢，尽量用外连接来替换子查询。

  Select\* from A where exists \(select \* from B where id&gt;=3000 and A.uuid=B.uuid\);

A表的数据为十万级表，B表为百万级表，在本机执行差不多用2秒左右，我们可以通过explain可以查看到子查询是一个相关子查询\(DEPENDENCE SUBQUERY\);[MySQL](http://lib.csdn.net/base/mysql)是先对外表A执行全表查询，然后根据uuid逐次执行子查询，如果外层表是一个很大的表，我们可以想象查询性能会表现比这个更加糟糕。

  一种简单的优化就是用innerjoin的方法来代替子查询，查询语句改为：

   Select\* from A inner join B using\(uuid\) where b.uuid&gt;=3000;

  这个语句执行[测试](http://lib.csdn.net/base/softwaretest)不到一秒；

C．在使用ON 和 WHERE 的时候，记得它们的顺序，如：

SELECT A.id,A.name,B.id,B.name FROM A LEFT JOIN B ON A.id =B.id WHERE B.NAME=’XXX’

执行过程会先执行ON 后面先过滤掉B表的一些行数。然而WHERE是后再过滤他们两个连接产生的记录。

不过在这里提醒一下大家：ON后面的条件只能过滤出B表的条数，但是连接返回的记录的行数还是A表的行数是一样。如：

SELECT A.id,A.name,B.id,B.name FROM A LEFT JOIN B ON A.id =B.id;

返回的记录数是A表的条数，ON后面的条件只起到过滤B表的记录数，而

SELECT A.id,A.name,B.id,B.name FROM A ,B WHERE A.id = B.id

返回的条数，是笛卡尔积后，符合A.id = B.id这个条件的记录

D．使用JOIN时候，应该用小的结果驱动打的结果（left join 左边表结果尽量小，如果有条件应该放到左边先处理，right join同理反向），同事尽量把牵涉到多表联合的查询拆分多个query\(多个表查询效率低，容易锁表和阻塞\)。如：

Select \* from A left join B ona.id=B.ref\_id where B.ref\_id&gt;10;

可以优化为：select \* from \(select \* from A wehre id &gt;10\) T1 left join B onT1.id=B.ref\_id;

**2.建立索引,加快查询性能.**

A．在建立复合索引的时候，在where条件中用到的字段在复合索引中，则最好把这个字段放在复合索引的最左端，这样才能使用索引，才能提高查询。

B．保证连接的索引是相同的类型，意思就是A表和B表相关联的字段，必须是同类型的。这些类型都建立了索引，这样才能两个表都能使用索引，如果类型不一样，至少有一个表使用不了索引。

C．索引，不仅仅是主键和唯一键，也可以是其他的任何列。在使用like其中一个有索引的字段列的时候。

如： select \*from A name like ‘xxx%’;

这个sql会使用name的索引（前提name建立了索引）；而下面的语句就使用不了索引

Select \* from A name like ‘%xxx’;

因为‘%’代表任何字符，%xxx不知道怎么去索引的，所以使用不了索引。

D.复合索引

比如有一条语句这样的：select\* from users where area =’beijing’ and age=22;

如果我们是在area和age上分别创建索引的话，由于[mysql](http://lib.csdn.net/base/mysql)查询每次只能使用一个索引，所以虽然这样已经相对不做索引时全表扫描提高了很多效率，但是如果area，age两列上创建复合索引的话将带来更高的效率。如果我们创建了（area,age,salary）的复合索引，那么其实相当于创建了（area,age,salary）,\(area,age\),\(area\)三个索引，这样称为最佳左前缀特性。因此我们在创建复合索引的应该将最常用作限制条件的列放在最左边，依次递减。

E.索引不会包含有NULL值的列

只要列中包含有NULL值都将不会被包含在索引中（除非是唯一值的域，可以存在一个NULL），复合索引中只要有一列含有NULL值，那么这一列对于此复合索引是无效的。所以我们在[数据库](http://lib.csdn.net/base/mysql)设计时不要让字段的默认值为NULL.

F.使用短索引

对串列进行索引，如果可能应该指定一个前缀长度。例如，如果有一个CHAR\(255\)的列，如果在钱10个或者20字符内，多数值是唯一的，那么就不要对整个列进行索引。短索引不仅可以提高查询速度而且可以节省磁盘空间和I/O操作。

G．排序的索引问题

Mysql查询只是用一个索引，因此如果where子句中已经使用了索引的话，那么order by中的列是不会使用索引的。因此数据库默认排序可以符合要求情况下不要使用排序操作；尽量不要包含多个列的排序，如果需要最好给这些列创建复合索引。

**3.limit千万级分页的时候优化。**

A．在我们平时用limit,如：

Select \* from A order by id limit 1,10;

这样在表数据很少的时候，看不出什么性能问题，倘若到达千万级，如：

Select \* from A order by id limit10000000,10;

虽然都是只查询10记录，但是这个就性能就让人受不了了。所以为什么当表数据很大的时候，我们还继续用持久层框架如[hibernate](http://lib.csdn.net/base/javaee),ibatis就会有一些性能问题，除非持久层框架对这些[大数据](http://lib.csdn.net/base/hadoop)表做过优化。

B．在遇见上面的情况，我们可以用另外一种语句优化，如：

Select \* from A where id&gt;=\(Select idfrom a limit 10000000,1\) limit 10;

确实这样快了很多，不过前提是，id字段建立了索引。也许这个还不是最优的，其实还可以这样写：

Select \* from A where id between 10000000and 10000010;

这样的效率更加高。

**4.尽量避免Select \* 命令**

A.从表中读取越多的数据，查询会变得更慢。它会增加磁盘的操作时间，还是在数据库服务器与web服务器是独立分开的情况下，你将会经历非常漫长的网络延迟。仅仅是因为数据不必要的在服务器之间传输。

**5.尽量不要使用BY RAND\(\)命令**

 A．如果您真需要随机显示你的结果，有很多更好的途径实现。而这个函数可能会为表中每一个独立的行执行BY RAND\(\)命令—这个会消耗处理器的处理能力，然后给你仅仅返回一行。



**6.利用limit 1取得唯一行**

A．有时要查询一张表时，你要知道需要看一行，你可能去查询一条独特的记录。你可以使用limit 1.来终止数据库引擎继续扫描整个表或者索引,如：

Select \* from A  where namelike ‘%xxx’ limit 1;

这样只要查询符合like ‘%xxx’的记录，那么引擎就不会继续扫描表或者索引了。



**7.尽量少排序**

A.排序操作会消耗较多的CPU资源，所以减少排序可以在缓存命中率高等



**8.尽量少OR**

A.当where子句中存在多个条件以“或”并存的时候，Mysql的优化器并没有很好的解决其执行计划优化问题，再加上mysql特有的sql与Storage分层[架构](http://lib.csdn.net/base/architecture)方式，造成了其性能比较地下，很多时候使用union all或者union\(必要的时候\)的方式代替“or”会得到更好的效果。



**9.尽量用union all 代替union**

A.union和union all的差异主要是前者需要将两个（或者多个）结果集合并后再进行唯一性过滤操作，这就会涉及到排序，增加大量的cpu运算，加大资源消耗及延迟。所以当我们可以确认不可能出现重复结果集或者不在乎重复结果集的时候，尽量使用union all而不是union.

**10.避免类型转换**

A.这里所说的“类型转换”是指where子句中出现column字段的类型和传入的参数类型不一致的时候发生的类型转换。人为的上通过转换函数进行转换，直接导致mysql无法使用索引。如果非要转型，应该在传入参数上进行转换。



**11.不要在列上进行运算**

A. 如下面:select \* fromusers where YEAR\(adddate\)&lt;2007;将在每个行进行运算，这些导致索引失效进行全表扫描，因此我们可以改成：

Select \* from users where adddate&lt;’2007-01-01’;



**12.尽量不要使用NOT IN和&lt;&gt;操作**

A. NOT IN和&lt;&gt;操作都不会使用索引，而是将会进行全表扫描。NOT IN可以NOT EXISTS代替，id&lt;&gt;3则可以使用id&gt;3 or id &lt;3;如果NOT EXISTS是子查询，还可以尽量转化为外连接或者等值连接，要看具体sql的业务逻辑。

B．把NOT IN转化为LEFT JOIN如：

SELECT \* FROM customerinfo WHERE CustomerIDNOT in \(SELECT CustomerID FROM salesinfo \);

优化：

SELECT \* FROM customerinfo LEFT JOINsalesinfoON customerinfo.CustomerID=salesinfo. CustomerID WHEREsalesinfo.CustomerID IS NULL;



**13.使用批量插入节省交互（最好是使用存储过程）**

A. 尽量使用insert intousers\(username,password\) values\(‘test1’,’pass1’\), \(‘test2’,’pass2’\), \(‘test3’,’pass3’\);



**14. 锁定表**

A. 尽管事务是维护数据库完整性的一个非常好的方法,但却因为它的独占性,有时会影响数据库的性能,尤其是在很多的应用系统中.由于事务执行的过程中,数据库将会被锁定,因此其他的用户请求只能暂时等待直到该事务结算.如果一个数据库系统只有少数几个用户来使用,事务造成的影响不会成为一个太大问题;但假设有成千上万的用户同时访问一个数据库系统,例如访问一个电子商务网站,就会产生比较严重的响应延迟.其实有些情况下我们可以通过锁定表的方法来获得更好的性能.如:

LOCK TABLE inventory write

Select quanity from inventory whereitem=’book’;

…

Update inventory set quantity=11 whereitem=’book’;

UNLOCK TABLES;

这里，我们用一个select语句取出初始数据，通过一些计算，用update语句将新值更新到列表中。包含有write关键字的LOCK TABLE语句可以保证在UNLOCK TABLES命令被执行之前，不会有其他的访问来对inventory进行插入，更新或者删除的操作。



**15.对多表关联的查询，建立视图**

A．对多表的关联可能会有性能上的问题，我们可以对多表建立视图，这样操作简单话，增加数据安全性，通过视图，用户只能查询和修改指定的数据。且提高表的逻辑独立性，视图可以屏蔽原有表结构变化带来的影响。

