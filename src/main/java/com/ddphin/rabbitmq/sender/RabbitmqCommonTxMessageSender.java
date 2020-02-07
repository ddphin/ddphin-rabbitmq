package com.ddphin.rabbitmq.sender;

/**
 * RabbitmqCommonTxMessageSender
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public interface RabbitmqCommonTxMessageSender {
    void send(String exchange, String routingKey, Integer millis, final Object message);
    void send(String exchange, String routingKey, final Object message);
}
