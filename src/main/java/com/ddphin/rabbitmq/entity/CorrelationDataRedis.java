package com.ddphin.rabbitmq.entity;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * CorrelationDataRedis
 *
 * @Date 2019/8/20 下午7:49
 * @Author ddphin
 */
@Data
public class CorrelationDataRedis implements Serializable {
    private String id;
    private String data;
    private String clazz;
    private String exchange;
    private String routingKey;
    private Integer millis;
    private Long created;

    public CorrelationDataRedis() {}

    public CorrelationDataRedis(CorrelationDataMQ correlationData) {
        Assert.notNull(correlationData.getId(), "id 消息体不能为NULL");
        Assert.notNull(correlationData.getData(), "message 消息体不能为NULL");
        Assert.notNull(correlationData.getExchange(), "exchange 不能为NULL");
        Assert.notNull(correlationData.getRoutingKey(), "routingKey 不能为NULL");
        Assert.notNull(correlationData.getClazz(), "clazz 不能为NULL");
        this.id = correlationData.getId();
        this.data = correlationData.getData();
        this.clazz = correlationData.getClazz();
        this.exchange = correlationData.getExchange();
        this.routingKey = correlationData.getRoutingKey();
        this.millis = correlationData.getMillis();
        this.created = System.currentTimeMillis();
    }

}
