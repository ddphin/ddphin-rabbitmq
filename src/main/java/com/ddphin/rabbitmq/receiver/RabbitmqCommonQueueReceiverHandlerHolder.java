package com.ddphin.rabbitmq.receiver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RabbitmqCommonQueueReceiverHandlerHolder
 *
 * @Date 2019/7/24 下午8:29
 * @Author ddphin
 */
public class RabbitmqCommonQueueReceiverHandlerHolder {
    private static final Map<String, Map<String, RabbitmqCommonQueueReceiverHandler>> map = new ConcurrentHashMap<>();

    public static void add(RabbitmqCommonQueueReceiverHandler handler) {
        Type genericSuperclass = handler.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Class tClass1 = (Class) actualTypeArguments[0];
        Class tClass2 = (Class) actualTypeArguments[1];
        map.computeIfAbsent(tClass1.getName(), o -> new ConcurrentHashMap<>()).put(tClass2.getName(), handler);
    }

    public static RabbitmqCommonQueueReceiverHandler get(Class<? extends RabbitmqCommonQueueReceiver> receiver, Object data) {
        return map.computeIfAbsent(receiver.getName(), o -> new ConcurrentHashMap<>()).get(data.getClass().getName());
    }
}
