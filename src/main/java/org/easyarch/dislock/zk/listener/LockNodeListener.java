package org.easyarch.dislock.zk.listener;

import org.apache.commons.lang.StringUtils;
import org.easyarch.dislock.lock.entity.LockEntity;
import org.easyarch.dislock.lock.impl.ZKLock;
import org.easyarch.dislock.lock.impl.ZLock;
import org.easyarch.dislock.zk.ZKClient;
import org.easyarch.dislock.zk.ZKKits;

/**
 * Created by xingtianyu(code4j) on 2017-8-7.
 */
public class LockNodeListener extends AbstractListener {
    private String instanceId;

    private ZLock lock;

    public LockNodeListener(ZLock lock, String instanceId){
        this.lock = lock;
        this.instanceId = instanceId;
    }

    @Override
    public void nodeChanged(ZKClient client,String nodePath, Object data, State state) {
        if (State.NODE_DELETE != state){
            return ;
        }
        LockEntity entity = LockEntity.newEntity(lock.getExpire(),instanceId);
        String createPath = ZKKits.createPerNode(nodePath,entity);
        if (StringUtils.isNotBlank(createPath)){
            lock.getLatch(instanceId).countDown();
            ZKKits.removeListener(nodePath,this);
            return ;
        }
    }

    @Override
    public ZKDataListener getListener() {
        return this.listener;
    }

}
