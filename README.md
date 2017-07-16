# DisLock 
### 简介
dislock是一个基于多种存储引擎的分布式锁中间件，对用户提供分布式锁服务，对上层接口抽象并屏蔽底层实现，能够让用户自主切换不同的引擎但并不用修改代码逻辑。

分布式锁的目标是多节点环境下同步访问共享资源的一种方式，当我们需要集群所共享的资源数据保持一致时，需要通过分布式锁来协调控制集群对资源的访问。

### 方案
实现分布式锁的方案分两种：
* 通过集群中一块全局存储介质作为锁资源，通过对资源的抢占实现业务逻辑的单行。
* 请求串行化，集群无需抢占资源，在请求全局存储介质时做排队，达到一次只有一个节点访问全局存储资源

本文介绍第一种方式，其中第一种方式也由于使用的存储介质不同，实现方式也有差异，简单列举一下常用的几种方案：
1. 基于磁盘的介质，数据库，文件等。
2. 基于内存的介质，memcache，redis等
3. zookeeper比较特别，虽然他的数据也在内存中，但他具备事件通知机制，能够告诉监听者内存数据的变化（实际上这一点 redis也可以通过 key notification来实现）

目前暂时实现了redis的方式（因为key notification需要配置开启，并不是所有的运维环境都允许支持，所以不算做通用解决方案）


### 特性
* 支持阻塞锁（lock），非阻塞锁（tryLock）
* 支持可重入

### 快速使用
阻塞锁：
```java
public static void print(Lock lock){
	try{
	    lock.lock();
	    System.out.println(Thread.currentThread().getId()+"获取到锁");
	    //do sth
	    Thread.sleep(2000);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    lock.unlock();
	}
}
```

非阻塞锁：

```java
public static void tryPrint(Lock lock){
	try {
	    if (lock.tryLock(1000,TimeUnit.MILLISECONDS)){
	        System.out.println(Thread.currentThread().getId()+"成功获取锁");
	    }else {
	        System.out.println(Thread.currentThread().getId()+"获取锁失败");
	        return;
	    }
	    //do sth
	    Thread.sleep(1000);
	    lock.unlock();
	} catch (Exception e) {
	    e.printStackTrace();
	}
}
```
main调用:
```java
public static void main(String[] args) throws Exception {
	RLock lock = new RLock("bizId-app1");
	Class.forName(LockHook.class.getName());
	new Thread(() -> {
	    print(lock);
	},"thread-"+1).start();
	Thread.sleep(1000);
	new Thread(() -> {
	    print(lock);
	},"thread-"+2).start();
}
```

###  lock0部分的流程图：
``` flow

 st=>start:开始
 e=>end:结束
 op1=>operation:操作步骤
 cond=>condition:是 或者 否?
 op2=>operation:还有一步呢
 op3=>operation:end 在哪儿？
 st->op1-cond
 cond(no)->op1
 cond(yes)-op2
 op2->op3->e->
 
```