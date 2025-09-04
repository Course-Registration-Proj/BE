package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.subject.domain.MemberSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final MemberRepository memberRepository;


    public void applyCourse(Long memberId, String code) {
        // 멤버 찾기
        Member member = findById(memberId);

        // 해당 과목 찾기
        Subject subject = findByCode(code);

        /*
        * 예외처리
        * - 이미 신청된 경우
        * - 신청한 과목과 같은 시간의 과목인 경우
        * - 과목코드가 같은 경우
        * */
        if (memberSubjectRepository.findByMemberAndSubject(member, subject).isPresent()) {
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
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


        // 저장
        MemberSubject memberSubject = MemberSubject.builder()
                .member(member)
                .subject(subject)
                .build();

        memberSubjectRepository.save(memberSubject);
        member.getMemberSubjects().add(memberSubject);
        subject.getMemberSubjects().add(memberSubject);

    }

    private Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Subject findByCode(String code) {
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }

}
