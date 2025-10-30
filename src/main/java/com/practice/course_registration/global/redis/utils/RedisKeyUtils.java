package com.practice.course_registration.global.redis.utils;

public class RedisKeyUtils {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:apply:"; // rate limit key
    private static final String IDEMPOTENCY_KEY_PREFIX = "idem:"; // 멱등키 prefix
    private static final String COURSE_PREFIX = "course:"; // 강의 key prefix
    private static final String RESERVATION_PREFIX = "reservation:";

    public static String idempotencyKey(Long memberId, String subjectCode) {
        return IDEMPOTENCY_KEY_PREFIX + memberId + ":" + subjectCode;
    }

    public static String rateLimitKey(Long memberId) {
        return RATE_LIMIT_KEY_PREFIX + memberId;
    }

    public static String courseStockKey(String courseId) {
        return COURSE_PREFIX + courseId + ":stock";
    }

    public static String reservationKey(String courseId, Long memberId) {
        return RESERVATION_PREFIX + courseId + ":" + memberId;
    }

}
