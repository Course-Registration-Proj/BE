package com.practice.course_registration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.practice.course_registration.global.redis.repository.LuaRepository;
import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class LuaAtomicityLimitTest {

    @Autowired
    LuaRepository luaRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void flushAll() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    private String kApplied(Long subjectId) { return RedisKeyUtils.courseRestStockKey(subjectId); }
    private String kUsers(Long subjectId)   { return RedisKeyUtils.courseMemberKey(subjectId); }

    @Test
    void concurrent_hold_upToLimit_onlyLimitSucceed() throws Exception {
        Long subjectId = 1L;
        int limit = 10;
        int total = 100;
        int ttl = 60;

        // 100명의 서로 다른 유저가 동시에 클릭
        List<Long> userIds = IntStream.rangeClosed(1, total).mapToObj(i -> (long)i).toList();

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch ready = new CountDownLatch(total);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (Long uid : userIds) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await(); // 모두 같은 시점에 시작하도록
                return luaRepository.hold(subjectId, uid, limit, ttl); // "OK" | "COURSE_CAPACITY_FULL" | "USER_ALREADY_ENROLLED"
            }));
        }

        ready.await();
        start.countDown();

        Map<String, Long> stats = new HashMap<>();
        for (Future<String> f : futures) {
            String r = f.get(5, TimeUnit.SECONDS);
            stats.merge(r, 1L, Long::sum);
        }
        pool.shutdown();

        long ok = stats.getOrDefault("OK", 0L);
        long full = stats.getOrDefault("COURSE_CAPACITY_FULL", 0L);

        // then: 정확히 limit만 성공
        assertThat(ok).isEqualTo(limit);
        assertThat(ok + full).isEqualTo(total);

        // Redis 상태 검증
        String applied = redisTemplate.opsForValue().get(kApplied(subjectId));
        Long setSize = redisTemplate.opsForSet().size(kUsers(subjectId));

        assertThat(applied).isEqualTo(String.valueOf(limit));
        assertThat(setSize).isEqualTo((long) limit);
    }
}
