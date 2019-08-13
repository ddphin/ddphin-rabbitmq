package com.ddphin.rabbitmq.receiver.impl;

import com.alibaba.fastjson.JSONObject;
import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiver;
import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiverHandler;
import com.ddphin.rabbitmq.receiver.RabbitmqCommonQueueReceiverHandlerHolder;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import java.io.IOException;

/**
 * RabbitmqCommonAbstractQueueReceiver
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
@Slf4j
public abstract class RabbitmqCommonAbstractQueueReceiver implements RabbitmqCommonQueueReceiver {
    @Override
    public void receiver(Message message, org.springframework.amqp.core.Message amqpMessage, Channel channel) throws IOException {

        Object data = message.getPayload();
        RabbitmqCommonQueueReceiverHandler handler = RabbitmqCommonQueueReceiverHandlerHolder.get(this.getClass(), data);
        if (null != handler) {
            log.info("收到延息且找到消息处理器:\n" +
                      "       data: {}\n" +
                      "    message: {}\n" +
                      "    channel: {}\n" +
                      "    receiver: {}",
                    JSONObject.toJSONString(data),
                    JSONObject.toJSONString(message),
                    JSONObject.toJSONString(channel),
                    handler.getClass().getName());
            Boolean requeue = handler.process(data);
            if (null == requeue) {
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
                log.info("收到并确认消息:\n" +
                          "       data: {}\n" +
                          "    message: {}\n" +
                          "    channel: {}\n" +
                          "    receiver: {}",
                        JSONObject.toJSONString(data),
                        JSONObject.toJSONString(message),
                        JSONObject.toJSONString(channel),
                        handler.getClass().getName());
            }
            else {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, requeue);
                log.warn("收到并丢弃消息:\n" +
                          "       data: {}\n" +
                          "    message: {}\n" +
                          "    channel: {}\n" +
                          "    receiver: {}\n" +
                          "    requeue: {}",
                        JSONObject.toJSONString(data),
                        JSONObject.toJSONString(message),
                        JSONObject.toJSONString(channel),
                        handler.getClass().getName(),
                        requeue);
            }
        }
        else {
            channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            log.warn("收到消息但未找到消息处理器丢弃消息:\n" +
                      "       data: {}\n" +
                      "    message: {}\n" +
                      "    channel: {}\n",
                    JSONObject.toJSONString(data),
                    JSONObject.toJSONString(message),
                    JSONObject.toJSONString(channel));
        }
    }
}
