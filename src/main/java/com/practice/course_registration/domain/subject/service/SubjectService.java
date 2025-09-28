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
    private static final int MAX_SCORE = 10;

    // 수강신청
    public void applyCourse(Long memberId, String code) {
        // 멤버 찾기
        Member member = findMemberById(memberId);

        // 해당 과목 찾기
        Subject subject = findByCode(code);
        /*
        * 예외처리
        * - 이미 신청된 경우
        * - 신청한 과목과 같은 시간의 과목인 경우
        * - 과목코드가 같은 경우
        * - 과목제한인원이 초과된 경우
        * - 신청가능학점을 넘긴경우
        * */
        if (memberSubjectRepository.findByMemberAndSubject(member, subject).isPresent()) {
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

        int result = subjectRepository.tryIncreaseRegistered(subject.getId());

        if (result == 0) {
            log.error("제한인원 초과, 제한인원 : " + subject.getLimitedNum() + " , 신청인원 : " + subject.getRegisteredNum());
            throw new ErrorHandler(ErrorStatus.CAPACITY_FULL);
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

    }


    // 수강취소
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
    }


    private Subject findSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }


    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Subject findByCode(String code) {
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));
    }

}
