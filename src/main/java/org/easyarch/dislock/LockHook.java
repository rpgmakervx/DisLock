package org.easyarch.dislock;

import org.easyarch.dislock.redis.RedisKits;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by code4j on 2017-7-8.
 */
public class LockHook {

    private static final String LOCK_KEY_NAME = "dislock-*";
    ReentrantLock lock;
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Set<String> keys = RedisKits.keys().keys(LOCK_KEY_NAME);
                System.out.println("clear:"+keys);
                for (String k:keys){
                    RedisKits.keys().del(k);
                }
            }
        }));
    }
}
