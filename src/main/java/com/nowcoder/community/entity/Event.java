package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    // kafka服务器要识别的topic事件类型
    private String topic;
    // 事件触发者信息
    private int fromUserId;
    // 事件触发对象的信息
    private int entityType;
    private int entityId;
    private int entityUserId;
    // 其他信息-用map封装保存
    private Map<String, Object> data = new HashMap<>();

    public int getFromUserId() {
        return fromUserId;
    }

    /**
     * 修改各自的set方法，设置返回类型为Event当前类，方便对当前对象重复设置调用
     * @param fromUserId
     * @return  返回Event类型对象
     */
    public Event setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    /**
     * map数据，传入key，value，向map中添加数据，返回类型当前对象，方便多次调用添加键值对
     * @param key       传入map的key
     * @param value     传入map的value
     * @return
     */
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
