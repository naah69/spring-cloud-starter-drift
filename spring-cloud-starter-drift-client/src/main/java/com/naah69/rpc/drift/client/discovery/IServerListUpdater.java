package com.naah69.rpc.drift.client.discovery;

/**
 * 服务列表更新接口
 * interface of server list updater
 *
 * @author naah
 */
public interface IServerListUpdater {

    /**
     * 更新动作接口
     * interface of update action
     */
    public interface IUpdateAction {
        /**
         * 更新
         * update action
         */
        void doUpdate();
    }

    /**
     * 开始任务
     * start to update action
     *
     * @param updateAction 更新动作接口
     */
    void start(IUpdateAction updateAction);

    /**
     * 停止任务
     * stop to update action
     */
    void stop();

}
