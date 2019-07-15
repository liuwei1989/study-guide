TreeMap的实现是红黑树算法的实现，所以要了解TreeMap就必须对红黑树有一定的了解,其实这篇博文的名字叫做：根据红黑树的算法来分析TreeMap的实现，但是为了与Java提高篇系列博文保持一致还是叫做TreeMap比较好。通过这篇博文你可以获得如下知识点：

1、红黑树的基本概念。

2、红黑树增加节点、删除节点的实现过程。

3、红黑树左旋转、右旋转的复杂过程。

4、Java 中TreeMap是如何通过put、deleteEntry两个来实现红黑树增加、删除节点的。

我想通过这篇博文你对TreeMap一定有了更深的认识。好了，下面先简单普及红黑树知识。

## 一、红黑树简介

红黑树又称红-黑二叉树，它首先是一颗二叉树，它具体二叉树所有的特性。同时红黑树更是一颗自平衡的排序二叉树。

我们知道一颗基本的二叉树他们都需要满足一个基本性质–即树中的任何节点的值大于它的左子节点，且小于它的右子节点。按照这个基本性质使得树的检索效率大大提高。我们知道在生成二叉树的过程是非常容易失衡的，最坏的情况就是一边倒（只有右/左子树），这样势必会导致二叉树的检索效率大大降低（O\(n\)），所以为了维持二叉树的平衡，大牛们提出了各种实现的算法，如：[AVL](http://baike.baidu.com/view/414610.htm)，[SBT](http://baike.baidu.com/view/2957252.htm)，[伸展树](http://baike.baidu.com/view/1118088.htm)，[TREAP](http://baike.baidu.com/view/956602.htm)，[红黑树](http://baike.baidu.com/view/133754.htm?fr=aladdin#1_1)等等。

平衡二叉树必须具备如下特性：它是一棵空树或它的左右两个子树的高度差的绝对值不超过1，并且左右两个子树都是一棵平衡二叉树。也就是说该二叉树的任何一个等等子节点，其左右子树的高度都相近。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700001_thumb.png "2014051700001")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700001.png)

红黑树顾名思义就是节点是红色或者黑色的平衡二叉树，它通过颜色的约束来维持着二叉树的平衡。对于一棵有效的红黑树二叉树而言我们必须增加如下规则：

**1、每个节点都只能是红色或者黑色**

**2、根节点是黑色**

**3、每个叶节点（NIL节点，空节点）是黑色的。**

**4、如果一个结点是红的，则它两个子节点都是黑的。也就是说在一条路径上不能出现相邻的两个红色结点。**

**5、从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。**

这些约束强制了红黑树的关键性质: 从根到叶子的最长的可能路径不多于最短的可能路径的两倍长。结果是这棵树大致上是平衡的。因为操作比如插入、删除和查找某个值的最坏情况时间都要求与树的高度成比例，这个在高度上的理论上限允许红黑树在最坏情况下都是高效的，而不同于普通的二叉查找树。所以红黑树它是复杂而高效的，其检索效率O\(logn\)。下图为一颗典型的红黑二叉树。

[  
![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700002_thumb.png "2014051700002")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700002.png)

对于红黑二叉树而言它主要包括三大基本操作：左旋、右旋、着色。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700004_thumb.gif "2014051700004")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700004.gif)左旋                          [![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700005_thumb.gif "2014051700005")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700005.gif)

                                                             右旋

