package org.easyarch.dislock.lock.impl;

import org.easyarch.dislock.lock.AbstractLock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.sys.SysProperties;
import org.easyarch.dislock.zk.curator.WatchMinSeqListener;
import org.easyarch.dislock.zk.curator.ZKKits;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-8-3.
 */
public class ZKLock extends AbstractLock{

    private static final ConcurrentHashMap<String,CountDownLatch> latches = new ConcurrentHashMap<>();

    //格式为：/dislock/{业务线}/{应用名}/lock-
    private volatile String nodePath;

    //格式为：/dislock/{业务线}/{应用名}
    private volatile String basePath;

    private volatile boolean start = true;

    public ZKLock(String basePath){
        this(basePath,DEFAULT_KEY_EXPIRE);
    }

    public ZKLock(String basePath, long keyExpire){
        this.basePath = basePath;
        this.nodePath = basePath + "/"+LOCK_KEY_NAME;
        this.keyExpire = keyExpire;
        ZKKits.init(this);
        init();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//            }
//        }).start();
    }

    private void init(){
        LockEntity entity = LockEntity.newEntity(keyExpire + SysProperties.sysMillisTime());
        try {
            ZKKits.createEphSeqNode(nodePath,entity.toBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CountDownLatch getLatch(String instanceId){
        return latches.get(instanceId);
    }

    /**
     * 废弃：
//     * 具体流程：
//     * 在basePath下创建临时有序节点，
//     * 然后获取basePath下所有有序节点，获得他们的序号，
//     * 通过自己的序号和他们的序号进行对比，得到比自己小的相邻的一个节点。
//     * 监听这个节点的删除事件。
//     * 当删除事件触发时，获得通知的节点需要先检查一下自己是否是basePath下序号最小的节点（防止客户端获取锁之前断开连接，导致次小节点获取锁）
//     * 如果是最小节点，获取到锁，否则不获取锁。
//     * 当方法设置超时时间时，通过对比当前时间与方法调用时间，判断超时则获取锁失败，退出方法
     *
     * 具体流程：
     *
     *
     * @param isTimeOut
     * @param timeout
     * @param unit
     * @param interruptable
     * @return
     */
    @Override
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
     * 持久节点方式
     * @param interruptable
     * @return
     */
//    private boolean aquire0(boolean interruptable){
//
//    }

    /**
     * 顺序临时节点方式
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
                    this.locked = true;
                    return true;
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
                        ZKKits.watchNode(basePath + "/" + node,new WatchMinSeqListener(
                                this,basePath,SysProperties.uniqueId()));
                        this.locked = false;
                        return false;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            this.locked = false;
            return false;
        }
        this.locked = true;
        return true;
    }

    @Override
    public boolean isLocked() {
        if (!locked){
            return false;
        }
        return false;
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            String minNode = ZKKits.getMinNode(basePath);
            String nodePath = basePath + "/" + minNode;
            LockEntity entity = LockEntity.getEntity(ZKKits.getData(nodePath));
            if (entity != null){
                if (entity.isCurrentInstance()){
                    if (entity.getCount() == 1){
                        ZKKits.rmNode(nodePath);
                        System.out.println(Thread.currentThread().getName()+" - 释放锁");
                        this.locked = false;
                    }else{
                        entity.decrCount();
                        ZKKits.setData(nodePath,entity.toBytes());
                    }
                    return;
                }
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

    public static void main(String[] args) throws Exception {
        ZKKits.init(new ZKLock("/dislock/suyun/app1"));
//        ZKKits.rmNode("/dislock/suyun/app1/lock-0000000004");
        ZKKits.watchNode("/dislock/suyun/app1",new WatchMinSeqListener(
                new ZKLock("/dislock/suyun/app1"),"/dislock/suyun/app1",SysProperties.uniqueId()));
        ZKKits.watchNode("/dislock/suyun/app1",new WatchMinSeqListener(
                new ZKLock("/dislock/suyun/app1"),"/dislock/suyun/app1",SysProperties.uniqueId()));
//        ZKKits.watchNode("/dislock/suyun/app1",new WatchMinSeqListener(
//                new ZKLock("/dislock/suyun/app1"),"/dislock/suyun/app1",SysProperties.uniqueId()));
        Thread.sleep(100000);
//        ZKKits.createPerNode("/dislock/suyun/app1/"+LOCK_KEY_NAME,"".getBytes());
    }
}
