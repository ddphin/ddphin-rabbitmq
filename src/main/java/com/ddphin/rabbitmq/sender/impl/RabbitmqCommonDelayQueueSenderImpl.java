package com.ddphin.rabbitmq.sender.impl;

import com.alibaba.fastjson.JSONObject;
import com.ddphin.rabbitmq.sender.RabbitmqCommonDelayQueueSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitmqCommonDelayQueueSenderImpl
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
@Slf4j
public class RabbitmqCommonDelayQueueSenderImpl implements RabbitmqCommonDelayQueueSender {
    private RabbitTemplate rabbitTemplate;

    public RabbitmqCommonDelayQueueSenderImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Object data, Long millis) {
        log.info("发送延迟消息:\n" +
                  "      data: {}\n" +
                  "    millis: {}",
                JSONObject.toJSONString(data),
                millis);
        this.rabbitTemplate.convertAndSend(
                SENDER_COMMON_DELAY_EXCHANGE,
                SENDER_COMMON_DELAY_ROUTING_KEY,
                data,
                message -> {
                    // 如果配置了 params.put("x-message-ttl", 5 * 1000);
                    // 那么这一句也可以省略,具体根据业务需要是声明 Queue 的时候就指定好延迟时间还是在发送自己控制时间
                    message.getMessageProperties().setExpiration(String.valueOf(millis));
                    return message;
                });
    }
}
