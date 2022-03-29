package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@Component
public class EventConsumer implements CommunityConstant {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    MessageService messageService;

    /**
     * 被动订阅消息，并将消息入库
     * @param record
     */
    @KafkaListener(topics = {TOPIC_FOLLOW, TOPIC_COMMENT, TOPIC_LIKE})
    public void handleMessage(ConsumerRecord record) {
        // 1.边界条件：先检查有无取到消息
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }
        // 2.将拿到的消息恢复成Object类型，方便操作
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("消息的格式错了!");
            return;
        }
        // 3.用拿到的数据创建Message对象，入库
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        // Message的content，是要通知的内容，由消息中的数据拼接而成
        // xxx了您的xxx
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getFromUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        // event中的其他数据，也都放进content中
        if (event.getData() != null) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        // 入库-调用service层业务代码-增加了过滤器
        messageService.addMessage(message);
    }
}
