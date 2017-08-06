package org.easyarch.dislock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.easyarch.dislock.lock.impl.ZLock;
import org.easyarch.dislock.sys.SysProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-7-16.
 */
public class WatchMinSeqListener implements TreeCacheListener {

    private String parentPath;

    private ZLock lock;
    /**
     * 用来获取一个实例上的latch
     */
    private String instanceId;

    public WatchMinSeqListener(ZLock lock,String parentPath,String instanceId){
        this.lock = lock;
        this.parentPath = parentPath;
        this.instanceId = instanceId;
    }



    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
//        System.out.println(instanceId+" status:"+event.getType());
        if (!TreeCacheEvent.Type.NODE_REMOVED.equals(event.getType())){
            return ;
        }
        List<String> nodes = ZKKits.getSortedNodes(parentPath);
        String []segs = nodes.get(0).split("-");
        Long minNum = Long.valueOf(nodes.get(0).split("-")[1]);
        if (minNum.equals(Long.valueOf(segs[1]))){
            lock.getLatch(instanceId).countDown();
        }
    }
}
