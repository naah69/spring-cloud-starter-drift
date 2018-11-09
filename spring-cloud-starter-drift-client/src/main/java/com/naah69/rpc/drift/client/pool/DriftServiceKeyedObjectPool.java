package com.naah69.rpc.drift.client.pool;

import com.naah69.rpc.drift.client.common.DriftServerNode;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * 服务连接池
 *
 * @author naah
 */
public class DriftServiceKeyedObjectPool extends GenericKeyedObjectPool<DriftServerNode, Object> {


    public DriftServiceKeyedObjectPool(KeyedPooledObjectFactory factory) {
        super(factory);
    }

    public DriftServiceKeyedObjectPool(KeyedPooledObjectFactory factory, GenericKeyedObjectPoolConfig config) {
        super(factory, config);
    }

    /**
     * 取出连接
     *
     * @param key
     * @return
     * @throws Exception
     */
    @Override
    public Object borrowObject(DriftServerNode key) throws Exception {
        return super.borrowObject(key);
    }

    /**
     * 归还连接
     *
     * @param key
     * @param obj
     */
    @Override
    public void returnObject(DriftServerNode key, Object obj) {
        super.returnObject(key, obj);
    }
}
