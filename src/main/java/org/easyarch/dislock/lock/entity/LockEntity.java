package org.easyarch.dislock.lock.entity;

import org.easyarch.dislock.kits.JsonKits;
import org.easyarch.dislock.sys.SysProperties;

import java.io.*;

/**
 * 一个服务器作为一个实例，每个实例都可以获取锁
 * 一个实例可以用物理地址+jvm进程id+线程id表征
 * redis value:
 * {
 *     "expireTime":10000,
 *     "mac":"xx-xx-xx-xx",
 *     "jvmPid":"10962",
 *     "threadId":"1112",
 *     "count":1
 * }
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class LockEntity implements Serializable{

    /**
     * 服务器物理地址
     */
    private String mac;
    /**
     * 应用所在的java进程id
     */
    private long jvmPid;
    /**
     * 线程id
     */
    private long threadId;
    /**
     * 超时时间点
     */
    private long expireTime;
    /**
     * 锁被持有次数
     */
    private int count = 1;


    public static LockEntity newEntity(long expireTime){
        LockEntity entity = new LockEntity();
        entity.count = 1;
        entity.expireTime = expireTime;
        entity.mac = SysProperties.mac();
        entity.jvmPid = SysProperties.jvmPid();
        entity.threadId = SysProperties.threadId();
        return entity;
    }

    public static LockEntity getEntity(byte[] bytes){
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (LockEntity) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LockEntity incrCount() {
        if (count == Integer.MAX_VALUE) {
            throw new Error("lock count too much");
        }
        ++count;
        return this;
    }

    public LockEntity decrCount() {
        --count;
        return this;
    }

    /**
     * 判断对象是否是当前实例
     * @return
     */
    public boolean isCurrentInstance(){
        return SysProperties.mac().equals(mac)
                && SysProperties.jvmPid() == jvmPid
                && SysProperties.threadId() == threadId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public long getJvmPid() {
        return jvmPid;
    }

    public void setJvmPid(long jvmPid) {
        this.jvmPid = jvmPid;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return JsonKits.toString(this);
    }

    public String toJson(){
        return JsonKits.toString(this);
    }

    public byte[] toBytes(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(this);
            oo.flush();
            baos.write(bo.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LockEntity entity = (LockEntity) o;

        if (jvmPid != entity.jvmPid) return false;
        if (threadId != entity.threadId) return false;
        return mac.equals(entity.mac);
    }

    @Override
    public int hashCode() {
        int result = mac.hashCode();
        result = 31 * result + (int) (jvmPid ^ (jvmPid >>> 32));
        result = 31 * result + (int) (threadId ^ (threadId >>> 32));
        return result;
    }
}
