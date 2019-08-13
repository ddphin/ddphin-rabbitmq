package com.ddphin.rabbitmq.receiver.impl;


import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiverHandler;
import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiverHandlerHolder;

import javax.annotation.PostConstruct;

/**
 * RabbitmqCommonQueueReceiverHandlerRegister
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public abstract class RabbitmqCommonQueueReceiverHandlerRegister<H, T> implements RabbitmqCommonQueueReceiverHandler<T> {
    @PostConstruct
    public void register() {
        RabbitmqCommonQueueReceiverHandlerHolder.add(this);
    }
}
