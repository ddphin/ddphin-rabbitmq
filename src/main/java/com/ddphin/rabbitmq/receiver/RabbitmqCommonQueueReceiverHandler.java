package com.ddphin.rabbitmq.receiver;

import com.ddphin.rabbitmq.entity.Result;

/**
 * RabbitmqCommonQueueReceiverHandler
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public interface RabbitmqCommonQueueReceiverHandler<T> {
    Result process(T data);
}
