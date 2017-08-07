package org.easyarch.dislock.zk.curator.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.easyarch.dislock.lock.impl.ZLock;
import org.easyarch.dislock.zk.curator.ZKKits;

import java.util.Collections;
import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-8-8.
 * 监听器，监听节点变化但是只对删除状态的节点做业务操作
 */
public class NodeChangeWatcher implements Watcher {

    private static final String LOCK_KEY_NAME = "lock-";

    private String basePath;

    private ZLock zkLock;

    private String instanceId;

    public NodeChangeWatcher(ZLock zkLock, String basePath, String instanceId){
        this.zkLock = zkLock;
        this.basePath = basePath;
        this.instanceId = instanceId;
    }

    /**
     * 1.不管节点收到什么事件，都要重新监听（坑4：zkclient的特点）
     * 2.触发删除事件时，被回调的实例遍历根节点的子节点，判断自己是不是最小的
     * （坑5：万一有客户端并没有获取锁但是挂了，也会触发该事件，但是此时的次小节点并不应该获取锁）
     * 3.是最小则正常获取锁，否则重新监听比该节点大的最大节点。
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        if (!Event.EventType.NodeDeleted.equals(event.getType())){
            try {
                ZKKits.watchNode(event.getPath(),this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            List<String> nodes = ZKKits.getSortedNodes(basePath);
            String []segs = nodes.get(0).split("-");
            Long minNum = Long.valueOf(nodes.get(0).split("-")[1]);
            if (minNum.equals(Long.valueOf(segs[1]))){
                zkLock.getLatch(instanceId).countDown();
                return ;
            }
            Collections.reverse(nodes);
            long currentNodeNum = Long.valueOf(event.getPath().split(LOCK_KEY_NAME)[1]);
            for (String node:nodes) {
                Long num = Long.valueOf(node.split("-")[1]);
                if (currentNodeNum > num) {
                    ZKKits.watchNode(event.getPath(),this);
                    return ;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
