package org.easyarch.dislock.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by xingtianyu(code4j) on 2017-8-3.
 */
public class ZKTest {

    private static final int TIME_OUT = 3000;
    private static final String HOST = "localhost:2181";

    public static void main(String[] args) throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(HOST, TIME_OUT, null);
        zookeeper.create("/linshi","lalala".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

    }

}
