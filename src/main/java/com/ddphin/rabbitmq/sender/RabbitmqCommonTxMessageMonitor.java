package com.ddphin.rabbitmq.sender;

/**
 * RabbitmqCommonTxMessageMonitor
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public interface RabbitmqCommonTxMessageMonitor {
    void retry();
    void redo();
    void clear();
}
