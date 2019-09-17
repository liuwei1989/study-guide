**&lt;dubbo:service /&gt;用于服务生产者暴露服务配置**

| **属    性** | **类    型** | **是否必填** | **缺省值** | **描    述** |
| :--- | :--- | :--- | :--- | :--- |
| interface | class | 必填 | 无 | 服务接口全路径 |
| ref | object | 必填 | 无 | 服务对象实现引用 |
| version | string | 可选 | 0.0.0 | 服务版本，建议使用两位数字版本如1.0，通常在接口不兼容时版本号才需要升级 |
| timeout | int | 可选 | 1000 | 远程服务调用超时时间（毫秒） |
| retries | int | 可选 | 2 | 远程服务调用重试次数，不包括第一次调用，不需要重试请设为0 |
| connections | int | 可选 | 100 | 每个生产者的最大连接数，短连接协议如rmi，表示限制连接数；长连接协议如dubbo表示建立的长连接个数 |
| loadbalance | string | 可选 | random | 负载均衡策略，可选值为：random（随机）、roundrobin（轮询）、leastactive（最少活跃调用） |
| async | boolean | 可选 | false | 是否缺省异步执行，不可靠的异步，只是忽略返回值，不阻塞执行线程 |
| register | boolean | 可选 | true | 该协议的服务是否注册到注册中心 |

  
**&lt;dubbo:reference /&gt;用于服务消费者引用服务配置**

| **属    性** | **类    型** | **是否必填** | **缺省值** | **描    述** |
| :--- | :--- | :--- | :--- | :--- |
| id | string | 必填 | 无 | 服务引用beanId |
| interface | class | 必填 | 无 | 服务接口全路径 |
| version | string | 可选 | 无 | 服务版本，与服务生产者的版本一致 |
| timeout | long | 可选 | 使用&lt;dubbo:consumer&gt;的timeout  | 服务方法调用超时时间（毫秒） |
| retries | int | 可选 | 使用&lt;dubbo:consumer&gt;的retries | 远程服务调用重试次数，不包括第一次调用，不需要重试请设为0 |
| connections  | int  | 可选  | 使用&lt;dubbo:consumer&gt;的connections  | 每个生产者的最大连接数，短连接协议如rmi，表示限制连接数；长连接协议如dubbo表示建立的长连接个数 |
| loadbalance  | string  | 可选  | 使用&lt;dubbo:consumer&gt;的loadbalance  | 负载均衡策略，可选值为：random（随机）、roundrobin（轮询）、leastactive（最少活跃调用）  |
|  async | boolean  | 可选  | 使用&lt;dubbo:consumer&gt;的async  | 是否缺省异步执行，不可靠的异步，只是忽略返回值，不阻塞执行线程  |
| check | boolean  | 可选 | 使用&lt;dubbo:consumer&gt;的check  | 启动时检查服务生产者是否存在，true则报错，false则忽略  |
|  url | string  | 可选  | 无  | 点对点直连服务提供者地址，将绕过注册中心，比如"dubbo://localhost:20890"，这个比较多的使用在测试中  |
| protocol  | string  | 可选  | 无 | 只调用指定协议的服务生产者，其他协议忽略  |



**&lt;dubbo:protocol /&gt;用于服务生产者协议配置（如果需要支持多协议，可以声明多个此标签，并在&lt;dubbo:service /&gt;通过protocol属性指定使用的协议）**

