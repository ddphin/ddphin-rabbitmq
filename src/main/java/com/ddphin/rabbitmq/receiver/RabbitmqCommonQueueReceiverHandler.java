package com.ddphin.rabbitmq.receiver;

/**
 * RabbitmqCommonQueueReceiverHandler
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public interface RabbitmqCommonQueueReceiverHandler<T> {
    Boolean process(T data);
}