（图片来自：[http://www.cnblogs.com/yangecnu/p/Introduce-Red-Black-Tree.html](http://www.cnblogs.com/yangecnu/p/Introduce-Red-Black-Tree.html)）

---

本节参考文献：[http://baike.baidu.com/view/133754.htm?fr=aladdin](http://baike.baidu.com/view/133754.htm?fr=aladdin)—–百度百科

**注：**由于本文主要是讲解Java中TreeMap，所以并没有对红黑树进行非常深入的了解和研究，如果诸位想对其进行更加深入的研究Lz提供几篇较好的博文：

**1、**[**红黑树系列集锦**](http://blog.csdn.net/v_JULY_v/article/category/774945)

**2、**[**红黑树数据结构剖析**](http://www.cnblogs.com/fanzhidongyzby/p/3187912.html)

**3、**[**红黑树**](http://blog.csdn.net/eric491179912/article/details/6179908)

  
  
二、TreeMap数据结构

**&gt;&gt;&gt;&gt;&gt;&gt;回归主角：TreeMap&lt;&lt;&lt;&lt;&lt;&lt;**

TreeMap的定义如下：

```
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable
```

TreeMap继承AbstractMap，实现NavigableMap、Cloneable、Serializable三个接口。其中AbstractMap表明TreeMap为一个Map即支持key-value的集合， NavigableMap（[更多](http://docs.oracle.com/javase/7/docs/api/java/util/NavigableMap.html)）则意味着它支持一系列的导航方法，具备针对给定搜索目标返回最接近匹配项的导航方法 。

TreeMap中同时也包含了如下几个重要的属性：

```
//比较器，因为TreeMap是有序的，通过comparator接口我们可以对TreeMap的内部排序进行精密的控制
        private final Comparator<? super K> comparator;
        //TreeMap红-黑节点，为TreeMap的内部类
        private transient Entry<K,V> root = null;
        //容器大小
        private transient int size = 0;
        //TreeMap修改次数
        private transient int modCount = 0;
        //红黑树的节点颜色--红色
        private static final boolean RED = false;
        //红黑树的节点颜色--黑色
        private static final boolean BLACK = true;
```

对于叶子节点Entry是TreeMap的内部类，它有几个重要的属性：

```
//键
        K key;
        //值
        V value;
        //左孩子
        Entry<K,V> left = null;
        //右孩子
        Entry<K,V> right = null;
        //父亲
        Entry<K,V> parent;
        //颜色
        boolean color = BLACK;
```

**注：**前面只是开胃菜，下面是本篇博文的重中之重，在下面两节我将重点讲解treeMap的put\(\)、delete\(\)方法。通过这两个方法我们会了解红黑树增加、删除节点的核心算法。

## 三、TreeMap put\(\)方法

在了解TreeMap的put\(\)方法之前，我们先了解红黑树增加节点的算法。

### 红黑树增加节点

红黑树在新增节点过程中比较复杂，复杂归复杂它同样必须要依据上面提到的五点规范，同时由于规则1、2、3基本都会满足，下面我们主要讨论规则4、5。假设我们这里有一棵最简单的树，我们规定新增的节点为N、它的父节点为P、P的兄弟节点为U、P的父节点为G。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700007_thumb.png "2014051700007")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700007.png)

对于新节点的插入有如下三个关键地方：

1、插入新节点总是红色节点 。

2、如果插入节点的父节点是黑色, 能维持性质 。

3、如果插入节点的父节点是红色, 破坏了性质. 故插入算法就是通过重新着色或旋转, 来维持性质 。

为了保证下面的阐述更加清晰和根据便于参考，我这里将红黑树的五点规定再贴一遍：

**1、每个节点都只能是红色或者黑色**

**2、根节点是黑色**

**3、每个叶节点（NIL节点，空节点）是黑色的。**

**4、如果一个结点是红的，则它两个子节点都是黑的。也就是说在一条路径上不能出现相邻的两个红色结点。**

**5、从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。**



**一、为跟节点**

若新插入的节点N没有父节点，则直接当做根据节点插入即可，同时将颜色设置为黑色。（如图一（1））

**二、父节点为黑色**

这种情况新节点N同样是直接插入，同时颜色为红色，由于根据规则四它会存在两个黑色的叶子节点，值为null。同时由于新增节点N为红色，所以通过它的子节点的路径依然会保存着相同的黑色节点数，同样满足规则5。（如图一（2））

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700008_thumb.png "2014051700008")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700008.png)

（图一）

**三、若父节点P和P的兄弟节点U都为红色**

对于这种情况若直接插入肯定会出现不平衡现象。怎么处理？P、U节点变黑、G节点变红。这时由于经过节点P、U的路径都必须经过G所以在这些路径上面的黑节点数目还是相同的。但是经过上面的处理，可能G节点的父节点也是红色，这个时候我们需要将G节点当做新增节点递归处理。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700009_thumb.png "2014051700009")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700009.png)

### 四、若父节点P为红色，叔父节点U为黑色或者缺少，且新增节点N为P节点的右孩子

对于这种情况我们对新增节点N、P进行一次左旋转。这里所产生的结果其实并没有完成，还不是平衡的（违反了规则四），这是我们需要进行情况5的操作。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000010_thumb.png "20140517000010")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000010.png)

### 五、父节点P为红色，叔父节点U为黑色或者缺少，新增节点N为父节点P左孩子

