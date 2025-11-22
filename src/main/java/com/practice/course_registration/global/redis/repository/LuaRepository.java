package com.practice.course_registration.global.redis.repository;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

/*
* @TODO
*   loa 스크립트를 사용하기 쉽게 레포지토리화 해놓은 클래스
*
*   - holdScript: 수강신청을 원자적으로 처리 (카프카 생각하고 PENDING으로 한건데 아닌가 어차피 대기열 쓰니까 그대로 둬도 되나?)
*   - rollbackScript: 수강신청 처리 도중 실패로 롤백해야 할 경우 처리
*   - cancelScript: 수강취소 처리
*
* */
@Repository
public class LuaRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<String> holdScript;
    private final DefaultRedisScript<String> rollbackScript;
    private final DefaultRedisScript<String> cancelScript;
    private final DefaultRedisScript<Long> issueTokensScript;

    public LuaRepository(StringRedisTemplate template) {
        this.redisTemplate = template;
        this.holdScript = script("lua/enroll_by_applied.lua", String.class);
        this.rollbackScript = script("lua/rollback_by_applied.lua", String.class);
        this.cancelScript = script("lua/cancel_enrolled_course.lua", String.class);
        this.issueTokensScript = script("lua/publish_issue_token.lua", Long.class);
    }

    public String hold(Long subjectId, Long memberId, int limit, int holdTTL) {
        // 각 작업을 위해 필요한 key를 생성
        String appliedKey = RedisKeyUtils.courseRestStockKey(subjectId);
        String usersKey = RedisKeyUtils.courseMemberKey(subjectId);
        String holdKey = RedisKeyUtils.reservationKey(subjectId, memberId);

        // lua 스크립트에 필요한 파라미터를 넘기는 작업 (lua 스크립트에 주석으로 써뒀는데 그거에 맞게 인자로 여기서 넣어줍니다)
        return redisTemplate.execute(holdScript, List.of(appliedKey, usersKey, holdKey), String.valueOf(memberId), String.valueOf(limit), String.valueOf(holdTTL));
    }

    public Long publishIssueTokens(int permits, int tokenTtlSeconds) {
        String queueKey = RedisKeyUtils.globalApplyQueueKey();
        Long cnt = redisTemplate.execute(
                issueTokensScript,
                List.of(queueKey),
                String.valueOf(permits),
                "apply:token:",
                String.valueOf(tokenTtlSeconds)
        );

        return cnt == null ? 0L : cnt;
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
            ClassPathResource resource = new ClassPathResource(path);
            byte[] binaryData =  FileCopyUtils.copyToByteArray(resource.getInputStream());
            String txt = new String(binaryData, StandardCharsets.UTF_8);
            var s = new DefaultRedisScript<T>();
            s.setScriptText(txt);
            s.setResultType(type);
            return s;

        } catch (IOException e) {
            throw new ErrorHandler(ErrorStatus.LUA_LOAD_FAILED);
        }
    }

}
