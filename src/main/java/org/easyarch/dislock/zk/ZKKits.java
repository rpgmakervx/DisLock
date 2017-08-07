package org.easyarch.dislock.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.easyarch.dislock.zk.listener.NodeListener;

import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class ZKKits {

    private static ZKClient client;

    public synchronized static void init(){
        if (client == null){
            String confPath = ZKKits.class.getResource("/config.properties").getPath();
            client = new ZKClient(confPath);
        }
    }

    public static String createEpNode(String nodePath, Object data) {
        return client.createEpNode(nodePath, data);
    }

    public static String createEpSeqNode(String nodePath, Object data) {
        return client.createEpSeqNode(nodePath, data);
    }

    public static String createPerNode(String nodePath, Object data) {
        return client.createPerNode(nodePath, data);
    }

    public static String createPerSeqNode(String nodePath, Object data) {
        return client.createPerSeqNode(nodePath, data);
    }

    public static <T> T getData(String nodePath) {
        return client.getData(nodePath);
    }

    public static void setData(String nodePath, Object data) {
        client.setData(nodePath, data);
    }

    public static boolean rmNode(String nodePath) {
        return client.rmNode(nodePath);
    }

    public static boolean checkExist(String nodePath) {
        return client.checkExist(nodePath);
    }

    public static List<String> getNodes(String basePath) {
        return client.getNodes(basePath);
    }

    public static void addListener(String nodePath, NodeListener listener) {
        client.addListener(nodePath, listener);
    }

    public static void removeListener(String nodePath, NodeListener listener) {
        client.removeListener(nodePath, listener);
    }


}
