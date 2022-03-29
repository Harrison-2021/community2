package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer implements CommunityConstant {
    @Autowired
    KafkaTemplate kafkaTemplate;

    /**
     * 将消息发送到指定主题上
     * @param event      要发送的消息封装成的主题对象
     */
    public void sendEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
