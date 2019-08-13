package com.ddphin.rabbitmq.sender;

/**
 * RabbitmqCommonDelayQueueSender
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public interface RabbitmqCommonDelayQueueSender {
    String SENDER_COMMON_DELAY_QUEUE = "sender.common.delay.queue";
    String SENDER_COMMON_DELAY_EXCHANGE = "sender.common.delay.exchange";
    String SENDER_COMMON_DELAY_ROUTING_KEY = "sender.common.delay.routing.key";

    void send(Object data, Long millis);
}
