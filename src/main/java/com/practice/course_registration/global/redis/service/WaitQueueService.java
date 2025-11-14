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


    // 대기열에 요청 삽입
    public void enqueueSubject(Long memberId, Long subjectId) {
        // 대기열에 요청 적재
        String queueKey = RedisKeyUtils.courseWaitingKey(subjectId);

        // redis list에 적재 (key, value)
        Long resultId = redisTemplate.opsForList().rightPush(queueKey, String.valueOf(memberId));

        log.info("수강신청 접수 성공 - memberId={}, subjectId={}, 대기열크기={}", memberId, subjectId, resultId);
    }

    // 대기열에서 요청 꺼내기
    public String dequeueSubject(Long subjectId, Duration ttl) {
        String queueKey = RedisKeyUtils.courseWaitingKey(subjectId);
        return redisTemplate.opsForList().leftPop(queueKey, ttl);
    }
}
