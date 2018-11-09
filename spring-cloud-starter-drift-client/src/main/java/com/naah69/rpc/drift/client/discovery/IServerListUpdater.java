package com.naah69.rpc.drift.client.discovery;

/**
 * 服务列表更新接口
 *
 * @author naah
 */
public interface IServerListUpdater {

    /**
     * 更新动作接口
     */
    public interface IUpdateAction {
        /**
         * 更新
         */
        void doUpdate();
    }

    /**
     * 开始任务
     *
     * @param updateAction 更新动作接口
     */
    void start(IUpdateAction updateAction);

    /**
     * 停止任务
     */
    void stop();

}
