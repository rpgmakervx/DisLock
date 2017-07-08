package org.easyarch.dislock;

/**
 * Created by code4j on 2017-7-8.
 */
public interface Lock {

    public void lock() throws Exception;

    public void unlock();
}
