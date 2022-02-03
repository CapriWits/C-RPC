# C-RPC

## Feature

- 使用 `Etcd` 作为 RPC 注册中心
- 不同于 Zookeeper 的 【services list - host】存储形式，使用 【host - services list】存储
- 序列化使用 `Protostuff`
- `Netty` 作 IO多路复用
- 客户端定时任务，定期拉取 services list，缓存在客户端，调用时的 LoadBalance 在客户端做
- 仿 Dubbo 实现 SPI 机制



## TODO

- [x] ServiceProvider based on **SPI** 

- [x] ServiceDiscovery based on **Etcd**

- [x] ServiceProvider based on **Etcd**

- [x] ServiceRegistry based on **Etcd**

- [x] Custom Netty codec

- [x] Add LoadBalance

- [x] Add **GZPI** Compress

- [x] Bean To Json to solve Etcd K-V one-to-one mapping problem based on **Gson**

- [x] Update Etcd storage structure from **[ServiceName, HostList]** to **[Host, ServicesList]**

