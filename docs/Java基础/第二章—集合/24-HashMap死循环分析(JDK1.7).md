假设HashMap初始化大小为4，插入个3节点，不巧的是，这3个节点都hash到同一个位置，如果按照默认的负载因子的话，插入第3个节点就会扩容，为了验证效果，假设负载因子是1.

```
void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }
```

以上是节点移动的相关逻辑。

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfAQ9TOx62U1SensH64M7UDnlqso4Iu7MOmV0U45Sm43jCR2wiaMmVbhg/?tp=webp&wxfrom=5&wx_lazy=1)

插入第4个节点时，发生rehash，假设现在有两个线程同时进行，线程1和线程2，两个线程都会新建新的数组。

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfnvvumv32NvPq5hQ6fabyeloBPvWcpn5j135nDztlAYNdj0ZosXR7WA/?tp=webp&wxfrom=5&wx_lazy=1)

假设 **线程2** 在执行到 `Entry<K,V>next=e.next;`之后，cpu时间片用完了，这时变量e指向节点a，变量next指向节点b。

**线程1**继续执行，很不巧，a、b、c节点rehash之后又是在同一个位置7，开始移动节点

第一步，移动节点a

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfibDtaibuMULBNPcLx0apUkaGqTbYj7KDoe2TrQK73xQVoiaFTdWuiadyQA/?tp=webp&wxfrom=5&wx_lazy=1)

第二步，移动节点b

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfibb44ZOJp4dp4Ch5ic9ouvvAwTMgKYnMyNIyMQY4ksJ29oc1ia2eTLtIg/?tp=webp&wxfrom=5&wx_lazy=1)

注意，这里的顺序是反过来的，继续移动节点c

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfxcNAXn0QcA3vaSyjm3vQpVhwlrRiaOGXHG40spDmqDIqrgXbMrv8O8A/?tp=webp&wxfrom=5&wx_lazy=1)

这个时候 **线程1** 的时间片用完，内部的table还没有设置成新的newTable， **线程2** 开始执行，这时内部的引用关系如下：

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFflVtvYzEAzcxJxs71y1wK7Xmz7QUeD4n3ACqohDLTic633rIv7K7UzFw/?tp=webp&wxfrom=5&wx_lazy=1)

这时，在 **线程2** 中，变量e指向节点a，变量next指向节点b，开始执行循环体的剩余逻辑。

```
Entry<K,V> next = e.next;
if (rehash) {
    e.hash = null == e.key ? 0 : hash(e.key);
}
int i = indexFor(e.hash, newCapacity);
e.next = newTable[i];
newTable[i] = e;
e = next;
```

执行之后的引用关系如下图

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFf36AnXbUJFOWKtjaNBGt1lKibXr16ShQC5cA9ZoNKWIbwFnKXZmPibCpg/?tp=webp&wxfrom=5&wx_lazy=1)

执行后，变量e指向节点b，因为e不是null，则继续执行循环体，执行后的引用关系

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFffscoKogjPSxvQq5xGFLDU3T3l0sPXrvicTLPAh0slzcLlqCdPYL9rYg/?tp=webp&wxfrom=5&wx_lazy=1)

变量e又重新指回节点a，只能继续执行循环体，这里仔细分析下： 1、执行完 `Entry<K,V>next=e.next;`，目前节点a没有next，所以变量next指向null； 2、 `e.next=newTable[i];` 其中 newTable\[i\] 指向节点b，那就是把a的next指向了节点b，这样a和b就相互引用了，形成了一个环； 3、 `newTable[i]=e` 把节点a放到了数组i位置； 4、 `e=next;` 把变量e赋值为null，因为第一步中变量next就是指向null；

所以最终的引用关系是这样的：

![](http://mmbiz.qpic.cn/mmbiz_png/8Jeic82Or04mAapMekicWGUABWbW4iacgFfEPt6ICBsBUczVHT9lbV9cuex4ZdFy51ST76PvYibXrk86TaLKZnYSSw/?tp=webp&wxfrom=5&wx_lazy=1)

节点a和b互相引用，形成了一个环，当在数组该位置get寻找对应的key时，就发生了死循环。

另外，如果线程2把newTable设置成到内部的table，节点c的数据就丢了，看来还有数据遗失的问题。

