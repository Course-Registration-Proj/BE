package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.subject.domain.MemberSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.dto.WaitPositionDTO;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.redis.repository.LuaRepository;
import com.practice.course_registration.global.redis.service.IdempotencyService;
import com.practice.course_registration.global.redis.service.WaitQueueService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final MemberRepository memberRepository;

    private final IdempotencyService idempotencyService;
    private final LuaRepository luaRepository;
    private final WaitQueueService waitQueueService;


    private static final int MAX_SCORE = 10;
    private static final int RATE_LIMIT_CNT = 5;
    private static final int RATE_LIMIT_TTL = 1;
    private static final int IDEM_KEY_TTL = 15;
    private static final int HOLD_TTL = 120; // 수강신청 성공여부 결정나도 안전망용 ttl
    private static final int RESULT_CACHE_TTL = 3; // 결과 캐시 TTL



    /*
    * @TODO : 수강신청 접수 -> 대기열 삽입
    *   - 실제 수강신청이 완료되지는 않고 수강신청을 위해 대기열에 요청을 넣는 코드
    *   - rate limit으로 입구 제어 구현
    *   - 멱등키를 통해 중복클릭 방지 (SSR이라 백엔드에서 직접 처리)
    *   - LUA 스크립트를 통해서 원자적 처리 구현 (LUA를 선점하면 원자적으로 수강 신청 가능, 물론 redis에 적용, DB에는 미적용)
    *   - 대기열에 넣어줌
    * */
    public void enqueueCourseRequest(Long memberId, String code) {
        // 입구 제어 (초 당 너무 많은 신청을 보내는지 제어)
        if (!idempotencyService.rateLimitAllow(memberId, RATE_LIMIT_CNT, Duration.ofSeconds(RATE_LIMIT_TTL))) {
            throw new ErrorHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        // 멱등키 획득 -> 중복클릭 방지
        if (!idempotencyService.acquireIdempotency(memberId, code, Duration.ofSeconds(IDEM_KEY_TTL))) {
            throw new ErrorHandler(ErrorStatus.DUPLICATE_REQUEST);
        }

        // Lua 선점 여부 확인
        boolean held = false;
        try {
            // 멤버 찾기
            //Member member = findMemberById(memberId);

            // 해당 과목 찾기
            //Subject subject = findByCode(code);

            // 유효성 검사
            //validateCheck(member, subject);

            // 원자성 추가
//            waitQueueService.enqueueGlobal(memberId, subject.getId(), System.currentTimeMillis());
            waitQueueService.enqueueGlobal(memberId, 1L, System.currentTimeMillis());
            //log.info("수강신청 접수 성공 (대기열 삽입). Course: {}, Member: {}", subject.getId(), memberId);

        } catch (ErrorHandler e) {
            idempotencyService.releaseIdempotency(memberId, code);
            throw e;
        }
    }

    /*
    * @TODO : 토큰 기반 실제 신청
    * */

    @Transactional
    public void applyCourseWithToken(Long memberId, String code) {
        // 멤버 찾기
        Member member = findMemberById(memberId);

        // 해당 과목 찾기
        Subject subject = findByCode(code);

        // 유효성 검사
        validateCheck(member, subject);

        if (!waitQueueService.consumeToken(memberId, subject.getId())) {
            throw new ErrorHandler(ErrorStatus.UNAUTHORIZED_ISSUE_TOKEN);
        }

        // Redis 원자 선점(FCFS)
        String luaResult = luaRepository.hold(subject.getId(), memberId, subject.getLimitedNum(), HOLD_TTL);
        switch (luaResult) {
            case "OK" -> {
                int updated = subjectRepository.tryIncreaseRegistered(subject.getId());
                if (updated == 0) {
                    luaRepository.rollback(subject.getId(), memberId);
                    throw new ErrorHandler(ErrorStatus.CAPACITY_FULL);
                }

                MemberSubject memberSubject = MemberSubject.builder()
                        .member(member)
                        .subject(subject)
                        .build()
                ;
                memberSubjectRepository.save(memberSubject);
                member.getMemberSubjects().add(memberSubject);
                subject.getMemberSubjects().add(memberSubject);
                member.addScore(subject.getScore());

                luaRepository.deleteHoldKeyOnly(subject.getId(), memberId); // hold 정리
            }
            case "ALREADY_ENROLLED" -> throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
            case "CAPACITY_FULL"    -> throw new ErrorHandler(ErrorStatus.CAPACITY_FULL);
            default -> throw new IllegalStateException("lua 오류 - " + luaResult);
        }

    }


    /**
     * @TODO : 유효성 검사
     * - 이미 신청된 경우
     * - 신청한 과목과 같은 시간의 과목인 경우
     * - 과목코드가 같은 경우
     * - 과목제한인원이 초과된 경우 -> 위 코드에서 lua 결과로 판단
     * - 신청가능학점을 넘긴경우 -> 위 코드에서 lua 결과로 판단
     * */
    private void validateCheck(Member member, Subject subject) {
        boolean alreadyApplied = member.getMemberSubjects().stream()
                .anyMatch(ms -> ms.getSubject().getId().equals(subject.getId()));

        if (alreadyApplied) {
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }

        if (member.getRegisteredScore() + subject.getScore() > MAX_SCORE) {
            throw new ErrorHandler(ErrorStatus.OVER_SOCRE_POSSIBLE);
        }
        boolean conflict = member.getMemberSubjects().stream()
                .map(MemberSubject::getSubject)
                .filter(subj ->
                        subj.getSubjectDay() == subject.getSubjectDay()
                )
                .anyMatch(subj -> subj.conflictCheck(subject))
                ;

        int isSameCode = member.getMemberSubjects().stream()
                .map(MemberSubject::getSubject)
                .filter(subj ->
                        subj.getCode().equals(subject.getCode())
                )
                .toList()
                .size()
                ;

        if (conflict) {
            log.error("시간 충돌");
            throw new ErrorHandler(ErrorStatus.CONFLICT_COURSE_TIME);
        }

        if (isSameCode != 0) {
            log.error("이미 신청한 과목");
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }
    }

    public WaitPositionDTO getWaitPosition(Long memberId, String code) {
        Long subjectId = findByCode(code).getId();
        Long position = waitQueueService.getQueuePosition(memberId, subjectId);
        return WaitPositionDTO.builder()
                .subjectId(subjectId)
                .position(position)
                .build();
    }


    // 수강취소
    @Transactional
    public void cancelCourse(Long memberId, Long subjectId) {

        // 멤버 찾기
        Member member = findMemberById(memberId);

        // 해당 과목 찾기
        Subject subject = findSubjectById(subjectId);

        MemberSubject memberSubject = memberSubjectRepository.findByMemberAndSubject(member, subject)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.NOT_APPLY_SUBJECT));

        // 신청 학점 줄이기
        member.cancelScore(subject.getScore());

        memberSubjectRepository.deleteByMemberIdAndSubjectId(memberId, subjectId);
        subjectRepository.tryDecreaseRegistered(subjectId);

        // redis 반영
        luaRepository.cancelEnrolled(subject.getId(), memberId);
    }


    private Subject findSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }


    private Member findMemberById(Long memberId) {
        return memberRepository.findWithSubjectsById(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Subject findByCode(String code) {
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }
}
