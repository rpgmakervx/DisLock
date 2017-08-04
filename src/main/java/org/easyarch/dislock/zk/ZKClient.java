package org.easyarch.dislock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;

import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class ZKClient {

    private static final String BASE_PATH = "dislock-suyun-app1";

    private CuratorFramework client;

    public static void main(String[] args) throws Exception {
        ZKClient client = new ZKClient();
//        client.addListener(new WatchMinSeqListener("/"+BASE_PATH+"-"));
//        client.createPerNode("/"+BASE_PATH,"root".getBytes());
        client.createEphSeqNode("/"+BASE_PATH+"/node-","hello".getBytes());
        client.createEphSeqNode("/"+BASE_PATH+"/node-","world".getBytes());
        client.createEphSeqNode("/"+BASE_PATH+"/node-","my".getBytes());
        client.createEphSeqNode("/"+BASE_PATH+"/node-","world".getBytes());
        Thread.sleep(100000);
    }

    public ZKClient(){
        this.client = CuratorFrameworkFactory
                .newClient("127.0.0.1:2181", new RetryNTimes(3,1000));
        client.start();
    }

    public String createPerSeqNode(String path,byte[] data){
        return createNode(path,data, CreateMode.PERSISTENT_SEQUENTIAL);
    }
    public String createPerNode(String path,byte[] data){
        return createNode(path,data,CreateMode.PERSISTENT);
    }
    public String createEphNode(String path,byte[] data){
        return createNode(path,data,CreateMode.EPHEMERAL);
    }
    public String createEphSeqNode(String path,byte[] data){
        return createNode(path,data,CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void addListener(CuratorListener listener){
        client.getCuratorListenable().addListener(listener);
    }

    private String createNode(String path,byte[] data,CreateMode mode){
        try {
            String name = client.create().withMode(mode).forPath(path,data);
            return name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkExist(String path)throws Exception{
        return client.checkExists().forPath(path)!=null;
    }

    public List<String> getNodes(String basePath) throws Exception {
        return client.getChildren().forPath(basePath);
    }

    public void setData(String nodePath,byte[] data) throws Exception {
        client.setData().forPath(nodePath,data);
    }

    public void rmNode(String nodePath) throws Exception {
        client.delete().forPath(nodePath);
    }

    public void watchNode(String parentNodePath,String watcherNodePath,String nodePath) throws Exception {
        TreeCache cache = new TreeCache(client,nodePath);
        cache.start();
        cache.getListenable().addListener(new WatchMinSeqListener(parentNodePath,watcherNodePath));
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
