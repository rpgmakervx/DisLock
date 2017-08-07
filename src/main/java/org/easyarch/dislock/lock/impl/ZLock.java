package org.easyarch.dislock.lock.impl;

import org.easyarch.dislock.lock.Lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class ZLock implements Lock {


    @Override
    public void lock() {

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
}
