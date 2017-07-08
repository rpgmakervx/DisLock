package org.easyarch.dislock;

import org.easyarch.dislock.redis.RedisKits;

import java.util.Set;

/**
 * Created by code4j on 2017-7-8.
 */
public class LockHook {

    private static final String LOCK_KEY_NAME = "dislock-*";

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
