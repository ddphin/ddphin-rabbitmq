package com.ddphin.rabbitmq.configuration;

import lombok.Data;

/**
 * DdphinRabbitmqProperties
 *
 * @Date 2019/8/26 下午3:59
 * @Author ddphin
 */
@Data
public class DdphinRabbitmqProperties {
    private Long idPrepareTimeout;
    private Long idDoTimeout;
    private String retryCron;
    private String redoCron;
    private String clearCron;
    private Integer poolSize;
    private Boolean enableRetry;
    private Boolean enableRedo;
    private Boolean enableClear;
}
