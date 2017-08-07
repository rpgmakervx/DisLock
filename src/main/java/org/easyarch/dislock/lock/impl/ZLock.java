package org.easyarch.dislock.lock.impl;

import org.apache.commons.lang.StringUtils;
import org.easyarch.dislock.lock.Lock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.sys.SysProperties;
import org.easyarch.dislock.zk.ZKClient;
import org.easyarch.dislock.zk.ZKKits;
import org.easyarch.dislock.zk.listener.AbstractListener;
import org.easyarch.dislock.zk.listener.NodeListener;
import org.easyarch.dislock.zk.listener.State;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class ZLock implements Lock {

    protected volatile long keyExpire = 30 * 1000;

    protected volatile boolean locked;

    private String basePath;

    private String nodePath;

    private volatile ConcurrentHashMap<String,CountDownLatch> latches = new ConcurrentHashMap<>();

    public ZLock(String basePath){
        this.basePath = basePath;
        this.nodePath = basePath + "/" + PER_LOCK_KEY_NAME;
        ZKKits.init();
    }

    public ZLock(String basePath,long timeout){
        this.basePath = basePath;
        this.nodePath = basePath + "/" + PER_LOCK_KEY_NAME;
        this.keyExpire = timeout;
        ZKKits.init();
    }

    public long getExpire(){
        return keyExpire;
    }

    public CountDownLatch getLatch(String instanceId){
        return latches.get(instanceId);
    }

    @Override
    public void lock() {
        CountDownLatch latch = new CountDownLatch(1);
        this.latches.put(SysProperties.uniqueId(),latch);
        if (aquire()){
            return ;
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.latches.remove(SysProperties.uniqueId());
    }

    private boolean aquire(){
        Object data = ZKKits.getData(nodePath);
        //节点不存在
        if (data == null){
            LockEntity entity = LockEntity.newEntity(keyExpire);
            String nodeName = ZKKits.createPerNode(nodePath,entity);
            //创建失败
            if (StringUtils.isBlank(nodeName)){
                return locked(false);
            }
            return locked(true);
        }
        LockEntity entity = (LockEntity) data;
        //可重入
        if (entity.isCurrentInstance()){
            entity.incrCount();
            ZKKits.setData(nodePath,entity);
            return locked(true);
        }
//        ZKKits.addListener("/dislock/suyun/app1/~lock", );
        return false;
    }

    private boolean locked(boolean flag){
        return this.locked = flag;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
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

    @Override
    public void unlock() {

    }

    public static void main(String[] args) throws InterruptedException {
        ZKKits.init();
        LockEntity entity = ZKKits.getData("/dislock/suyun/app1/~lock");
        ZKKits.createPerNode("/dislock/suyun/app1/~lock",LockEntity.newEntity(10000));
//        System.out.println("threadname outside:"+Thread.currentThread().getName());
        ZKKits.addListener("/dislock/suyun/app1/~lock", new AbstractListener() {
            @Override
            public void nodeChanged(ZKClient client, String nodePath, Object data, State state) {
                System.out.println(nodePath);
                ZKKits.removeListener(nodePath,this);
            }
        });
//        System.out.println(entity);
        Thread.sleep(1000000);
    }

}
