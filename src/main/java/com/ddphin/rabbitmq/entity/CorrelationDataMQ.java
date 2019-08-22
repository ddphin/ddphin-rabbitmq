package com.ddphin.rabbitmq.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.util.Assert;

/**
 * CorrelationDataMQ
 *
 * @Date 2019/8/20 下午7:49
 * @Author ddphin
 */
@Data
public class CorrelationDataMQ extends CorrelationData {
    private String id;
    private String data;
    private String clazz;
    private Integer retry;
    private String exchange;
    private String routingKey;
    private Long millis;

    public CorrelationDataMQ() {}

    public CorrelationDataMQ(String exchange, String routingKey, Long millis, final Object message, String id) {
        Assert.notNull(id, "id 消息体不能为NULL");
        Assert.notNull(message, "message 消息体不能为NULL");
        Assert.notNull(exchange, "exchange 不能为NULL");
        Assert.notNull(routingKey, "routingKey 不能为NULL");
        this.id = id;
        this.data = JSONObject.toJSONString(message);
        this.clazz = message.getClass().getName();
        this.retry = 0;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.millis = millis;

    }

    public CorrelationDataMQ retry() {
        ++this.retry;
        return this;
    }

}
