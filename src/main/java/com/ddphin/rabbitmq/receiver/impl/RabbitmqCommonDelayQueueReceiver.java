package com.ddphin.rabbitmq.receiver.impl;

import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiver;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;

import java.io.IOException;

/**
 * RabbitmqCommonDelayQueueReceiver
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
@Slf4j
public class RabbitmqCommonDelayQueueReceiver extends RabbitmqCommonAbstractQueueReceiver implements RabbitmqCommonQueueReceiver {
    public final static String DDPHIN_COMMON_DELAY_QUEUE = "ddphin.common.delay.queue";
    public final static String DDPHIN_COMMON_DELAY_EXCHANGE = "ddphin.common.delay.exchange";
    public final static String DDPHIN_COMMON_DELAY_ROUTING_KEY = "ddphin.common.delay.routing.key";

    @Override
    @RabbitListener(queues = {DDPHIN_COMMON_DELAY_QUEUE})
    public Object receiver(Message message, org.springframework.amqp.core.Message amqpMessage, Channel channel) throws IOException {
        return super.receiver(message, amqpMessage, channel);
    }
}
