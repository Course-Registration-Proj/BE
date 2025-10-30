package com.practice.course_registration.global.redis.utils;

public class RedisKeyUtils {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:apply:"; // rate limit key
    private static final String IDEMPOTENCY_KEY_PREFIX = "idem:"; // 멱등키 prefix
    private static final String COURSE_PREFIX = "course:"; // 강의 key prefix
    private static final String RESERVATION_PREFIX = "reservation:";

    // idempotency key (멱등키)
    public static String idempotencyKey(Long memberId, String subjectCode) {
        return IDEMPOTENCY_KEY_PREFIX + memberId + ":" + subjectCode;
    }

    // rate limit key
    public static String rateLimitKey(Long memberId) {
        return RATE_LIMIT_KEY_PREFIX + memberId;
    }

    // 현재 신청 인원
    public static String courseRestStockKey(String courseId) {
        return COURSE_PREFIX + courseId + ":applied";
    }

    // 해당 과목 신청 확정유저 set
    public static String courseMemberKey(String courseId) {
        return COURSE_PREFIX + courseId + ":users";
    }

    // 임시 예약 상태 키 (신청 확정은 kafka consumer에서 진행)
    public static String reservationKey(String courseId, Long memberId) {
        return RESERVATION_PREFIX + courseId + ":" + memberId;
    }

}
