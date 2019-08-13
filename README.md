# Rabbitmq 通用延时消息 与 通用消息接处理

## 通用延时消息
- 自定义消息类
```$xslt
@Data
public class DDphinMessage {
    private Long id;
    private String data;
    private Date time;
    
    public DDphinMessage(Long id, String data, Date time) {
        this.id = id;
        this.data = data;
        this.time = time;
    }
}
```
- 注入延时消息发送器
```$xslt
@Autowired
private RabbitmqCommonDelayQueueSender sender;
```
- 创建待发送消息
```$xslt
DDphinMessage message = new DDphinMessage(1L, "ddphin", new Date());
```
- 发送延时消息
```$xslt
this.sender.send(message, 5*1000L);
```
- 实现消息处理器<br>
基于延时消息鉴定器`RabbitmqCommonDelayQueueReceiver`消息类`DDphinMessage`实现消息处理器<br>
只需要继承`RabbitmqCommonQueueReceiverHandlerRegister<H,T>`并实现`RabbitmqCommonQueueReceiverHandler<T>`接口
  - H: 消息监听器
  - T: 自定义消息类
```$xslt
@Slf4j
@Service
public class RabbitmqDelayDDphinMessageReceiverHandler
        extends RabbitmqCommonQueueReceiverHandlerRegister<RabbitmqCommonDelayQueueReceiver, DDphinMessage>
        implements RabbitmqCommonQueueReceiverHandler<DDphinMessage> {

    @Override
    public Boolean process(DDphinMessage message) {
        log.info("RabbitmqDelayDDphinMessageReceiverHandler:\n" +
                "    message: {}", JSONObject.toJSONString(message));
        return null;
    }
}
```

## 通用消息接处理
- 自定义并注入延时消息发送器

  `根据需求自己实现`
- 自定义消息监听器
```$xslt
@Service
public class DDphinMessageQueueReceiver
        extends RabbitmqCommonAbstractQueueReceiver
        implements RabbitmqCommonQueueReceiver {
    @Override
    @RabbitListener(queues = "DDphinMessageQueue"})
    public void receiver(org.springframework.messaging.Message message, 
                        org.springframework.amqp.core.Message amqpMessage, 
                        Channel channel) throws IOException {
        super.receiver(message, amqpMessage, channel);
    }
}
```
- 自定义消息类 与【通用延时消息 > 自定义消息类】类似
- 创建待发送消息 与【通用延时消息 > 创建待发送消息】类似
- 发送消息 与【通用延时消息 > 发送延时消息】类似
- 实现消息处理器<br>
基于自定义的消息监听器`DDphinMessageQueueReceiver`自定义的消息类实现消息处理器 与【通用延时消息 > 实现消息处理器】类似