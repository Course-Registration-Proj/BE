package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.subject.domain.MemberSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.redis.repository.LuaRepository;
import com.practice.course_registration.global.redis.service.WaitQueueService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseWorkerService {

    private final SubjectRepository subjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final MemberRepository memberRepository;
    private final LuaRepository luaRepository;
    private final WaitQueueService waitQueueService;

    private static final int MAX_SCORE = 10;


    // 1초마다 모든 과목 큐 확인하고 처리, Spring 스케줄러기능
    @Scheduled(fixedDelay = 1000)
    public void processWaitingQueue() {
        for (Long subjectId : subjectRepository.findAllIds()) {
            processCourse(subjectId);
        }
    }

    // redis 리스트에서 요청 가져와서 처리
    private void processCourse(Long subjectId) {
        String memberIdStr = waitQueueService.dequeueSubject(subjectId, Duration.ofSeconds(1));

        if (memberIdStr != null) {
            Long memberId = Long.valueOf(memberIdStr);

            try {
                processEnrollment(subjectId, memberId);
                log.info("수강신청 DB 저장 성공. Course: {}, Member: {}", subjectId, memberId);
            } catch (Exception e) {
                // redis 롤백
                luaRepository.rollback(subjectId, memberId);
                log.error("수강신청 DB 저장 실패. Course: {}, Member: {}", subjectId, memberId, e);
            }
        }
    }

    // 실제 DB저장
    @Transactional
    public void processEnrollment(Long subjectId, Long memberId) {
// 멤버 찾기
        Member member = findMemberById(memberId);

        // 해당 과목 찾기
        Subject subject = findSubjectById(subjectId);

        if (memberSubjectRepository.findByMemberAndSubject(member, subject).isPresent()) {
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }

        if (member.getRegisteredScore() + subject.getScore() > MAX_SCORE) {
            throw new ErrorHandler(ErrorStatus.OVER_SOCRE_POSSIBLE);
        }

        int isSameCode = member.getMemberSubjects().stream()
                .map(MemberSubject::getSubject)
                .filter(subj ->
                        subj.getCode().equals(subject.getCode())
                )
                .toList()
                .size()
        ;

        if (isSameCode != 0) {
            log.error("이미 신청한 과목");
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }

        boolean conflict = member.getMemberSubjects().stream()
                .map(MemberSubject::getSubject)
                .filter(subj ->
                        subj.getSubjectDay() == subject.getSubjectDay()
                )
                .anyMatch(subj -> subj.conflictCheck(subject))
                ;

        if (conflict) {
            log.error("시간 충돌");
            throw new ErrorHandler(ErrorStatus.CONFLICT_COURSE_TIME);
        }

        int result = subjectRepository.workerIncreaseRegistered(subject.getId());

        if (result != 1) {
            log.error("DB 정합성 문제 발생: result=" + result);
            throw new ErrorHandler(ErrorStatus.ENROLL_FAILED);
        }

        // 신청학점 추가
        member.addScore(subject.getScore());

        // 저장
        MemberSubject memberSubject = MemberSubject.builder()
                .member(member)
                .subject(subject)
                .build();

        memberSubjectRepository.save(memberSubject);
        member.getMemberSubjects().add(memberSubject);
        subject.getMemberSubjects().add(memberSubject);

        // redis hold key 삭제
        luaRepository.deleteHoldKeyOnly(subject.getId(), memberId);

    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Subject findSubjectById(Long subject) {
        return subjectRepository.findById(subject).orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }

}
