package org.easyarch.dislock.zk.listener;

import org.easyarch.dislock.zk.ZKClient;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public interface NodeListener{

    public void nodeChanged(ZKClient client,String nodePath,Object data,State state);

}
