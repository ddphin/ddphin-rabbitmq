package com.ddphin.rabbitmq.entity;

import lombok.Data;

/**
 * Result
 *
 * @Date 2019/9/5 下午9:31
 * @Author ddphin
 */
@Data
public class Result {
    private Boolean success;
    private Boolean requeue;
    private Object data;

    public Result(Boolean success, Boolean requeue, Object data) {
        this.success = success;
        this.requeue = requeue;
        this.data = data;
    }
}
