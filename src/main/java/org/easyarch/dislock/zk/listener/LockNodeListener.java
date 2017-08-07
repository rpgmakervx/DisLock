package org.easyarch.dislock.zk.listener;

import org.apache.commons.lang.StringUtils;
import org.easyarch.dislock.lock.entity.LockEntity;
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
        System.out.println(State.NODE_DELETE.equals(state));
        if (!State.NODE_DELETE.equals(state)){
            System.out.println(state);
            return ;
        }
        System.out.println("instanceId:"+instanceId+" - "+nodePath);
        LockEntity entity = LockEntity.newEntity(lock.getExpire(),instanceId);
        System.out.println("instanceId"+instanceId+" - createPath:"+nodePath);
        String createPath = ZKKits.createPerNode(nodePath,entity);
        if (StringUtils.isNotBlank(createPath)){
            lock.getLatch(instanceId).countDown();
            ZKKits.removeListener(nodePath,this);
            System.out.println("instanceId:"+instanceId+"- "+nodePath+" 节点删除");
            return ;
        }
    }

    @Override
    public ZKDataListener getListener() {
        return this.listener;
    }

}
