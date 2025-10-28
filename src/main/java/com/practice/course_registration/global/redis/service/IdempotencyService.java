package com.practice.course_registration.global.redis.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:"; // rate limit key
    private static final String IDEMPOTENCY_KEY_PREFIX = "idem:"; // 멱등키 prefix

    public boolean rateLimitAllow(String k, int limit, Duration ttl) {
        String key = RATE_LIMIT_KEY_PREFIX + k;
        Long cnt = stringRedisTemplate.opsForValue().increment(key);
        System.out.println("cnt : " + cnt);
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
        return IDEMPOTENCY_KEY_PREFIX + memberId + ":" + subjectCode;
    }

}