这种情况有可能是由于情况四而产生的，也有可能不是。对于这种情况先已P节点为中心进行右旋转，在旋转后产生的树中，节点P是节点N、G的父节点。但是这棵树并不规范，它违反了规则4，所以我们将P、G节点的颜色进行交换，使之其满足规范。开始时所有的路径都需要经过G其他们的黑色节点数一样，但是现在所有的路径改为经过P，且P为整棵树的唯一黑色节点，所以调整后的树同样满足规范5。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000011_thumb.png "20140517000011")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000011.png)

上面展示了红黑树新增节点的五种情况，这五种情况涵盖了所有的新增可能，不管这棵红黑树多么复杂，都可以根据这五种情况来进行生成。下面就来分析Java中的TreeMap是如何来实现红黑树的。

### TreeMap put\(\)方法实现分析

在TreeMap的put\(\)的实现方法中主要分为两个步骤，第一：构建排序二叉树，第二：平衡二叉树。

对于排序二叉树的创建，其添加节点的过程如下：

1、以根节点为初始节点进行检索。

2、与当前节点进行比对，若新增节点值较大，则以当前节点的右子节点作为新的当前节点。否则以当前节点的左子节点作为新的当前节点。

3、循环递归2步骤知道检索出合适的叶子节点为止。

4、将新增节点与3步骤中找到的节点进行比对，如果新增节点较大，则添加为右子节点；否则添加为左子节点。

按照这个步骤我们就可以将一个新增节点添加到排序二叉树中合适的位置。如下：

```
public V put(K key, V value) {
           //用t表示二叉树的当前节点
            Entry<K,V> t = root;
            //t为null表示一个空树，即TreeMap中没有任何元素，直接插入
            if (t == null) {
                //比较key值，个人觉得这句代码没有任何意义，空树还需要比较、排序？
                compare(key, key); // type (and possibly null) check
                //将新的key-value键值对创建为一个Entry节点，并将该节点赋予给root
                root = new Entry<>(key, value, null);
                //容器的size = 1，表示TreeMap集合中存在一个元素
                size = 1;
                //修改次数 + 1
                modCount++;
                return null;
            }
            int cmp;     //cmp表示key排序的返回结果
            Entry<K,V> parent;   //父节点
            // split comparator and comparable paths
            Comparator<? super K> cpr = comparator;    //指定的排序算法
            //如果cpr不为空，则采用既定的排序算法进行创建TreeMap集合
            if (cpr != null) {
                do {
                    parent = t;      //parent指向上次循环后的t
                    //比较新增节点的key和当前节点key的大小
                    cmp = cpr.compare(key, t.key);
                    //cmp返回值小于0，表示新增节点的key小于当前节点的key，则以当前节点的左子节点作为新的当前节点
                    if (cmp < 0)
                        t = t.left;
                    //cmp返回值大于0，表示新增节点的key大于当前节点的key，则以当前节点的右子节点作为新的当前节点
                    else if (cmp > 0)
                        t = t.right;
                    //cmp返回值等于0，表示两个key值相等，则新值覆盖旧值，并返回新值
                    else
                        return t.setValue(value);
                } while (t != null);
            }
            //如果cpr为空，则采用默认的排序算法进行创建TreeMap集合
            else {
                if (key == null)     //key值为空抛出异常
                    throw new NullPointerException();
                /* 下面处理过程和上面一样 */
                Comparable<? super K> k = (Comparable<? super K>) key;
                do {
                    parent = t;
                    cmp = k.compareTo(t.key);
                    if (cmp < 0)
                        t = t.left;
                    else if (cmp > 0)
                        t = t.right;
                    else
                        return t.setValue(value);
                } while (t != null);
            }
            //将新增节点当做parent的子节点
            Entry<K,V> e = new Entry<>(key, value, parent);
            //如果新增节点的key小于parent的key，则当做左子节点
            if (cmp < 0)
                parent.left = e;
          //如果新增节点的key大于parent的key，则当做右子节点
            else
                parent.right = e;
            /*
             *  上面已经完成了排序二叉树的的构建，将新增节点插入该树中的合适位置
             *  下面fixAfterInsertion()方法就是对这棵树进行调整、平衡，具体过程参考上面的五种情况
             */
            fixAfterInsertion(e);
            //TreeMap元素数量 + 1
            size++;
            //TreeMap容器修改次数 + 1
            modCount++;
            return null;
        }
```

