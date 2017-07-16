package org.easyarch.dislock.zk;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by xingtianyu(code4j) on 2017-7-16.
 */
public class MinSeqWatcher implements CuratorWatcher {

    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {
        System.out.println("node changed :"+watchedEvent);
    }
}
