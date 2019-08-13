package com.ddphin.rabbitmq.receiver;

import com.rabbitmq.client.Channel;
import org.springframework.messaging.Message;

import java.io.IOException;

/**
 * RabbitmqCommonQueueReceiver
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */

public interface RabbitmqCommonQueueReceiver {
    void receiver(Message message, org.springframework.amqp.core.Message amqpMessage, Channel channel) throws IOException;
}
