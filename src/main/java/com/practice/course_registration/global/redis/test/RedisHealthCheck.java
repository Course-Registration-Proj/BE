package com.practice.course_registration.global.redis.test;// package com.practice.course_registration.global.redis.debug;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test") // 테스트 제외(선택)
@RequiredArgsConstructor
public class RedisHealthCheck implements CommandLineRunner {

    private final StringRedisTemplate redis;
    private final RedisConnectionFactory cf;

    @Override
    public void run(String... args) {
        String key = "debug:hello";
        redis.opsForValue().set(key, "fromApp", Duration.ofMinutes(10));

        String pong = redis.execute((RedisCallback<String>) conn -> conn.ping());
        String val  = redis.opsForValue().get(key);

        String host = "unknown";
        int port = -1;
        int db = -1;
        if (cf instanceof LettuceConnectionFactory lcf) {
            host = lcf.getHostName();
            port = lcf.getPort();
            db   = lcf.getDatabase();
        }

        log.info("🔎 REDIS DEBUG host={}, port={}, db={}, ping={}, key={}, val={}",
                host, port, db, pong, key, val);
    }
}
