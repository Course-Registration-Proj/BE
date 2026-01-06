package com.practice.course_registration.global.redis.service;

import com.practice.course_registration.global.redis.repository.LuaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublishIssueTokenScheduler {

    private final LuaRepository luaRepository;

    private static final int PERMITS_PER_TICKS = 100;   // tick당 최대 발급 수
    private static final int TOKEN_TTL = 5; // token ttl

    @Scheduled(fixedDelay = 100)
    public void issue() {
        try {
            long cnt = luaRepository.publishIssueTokens(PERMITS_PER_TICKS, TOKEN_TTL);
            if (cnt > 0) {
                log.info("토큰 발급 성공: count={}, sample={}", cnt);
            }
        } catch (Exception e) {
            log.error("토큰 발급 실패", e);
        }
    }
}
