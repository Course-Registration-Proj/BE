package com.practice.course_registration.global.redis.service;

import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/*
* @TODO
*   rate-limit과 멱등키 처리 관련 클래스
* */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean rateLimitAllow(Long memberId, int limit, Duration ttl) {
        String key = RedisKeyUtils.rateLimitKey(memberId);
        Long cnt = stringRedisTemplate.opsForValue().increment(key);
        // System.out.println("cnt : " + cnt);
        // 첫 요청인 경우 -> TTL 1로 세팅
        if (cnt != null && cnt == 1L) {
            stringRedisTemplate.expire(key, ttl);
        }

        return cnt != null && cnt <= limit;
    }

    public boolean acquireIdempotency(Long memberId, String subjectCode, Duration ttl) {
        String key = makeIdemKey(memberId, subjectCode);

        // key 없을 때 만들어서 1 넣기
        Boolean isSuccess = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(isSuccess);
    }

    // 멱등키 제거
    public void releaseIdempotency(Long memberId, String subjectCode) {
        stringRedisTemplate.delete(makeIdemKey(memberId, subjectCode));
    }

    private String makeIdemKey(Long memberId, String subjectCode) {
        return RedisKeyUtils.idempotencyKey(memberId, subjectCode);
    }

}
