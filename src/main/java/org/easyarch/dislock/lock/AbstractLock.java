package org.easyarch.dislock.lock;


import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
abstract public class AbstractLock implements Lock {

    protected static final long DEFAULT_KEY_EXPIRE = 20*1000;

    protected volatile long keyExpire;

    protected volatile boolean locked;

    @Override
    public void lock() {
        try {
            lock0(false,0,null,false);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        try {
            return lock0(true,time,unit,false);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     *
     * @throws InterruptedException
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock0(false, 0, null, true);
    }

    protected abstract boolean lock0(boolean isTimeOut, long timeout, TimeUnit unit, boolean interruptable) throws InterruptedException;

    public abstract boolean isLocked();
}
