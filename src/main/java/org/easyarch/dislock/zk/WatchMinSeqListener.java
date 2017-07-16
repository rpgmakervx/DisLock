package org.easyarch.dislock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-7-16.
 */
public class WatchMinSeqListener implements CuratorListener {

    private String basePath;

    public WatchMinSeqListener(String basePath){
        this.basePath = basePath;
    }

    @Override
    public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
        System.out.println(String.format("received a node even，node %s changed",curatorEvent.getName()));
        List<String> nodes = curatorEvent.getChildren();
        List<Long> sequences = new ArrayList<>();
        for (String path:nodes){
            String[] seg = path.split("-");
            sequences.add(Long.valueOf(seg[1]));
        }
        Collections.sort(sequences);
        curatorFramework.getChildren().watched().forPath(basePath + "-" + sequences.get(sequences.size()));
    }
}
