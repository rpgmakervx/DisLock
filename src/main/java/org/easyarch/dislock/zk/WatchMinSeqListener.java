package org.easyarch.dislock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-7-16.
 */
public class WatchMinSeqListener implements TreeCacheListener {

    private String parentPath;

    private String watcherNodePath;

    public WatchMinSeqListener(String parentPath,String watcherNodePath){
        this.parentPath = parentPath;
        this.watcherNodePath = watcherNodePath;
    }



    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        if (event.getType() != TreeCacheEvent.Type.NODE_REMOVED){
            return ;
        }
        System.out.println(String.format("received a node even，node %s changed",event.getType()));
        System.out.println("parentPath:"+parentPath);
        List<String> nodes = client.getChildren().forPath(parentPath);
        System.out.println("nodes:"+nodes);
        List<Long> sequences = new ArrayList<>();
        for (String path:nodes){
            String[] seg = path.split("-");
            sequences.add(Long.valueOf(seg[1]));
        }
        Collections.sort(sequences);
//        client.getChildren().watched().forPath(parentPath + "-" + sequences.get(sequences.size()));
        String []segs = watcherNodePath.split("-");
        System.out.println("watcherNodePath num:"+Long.valueOf(segs[1])+" ,min:"+sequences.get(0));
        System.out.println(sequences.get(0).equals(Long.valueOf(segs[1])));
        if (sequences.get(0).equals(Long.valueOf(segs[1]))){
            System.out.println("得到锁");
        }else{
            System.out.println("还没得到锁");
        }
    }
}
