package com.practice.course_registration.global.redis.service;

import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


/*
* TODO : 대기열 처리와 관련된 코드를 넣어둔 클래스
*  대기열에 enqueue, dequeue 하는 코드가 있음
* */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaitQueueService {

    private final StringRedisTemplate redisTemplate;

    // 전역 대기열에 요청 삽입
    public void enqueueGlobal(Long memberId, Long subjectId, long nowMillis) {
        long start = System.currentTimeMillis(); // 측정 시작
        String queueKey = RedisKeyUtils.globalApplyQueueKey();
        String value = memberId + ":" + subjectId; // payload
        redisTemplate.opsForZSet().add(queueKey, value, nowMillis);
        long end = System.currentTimeMillis(); // 측정 종료
        log.info("Redis [enqueueGlobal] 소요시간: {}ms", (end - start));
        // Long size = redisTemplate.opsForZSet().size(queueKey);
        // log.info("대기열 등록: {}, queueSize={}", value, size);
    }

    // 사용자에게 보여줄 대기열 순번 조회
    public Long getQueuePosition(Long memberId, Long subjectId) {
        String queueKey = RedisKeyUtils.globalApplyQueueKey();
        String value = memberId + ":" + subjectId;
        Long rank = redisTemplate.opsForZSet().rank(queueKey, value);
        return rank == null ? null : (rank + 1);
    }

    // 토큰 존재 여부(값은 subjectId)
    public String peekToken(Long memberId) {
        String key = RedisKeyUtils.applyTokenKey(memberId);
        return redisTemplate.opsForValue().get(key);
    }

    /* 토큰 확인 & 소비 */
    public boolean consumeToken(Long memberId, Long subjectId) {
        String tokenKey = RedisKeyUtils.applyTokenKey(memberId);
        String redisSubjectId = redisTemplate.opsForValue().get(tokenKey);
        if (redisSubjectId == null) return false;
        redisTemplate.delete(tokenKey); // 일회성
        return true;
    }
}
