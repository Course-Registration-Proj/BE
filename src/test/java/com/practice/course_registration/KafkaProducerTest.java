package com.practice.course_registration;

import com.practice.course_registration.global.kafka.KafkaProducer;
import com.practice.course_registration.global.kafka.RegistrationMessage;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerTest {
    @Mock
    private KafkaTemplate<String, RegistrationMessage> kafkaTemplate;
    @InjectMocks
    KafkaProducer kafkaProducer;


    @Test
    @DisplayName("예외 발생 시 DLT로 전송한다.")
    void ShouldSendToDLT() {
        // given
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("브로커 이용 불가")
                ));

        // when
        kafkaProducer.create(1L, "TEST1");

        // then
        verify(kafkaTemplate, timeout(1000))
                .send(eq("registration-queue.DLT"), any(RegistrationMessage.class));
    }


    @Test
    @DisplayName("RetriableException은 DLT로 안보낸다")
    void ShouldNotSendToDLT() {
        // given
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(
                        new TimeoutException("Temporary failure")
                ));

        // when
        kafkaProducer.create(1L, "TEST1");

        // then
        verify(kafkaTemplate, times(1))
                .send(eq("registration-queue"), anyString(), any());

        verify(kafkaTemplate, never())
                .send(eq("registration-queue.DLT"), any());

    }
}
