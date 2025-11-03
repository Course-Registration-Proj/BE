package com.practice.course_registration.global.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, RegistrationMessage> kafkaTemplate;
    private final String targetTopic = "registration-queue";

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
        try{
            CompletableFuture<SendResult<String, RegistrationMessage>> future =
                    kafkaTemplate.send(targetTopic, subjectCode, message);

            future.whenCompleteAsync((result, ex) -> {
                if (ex == null){
                    onSuccess(result, message, requestId);
                }
                else {
                    // 비동기 실패
                    handleException(ex, message);
                }
            });
        } catch (Exception ex){
            // 동기 실패
            handleException(ex, message);
        }
    }

    private <T> void onSuccess(final SendResult<String, RegistrationMessage> result, final T t, String requestId){
        log.info("성공적인 메시지 =[{}] request-id= {} topic-partition={}-{} offset={}",
                t,
                requestId,
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }

    // Message를 발급하는 과정에서 생긴 오류 핸들링
    private <T> void handleException(Throwable ex, RegistrationMessage message) {
        if (ex.getCause() instanceof RetriableException){
            log.warn("Retriable Exception이 발생하였습니다. 자동으로 다시 시도합니다. {}", ex.getMessage());
        } else if (ex.getCause() instanceof SerializationException){
            log.error("Serialization 요류입니다. 메시지를 확인하세요. {}", message, ex.getMessage());
        } else {
            log.error("{} 메시지를 dead-letter queue로 전송합니다. 에러는 다음과 같습니다. {}", message, ex.getMessage());
            sendToDeadLetterTopic(message, targetTopic);
        }
    }

    private <T> void sendToDeadLetterTopic(T message, final String topic){
        String deadLetterTopic = topic + ".DLT";
        log.info("DLT로 메시지를 보냅니다 : {}", deadLetterTopic);
        kafkaTemplate.send(deadLetterTopic, (RegistrationMessage) message);
    }
}
