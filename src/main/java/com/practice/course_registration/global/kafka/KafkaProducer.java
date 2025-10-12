package com.practice.course_registration.global.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, RegistrationMessage> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, RegistrationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(Long userId, String subjectCode){
        String requestId = UUID.randomUUID().toString();

        RegistrationMessage message = new RegistrationMessage(
                userId,
                subjectCode,
                System.currentTimeMillis(),
                requestId
        );

        // 카프카로 전송한다.
        kafkaTemplate.send("registration-queue", subjectCode, message);
    }
}
