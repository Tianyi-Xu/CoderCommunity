package com.tianyi.community.event;

import com.alibaba.fastjson.JSONObject;
import com.tianyi.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplete;

    // produce
    public void fireEvent(Event event) {
        // produce the event to its topic
        kafkaTemplete.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
