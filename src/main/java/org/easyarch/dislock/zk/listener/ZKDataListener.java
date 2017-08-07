package org.easyarch.dislock.zk.listener;

import org.I0Itec.zkclient.IZkDataListener;
import org.easyarch.dislock.zk.ZKClient;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class ZKDataListener implements IZkDataListener {

    private NodeListener listener;

    private ZKClient client;

    public ZKDataListener(ZKClient client,NodeListener listener){
        this.client = client;
        this.listener = listener;
        listener.setListener(this);
    }

    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {
        listener.nodeChanged(client,dataPath,data,State.NODE_DATA_CHANGE);
    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        listener.nodeChanged(client,dataPath,null,State.NODE_DELETE);

    }
}
