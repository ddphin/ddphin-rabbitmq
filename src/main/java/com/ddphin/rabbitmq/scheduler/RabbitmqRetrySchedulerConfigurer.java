package com.ddphin.rabbitmq.scheduler;

import com.ddphin.rabbitmq.sender.RabbitmqCommonTxMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitmqRetryScheduler
 *
 * @Date 2019/7/24 下午6:12
 * @Author ddphin
 */
@Slf4j
public class RabbitmqRetrySchedulerConfigurer implements SchedulingConfigurer {
    private RabbitmqCommonTxMessageSender rabbitmqCommonTxMessageSender;
    private AtomicInteger integer = new AtomicInteger(0);
    private String retryCron = "0 0/1 * * * ?";
    private String redoCron = "30 0/1 * * * ?";
    private String clearCron = "0 0/1 * * * ?";

    public RabbitmqRetrySchedulerConfigurer(RabbitmqCommonTxMessageSender rabbitmqCommonTxMessageSender) {
        this.rabbitmqCommonTxMessageSender = rabbitmqCommonTxMessageSender;
    }

    public String getRetryCron() {
        return this.retryCron;
    }
    public void setRetryCron(String retryCron) {
        this.retryCron = retryCron;
    }
    public String getRedoCron() {
        return this.redoCron;
    }
    public void setRedoCron(String redoCron) {
        this.redoCron = redoCron;
    }
    public String getClearCron() {
        return this.clearCron;
    }
    public void setClearCron(String clearCron) {
        this.clearCron = clearCron;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(this.newExecutors());

        scheduledTaskRegistrar.addTriggerTask(this::retry, triggerContext -> {
            CronTrigger trigger = new CronTrigger(this.getRetryCron());
            return trigger.nextExecutionTime(triggerContext);
        });

        scheduledTaskRegistrar.addTriggerTask(this::redo, triggerContext -> {
            CronTrigger trigger = new CronTrigger(this.getRedoCron());
            return trigger.nextExecutionTime(triggerContext);
        });

        scheduledTaskRegistrar.addTriggerTask(this::clear, triggerContext -> {
            CronTrigger trigger = new CronTrigger(this.getClearCron());
            return trigger.nextExecutionTime(triggerContext);
        });
    }

    private Executor newExecutors() {
        return Executors.newScheduledThreadPool(10, r -> new Thread(r, String.format("DDphin-Rabbitmq-%s", integer.incrementAndGet())));
    }

    private void retry() {
        log.info("MQ message retry begin: CRON@{} - AT@{}", this.getRetryCron(), new Date());
        rabbitmqCommonTxMessageSender.retry();
        log.info("MQ message retry end: CRON@{} - AT@{}",this.getRetryCron(), new Date());
    }

    private void redo() {
        log.info("MQ message redo begin: CRON@{} - AT@{}", this.getRedoCron(), new Date());
        rabbitmqCommonTxMessageSender.redo();
        log.info("MQ message redo end: CRON@{} - AT@{}",this.getRedoCron(), new Date());
    }

    private void clear() {
        log.info("MQ message clear begin: CRON@{} - AT@{}", this.getRedoCron(), new Date());
        rabbitmqCommonTxMessageSender.clear();
        log.info("MQ message clear end: CRON@{} - AT@{}",this.getRedoCron(), new Date());
    }
}
