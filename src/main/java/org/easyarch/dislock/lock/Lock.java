package org.easyarch.dislock.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public interface Lock {

    String LOCK_KEY_NAME = "lock-";

    /**
     * 阻塞性的获取锁, 不响应中断
     */
    void lock();

    /**
     * 阻塞性的获取锁, 响应中断
     *
     * @throws InterruptedException
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * 尝试获取锁, 获取不到立即返回, 不阻塞
     */
    boolean tryLock();

    /**
     * 超时自动返回的阻塞性的获取锁, 不响应中断
     * 超时时间是获取锁花费的时间，超过时间限制获取锁失败
     * @param time
     * @param unit
     * @return {@code true} 若成功获取到锁, {@code false} 若在指定时间内未获取到锁后返回结果，不会继续阻塞
     *
     */
    boolean tryLock(long time, TimeUnit unit);

    /**
     * 超时自动返回的阻塞性的获取锁, 响应中断
     * 超时时间是获取锁花费的时间，超过时间限制获取锁失败
     * @param time
     * @param unit
     * @return {@code true} 若成功获取到锁, {@code false} 若在指定时间内未获取到锁响应中断
     * @throws InterruptedException 在尝试获取锁的当前线程被中断
     */
    boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁
     */
    void unlock();

}
