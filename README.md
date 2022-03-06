> Actor 模型及 Akka 简介

# V1.0 ReleaseNote
* 使用protostuff序列化（.proto文件编写恶心，与Protocol Buffer性能几乎接近）
* 使用Netty进行通讯（同节点RPC不走网络，直接入收件箱队列）；
* 路由策略：随机路由、指定Key路由、资源Id路由、强制路由
* 使用ZK进行集群状态管理
* 使用自定义注解进行服务注册及辅助控制（线程数量、方法名称设置等）

不带路由测试示例代码入口：
cn.bossfridy.rpc.test.actorsystem.Bootstrap

带路由测试示例代码入口（依赖ZK，配置文件：test/resources/servie-config.xml）：
cn.bossfridy.rpc.test.router.Bootstrap

# 1. 背景
随着业务的发展，现代分布式系统对于垂直扩展、水平扩展、容错性的要求越来越高。常见的一些编程模式已经不能很好的解决这些问题。  

解决并发问题核心是并发线程中的数据通讯问题，一般有两种策略：

* 共享数据
* 消息传递

##### 共享数据
基于 JVM 内存模型的设计，需要通过加锁等同步机制保证共享数据的一致性。但其实使用锁对于高并发系统并不是一个很好的解决方案：

运行低效，代价昂贵，非常限制并发。
调用线程会被阻塞，以致于它不能去做其他有意义的任务。
很难实现，比较容易出现死锁等各种问题。

##### 消息传递
与共享数据方式相比，消息传递机制的最大优点就是不会产生竞争。实现消息传递的两种常见形式：

* 基于 Channel 的消息传递
* 基于 Actor 模型的消息传递


# 2. Actor 模型
Actor 的基础就是消息传递，一个 Actor 可以认为是一个基本的计算单元，它能接收消息并基于其执行运算，它也可以发送消息给其他 Actor。Actors 之间相互隔离，它们之间并不共享内存。

Actor 本身封装了状态和行为，在进行并发编程时，Actor 只需要关注消息和它本身。而消息是一个不可变对象，所以 Actor 不需要去关注锁和内存原子性等一系列多线程常见的问题。

所以 Actor 是由状态（State）、行为（Behavior）和邮箱（MailBox，可以认为是一个消息队列）三部分组成：

* 状态：Actor 中的状态指 Actor 对象的变量信息，状态由 Actor 自己管理，避免了并发环境下的锁和内存原子性等问题。
* 行为：Actor 中的计算逻辑，通过 Actor 接收到的消息来改变 Actor 的状态。
* 邮箱：邮箱是 Actor 和 Actor 之间的通信桥梁，邮箱内部通过 FIFO（先入先出）消息队列来存储发送方 Actor 消息，接受方 Actor 从邮箱队列中获取消息。

## 2.1 模型概念
![IMG](https://pic1.zhimg.com/80/v2-f91a4ec82f8175bcb40ee582d6d59b24_720w.jpg)  
可以看出按消息的流向，可以将 Actor 分为发送方和接收方，一个 Actor 既可以是发送方也可以是接受方。

另外我们可以了解到 Actor 是串行处理消息的，另外 Actor 中消息不可变。

##### Actor 模型特点
* 对并发模型进行了更高的抽象。
* 使用了异步、非阻塞、高性能的事件驱动编程模型。
* 轻量级事件处理（1 GB 内存可容纳百万级别 Actor）。
* 简单了解了 Actor 模型，我们来看一个基于其实现的框架。

##### Akka Actor
Akka 是一个构建在 JVM 上，基于 Actor 模型的的并发框架，为构建伸缩性强，有弹性的响应式并发应用提高更好的平台。

##### ActorSystem
ActorSystem 可以看做是 Actor 的系统工厂或管理者。主要有以下功能：

* 管理调度服务
* 配置相关参数

##### ActorRef
ActorRef 可以看做是 Actor 的引用，是一个 Actor 的不可变，可序列化的句柄（handle），它可能不在本地或同一个 ActorSystem 中，它是实现网络空间位置透明性的关键设计。

ActorRef 最重要功能是支持向它所代表的 Actor 发送消息：

##### Dispatcher 和 MailBox（queue）
ActorRef 将消息处理能力委派给 Dispatcher，实际上，当我们创建 ActorSystem 和 ActorRef 时，Dispatcher 和 MailBox 就已经被创建了。

Dispatcher 从 ActorRef 中获取消息并传递给 MailBox，Dispatcher 封装了一个线程池，之后在线程池中执行 MailBox。

##### 流程
通过了解上面的一些概念，我们可以 Akka Actor 的处理流程归纳如下：

* 创建 ActorSystem
* 通过 ActorSystem 创建 ActorRef，并将消息发送到 ActorRef
* ActorRef 将消息传递到 Dispatcher中
* Dispatcher 依次的将消息发送到 Actor 邮箱中
* Dispatcher 将邮箱推送至一个线程中
* 邮箱取出一条消息并委派给 Actor 的 receive 方法

# 3. 总结
在Actor模型中，一切都可以抽象为Actor。
而Actor是封装了状态和行为的对象，他们的唯一通讯方式就是交换消息，交换的消息放在接收方的邮箱(Inbox)里。也就是说Actor之间并不直接通信，而是通过消息来相互沟通，每一个Actor都把它要做的事情都封装在了它的内部。

每一个Actor是可以有状态也可以是无状态的，理论上来讲，每一个Actor都拥有属于自己的轻量级线程，保护它不会被系统中的其他部分影响。因此，我们在编写Actor时，就不用担心并发的问题。

通过Actor能够简化锁以及线程管理，Actor具有以下的特性：

* 提供了一种高级的抽象，能够封装状态和操作。简化并发应用的开发。
* 提供异步非阻塞/高性能的事件驱动模型，避免锁的滥用。
* 超级轻量级的线程事件处理能力。



