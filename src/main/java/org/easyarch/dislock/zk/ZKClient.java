package org.easyarch.dislock.zk;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.easyarch.dislock.kits.PropertyKits;
import org.easyarch.dislock.zk.listener.NodeListener;
import org.easyarch.dislock.zk.listener.ZKDataListener;

import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class ZKClient {

    private ZkClient zkClient;

    public ZKClient(String confPath){
        try {
            PropertyKits kits = new PropertyKits(confPath);
            zkClient = new ZkClient(kits.getString("zk.server"),Integer.valueOf(kits.getString("zk.sessionTimeOut")),Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createEpNode(String nodePath,Object data){
        return zkClient.create(nodePath,data, CreateMode.EPHEMERAL);
    }

    public String createEpSeqNode(String nodePath,Object data){
        return zkClient.create(nodePath,data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public String createPerNode(String nodePath,Object data){
        try {
            return zkClient.create(nodePath,data,CreateMode.PERSISTENT);
        }catch (Exception e){
            return null;
        }
    }

    public String createPerSeqNode(String nodePath,Object data){
        return zkClient.create(nodePath,data,CreateMode.PERSISTENT_SEQUENTIAL);
    }

    public<T> T getData(String nodePath){
        return zkClient.readData(nodePath,true);
    }

    public void setData(String nodePath,Object data){
        zkClient.writeData(nodePath,data);
    }

    public boolean rmNode(String nodePath){
        return zkClient.delete(nodePath);
    }

    public boolean checkExist(String nodePath){
        return zkClient.exists(nodePath);
    }

    public List<String> getNodes(String basePath){
        return zkClient.getChildren(basePath);
    }

    public void addListener(String nodePath, NodeListener listener){
        zkClient.subscribeDataChanges(nodePath,new ZKDataListener(this,listener));
    }

    public void removeListener(String nodePath, NodeListener listener){
        zkClient.unsubscribeDataChanges(nodePath,listener.getListener());
    }

}