上面代码中do{}代码块是实现排序二叉树的核心算法，通过该算法我们可以确认新增节点在该树的正确位置。找到正确位置后将插入即可，这样做了其实还没有完成，因为我知道TreeMap的底层实现是红黑树，红黑树是一棵平衡排序二叉树，普通的排序二叉树可能会出现失衡的情况，所以下一步就是要进行调整。fixAfterInsertion\(e\); 调整的过程务必会涉及到红黑树的左旋、右旋、着色三个基本操作。代码如下：

```
/**
     * 新增节点后的修复操作
     * x 表示新增节点
     */
     private void fixAfterInsertion(Entry<K,V> x) {
            x.color = RED;    //新增节点的颜色为红色

            //循环 直到 x不是根节点，且x的父节点不为红色
            while (x != null && x != root && x.parent.color == RED) {
                //如果X的父节点（P）是其父节点的父节点（G）的左节点
                if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                    //获取X的叔节点(U)
                    Entry<K,V> y = rightOf(parentOf(parentOf(x)));
                    //如果X的叔节点（U） 为红色（情况三）
                    if (colorOf(y) == RED) {     
                        //将X的父节点（P）设置为黑色
                        setColor(parentOf(x), BLACK);
                        //将X的叔节点（U）设置为黑色
                        setColor(y, BLACK);
                        //将X的父节点的父节点（G）设置红色
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    }
                    //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）
                    else {   
                        //如果X节点为其父节点（P）的右子树，则进行左旋转（情况四）
                        if (x == rightOf(parentOf(x))) {
                            //将X的父节点作为X
                            x = parentOf(x);
                            //右旋转
                            rotateLeft(x);
                        }
                        //（情况五）
                        //将X的父节点（P）设置为黑色
                        setColor(parentOf(x), BLACK);
                        //将X的父节点的父节点（G）设置红色
                        setColor(parentOf(parentOf(x)), RED);
                        //以X的父节点的父节点（G）为中心右旋转
                        rotateRight(parentOf(parentOf(x)));
                    }
                }
                //如果X的父节点（P）是其父节点的父节点（G）的右节点
                else {
                    //获取X的叔节点（U）
                    Entry<K,V> y = leftOf(parentOf(parentOf(x)));
                  //如果X的叔节点（U） 为红色（情况三）
                    if (colorOf(y) == RED) {
                        //将X的父节点（P）设置为黑色
                        setColor(parentOf(x), BLACK);
                        //将X的叔节点（U）设置为黑色
                        setColor(y, BLACK);
                        //将X的父节点的父节点（G）设置红色
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    }
                  //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）
                    else {
                        //如果X节点为其父节点（P）的右子树，则进行左旋转（情况四）
                        if (x == leftOf(parentOf(x))) {
                            //将X的父节点作为X
                            x = parentOf(x);
                           //右旋转
                            rotateRight(x);
                        }
                        //（情况五）
                        //将X的父节点（P）设置为黑色
                        setColor(parentOf(x), BLACK);
                        //将X的父节点的父节点（G）设置红色
                        setColor(parentOf(parentOf(x)), RED);
                        //以X的父节点的父节点（G）为中心右旋转
                        rotateLeft(parentOf(parentOf(x)));
                    }
                }
            }
            //将根节点G强制设置为黑色
            root.color = BLACK;
        }
```

对这段代码的研究我们发现,其处理过程完全符合红黑树新增节点的处理过程。所以在看这段代码的过程一定要对红黑树的新增节点过程有了解。在这个代码中还包含几个重要的操作。左旋\(rotateLeft\(\)\)、右旋（rotateRight\(\)）、着色（setColor\(\)）。

左旋：rotateLeft\(\)

所谓左旋转，就是将新增节点（N）当做其父节点（P），将其父节点P当做新增节点（N）的左子节点。即：G.left —&gt; N ,N.left —&gt; P。

