package com.practice.course_registration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.kafka.enabled=false")
class CourseRegistrationApplicationTests {

	@Test
	void contextLoads() {
	}

}
