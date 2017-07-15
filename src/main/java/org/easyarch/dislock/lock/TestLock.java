package org.easyarch.dislock.lock;

import org.easyarch.dislock.LockHook;
import org.easyarch.dislock.lock.impl.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class TestLock {

    public static void print(Lock lock){
        try{
            lock.lock();
            System.out.println(Thread.currentThread().getId()+"获取到锁");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void tryPrint(Lock lock){
        try {
            if (lock.tryLock(1000,TimeUnit.MILLISECONDS)){
                System.out.println(Thread.currentThread().getId()+"成功获取锁");
            }else {
                System.out.println(Thread.currentThread().getId()+"获取锁失败");
                return;
            }
            Thread.sleep(1000);
            lock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+" finished");
    }

    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        RLock lock = new RLock("suyun-app1");
        Class.forName(LockHook.class.getName());
        new Thread(() -> {
            print(lock);
        },"thread-"+1).start();
        Thread.sleep(1000);
        System.out.println("isLocked?:"+lock.isLocked());
//        new Thread(() -> {
//            tryPrint(lock);
//        },"thread-"+2).start();

//        System.out.println(new Date(1500114867850l));
//        long s = SysProperties.sysMillisTime();
//        RedisKits.strings().get("dislock-suyun-app1");
//        LockEntity newEntity = LockEntity.newEntity(
//                20000 + SysProperties.sysMillisTime());
//        RedisKits.strings()
//                .setnx("dislock-suyun-app1",newEntity.toString());
//        System.out.println("耗时："+(SysProperties.sysMillisTime() - s));
    }
}
