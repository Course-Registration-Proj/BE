package com.practice.course_registration.global.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        // 비동기로 카프카에 전송
        CompletableFuture<SendResult<String, RegistrationMessage>> future =
                kafkaTemplate.send("registration-queue", subjectCode, message);

        future.whenComplete((result, ex) -> {
            if (ex == null){
                log.info("메시지 전송 성공 - requestId: {}, topic: {}, partition: {}, offset: {}",
                        requestId,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
            else {
                log.error("메시지 전송 실패 - requestId: {}, userId: {}, subjectCode: {}, error: {}",
                        requestId, userId, subjectCode, ex.getMessage());
            }
        });

    }
}
