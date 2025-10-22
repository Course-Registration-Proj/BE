package com.practice.course_registration.global.redis.test;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthCheck implements CommandLineRunner {
    private final StringRedisTemplate redis;

    @Override
    public void run(String... args) {
        redis.opsForValue().set("test:key", "HelloRedis");
        System.out.println("✅ Redis connected: " + redis.opsForValue().get("test:key"));
    }
}