```
private void rotateLeft(Entry<K,V> p) {
        if (p != null) {
            //获取P的右子节点，其实这里就相当于新增节点N（情况四而言）
            Entry<K,V> r = p.right;
            //将R的左子树设置为P的右子树
            p.right = r.left;
            //若R的左子树不为空，则将P设置为R左子树的父亲
            if (r.left != null)
                r.left.parent = p;
            //将P的父亲设置R的父亲
            r.parent = p.parent;
            //如果P的父亲为空，则将R设置为跟节点
            if (p.parent == null)
                root = r;
            //如果P为其父节点（G）的左子树，则将R设置为P父节点(G)左子树
            else if (p.parent.left == p)
                p.parent.left = r;
            //否则R设置为P的父节点（G）的右子树
            else
                p.parent.right = r;
            //将P设置为R的左子树
            r.left = p;
            //将R设置为P的父节点
            p.parent = r;
        }
    }
```

右旋：rotateRight\(\)

所谓右旋转即，P.right —&gt; G、G.parent —&gt; P。

```
private void rotateRight(Entry<K,V> p) {
        if (p != null) {
            //将L设置为P的左子树
            Entry<K,V> l = p.left;
            //将L的右子树设置为P的左子树
            p.left = l.right;
            //若L的右子树不为空，则将P设置L的右子树的父节点
            if (l.right != null) 
                l.right.parent = p;
            //将P的父节点设置为L的父节点
            l.parent = p.parent;
            //如果P的父节点为空，则将L设置根节点
            if (p.parent == null)
                root = l;
            //若P为其父节点的右子树，则将L设置为P的父节点的右子树
            else if (p.parent.right == p)
                p.parent.right = l;
            //否则将L设置为P的父节点的左子树
            else 
                p.parent.left = l;
            //将P设置为L的右子树
            l.right = p;
            //将L设置为P的父节点
            p.parent = l;
        }
    }
```

左旋、右旋的示意图如下：

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700004_thumb1.gif "2014051700004")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000041.gif)（左旋）                              [![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/2014051700005_thumb1.gif "2014051700005")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000051.gif)

                                                 （右旋）

（图片来自：[http://www.cnblogs.com/yangecnu/p/Introduce-Red-Black-Tree.html](http://www.cnblogs.com/yangecnu/p/Introduce-Red-Black-Tree.html)）

着色：setColor\(\)

着色就是改变该节点的颜色，在红黑树中，它是依靠节点的颜色来维持平衡的。

```
private static <K,V> void setColor(Entry<K,V> p, boolean c) {
        if (p != null)
            p.color = c;
    }
```

## 四、TreeMap delete\(\)方法

### 红黑树删除节点

针对于红黑树的增加节点而言，删除显得更加复杂，使原本就复杂的红黑树变得更加复杂。同时删除节点和增加节点一样，同样是找到删除的节点，删除之后调整红黑树。但是这里的删除节点并不是直接删除，而是通过走了“弯路”通过一种捷径来删除的：**找到被删除的节点D的子节点C，用C来替代D，不是直接删除D，因为D被C替代了，直接删除C即可。**所以这里就将删除父节点D的事情转变为了删除子节点C的事情，这样处理就将复杂的删除事件简单化了。**子节点C的规则是：右分支最左边，或者 左分支最右边的。**

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000012_thumb.png "20140517000012")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000012.png)

红-黑二叉树删除节点，最大的麻烦是要保持 各分支黑色节点数目相等。 因为是删除，所以不用担心存在颜色冲突问题——插入才会引起颜色冲突。

红黑树删除节点同样会分成几种情况，这里是按照待删除节点有几个儿子的情况来进行分类：

1、没有儿子，即为叶结点。直接把父结点的对应儿子指针设为NULL，删除儿子结点就OK了。

2、只有一个儿子。那么把父结点的相应儿子指针指向儿子的独生子，删除儿子结点也OK了。

3、有两个儿子。这种情况比较复杂，但还是比较简单。上面提到过用子节点C替代代替待删除节点D，然后删除子节点C即可。

下面就论各种删除情况来进行图例讲解，但是在讲解之前请允许我再次啰嗦一句，**请时刻牢记红黑树的5点规定：**

**1、每个节点都只能是红色或者黑色**

**2、根节点是黑色**

**3、每个叶节点（NIL节点，空节点）是黑色的。**

**4、如果一个结点是红的，则它两个子节点都是黑的。也就是说在一条路径上不能出现相邻的两个红色结点。**

**5、从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。**

**（注：已经讲三遍了，再不记住我就怀疑你是否适合搞IT了 O\(∩\_∩\)O~）**

诚然，既然删除节点比较复杂，那么在这里我们就约定一下规则：

1、下面要讲解的删除节点一定是实际要删除节点的后继节点（N），如前面提到的C。

