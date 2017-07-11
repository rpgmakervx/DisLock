package org.easyarch.dislock;


import org.easyarch.dislock.redis.RedisKits;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by code4j on 2017-7-7.
 */
public class DisLock implements Lock{

    private static final String LOCK_KEY_NAME = "dislock-";

    private static final long DEFAULT_AQUIRE_LOCK_STEP = 1;

    private String departId;

    private String appId;

    private String key;

    private long timeout;

    private AtomicInteger count = new AtomicInteger(0);

    public DisLock(String departId,String appId,long timeout) {
        this.departId = departId;
        this.appId = appId;
        this.timeout = timeout;
        if (timeout < 0){
            this.timeout = Long.MAX_VALUE;
        }
        this.key = String.format(LOCK_KEY_NAME+"%s-%s",departId,appId);
        System.out.println("key:"+key);
    }

    private boolean aquire(){
        long timestamp = System.currentTimeMillis();
        long value = RedisKits.strings()
                .setnx(key,String.valueOf(timestamp));
        if (value == 1){
            return true;
        }
        String timeStr = RedisKits.strings().get(key);
        if (timeStr == null){
            return false;
        }
        long lastLockTimeStamp = Long.valueOf(timeStr);
        //true则表示未超时，锁还是有效被占用的
        if (timeout > timestamp - lastLockTimeStamp){
//            System.out.println(String
//                    .format("%s > %s - %s = %s",timeout,timestamp,lastLockTimeStamp,timestamp - lastLockTimeStamp));
            return false;
        }
        timestamp = System.currentTimeMillis();
        String oldTimeStr = RedisKits.strings()
                .getSet(key,String.valueOf(timestamp));
        //在这期间锁被释放了
        if (oldTimeStr == null){
            return true;
        }
        long oldLastLockTimeStamp = Long.valueOf(oldTimeStr);
        //false表示超时时间被修改了，在当前进程抢占之前被别的进程抢占了。
        if (timeout < timestamp - oldLastLockTimeStamp){
            return true;
        }
        return false;
    }

    @Override
    public void lock() throws Exception {
        while (!aquire()){
            Thread.sleep(DEFAULT_AQUIRE_LOCK_STEP);
        }
    }

    @Override
    public boolean tryLock() throws Exception {
        return aquire();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws Exception {
        long begin = System.currentTimeMillis();
        while (!aquire()){
            Thread.sleep(DEFAULT_AQUIRE_LOCK_STEP);
            long current = System.currentTimeMillis();
//            System.out.println(String
//                    .format("%s < %s - %s = %s",unit.toMillis(time),current,begin,current - begin));
            if ( unit.toMillis(time) < current - begin){
                return false;
            }
        }
        System.out.println("tryLock 获取成功");
        return true;
    }

    @Override
    public void unlock() {
        RedisKits.keys().del(key);
//        System.err.println(Thread.currentThread().getName()+" 释放锁");
    }

}
