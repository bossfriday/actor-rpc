**该项目停止维护，最新版本请详见：https://github.com/bossfriday/bossfriday-nubybear 中的common模块，本项目只做为一个学习用途原型**

> 基于Actor模型用JAVA实现的RPC介绍

# 1. ReleaseNote
## 1.1 V1.0 
* **使用protostuff序列化（.proto文件编写恶心，与Protocol Buffer性能几乎接近）**
* **使用Netty进行通讯（同节点RPC不走网络，直接入收件箱队列）；**
* **路由策略：随机路由、指定Key路由、资源Id路由、强制路由**
* **使用ZK进行集群状态管理**
* **使用自定义注解进行服务注册及辅助控制（线程数量、方法名称设置等）**

不带路由测试示例代码入口：
cn.bossfridy.rpc.test.actorsystem.Bootstrap

带路由测试示例代码入口（依赖ZK，配置文件：test/resources/servie-config.xml）：
cn.bossfridy.rpc.test.router.Bootstrap

## 1.2 V1.1 
* **使用Disruptor优化收件箱、发件箱；**   

# 2. 性能测试表现
详见：https://blog.csdn.net/camelials/article/details/123614068


# 3. Actor 模型及 Akka 简介
## 3.1 背景
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


## 3.2 Actor 模型
Actor 的基础就是消息传递，一个 Actor 可以认为是一个基本的计算单元，它能接收消息并基于其执行运算，它也可以发送消息给其他 Actor。Actors 之间相互隔离，它们之间并不共享内存。

Actor 本身封装了状态和行为，在进行并发编程时，Actor 只需要关注消息和它本身。而消息是一个不可变对象，所以 Actor 不需要去关注锁和内存原子性等一系列多线程常见的问题。

所以 Actor 是由状态（State）、行为（Behavior）和邮箱（MailBox，可以认为是一个消息队列）三部分组成：

* 状态：Actor 中的状态指 Actor 对象的变量信息，状态由 Actor 自己管理，避免了并发环境下的锁和内存原子性等问题。
* 行为：Actor 中的计算逻辑，通过 Actor 接收到的消息来改变 Actor 的状态。
* 邮箱：邮箱是 Actor 和 Actor 之间的通信桥梁，邮箱内部通过 FIFO（先入先出）消息队列来存储发送方 Actor 消息，接受方 Actor 从邮箱队列中获取消息。

### 3.2.1 模型概念
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

## 3.3 总结
在Actor模型中，一切都可以抽象为Actor。
而Actor是封装了状态和行为的对象，他们的唯一通讯方式就是交换消息，交换的消息放在接收方的邮箱(Inbox)里。也就是说Actor之间并不直接通信，而是通过消息来相互沟通，每一个Actor都把它要做的事情都封装在了它的内部。

每一个Actor是可以有状态也可以是无状态的，理论上来讲，每一个Actor都拥有属于自己的轻量级线程，保护它不会被系统中的其他部分影响。因此，我们在编写Actor时，就不用担心并发的问题。

通过Actor能够简化锁以及线程管理，Actor具有以下的特性：

* 提供了一种高级的抽象，能够封装状态和操作。简化并发应用的开发。
* 提供异步非阻塞/高性能的事件驱动模型，避免锁的滥用。
* 超级轻量级的线程事件处理能力。

# 4. Disruptor简介  
Disruptor是一个高性能的有界内存队列，它在 Apache Storm、Camel、Log4j 2 等很多知名项目中都有广泛应用。之所以如此受青睐，主要还是因为它的性能表现非常优秀。它比 Java 中另外一个非常常用的内存消息队列 ArrayBlockingQueue（ABS）的性能，要高一个数量级，可以算得上是最快的内存消息队列了。它还因此获得过 Oracle 官方的 Duke 大奖。

## 4.1 Disruptor 是如何做到如此高性能的？
* 使用 RingBuffer 数据结构，数组元素在初始化时一次性全部创建，提升缓存命中率；对象循环利用，避免频繁 GC。此外根据Index进行环形定位并非简单取模，而是使用位运算，效率更高，定位更快。
* 前后56字节（7个long）缓存行填充的手法，使得每个变量独占一个缓存行，避免伪共享，提升CPU缓存利用率。
* 采用CAS无锁算法，避免频繁加锁、解锁的性能消耗。


**伪共享：** 由于共享缓存行导致缓存无效的场景。
    伪共享和 CPU 内部的 Cache 有关，Cache 内部是按照缓存行（Cache Line）管理的，缓存行的大小通常是 64 个字节。CPU 的缓存就利用了程序的局部性原理：时间局部性（指的是程序中的某条指令一旦被执行，不久之后这条指令很可能再次被执行；如果某条数据被访问，不久之后这条数据很可能再次被访问。）、空间局部性（指某块内存一旦被访问，不久之后这块内存附近的内存也很可能被访问）。
为了更好地利用缓存，我们必须避免伪共享，解决手法为：前后56字节（7个long）缓存行填充。

**CAS：** 比较并交换（CompareAndSwap）。
  本质上是无锁，不过也称之为自旋锁或者自旋。

## 4.2 总结
Disruptor的高性能归根于：1、利用CAS无锁算法避免锁的争用；2、将硬件（CPU）的性能发挥到极致。  
发挥硬件的能力一般是 C 这种面向硬件的语言常干的事儿，C 语言领域经常通过调整内存布局优化内存占用，而 Java 领域则用的很少，原因在于 Java 可以智能地优化内存布局，内存布局对 Java 程序员的透明的。这种智能的优化大部分场景是很友好的，但是如果你想通过填充方式避免伪共享就必须绕过这种优化，因此Disruptor无愧于Oracle 官方的 Duke 大奖。