| **属    性** | **类    型** | **是否必填** | **缺省值** | **描    述** |
| :--- | :--- | :--- | :--- | :--- |
| id | string | 可选 | dubbo | 协议beanId，&lt;dubbo service /&gt;中的protocol引用此ID，如果不填缺省和name属性值一样 |
| name | sring | 必填 | dubbo | 协议名称 |
| port | int | 可选 | dubbo-&gt;20800,rmi-&gt;1099,http-&gt;80,hessian-&gt;80如果配置为-1或未配置，则会分配一个没有被占用的端口 | 服务端口 |
| host | string | 可选 | 自动查找本机ip | 为空则自动查找本机ip，建议不配置让Dubbo自动获取本机ip |
| threadpool | string | 可选 | fixed | 线程池类型，可选fixed/cached |
| threads | int | 可选 | 100 | 服务线程池大小（固定大小） |
| serialization | string | 可选 | dubbo-&gt;hession2,rmi-&gt;java,http-&gt;json | 协议序列化方式，当协议支持多种序列化方式时使用 |
| register | boolean | 可选 | true | 该协议的服务是否注册到注册中心 |



**&lt;dubbo:registry /&gt;用于注册中心配置（如果有多个不同的注册中心可以声明多个标签并且&lt;dubbo:service /&gt;或&lt;dubbo:reference /&gt;中使用registry属性指定）**

| **属    性** | **类    型** | **是否必填** | **缺省值** | **描    述** |
| :--- | :--- | :--- | :--- | :--- |
| id | string | 可选 | 无 | 注册中心引用beanId，可在&lt;dubbo:service /&gt;或&lt;dubbo:reference /&gt;中引用此ID |
| address | string | 必填 | 无 | 注册中心服务地址，如果地址没有端口缺省为9090，同一个集群内的多个地址用逗号分隔，如：ip:port,ip:port，不同的集群注册中心请配置多个&lt;dubbo:registry /&gt;标签 |
|  protocol | string  |  可选  | dubbo  | 注册中心地址协议，支持dubbo、http、local三种协议，分别表示dubbo地址、http地址和本地注册中心  |
| port | int | 可选 | 9090 | 注册中心缺省端口，当address没有带端口时使用此端口作为缺省值  |
| username | string | 可选 | 无 | 登陆注册中心用户名，如果注册中心不需要验证可不填 |
| password | string | 可选 | 无 | 登陆注册中心密码，如果注册中心不需要验证可不填  |
| transport | string | 可选 | netty | 网络传输方式，可选mina、netty |
| timeout | int | 可选 | 5000 | 注册中心请求超时时间（毫秒） |
| file | string | 可选 | 无 | 使用文件缓存注册中心地址列表以及服务提供者列表，应用重启时将基于此文件恢复，注意两个注册中心不能使用同一文件存储 |
| check | boolean | 可选 | true | 注册中心不存在时，是否报错 |
| register | boolean | 可选 | true | 是否向此注册中心注册服务，如果设为false，将只订阅，不注册 |
| subscribe | boolean | 可选 | true | 是否向此注册中心订阅服务，如果设为false，将只注册，不订阅 |



**&lt;dubbo:method /&gt;用于方法级配置（该标签为&lt;dubbo:service/&gt;或&lt;dubbo:reference/&gt;的子标签，用于控制到方法级）**

| **属    性** | **类    型** | **是否必填** | **缺省值** | **描    述** |
| :--- | :--- | :--- | :--- | :--- |
| method | string | 必填 | 无 | 方法名 |
| timeout | int | 可选 | 缺省为&lt;dubbo:reference/&gt;的timeout | 方法调用超时时间（毫秒） |
| retires | int | 可选 | 缺省为&lt;dubbo:reference/&gt;的retries | 远程服务调用重试次数，不包括第一次调用，不需要重试请设为0 |
| loadbalance | string | 可选 | 缺省为&lt;dubbo:reference/&gt;的loadbalance | 负载均衡策略，可选值为：random（随机）、roundrobin（轮询）、leastactive（最少活跃调用）  |
| async | boolean | 可选 | 缺省为&lt;dubbo:reference/&gt;的async | 是否异步执行，不可靠异步，只是忽略返回值，不阻塞执行线程 |
| actives | int | 可选 | 0 | 每服务消费者最大并发调用限制 |
|  executes | int  | 可选 | 0 | 每服务每方法最大使用线程数限制，此属性只在&lt;dubbo:method/&gt;作为&lt;dubbo:service/&gt;子标签时有效 |



