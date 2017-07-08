package org.easyarch.dislock;

import org.easyarch.dislock.redis.RedisKits;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by code4j on 2017-7-7.
 */
public class App {
    private AtomicReference<Thread> owner =new AtomicReference<>();
    private AtomicInteger stack = new AtomicInteger(0);

    public void lock(){
        Thread current = Thread.currentThread();
        if (current == owner.get()){
            stack.incrementAndGet();
            return;
        }
        while(!owner.compareAndSet(null, current)){
//            System.out.println("lock rolling..");
        }
    }
    public void unlock (){
        Thread current = Thread.currentThread();
        if (stack.get() == 0){
            return;
        }else{
            stack.decrementAndGet();
        }
        owner.compareAndSet(current, null);
    }

    public static void print(DisLock lock,String id){
        try {
            lock.lock();
//            System.out.println(String.format("%s is locking",id));
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        System.out.println(Thread.currentThread().getName()+" finished");
    }

    public static void main(String[] args) {
        DisLock lock = new DisLock("suyun","app1",10000);
        for (int i = 0; i<10;i++){
            int finalI = i;
            new Thread(() -> {
                print(lock,"thread"+ finalI);
            },"thread-"+i).start();
        }
//        AtomicInteger stack = new AtomicInteger(0);
//        System.out.println(stack.equals(new AtomicInteger(0)));
//        long value = RedisKits.strings()
//                .setnx("lock_suyun","10009");
//        System.out.println("value = " + value);
    }
}
