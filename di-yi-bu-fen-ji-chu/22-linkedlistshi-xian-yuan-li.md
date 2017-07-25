LinkedList 顾名思义是一个列表。与上一遍中的ArrayList相比，LinkedList的存储结构是链式的，非连续存储。如此它在插入，删除元素方面的效率强于ArrayList，但是随机访问和遍历的效率弱于ArrayList。



LinkedList构造方法：

```
LinkedList()
LinkedList(Collection<? extends E> c)
```

不同于ArrayList，LinkedList不可以在初始化时候指定大小。同样ArrayList有加载因子，在容量不够的时候可以扩容，然而LinkedList容量的说法，每次向其中加入元素时候，容量自动加1。



当我们调用add\(E e\)方法时候发生了什么？

```java
/**
* Pointer to first node.
* Invariant: (first == null && last == null) ||
*            (first.prev == null && first.item != null)
*/
transient Node<E> first;

/**
* Pointer to last node.
* Invariant: (first == null && last == null) ||
*            (last.next == null && last.item != null)
*/
transient Node<E> last;
public boolean add(E e) {
        linkLast(e);
        return true;
}

void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
}

private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
}
```

由此可知，当我们向容器添加一个元素的时候，首先它会将我们加入的元素包装在一个内部类Node里面，这个Node里面包含了它的左右邻居，左邻居即为当前的最末元素，右邻居为空元素，之后将加入的元素放在最末尾，并且记录向前容器内的首尾元素，然后容器的容量值加1。



当我们调用remove\(Object o\)时候发生了什么呢？

```java
public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
}
    
E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
}
```

由此可知，调用remove后，容器首先在内部找到传入的元素，然后调用unlink方法使得当前的元素的左右邻居连接起来，并且将当前元素置空。



总结

LinkedList以双向链表实现。链表无容量限制，但双向链表本身使用了更多空间，每插入一个元素都要构造一个额外的Node对象，也需要额外的链表指针操作。

按下标访问元素－get（i）、set（i,e） 要悲剧的部分遍历链表将指针移动到位 （如果i&gt;数组大小的一半，会从末尾移起）。

插入、删除元素时修改前后节点的指针即可，不再需要复制移动。但还是要部分遍历链表的指针才能移动到下标所指的位置。

只有在链表两头的操作－add（）、addFirst（）、removeLast（）或用iterator（）上的remove（）倒能省掉指针的移动。

Apache Commons 有个TreeNodeList，里面是棵二叉树，可以快速移动指针到位。