2、下面提到的删除节点的树都是如下结构，该结构所选取的节点是待删除节点的右树的最左边子节点。这里我们规定真实删除节点为N、父节点为P、兄弟节点为W兄弟节点的两个子节点为X1、X2。如下图（2.1）。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000013_thumb.png "20140517000013")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000013.png)

现在我们就上面提到的三种情况进行分析、处理。

**情况一、无子节点（红色节点）**

这种情况对该节点直接删除即可，不会影响树的结构。因为该节点为叶子节点它不可能存在子节点—–如子节点为黑，则违反黑节点数原则（规定5），为红，则违反“颜色”原则（规定4）。 如上图（2.2）。

**情况二、有一个子节点**

这种情况处理也是非常简单的，用子节点替代待删除节点，然后删除子节点即可。如上图（2.3）

**情况三、有两个子节点**

这种情况可能会稍微有点儿复杂。它需要找到一个替代待删除节点（N）来替代它，然后删除N即可。它主要分为四种情况。

1、N的兄弟节点W为红色

2、N的兄弟w是黑色的，且w的俩个孩子都是黑色的。

3、N的兄弟w是黑色的，w的左孩子是红色，w的右孩子是黑色。

4、N的兄弟w是黑色的，且w的右孩子时红色的。

**情况3.1、N的兄弟节点W为红色**

W为红色，那么其子节点X1、X2必定全部为黑色，父节点P也为黑色。处理策略是：改变W、P的颜色，然后进行一次左旋转。这样处理就可以使得红黑性质得以继续保持。N的新兄弟new w是旋转之前w的某个孩子，为黑色。这样处理后将情况3.1、转变为3.2、3.3、3.4中的一种。如下：

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000014_thumb.png "20140517000014")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000014.png)

### 

**情况3.2、N的兄弟w是黑色的，且w的俩个孩子都是黑色的。**

这种情况其父节点可红可黑，由于W为黑色，这样导致N子树相对于其兄弟W子树少一个黑色节点，这时我们可以将W置为红色。这样，N子树与W子树黑色节点一致，保持了平衡。如下

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000015_thumb.png "20140517000015")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000015.png)

将W由黑转变为红，这样就会导致新节点new N相对于它的兄弟节点会少一个黑色节点。但是如果new x为红色，我们直接将new x转变为黑色，保持整棵树的平衡。否则情况3.2 会转变为情况3.1、3.3、3.4中的一种。

**情况3.3、N的兄弟w是黑色的，w的左孩子是红色，w的右孩子是黑色。**

针对这种情况是将节点W和其左子节点进行颜色交换，然后对W进行右旋转处理。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000016_thumb.png "20140517000016")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000016.png)

此时N的新兄弟X1\(new w\)是一个有红色右孩子的黑结点，于是将情况3转化为情况4.

**情况3.4、N的兄弟w是黑色的，且w的右孩子时红色的。**

交换W和父节点P的颜色，同时对P进行左旋转操作。这样就把左边缺失的黑色节点给补回来了。同时将W的右子节点X2置黑。这样左右都达到了平衡。

