package com.ddphin.rabbitmq.sender.impl;

import com.alibaba.fastjson.JSONObject;
import com.ddphin.id.endpoint.IDWorkerAware;
import com.ddphin.jedis.helper.JedisHelper;
import com.ddphin.rabbitmq.configuration.DdphinRabbitmqProperties;
import com.ddphin.rabbitmq.entity.CorrelationDataMQ;
import com.ddphin.rabbitmq.entity.CorrelationDataRedis;
import com.ddphin.rabbitmq.sender.RabbitmqCommonTxMessageMonitor;
import com.ddphin.rabbitmq.sender.RabbitmqCommonTxMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RabbitmqCommonTxMessageActor
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
@Slf4j
public abstract class RabbitmqCommonTxMessageActor
        extends IDWorkerAware
        implements RabbitmqCommonTxMessageSender, RabbitmqCommonTxMessageMonitor
        , RabbitTemplate.ConfirmCallback
        , RabbitTemplate.ReturnCallback
        , TransactionSynchronization
        , Ordered
        , InitializingBean {
    private final static String message_cache_name_id_prepare = "_mq_@message@zset@id_prepare";
    private final static String message_cache_name_id_do = "_mq_@message@zset@id_do";
    private final static String message_cache_name_id_redo = "_mq_@message@set@id_redo";

    private final static String message_cache_name_data_death = "_mq_@message@hash@data_death";
    private final static String message_cache_name_data_normal = "_mq_@message@hash@data_normal";

    private String script_mq_save_prepare;
    private String script_mq_move_prepare_to_do;
    private String script_mq_remove_prepare;
    private String script_mq_remove_do;
    private String script_mq_move_do_to_redo_with_id;
    private String script_mq_save_death;
    private String script_mq_move_redo_to_do;
    private String script_mq_move_do_to_redo_with_timestamp;
    private String script_mq_move_prepare_to_death;

    private RabbitTemplate rabbitTemplate;
    private JedisHelper jedisHelper;
    private ThreadLocal<List<CorrelationDataMQ>> messageList = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<Boolean> register = ThreadLocal.withInitial(() -> false);

    private Long idPrepareTimeout = 60000L;
    private Long idDoTimeout = 60000L;

    public RabbitmqCommonTxMessageActor(
            RabbitTemplate rabbitTemplate,
            JedisHelper jedisHelper,
            DdphinRabbitmqProperties ddphinRabbitmqProperties) throws IOException {
        this.rabbitTemplate = rabbitTemplate;
        this.jedisHelper = jedisHelper;
        if (null != ddphinRabbitmqProperties.getIdPrepareTimeout()) {
            this.idPrepareTimeout = ddphinRabbitmqProperties.getIdPrepareTimeout();
        }
        if (null != ddphinRabbitmqProperties.getIdDoTimeout()) {
            this.idDoTimeout = ddphinRabbitmqProperties.getIdDoTimeout();
        }

        ResourceLoader resourceLoader = new DefaultResourceLoader();

        this.script_mq_save_prepare = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_save_prepare.lua")).getScriptAsString();

        this.script_mq_move_prepare_to_do = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_move_prepare_to_do.lua")).getScriptAsString();

        this.script_mq_remove_prepare = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_remove_prepare.lua")).getScriptAsString();

        this.script_mq_remove_do = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_remove_do.lua")).getScriptAsString();

        this.script_mq_move_do_to_redo_with_id = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_move_do_to_redo_with_id.lua")).getScriptAsString();

        this.script_mq_save_death = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_save_death.lua")).getScriptAsString();

        this.script_mq_move_redo_to_do = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_move_redo_to_do.lua")).getScriptAsString();

        this.script_mq_move_do_to_redo_with_timestamp = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_move_do_to_redo_with_timestamp.lua")).getScriptAsString();

        this.script_mq_move_prepare_to_death = new ResourceScriptSource(resourceLoader.getResource(
                "classpath:lua/mq_move_prepare_to_death.lua")).getScriptAsString();
    }

    private Long script_mq_save_prepare(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_save_prepare,
                Arrays.asList(
                        message_cache_name_id_prepare,
                        message_cache_name_data_normal),
                data.getId(),
                JSONObject.toJSONString(data),
                System.currentTimeMillis());
    }

    private Long script_mq_move_prepare_to_do(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_move_prepare_to_do,
                Arrays.asList(
                        message_cache_name_id_prepare,
                        message_cache_name_id_do),
                data.getId(),
                System.currentTimeMillis());
    }

    private Long script_mq_remove_prepare(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_remove_prepare,
                Arrays.asList(
                        message_cache_name_id_prepare,
                        message_cache_name_data_normal),
                data.getId());
    }

    private Long script_mq_remove_do(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_remove_do,
                Arrays.asList(
                        message_cache_name_id_do,
                        message_cache_name_data_normal),
                data.getId());
    }


    private Long script_mq_move_do_to_redo_with_id(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_move_do_to_redo_with_id,
                Arrays.asList(
                        message_cache_name_id_do,
                        message_cache_name_id_redo),
                data.getId());
    }

    private Long script_mq_save_death(CorrelationDataRedis data) {
        return (Long) eval(
                script_mq_save_death,
                Collections.singletonList(
                        message_cache_name_data_death),
                data.getId(),
                data);
    }


    private String script_mq_move_redo_to_do() {
        return (String) eval(
                script_mq_move_redo_to_do,
                Arrays.asList(
                        message_cache_name_id_redo,
                        message_cache_name_id_do,
                        message_cache_name_data_normal),
                System.currentTimeMillis());
    }


    private Long script_mq_move_do_to_redo_with_timestamp() {
        return (Long) eval(
                script_mq_move_do_to_redo_with_timestamp,
                Arrays.asList(
                        message_cache_name_id_do,
                        message_cache_name_id_redo),
                System.currentTimeMillis() - this.idDoTimeout);//一分钟之前
    }


    private Long script_mq_move_prepare_to_death() {
        return (Long) eval(
                script_mq_move_prepare_to_death,
                Arrays.asList(
                        message_cache_name_id_prepare,
                        message_cache_name_data_normal,
                        message_cache_name_data_death),
                System.currentTimeMillis() - this.idPrepareTimeout);//一分钟之前
    }

    private Object eval(String script, List<String> keys, Object... params) {
        Jedis jedis = jedisHelper.getJedis();
        Long x =  (Long) jedis.eval(
                script,
                keys,
                Arrays.stream(params).map(String::valueOf).collect(Collectors.toList()));
        jedisHelper.closeJedis(jedis);
        return x;
    }

    @Override
    public void send(String exchange, String routingKey, final Object message) {
        this.send(exchange, routingKey, null, message);
    }

    @Override
    public void send(String exchange, String routingKey, Integer millis, final Object message) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "@Transactional is required");

        CorrelationDataMQ correlationData = new CorrelationDataMQ(exchange, routingKey, millis, message, String.valueOf(this.nextId()));

        if (!register.get()) {
            TransactionSynchronizationManager.registerSynchronization(this);
            register.set(true);
            messageList.set(new ArrayList<>());
        }
        messageList.get().add(correlationData);
    }

    private void send(CorrelationDataMQ correlationData) {
        log.info("发送消息:\n" +
                        "      exchange: {}\n" +
                        "    routingKey: {}\n" +
                        "        millis: {}\n" +
                        "       message: {}",
                correlationData.getExchange(),
                correlationData.getRoutingKey(),
                correlationData.getMillis(),
                correlationData.getData());
        Object message = null;
        try {
            message = JSONObject.parseObject(correlationData.getData(), Class.forName(correlationData.getClazz()));
        } catch (ClassNotFoundException e) {
            log.error("retry send message error", e);
        }
        if (null != correlationData.getMillis()) {
            this.rabbitTemplate.convertAndSend(
                    correlationData.getExchange(),
                    correlationData.getRoutingKey(),
                    message,
                    msg -> {
                        //设置消息持久化
                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        //message.getMessageProperties().setHeader("x-delay", "6000");
                        msg.getMessageProperties().setDelay(correlationData.getMillis());
                        return msg;
                    },
                    correlationData);
        }
        else {
            this.rabbitTemplate.convertAndSend(
                    correlationData.getExchange(),
                    correlationData.getRoutingKey(),
                    message,
                    correlationData);
        }
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        CorrelationDataMQ correlationDataExt = (CorrelationDataMQ) correlationData;
        if (ack) {
            log.info("消息发送到exchange成功:\n" +
                            "    id: {}",
                    correlationData.getId());
            this.script_mq_remove_do(new CorrelationDataRedis(correlationDataExt));

        } else {
            if (3 > correlationDataExt.getRetry()) {
                try {
                    Thread.sleep((correlationDataExt.getRetry() + 1) * 1000);
                } catch (InterruptedException e) {
                    log.error("confirm error", e);
                }

                log.warn("消息发送到exchange失败:重试:\n" +
                                "       id: {}\n" +
                                "    cause: {}",
                        correlationData.getId(),
                        cause);
                this.send(correlationDataExt.retry());
            }
            else {
                log.error("消息发送到exchange失败:保存:\n" +
                                "       id: {}\n" +
                                "    cause: {}",
                        correlationData.getId(),
                        cause);

                this.script_mq_move_do_to_redo_with_id(new CorrelationDataRedis(correlationDataExt));
            }
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        Integer millis = message.getMessageProperties().getReceivedDelay();

        if (null != millis) {
            String id = String.valueOf(message.getMessageProperties().getHeaders().get("spring_returned_message_correlation"));
            String clazz = String.valueOf(message.getMessageProperties().getHeaders().get("__TypeId__"));
            String data = new String(message.getBody());
            Object obj = this.getObject(data, clazz);
            Assert.notNull(obj, "message 消息体不能为NULL");


            log.error("消息发送到queue失败:保存:\n" +
                            "            id: {}\n" +
                            "         clazz: {}\n"+
                            "          data: {}\n"+
                            "      exchange: {}\n"+
                            "    routingKey: {}\n"+
                            "     replyText: {}\n",
                    id,
                    clazz,
                    data,
                    exchange,
                    routingKey,
                    replyText);

            CorrelationDataMQ correlationData = new CorrelationDataMQ(exchange, routingKey, millis, obj, id);
            this.script_mq_save_death(new CorrelationDataRedis(correlationData));
        }

    }

    private Object getObject(String data, String clazz) {
        try {
            return JSONObject.parseObject(data, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            log.error("getObject", e);
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.rabbitTemplate.setConfirmCallback(this);
        this.rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        if (!CollectionUtils.isEmpty(messageList.get())) {
            messageList.get().forEach(o -> this.script_mq_save_prepare(new CorrelationDataRedis(o)));
        }
    }

    @Override
    public void afterCommit() {
        if (!CollectionUtils.isEmpty(messageList.get())) {
            messageList.get().forEach(o -> {
                this.script_mq_move_prepare_to_do(new CorrelationDataRedis(o));
                this.send(o);
            });
        }

    }

    @Override
    public void afterCompletion(int status) {
        if (TransactionSynchronization.STATUS_COMMITTED != status && !CollectionUtils.isEmpty(messageList.get())) {
            messageList.get().forEach(o -> this.script_mq_remove_prepare(new CorrelationDataRedis(o)));
        }
        register.remove();
        register.set(false);
        messageList.remove();
        messageList.set(null);
    }

    @Override
    public void retry() {
        log.info("retry");
        String data;
        do {
            data = this.script_mq_move_redo_to_do();
            log.info("retry\n" +
                    "    data:{}", data);
            if (null != data) {
                CorrelationDataRedis obj = JSONObject.parseObject(data, CorrelationDataRedis.class);
                this.send(new CorrelationDataMQ(obj.getExchange(), obj.getRoutingKey(), obj.getMillis(), obj.getData(), obj.getId()));
            }
        }
        while(null != data);
    }

    @Override
    public void redo() {
        log.info("redo");
       this.script_mq_move_do_to_redo_with_timestamp();
    }

    @Override
    public void clear() {
        log.info("clear");
        this.script_mq_move_prepare_to_death();
    }
}
