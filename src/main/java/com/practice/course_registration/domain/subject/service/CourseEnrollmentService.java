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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentService {

    private final MemberRepository memberRepository;
    private final SubjectRepository subjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final LuaRepository luaRepository; // holdKey 삭제용
    private static final int MAX_SCORE = 10;

    // 반드시 public이어야 다른 클래스에서 호출 가능
    @Transactional
    public void processEnrollment(Long subjectId, Long memberId) {
        // 멤버 찾기
        Member member = findMemberById(memberId);

        // 해당 과목 찾기
        Subject subject = findSubjectById(subjectId);

        if (memberSubjectRepository.findByMemberIdAndSubjectId(memberId, subjectId).isPresent()) {
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }

        if (member.getRegisteredScore() + subject.getScore() > MAX_SCORE) {
            throw new ErrorHandler(ErrorStatus.OVER_SOCRE_POSSIBLE);
        }

        // 연관관계 탐색 대신 memberId로 신청 내역 조회 후 과목을 id로 조회
        List<Long> enrolledSubjectIds = memberSubjectRepository.findAllByMemberId(memberId).stream()
                .map(MemberSubject::getSubjectId)
                .toList();
        List<Subject> enrolledSubjects = subjectRepository.findAllById(enrolledSubjectIds);

        int isSameCode = enrolledSubjects.stream()
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

        boolean conflict = enrolledSubjects.stream()
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
                .memberId(memberId)
                .subjectId(subject.getId())
                .build();

        memberSubjectRepository.save(memberSubject);

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