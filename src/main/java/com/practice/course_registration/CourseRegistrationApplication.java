package com.practice.course_registration;

import com.practice.course_registration.global.security.utils.HeaderUserIdProvider;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourseRegistrationApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(CourseRegistrationApplication.class, args);
        HeaderUserIdProvider headerUserIdProvider = context.getBean(HeaderUserIdProvider.class);
        SecurityUtils.setUserIdProvider(headerUserIdProvider);
	}
}
