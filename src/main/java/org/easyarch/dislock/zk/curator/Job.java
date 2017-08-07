package org.easyarch.dislock.zk.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * Created by xingtianyu(code4j) on 2017-8-1.
 */
public class Job implements Runnable {

    private InterProcessMutex lock;

    public Job(CuratorFramework client,String lockPath) {
        lock = new InterProcessMutex(client, lockPath);
    }

    public void process() {
        try {
            lock.acquire();
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        process();
    }
}
