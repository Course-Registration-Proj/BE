package com.practice.course_registration.global.redis.utils;

/*
* @TODO : redis key를 좀 더 깔끔하고! 실수 없게 작성하기 위한 util 클래스
*   추후 필요한 key는 여기서 같은 방식으로 만들어주면 된답니다
* */
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
    public static String courseRestStockKey(Long courseId) {
        return COURSE_PREFIX + courseId + ":applied";
    }

    // 해당 과목 신청 확정유저 set
    public static String courseMemberKey(Long courseId) {
        return COURSE_PREFIX + courseId + ":users";
    }

    // 임시 예약 상태 키
    public static String reservationKey(Long courseId, Long memberId) {
        return RESERVATION_PREFIX + courseId + ":" + memberId;
    }

    // 과목별 대기열
    public static String courseWaitingKey(Long courseId) {
        return COURSE_PREFIX + courseId + ":wait";
    }


    // 신청 결과 키 (캐시용)
    public static String couseResultKey(Long memberId, Long courseId) {
        return COURSE_PREFIX + courseId + ":result:" + memberId;
    }

    public static String globalApplyQueueKey() { // 전역 대기열 (ZSET)
        return "apply:queue";
    }
    public static String applyTokenKey(Long memberId) { // 토큰 키 (String)
        return "apply:token:" + memberId;
    }

}
