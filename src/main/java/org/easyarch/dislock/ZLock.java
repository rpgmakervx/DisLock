package org.easyarch.dislock;

import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-7-11.
 */
public class ZLock implements Lock {



    @Override
    public void lock() throws Exception {

    }

    @Override
    public boolean tryLock() throws Exception {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws Exception {
        return false;
    }

    @Override
    public void unlock() {

    }
}
