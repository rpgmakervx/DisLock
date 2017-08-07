package org.easyarch.dislock.lock.impl;

import org.apache.commons.lang.StringUtils;
import org.easyarch.dislock.lock.Lock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.sys.SysProperties;
import org.easyarch.dislock.zk.curator.ZKKits;
import org.easyarch.dislock.zk.curator.watcher.NodeChangeWatcher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-8-3.
 */
public class ZLock implements Lock{

    private static final ConcurrentHashMap<String,CountDownLatch> latches = new ConcurrentHashMap<>();

    //格式为：/dislock/{业务线}/{应用名}/lock-
    private volatile String nodePath;

    //格式为：/dislock/{业务线}/{应用名}
    private volatile String basePath;

    private volatile boolean start = true;

    protected static final long DEFAULT_KEY_EXPIRE = 20*1000;

    protected volatile long keyExpire;

    protected volatile boolean locked;

    public ZLock(String basePath){
        this(basePath,DEFAULT_KEY_EXPIRE);
    }

    public ZLock(String basePath, long keyExpire){
        this.basePath = basePath;
        this.nodePath = basePath + "/"+LOCK_KEY_NAME;
        this.keyExpire = keyExpire;
        ZKKits.init(this);
        init();
    }

    private void init(){
        LockEntity entity = LockEntity.newEntity(keyExpire + SysProperties.sysMillisTime());
        try {
            ZKKits.createEphSeqNode(nodePath,entity.toBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public long getExpire(){
        return keyExpire;
    }

    public CountDownLatch getLatch(String instanceId){
        return latches.get(instanceId);
    }

    /**
     * 具体流程：
     *
     *
     * @param isTimeOut
     * @param timeout
     * @param unit
     * @param interruptable
     * @return
     */
    protected boolean lock0(boolean isTimeOut, long timeout, TimeUnit unit, boolean interruptable) throws InterruptedException {
        if (!aquire(interruptable)){
            CountDownLatch latch = new CountDownLatch(1);
            this.latches.put(SysProperties.uniqueId(),latch);
            if (isTimeOut&&unit!=null){
                latch.await(timeout,unit);
                //方法调用超时，获取锁失败
                this.latches.remove(SysProperties.uniqueId());
                return false;
            }else{
                latch.await();
                this.latches.remove(SysProperties.uniqueId());
                return true;
            }
        }
        return true;
    }

    /**
     * 原则是一个实例在zk目录下一次只能有一个临时节点存在
     *
     * 顺序临时节点方式的具体流程：
     * 1.启动前，先在一个新的线程插入一个节点，（坑1：如果没有这个节点，一开始并发的多个线程可能会同时获得锁，而且一定要是新的线程，防止被第一个实例重入）
     * 2.获取根节点的全部子节点（按从小到大顺序）
     * 3.获取最小的那个节点，判断可重入,先获取节点信息，判断是否是当前实例
     * 4.是当前实例，则说明是获取到重入锁，更新data数据，获取锁成功
     * 5.不是当前实例，则尝试创建子节点（坑2：创建这一步不能放在判断重入前，由于顺序节点必定创建成功，这样会对后续的节点造成干扰）
     * 6.判断当前是不是该锁对象第一次创建zk目录，是的话则删除第一步启动时创建的子节点（从所有实例中分出一个获取锁的，对应坑1）
     * 7.再次获取根节点所有排序后的子节点，找出第一个小于当前创建节点的子节点，监听这个节点
     * （坑3：监听时注意为了在监听器获取latch放开锁执行，必须要把实例id通过参数传递进去，不能再监听器使用Thread.currentThread,因为监听器处于一个新的线程中。）
     * 8.如果通过第7步判断发现自己就是最小的节点，则获取锁成功
     *
     *
     * @param interruptable
     * @return
     * @throws InterruptedException
     */
    private boolean aquire(boolean interruptable) throws InterruptedException {
        if (interruptable){
            //中断状态检查
            checkInterrupt();
        }
        LockEntity entity = LockEntity.newEntity(keyExpire + SysProperties.sysMillisTime());
        try {
            List<String> nodes = ZKKits.getSortedNodes(basePath);
            //判断可重入
            if (nodes!= null&&!nodes.isEmpty()){
                String minNode = nodes.get(0);
                LockEntity zkEntity = LockEntity.getEntity(ZKKits.getData(basePath + "/" + minNode));
                //确认最小节点就是当前实例
                if (zkEntity.isCurrentInstance()){
                    System.out.println("满足重入条件");
                    zkEntity.incrCount();
                    ZKKits.setData(basePath + "/" + minNode,zkEntity.toBytes());
                    return locked(true);
                }
            }
            String currentNode = ZKKits.createEphSeqNode(nodePath,entity.toBytes());
            Long currentNodeNum = Long.valueOf(currentNode.split("-")[1]);
            if (start){
                start = false;
                String defaultNode = nodes.get(0);
                ZKKits.rmNode(basePath + "/" + defaultNode);
            }
            nodes = ZKKits.getSortedNodes(basePath);
            Collections.reverse(nodes);
            //找到相邻的比当前节点小的节点则监听它，否则自己就是最小节点，获取到锁
            for (String node:nodes){
                Long num = Long.valueOf(node.split("-")[1]);
                if (currentNodeNum > num){
                    if(ZKKits.checkExist(basePath + "/" + node)){
                        String instanceId = SysProperties.uniqueId();
                        ZKKits.watchNode(basePath + "/" + node,
                                new NodeChangeWatcher(this,basePath,instanceId));
                        return locked(false);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            locked(false);
        }
        return locked(true);
    }

    private boolean locked(boolean flag){
        return this.locked = flag;
    }

    @Override
    public void lock() {
        try {
            lock0(false,0,null,false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        aquire(true);
    }

    @Override
    public boolean tryLock() {
        try {
            return aquire(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return false;
    }

    @Override
    public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * 释放锁步骤：
     * 1.获得最小节点，不存在说明没有节点，属于违规操作，抛异常
     * 2.获取最小节点内容，如果不是当前实例获取锁，属于违规操作，抛异常.
     * 3.是当前实例，则判断计数器，计数器为1，说明没有可重入状态，则直接删除节点
     * 4.否则修改节点内容，计数器减一，不删除节点。
     */
    @Override
    public void unlock() {
        try {
            String minNode = ZKKits.getMinNode(basePath);
            String nodePath = basePath + "/" + minNode;
            //没有锁不能凭空操作
            if (StringUtils.isBlank(minNode)){
                throw new IllegalMonitorStateException();
            }
            LockEntity entity = LockEntity.getEntity(ZKKits.getData(nodePath));
            if (entity.isCurrentInstance()){
                if (entity.getCount() == 1){
                    ZKKits.rmNode(nodePath);
                    System.out.println(Thread.currentThread().getName()+" - 释放锁");
                    this.locked = false;
                }else{
                    entity.decrCount();
                    ZKKits.setData(nodePath,entity.toBytes());
//                        System.out.println(Thread.currentThread().getName()+" - 减少重入次数");
                }
                return;
            }
            throw new IllegalMonitorStateException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断开始时间点+超时时间间隔是否超时
     * @param start 开始时间
     * @param expire 超时间隔
     * @return
     */
    private boolean isTimeOut(long start,long expire){
        return start + expire < SysProperties.sysMillisTime();
    }

    /**
     *  判断约定的时间是否超时
     * @param timestamp 约定的超时时间点
     * @return
     */
    private boolean isTimeOut(long timestamp){
        return timestamp < SysProperties.sysMillisTime();
    }

    private void checkInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()){
            throw new InterruptedException();
        }
    }

}
