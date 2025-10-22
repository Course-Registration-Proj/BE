package com.practice.course_registration;

import com.practice.course_registration.global.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CourseRegistrationApplicationTests {
    @MockitoBean
    private KafkaProducer kafkaProducer;
	@Test
	void contextLoads() {
	}

}
