package org.easyarch.dislock.zk.listener;

import org.easyarch.dislock.lock.AbstractLock;
import org.easyarch.dislock.zk.ZKClient;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
abstract public class AbstractListener implements NodeListener {

    protected ZKDataListener listener;

    @Override
    public ZKDataListener getListener() {
        return listener;
    }

    @Override
    public void setListener(ZKDataListener listener) {
        this.listener = listener;
    }
}
