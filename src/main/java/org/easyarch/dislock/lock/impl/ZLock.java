package org.easyarch.dislock.lock.impl;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.KeeperException;
import org.easyarch.dislock.lock.AbstractLock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.sys.SysProperties;
import org.easyarch.dislock.zk.WatchMinSeqListener;
import org.easyarch.dislock.zk.ZKClient;
import org.easyarch.dislock.zk.ZKKits;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-8-3.
 */
public class ZLock extends AbstractLock implements TreeCacheListener {

    private static final ConcurrentHashMap<String,CountDownLatch> latches = new ConcurrentHashMap<>();

    private volatile CountDownLatch latch;

    //格式为：/dislock/{业务线}/{应用名}/lock-
    private volatile String nodePath;

    //格式为：/dislock/{业务线}/{应用名}
    private volatile String basePath;

    public ZLock(String basePath){
        this(basePath,DEFAULT_KEY_EXPIRE);
    }

    public ZLock(String basePath, long keyExpire){
        this.basePath = basePath;
        this.nodePath = basePath + "/"+LOCK_KEY_NAME;
        this.keyExpire = keyExpire;
        ZKKits.init(this);
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



    private boolean aquire(boolean interruptable) throws InterruptedException {
        if (interruptable){
            //中断状态检查
            checkInterrupt();
        }
        LockEntity entity = LockEntity.newEntity(keyExpire + SysProperties.sysMillisTime());
        try {
            String currentNode = ZKKits.createEphSeqNode(nodePath,entity.toBytes());
            List<String> nodes = ZKKits.getSortedNodes(basePath);
            Long currentNodeNum = Long.valueOf(currentNode.split("-")[1]);
//            System.out.println(Thread.currentThread().getName()+" - currentNodeNum:"+currentNodeNum);
//            System.out.println(Thread.currentThread().getName()+" - nodes:"+nodes);
            //找到相邻的比当前节点小的节点则监听它，否则自己就是最小节点，获取到锁
            for (String node:nodes){
                Long num = Long.valueOf(node.split("-")[1]);
                if (currentNodeNum - num == 1){
                    ZKKits.watchNode(basePath + "/" + node,new WatchMinSeqListener(
                            this,basePath,basePath + "/" + node,SysProperties.uniqueId()));
                    return false;
                }
            }
//            System.out.println(Thread.currentThread().getName()+" - 当前节点最小");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isLocked() {
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
            System.out.println("delete path: "+basePath + "/" + minNode);
            ZKKits.rmNode(basePath + "/" + minNode);
            System.out.println(Thread.currentThread().getName()+" - 释放锁");
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
        ZKKits.init(new ZLock("/dislock/suyun/app1"));
        ZKKits.rmNode("/dislock/suyun/app1/lock-0000000004");

//        ZKKits.createPerNode("/dislock/suyun/app1/"+LOCK_KEY_NAME,"".getBytes());
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        if (!TreeCacheEvent.Type.NODE_REMOVED.equals(event.getType())){
            return ;
        }
        List<String> nodes = ZKKits.getSortedNodes(basePath);
        if(this.latch != null) {
            this.latch.countDown();
        }
    }
}
