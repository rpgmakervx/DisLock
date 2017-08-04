package org.easyarch.dislock.zk;

import org.apache.curator.framework.api.CuratorListener;

import java.util.List;

/**
 * Created by xingtianyu(code4j) on 2017-8-4.
 */
public class ZKKits {

    private static ZKClient client = new ZKClient();

    public static String createPerSeqNode(String path, byte[] data) {
        return client.createPerSeqNode(path, data);
    }

    public static String createPerNode(String path, byte[] data) {
        return client.createPerNode(path, data);
    }

    public static String createEphNode(String path, byte[] data) {
        return client.createEphNode(path, data);
    }

    public static String createEphSeqNode(String path, byte[] data) {
        return client.createEphSeqNode(path, data);
    }

    public static void addListener(CuratorListener listener) {
        client.addListener(listener);
    }

    public static boolean checkExist(String path) throws Exception {
        return client.checkExist(path);
    }

    public static List<String> getNodes(String basePath) throws Exception {
        return client.getNodes(basePath);
    }

    public static void setData(String nodePath, byte[] data) throws Exception {
        client.setData(nodePath, data);
    }

    public static void rmNode(String nodePath) throws Exception {
        client.rmNode(nodePath);
    }

    public static void watchNode(String parentNodePath, String watcherNodePath, String nodePath) throws Exception {
        client.watchNode(parentNodePath, watcherNodePath, nodePath);
    }

    public static void watchChildNode(String path) throws Exception {
        client.watchChildNode(path);
    }
}
