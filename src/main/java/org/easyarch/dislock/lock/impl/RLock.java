package org.easyarch.dislock.lock.impl;

import org.easyarch.dislock.kits.JsonKits;
import org.easyarch.dislock.lock.AbstractLock;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.redis.RedisKits;
import org.easyarch.dislock.sys.SysProperties;

import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class RLock extends AbstractLock {

    private static final long DEFAULT_KEY_EXPIRE = 20*1000;

    private String lockKey;

    private long keyExpire;

    public RLock(String lockKey){
        this(lockKey,DEFAULT_KEY_EXPIRE);
    }

    public RLock(String lockKey,long keyExpire){
        this.lockKey = LOCK_KEY_NAME+lockKey;
        this.keyExpire = keyExpire;
    }

    @Override
    public boolean tryLock() {
        try {
            return acquire(0,0,null,false);
        } catch (InterruptedException e) {
        }
        return false;
    }

    @Override
    public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    /**
     * 整个过程存在于一个loop中，
     * 本方法作为loop，逻辑执行见acquire
     * loop除了执行取锁，释放锁等业务，
     * 还要检查方法是否出现调用超时（设置超时的情况），检查是否调用了中断操作
     *
     * @param isTimeOut
     * @param timeout
     * @param unit
     * @param interruptable
     * @return
     * @throws InterruptedException
     */
    @Override
    protected boolean lock0(boolean isTimeOut, long timeout, TimeUnit unit, boolean interruptable) throws InterruptedException {
        long callTime = SysProperties.sysMillisTime();
        while (!acquire(callTime,timeout,unit,interruptable)){
        }
        this.locked = true;
        return true;
    }

    /**
     * 具体步骤：
     * 进入循环前，记录调用时间
     * 1 如果方法有调用超时设置，先检查方法调用是否超时。
     *      1.1 调用超时返回false，获取锁失败
     *      1.2 若未超时，执行主流程
     * 2 是否设置可interrupt
     *      2.1 是，检查当前线程是否是interrupt状态，是则抛出异常，执行主流程
     *      2.2 否，则执行主流程
     * 3 redis.setNx设置value。
     *      3.1 设置成功，则当前实例获取锁成功。
     *      3.2 若失败，执行主流程（不代表获取锁失败，有可能key逻辑超时了）
     * 4 获取value信息，有可能这一瞬间key被删除（entity == null），
     *      4.1 若被删除，则回到（1）尝试重新获取锁
     *      4.2 若没被删除，则执行主流程
     * 5 根据value序列化得到的LockEntity中的expireTime(这是超时时间点)和当前服务器时间判断是否已超时（expireTime < sysTime）
     *      5.1 超时，说明存在竞争，尝试redis.getSet竞争锁。
     *          5.1.1 取得的实体不为空，且已超时（expireTime < sysTime），说明期间没有其他实例竞争到锁，则当前实例获取锁成功。
     *          5.1.2 否则说明锁被其他实例获取，回到（1）尝试重新获取锁
     *      5.1 未超时，说明锁还是有效的，但是因为锁是可重入的，有可能持锁实例就是自己。
     *          5.2.1 实例是自己，则将value更新（计数器+1），
     *          若此过程期间有其他实例获取锁，则获取锁失败，回到（1）尝试重新获取锁，否则获取锁成功
     *          5.2.2 实例不是自己，则回到（1）尝试重新获取锁
     *
     *  redis写入耗时500ms,获取100ms
     * @param begin
     * @param timeout
     * @param unit
     * @param interruptable
     * @return
     * @throws InterruptedException
     */
    private boolean acquire(long begin, long timeout, TimeUnit unit, boolean interruptable) throws InterruptedException {
        if (unit != null&&isTimeOut(begin,unit.toMillis(timeout))){
            //调用超时
            return true;
        }
        if (interruptable){
            //中断状态检查
            checkInterrupt();
        }
        LockEntity newEntity = LockEntity.newEntity(
                keyExpire + SysProperties.sysMillisTime());
        if (RedisKits.strings()
                .setnx(lockKey,newEntity.toString()) == 1){
            this.locked = true;
            return true;
        }
        LockEntity regetEntity = JsonKits.toObject(
                RedisKits.strings().get(lockKey),LockEntity.class);
        if (regetEntity == null){
            return true;
        }
        //旧实例持锁超时
        if (isTimeOut(regetEntity.getExpireTime())){
            LockEntity oldEntity = JsonKits.toObject(
                    RedisKits.strings().getSet(lockKey,newEntity.toString())
                    ,LockEntity.class);
            if (oldEntity != null && isTimeOut(oldEntity.getExpireTime())){
                this.locked = true;
                return true;
            }
            return false;
        }
        //未超时，判断可重入
        if (regetEntity.isCurrentInstance()){
            regetEntity.setExpireTime(keyExpire + SysProperties.sysMillisTime());
            regetEntity.incrCount();
            LockEntity oldEntity = JsonKits.toObject(
                    RedisKits.strings().getSet(lockKey,regetEntity.toString())
                    ,LockEntity.class);
            //检查实例重入时是否被其他实例抢到锁
            if (oldEntity != null &&oldEntity.equals(regetEntity)){
                this.locked = true;
                return true;
            }
        }
        return false;
    }

    /**
     * 具体步骤：
     * 1 获取value
     * 2 判断锁是否过期
     *      2.1 锁过期，return
     *      2.2 未过期，执行主流程
     * 3 判断持锁实例是不是自己
     *      3.1 是自己，则判断计数器
     *          3.1.1 计数器大于1，说明锁出现重入现象，修改计数器count - 1
     *          3.1.2 计数器等于1，说明锁不存在重入，调用redis.del 删除key释放锁
     *      3.2 不是自己，则当前线程不含有当前对象的锁资源的情况下调用lock，抛异常
     * @return
     */
    @Override
    public void unlock() {
        LockEntity entity = JsonKits.toObject(
                RedisKits.strings().get(lockKey)
                ,LockEntity.class);
        if (entity == null || isTimeOut(entity.getExpireTime())){
            return ;
        }
        if (entity.isCurrentInstance()){
            if (entity.getCount() == 1){
                RedisKits.keys().del(lockKey);
                System.out.println(entity.getThreadId()+"释放锁");
            }else{
                entity.decrCount();
                RedisKits.strings().set(lockKey,entity.toString());
            }
            return;
        }
        throw new IllegalMonitorStateException();
    }

    /**
     * 具体步骤：
     * 1 先判断锁标记
     *      1.1 锁标记false，说明锁没有被获取
     *      1.2 标记可能还没来得及修改，继续主流程
     * 2 获取value，
     *      2.1 若为空说明没有锁
     *      2.2 否则锁对象存在，超时说明没锁，否则是锁住状态
     * @return
     */
    @Override
    public boolean isLocked() {
        if (!locked){
            return locked;
        }
        LockEntity entity = JsonKits.toObject(
                RedisKits.strings().get(lockKey)
                ,LockEntity.class);
        return entity == null?false:!isTimeOut(entity.getExpireTime());
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
