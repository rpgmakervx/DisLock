package org.easyarch.dislock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by xingtianyu(code4j) on 2017-8-1.
 */
public class ZKLock {

    private static final String ZKSTRING = "";
    private static final String PATH = "/dislock/suyun/app1";

    public static void main(String[] args) throws InterruptedException {
        for (int index = 0; index < 10; index++) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(ZKSTRING, new ExponentialBackoffRetry(1000, 3));
            client.start();
            new Thread(new Job(client,PATH)).start();
        }
        Thread.sleep(100000);
    }
}
