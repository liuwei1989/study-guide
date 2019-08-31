# 加密模式

`ECB:`电子密码本，就是每个块都是独立加密的。

![](https://upload-images.jianshu.io/upload_images/3691932-13fb4d5ef715c503.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/451)

`CBC`：密码块链，使用一个密钥和一个初始化向量\(IV\)对数据执行加密转换。

![](https://upload-images.jianshu.io/upload_images/3691932-9009243545b61bc1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/528)

只要是对称加密都有 ECB和 CBC模式，加密模式是加密过程对独立数据块的处理。对于较长的明文进行加密需要进行分块加密，在实际开发中，推荐使用CBC的，ECB的要少用。

