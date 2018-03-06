ArrayList是一个相对来说比较简单的数据结构，最重要的一点就是它的自动扩容，可以认为就是我们常说的“动态数组”。  
来看一段简单的代码：

```
ArrayList<String> list = new ArrayList<String>();
list.add("语文: 99");
list.add("数学: 98");
list.add("英语: 100");
list.remove(0);
```

在执行这四条语句时，是这么变化的：  
[![](https://cloud.githubusercontent.com/assets/1736354/6993037/5d4ba306-db19-11e4-85fb-61b0154d0d96.png)](https://cloud.githubusercontent.com/assets/1736354/6993037/5d4ba306-db19-11e4-85fb-61b0154d0d96.png)  
其中，`add`操作可以理解为直接将数组的内容置位，`remove`操作可以理解为删除index为0的节点，并将后面元素移到0处。

### 2. add函数 {#2-_add函数}

当我们在ArrayList中增加元素的时候，会使用`add`函数。他会将元素放到末尾。具体实现如下：

```java
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}
```

我们可以看到他的实现其实最核心的内容就是`ensureCapacityInternal`。这个函数其实就是**自动扩容机制的核心**。我们依次来看一下他的具体实现

```java
private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;

    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}

private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    // 扩展为原来的1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    // 如果扩为1.5倍还不满足需求，直接扩为需求值
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

也就是说，当增加数据的时候，如果ArrayList的大小已经不满足需求时，那么就将数组变为原长度的1.5倍，之后的操作就是把老的数组拷到新的数组里面。例如，默认的数组大小是10，也就是说当我们`add`10个元素之后，再进行一次add时，就会发生自动扩容，数组长度由10变为了15具体情况如下所示：  
[![](https://cloud.githubusercontent.com/assets/1736354/6993129/e892246e-db1c-11e4-9ae8-f9719688a1ca.png)](https://cloud.githubusercontent.com/assets/1736354/6993129/e892246e-db1c-11e4-9ae8-f9719688a1ca.png)

### 3 set和get函数 {#3_set和get函数}

Array的put和get函数就比较简单了，先做index检查，然后执行赋值或访问操作：

```java
public E set(int index, E element) {
    rangeCheck(index);

    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}

public E get(int index) {
    rangeCheck(index);

    return elementData(index);
}
```

### 4 remove函数

```java
public E remove(int index) {
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);

    int numMoved = size - index - 1;
    if (numMoved > 0)
        // 把后面的往前移
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    // 把最后的置null
    elementData[--size] = null; // clear to let GC do its work

    return oldValue;
}
```

总结

ArrayList以数组实现。节约空间，但数组有容量限制。超出限制时会增加50%容量，用System.arraycopy\(\)复制到新的数组。因此最好能给出数组大小的预估值。默认第一次插入元素时创建大小为10的数组。

按数组下标访问元素－get（i）、set（i,e） 的性能很高，这是数组的基本优势。

如果按下标插入元素、删除元素－add（i,e）、 remove（i）、remove（e），则要用System.arraycopy\(\)来复制移动部分受影响的元素，性能就变差了。

越是前面的元素，修改时要移动的元素越多。直接在数组末尾加入元素－常用的add（e），删除最后一个元素则无影响。

面试题

1、ArrayList的大小是如何自动增加的？你能分享一下你的代码吗？

这是最有技巧性的的一个问题，大多数人都无法回答。事实上，当有人试图在ArrayList中增加一个对象的时候，Java会去检查ArrayList，以确保已存在的数组中有足够的容量来存储这个新的对象。如果没有足够容量的话，那么就会新建一个长度更长的数组，旧的数组就会使用Arrays.copyOf\(\)方法被复制到新的数组中去，现有的数组引用指向了新的数组。

2、什么情况下你会使用ArrayList？什么时候你会选择LinkedList？

这又是一个大多数面试者都会困惑的问题。多数情况下，当你遇到访问元素比插入或者是删除元素更加频繁的时候，你应该使用ArrayList。另外一方面，当你在某个特别的索引中，插入或者是删除元素更加频繁，或者你压根就不需要访问元素的时候，你会选择LinkedList。这里的主要原因是，在ArrayList中访问元素的最糟糕的时间复杂度是”1″，而在LinkedList中可能就是”n”了。在ArrayList中增加或者删除某个元素，通常会调用System.arraycopy方法，这是一种极为消耗资源的操作，因此，在频繁的插入或者是删除元素的情况下，LinkedList的性能会更加好一点。

3、当传递ArrayList到某个方法中，或者某个方法返回ArrayList，什么时候要考虑安全隐患？如何修复安全违规这个问题呢？

当ArrayList被当做参数传递到某个方法中，如果ArrayList在没有被复制的情况下直接被分配给了成员变量，那么就可能发生这种情况，即当原始的数组被调用的方法改变的时候，传递到这个方法中的数组也会改变。下面的这段代码展示的就是安全违规以及如何修复这个问题。

ArrayList被直接赋给成员变量——安全隐患：

[![](http://incdn1.b0.upaiyun.com/2014/03/6e195cbae211d3470879ae52f62cfe39-300x52.jpg "1.Array-Stored-Directly-Considered-as-Security-Violation")](http://incdn1.b0.upaiyun.com/2014/03/6e195cbae211d3470879ae52f62cfe39.jpg)

修复这个安全隐患：

[![](http://incdn1.b0.upaiyun.com/2014/03/81523f6d0a66f53529ccc7ce4810350d-300x71.jpg "2.Copy-the-Array-as-a-Fix-for-the-Security-Violation")](http://incdn1.b0.upaiyun.com/2014/03/81523f6d0a66f53529ccc7ce4810350d.jpg)

4、如何复制某个ArrayList到另一个ArrayList中去？

使用

1. clone\(\)方法，比如：ArrayList newArray = oldArray.clone\(\);

2. 使用ArrayList构造方法，比如：ArrayList myObject = new ArrayList\(myTempObject\);

3. 使用Collection的copy方法。

注意1和2是浅拷贝\(shallow copy\)。

