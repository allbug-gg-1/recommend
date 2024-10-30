package com.sofm.recommend.infrastructure.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageForwarder {

    @Lazy
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageForwarder(@Lazy KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void forwardMessage(String topic, String itemId, String message) {
        kafkaTemplate.send(topic, itemId, message);
    }
}
