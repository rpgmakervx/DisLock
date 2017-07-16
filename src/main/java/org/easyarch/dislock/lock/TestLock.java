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
        RLock lock = new RLock("bizId-app1");
        Class.forName(LockHook.class.getName());
        new Thread(() -> {
            print(lock);
        },"thread-"+1).start();
        Thread.sleep(1000);
        new Thread(() -> {
            tryPrint(lock);
        },"thread-"+2).start();
    }
}
