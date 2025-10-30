package com.practice.course_registration.global.redis.repository;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class LuaRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<String> holdScript;
    private final DefaultRedisScript<String> rollbackScript;
    private final DefaultRedisScript<String> cancelScript;

    public LuaRepository(StringRedisTemplate template) {
        this.redisTemplate = template;
        this.holdScript = script("lua/enroll_by_applied.lua", String.class);
        this.rollbackScript = script("lua/rollback_by_applied.lua", String.class);
        this.cancelScript = script("lua/cancel_enrolled_course.lua", String.class);
    }

    public String hold(Long subjectId, Long memberId, int limit, int holdTTL) {
        String appliedKey = RedisKeyUtils.courseRestStockKey(subjectId);
        String usersKey = RedisKeyUtils.courseMemberKey(subjectId);
        String holdKey = RedisKeyUtils.reservationKey(subjectId, memberId);

        return redisTemplate.execute(holdScript, List.of(appliedKey, usersKey, holdKey), String.valueOf(memberId), String.valueOf(limit), String.valueOf(holdTTL));
    }

    public String rollback(Long subjectId, Long memberId) {
        String appliedKey = RedisKeyUtils.courseRestStockKey(subjectId);
        String usersKey = RedisKeyUtils.courseMemberKey(subjectId);
        String holdKey = RedisKeyUtils.reservationKey(subjectId, memberId);

        return redisTemplate.execute(rollbackScript, List.of(appliedKey, usersKey, holdKey), String.valueOf(memberId));
    }

    public String cancelEnrolled(Long subjectId, Long memberId) {
        String appliedKey = RedisKeyUtils.courseRestStockKey(subjectId);
        String usersKey = RedisKeyUtils.courseMemberKey(subjectId);
        String holdKey = RedisKeyUtils.reservationKey(subjectId, memberId);

        return redisTemplate.execute(cancelScript, List.of(appliedKey, usersKey, holdKey), String.valueOf(memberId));
    }

    // 성공 시 holdkey만 삭제
    public void deleteHoldKeyOnly(Long subjectId, Long memberId) {
        String holdKey = RedisKeyUtils.reservationKey(subjectId, memberId);
        redisTemplate.delete(holdKey);
    }

    private <T> DefaultRedisScript<T> script(String path, Class<T> type) {
        try {
            var txt = Files.readString(new ClassPathResource(path).getFile().toPath(), StandardCharsets.UTF_8);
            var s = new DefaultRedisScript<T>();
            s.setScriptText(txt);
            s.setResultType(type);
            return s;

        } catch (IOException e) {
            throw new ErrorHandler(ErrorStatus.LUA_LOAD_FAILED);
        }
    }

}
