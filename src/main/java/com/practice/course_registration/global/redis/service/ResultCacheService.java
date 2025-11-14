package com.practice.course_registration.global.redis.service;

import com.practice.course_registration.global.enums.WaitQueueStatus;
import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultCacheService {

    private final StringRedisTemplate redisTemplate;

    public void inputResultCacheStatus(Long memberId, Long subjectId, Duration ttl) {
        String resultKey = RedisKeyUtils.couseResultKey(memberId, subjectId);
        redisTemplate.opsForValue().set(resultKey, String.valueOf(WaitQueueStatus.PENDING), ttl);
    }

    public String returnResultCacheStatus(Long memberId, Long subjectId) {
        String resultKey = RedisKeyUtils.couseResultKey(memberId, subjectId);
        return redisTemplate.opsForValue().get(resultKey);
    }
}
