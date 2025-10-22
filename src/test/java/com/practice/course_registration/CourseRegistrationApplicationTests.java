package com.practice.course_registration;

import com.practice.course_registration.global.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest
class CourseRegistrationApplicationTests {
    @TestConfiguration
    static class TestConfig {
        @Bean
        public KafkaProducer kafkaProducer() {
            // 실제 KafkaTemplate 등 의존을 가진 KafkaProducer를 Mockito로 대체
            return Mockito.mock(KafkaProducer.class);
        }
        // 필요한 경우 KafkaTemplate 같은 다른 빈들도 여기서 mock으로 등록
    }

	@Test
	void contextLoads() {
	}

}
