# 对象 {#对象}

### 概述 {#概述}

Redis使用前面的数据结构来构建一个对象系统，包含字符串对象，列表对象，哈希对象，集合对象和有序集合对象这五种类型的对象。根据对象的不同来判断命令是否可以被执行 引用计数计数来进行内存回收机制，对象带有访问时间的记录信息，可以用于计算数据库键的空转时长

### 类型与编码 {#类型与编码}

使用对象来表示数据库中的键和值，因此每次使用redis数据库的时候会至少创建两个对象。其中一个座位键值对的键对象，另一个作为值对象。

```
typedef struct redisObject {

    // 类型 REDIS_STRING REDIS_LIST REDIS_HASH REDIS_SET REDIS_ZET
    unsigned type:4;

    // 编码
    unsigned encoding:4;

    // 对象最后一次被访问的时间
    unsigned lru:REDIS_LRU_BITS; /* lru time (relative to server.lruclock) */

    // 引用计数
    int refcount;

    // 指向实际值的指针，指向对象的底层实现数据结构，这些数据结构由对象的encoding属性决定
    void *ptr;

} robj;
```

在redis数据库中保存的键总是一个字符串对象，而值可以是是字符串对象，列表对象，哈希对象，集合对象，有序集合对象其中之一。因此称redis数据库中的字符串键指的是键对应的值的对象类型 执行TYPE命令时可以查看数据库键值对应的对象类型 SET RPUSH HMSET SADD ZADD分别对应字符串对象，列表对象，哈希对象，集合对象，有序集合对象。 对象的编码定义宏：

```
// 对象编码, 在cli中可以使用OBJECT ENCODING命令来查看数据库键对应的值对象的编码
#define REDIS_ENCODING_RAW 0     /* Raw representation */
#define REDIS_ENCODING_INT 1     /* Encoded as integer */
#define REDIS_ENCODING_HT 2      /* Encoded as hash table */
#define REDIS_ENCODING_ZIPMAP 3  /* Encoded as zipmap */
#define REDIS_ENCODING_LINKEDLIST 4 /* Encoded as regular linked list */
#define REDIS_ENCODING_ZIPLIST 5 /* Encoded as ziplist */
#define REDIS_ENCODING_INTSET 6  /* Encoded as intset */
#define REDIS_ENCODING_SKIPLIST 7  /* Encoded as skiplist */
#define REDIS_ENCODING_EMBSTR 8  /* Embedded sds string encoding */
```



