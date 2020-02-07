package com.ddphin.rabbitmq.sender.impl;

import com.ddphin.jedis.helper.JedisHelper;
import com.ddphin.rabbitmq.configuration.DdphinRabbitmqProperties;
import com.ddphin.rabbitmq.sender.RabbitmqCommonTxMessageMonitor;
import com.ddphin.rabbitmq.sender.RabbitmqCommonTxMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;

import java.io.IOException;

/**
 * TestSender
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
@Slf4j
public class RabbitmqCommonTxMessageMonitorImpl
        extends RabbitmqCommonTxMessageActor
        implements RabbitmqCommonTxMessageMonitor
        , RabbitTemplate.ConfirmCallback
        , RabbitTemplate.ReturnCallback
        , TransactionSynchronization
        , Ordered
        , InitializingBean {

    public RabbitmqCommonTxMessageMonitorImpl(
            RabbitTemplate rabbitTemplate,
            JedisHelper jedisHelper,
            DdphinRabbitmqProperties ddphinRabbitmqProperties) throws IOException {
        super(rabbitTemplate, jedisHelper, ddphinRabbitmqProperties);
    }
}
