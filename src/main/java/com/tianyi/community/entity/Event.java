package com.tianyi.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {

    private String topic;

    private int userId;

    private int entityType;

    private int entityId;

    private int entityUserId;


    private Map<String, Object> otherData = new HashMap<>(); // make it more extensible

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
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

    public Map<String, Object> getOtherData() {
        return otherData;
    }

    public Event setOtherData(String key, Object value) {
        this.otherData.put(key, value);
        return this;
    }

}
