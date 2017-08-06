package org.easyarch.dislock.zk;

import com.sun.javafx.sg.prism.NodePath;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.easyarch.dislock.lock.impl.ZLock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class ZKClient {

    private static final String BASE_PATH = "dislock-suyun-app1";

    private CuratorFramework client;

    private ZLock lock;

    public ZKClient(ZLock lock){
        this.lock = lock;
        this.client = CuratorFrameworkFactory
                .newClient("127.0.0.1:2181", new RetryNTimes(3,1000));
        client.sync();
        client.start();
    }

    public String createPerSeqNode(String path,byte[] data) throws Exception {
        return createNode(path,data, CreateMode.PERSISTENT_SEQUENTIAL);
    }
    public String createPerNode(String path,byte[] data) throws Exception {
        return createNode(path,data,CreateMode.PERSISTENT);
    }
    public String createEphNode(String path,byte[] data) throws Exception {
        return createNode(path,data,CreateMode.EPHEMERAL);
    }
    public String createEphSeqNode(String path,byte[] data) throws Exception {
        return createNode(path,data,CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void addListener(CuratorListener listener){
        client.getCuratorListenable().addListener(listener);
    }

    private String createNode(String path,byte[] data,CreateMode mode) throws Exception {
        String name = client.create().withMode(mode).forPath(path,data);
        return name;
    }

    public boolean checkExist(String path)throws Exception{
        return client.checkExists().forPath(path)!=null;
    }

    public List<String> getNodes(String basePath) throws Exception {
        return client.getChildren().forPath(basePath);
    }

    public List<String> getSortedNodes(String basePath) throws Exception {
        List<String> nodes = getNodes(basePath);
        sortNodes(nodes);
        return nodes;
    }

    public String getMinNode(String basePath) throws Exception {
        return getSortedNodes(basePath).get(0);
    }

    public byte[] getData(String node) throws Exception {
        return client.getData().forPath(node);
    }

    /**
     * 根据字符串后缀的数字进行冒泡排序,从小到大。
     * @param nodes
     */
    private void sortNodes(List<String> nodes){
        boolean flag = true;
        for (int index = 0;index<nodes.size();index++){
            for (int inner = 0;inner<nodes.size() -index - 1;inner++){
                String front = nodes.get(inner);
                String behind = nodes.get(inner + 1);
                long frontNum = Long.valueOf(front.split("-")[1]);
                long behindNum = Long.valueOf(behind.split("-")[1]);
                String temp = "";
                if (frontNum > behindNum){
                    temp = behind;
                    flag = false;
                    nodes.set(inner + 1,front);
                    nodes.set(inner,temp);
                }
            }
            if (flag){
                break;
            }
        }
    }

    public void setData(String nodePath,byte[] data) throws Exception {
        client.setData().forPath(nodePath,data);
    }

    public void rmNode(String nodePath) throws Exception {
        client.delete().forPath(nodePath);
    }

    public void watchNode(String nodePath,TreeCacheListener listener){
        TreeCache cache = new TreeCache(client, nodePath);
        try {
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cache.getListenable().addListener(listener);
    }

    public void watchChildNode(String path) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client,path,true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//                if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
//                    event.get
//                }
                System.out.println("childNode type:"+event.getType());
                System.out.println("childNode data:"+event.getData());
                System.out.println("childNodes :"+event.getInitialData());
            }
        });
        pathChildrenCache.start();
    }


}