[![](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000017_thumb.png "20140517000017")](http://cmsblogs.qiniudn.com/wp-content/uploads/2014/05/20140517000017.png)

**总结**

个人认为这四种情况比较难理解，首先他们都不是单一的某种情况，他们之间是可以进行互转的。相对于其他的几种情况，情况3.2比较好理解，仅仅只是一个颜色的转变，通过减少右子树的一个黑色节点使之保持平衡，同时将不平衡点上移至N与W的父节点，然后进行下一轮迭代。情况3.1，是将W旋转将其转成情况2、3、4情况进行处理。而情况3.3通过转变后可以化成情况3.4来进行处理，从这里可以看出情况3.4应该最终结。情况3.4、右子节点为红色节点，那么将缺失的黑色节点交由给右子节点，通过旋转达到平衡。

通过上面的分析，我们已经初步了解了红黑树的删除节点情况，相对于增加节点而言它确实是选的较为复杂。下面我将看到在Java TreeMap中是如何实现红黑树删除的。

### TreeMap deleteEntry\(\)方法实现分析

通过上面的分析我们确认删除节点的步骤是：找到一个替代子节点C来替代P，然后直接删除C，最后调整这棵红黑树。下面代码是寻找替代节点、删除替代节点。

```
private void deleteEntry(Entry<K,V> p) {
        modCount++;      //修改次数 +1
        size--;          //元素个数 -1

        /*
         * 被删除节点的左子树和右子树都不为空，那么就用 p节点的中序后继节点代替 p 节点
         * successor(P)方法为寻找P的替代节点。规则是右分支最左边，或者 左分支最右边的节点
         * ---------------------（1）
         */
        if (p.left != null && p.right != null) {  
            Entry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        }

        //replacement为替代节点，如果P的左子树存在那么就用左子树替代，否则用右子树替代
        Entry<K,V> replacement = (p.left != null ? p.left : p.right);

        /*
         * 删除节点，分为上面提到的三种情况
         * -----------------------（2）
         */
        //如果替代节点不为空
        if (replacement != null) {
            replacement.parent = p.parent;
            /*
             *replacement来替代P节点
             */
            //若P没有父节点，则跟节点直接变成replacement
            if (p.parent == null)
                root = replacement;
            //如果P为左节点，则用replacement来替代为左节点
            else if (p == p.parent.left)
                p.parent.left  = replacement;
          //如果P为右节点，则用replacement来替代为右节点
            else
                p.parent.right = replacement;

            //同时将P节点从这棵树中剔除掉
            p.left = p.right = p.parent = null;

            /*
             * 若P为红色直接删除，红黑树保持平衡
             * 但是若P为黑色，则需要调整红黑树使其保持平衡
             */
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) {     //p没有父节点，表示为P根节点，直接删除即可
            root = null;
        } else {      //P节点不存在子节点，直接删除即可
            if (p.color == BLACK)         //如果P节点的颜色为黑色，对红黑树进行调整
                fixAfterDeletion(p);

            //删除P节点
            if (p.parent != null) {
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }
```

**（1）**除是寻找替代节点replacement，其实现方法为successor\(\)。如下：

```
static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
        if (t == null)
            return null;
        /*
         * 寻找右子树的最左子树
         */
        else if (t.right != null) {
            Entry<K,V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } 
        /*
         * 选择左子树的最右子树
         */
        else {
            Entry<K,V> p = t.parent;
            Entry<K,V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }
```

**（2）**处是删除该节点过程。它主要分为上面提到的三种情况，它与上面的if…else if… else一一对应 。如下：

1、有两个儿子。这种情况比较复杂，但还是比较简单。上面提到过用子节点C替代代替待删除节点D，然后删除子节点C即可。

2、没有儿子，即为叶结点。直接把父结点的对应儿子指针设为NULL，删除儿子结点就OK了。

3、只有一个儿子。那么把父结点的相应儿子指针指向儿子的独生子，删除儿子结点也OK了。

删除完节点后，就要根据情况来对红黑树进行复杂的调整：fixAfterDeletion\(\)。

```
private void fixAfterDeletion(Entry<K,V> x) {
        // 删除节点需要一直迭代，知道 直到 x 不是根节点，且 x 的颜色是黑色
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {      //若X节点为左节点
                //获取其兄弟节点
                Entry<K,V> sib = rightOf(parentOf(x));

                /*
                 * 如果兄弟节点为红色----（情况3.1）
                 * 策略：改变W、P的颜色，然后进行一次左旋转
                 */
                if (colorOf(sib) == RED) {     
                    setColor(sib, BLACK);     
                    setColor(parentOf(x), RED);  
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                /*
                 * 若兄弟节点的两个子节点都为黑色----（情况3.2）
                 * 策略：将兄弟节点编程红色
                 */
                if (colorOf(leftOf(sib))  == BLACK &&
                    colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } 
                else {
                    /*
                     * 如果兄弟节点只有右子树为黑色----（情况3.3）
                     * 策略：将兄弟节点与其左子树进行颜色互换然后进行右转
                     * 这时情况会转变为3.4
                     */
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    /*
                     *----情况3.4
                     *策略：交换兄弟节点和父节点的颜色，
                     *同时将兄弟节点右子树设置为黑色，最后左旋转
                     */
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } 
            
            /**
             * X节点为右节点与其为做节点处理过程差不多，这里就不在累述了
             */
            else {
                Entry<K,V> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }
```

这是红黑树在删除节点后，对树的平衡性进行调整的过程，其实现过程与上面四种复杂的情况一一对应，所以在这个源码的时候一定要对着上面提到的四种情况看。





