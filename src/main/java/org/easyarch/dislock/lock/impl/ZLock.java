package org.easyarch.dislock.lock.impl;

import org.easyarch.dislock.lock.AbstractLock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.sys.SysProperties;
import org.easyarch.dislock.zk.ZKClient;
import org.easyarch.dislock.zk.ZKKits;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-8-3.
 */
public class ZLock extends AbstractLock {
    //格式为：/dislock/{业务线}/{应用名}/lock-
    private String nodePath;

    //格式为：/dislock/{业务线}/{应用名}
    private String basePath;

    public ZLock(String nodePath){
        this(nodePath,DEFAULT_KEY_EXPIRE);
    }

    public ZLock(String nodePath, long keyExpire){
        this.nodePath = nodePath + "/"+LOCK_KEY_NAME;
        this.keyExpire = keyExpire;
    }

    /**
     * 具体流程：
     * 在basePath下创建临时有序节点，
     * 然后获取basePath下所有有序节点，获得他们的序号，
     * 通过自己的序号和他们的序号进行对比，得到比自己小的相邻的一个节点。
     * 监听这个节点的删除事件。
     * 当删除事件触发时，获得通知的节点需要先检查一下自己是否是basePath下序号最小的节点（防止客户端获取锁之前断开连接，导致次小节点获取锁）
     * 如果是最小节点，获取到锁，否则不获取锁。
     * 当方法设置超时时间时，通过对比当前时间与方法调用时间，判断超时则获取锁失败，退出方法
     *
     * @param isTimeOut
     * @param timeout
     * @param unit
     * @param interruptable
     * @return
     */
    @Override
    protected boolean lock0(boolean isTimeOut, long timeout, TimeUnit unit, boolean interruptable) {
//        long callTime = SysProperties.sysMillisTime();
//        if (isTimeOut&&unit != null&&isTimeOut(callTime,unit.toMillis(timeout))){
//            //锁调用超时
//            return false;
//        }
        LockEntity entity = LockEntity.newEntity(keyExpire + SysProperties.sysMillisTime());
        try {
            ZKKits.createEphNode(nodePath,entity.toBytes());
            List<String> nodes = ZKKits.getNodes(basePath);
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

    public static void main(String[] args) throws Exception {
        ZKClient zkClient = new ZKClient();
        LockEntity entity = new LockEntity();
        entity.setExpireTime(3000);
        entity.setCount(0);
        entity.setMac(SysProperties.mac());
        entity.setJvmPid(SysProperties.jvmPid());
        entity.setThreadId(SysProperties.getThreadId());
        zkClient.createEphSeqNode("/dislock/zhifu/app1/"+LOCK_KEY_NAME,entity.toBytes());
        zkClient.createEphSeqNode("/dislock/zhifu/app1/"+LOCK_KEY_NAME,entity.toBytes());
        zkClient.createEphSeqNode("/dislock/zhifu/app1/"+LOCK_KEY_NAME,entity.toBytes());
        zkClient.createEphSeqNode("/dislock/zhifu/app1/"+LOCK_KEY_NAME,entity.toBytes());
        List<String> nodes = zkClient.getNodes("/dislock/zhifu/app1");
        Collections.sort(nodes);
        String node = nodes.get(0);
        String watcherNode = nodes.get(1);
        System.out.println(node);
//        zkClient.watchChildNode("/dislock/zhifu/app1");
//        zkClient.get
        System.out.println("exists:"+zkClient.checkExist("/dislock/zhifu/app1/"+node));
        zkClient.watchNode("/dislock/zhifu/app1","/dislock/zhifu/app1/"+watcherNode,"/dislock/zhifu/app1/"+node);
        zkClient.rmNode("/dislock/zhifu/app1/"+node);
        Thread.sleep(100000);
    }
}
