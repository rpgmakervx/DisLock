package org.easyarch.dislock;

import java.util.concurrent.TimeUnit;

/**
 * Created by code4j on 2017-7-8.
 */
public interface Lock {

    public void lock() throws Exception;

    public boolean tryLock() throws Exception;

    public boolean tryLock(long time, TimeUnit unit) throws Exception;

    public void unlock();
}
