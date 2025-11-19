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


/*
* @TODO : 대기열에 들어가있는 수강신청 요청들을 실제로 DB에 넣어주는 코드!
*   🌟 오류가 발생하는 문제의 코드입니다 🌟
* */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseWorkerService {

    private final SubjectRepository subjectRepository;
    private final MemberRepository memberRepository;
    private final LuaRepository luaRepository;
    private final WaitQueueService waitQueueService;
    private final CourseEnrollmentService courseEnrollmentService;


    // 1초마다 모든 과목 큐 확인하고 처리, Spring 스케줄러기능
    @Scheduled(fixedDelay = 1000)
    public void processWaitingQueue() {
        for (Long subjectId : subjectRepository.findAllIds()) {
            log.info("스케줄링 작업중 , subjectId : {}", subjectId);
            processCourse(subjectId);
        }
    }

    // redis 리스트에서 요청 가져와서 처리
    private void processCourse(Long subjectId) {
        String memberIdStr = waitQueueService.dequeueSubject(subjectId, Duration.ofSeconds(1));

        if (memberIdStr != null) {
            Long memberId = Long.valueOf(memberIdStr);

            try {
                courseEnrollmentService.processEnrollment(subjectId, memberId);
                log.info("수강신청 DB 저장 성공. Course: {}, Member: {}", subjectId, memberId);
            } catch (Exception e) {
                // redis 롤백
                luaRepository.rollback(subjectId, memberId);
                log.error("수강신청 DB 저장 실패. Course: {}, Member: {}", subjectId, memberId, e);
            }
        }
    }


    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Subject findSubjectById(Long subject) {
        return subjectRepository.findById(subject).orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }

}
