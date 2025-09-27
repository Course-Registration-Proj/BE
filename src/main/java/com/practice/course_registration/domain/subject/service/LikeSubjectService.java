package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.dto.LikeSubjectDTO;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.enums.SubjectDay;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeSubjectService {

    private final LikeSubjectRepository likeSubjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final MemberRepository memberRepository;

    public LikeSubjectService(LikeSubjectRepository likeSubjectRepository,
                              MemberSubjectRepository memberSubjectRepository,
                              SubjectRepository subjectRepository,
                              MemberRepository memberRepository) {
        this.likeSubjectRepository = likeSubjectRepository;
        this.memberSubjectRepository = memberSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void addLikeSubject(Long memberId, String code) {
        // code로 Subject 찾기
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));

        // Member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 이미 희망과목에 있는지 확인
        if (likeSubjectRepository.findByMemberIdAndSubjectId(memberId, subject.getId()).isPresent()) {
            throw new ErrorHandler(ErrorStatus.ALREADY_APPLY_SUBJECT);
        }

        // LikeSubject 생성 및 저장
        LikeSubject likeSubject = LikeSubject.builder()
                .member(member)
                .subject(subject)
                .isRegistration(false)
                .build();

        likeSubjectRepository.save(likeSubject);
    }

    @Transactional
    public void removeLikeSubject(Long memberId, String code) {
        // code로 Subject 찾기
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));

        // LikeSubject 삭제
        likeSubjectRepository.deleteByMemberIdAndSubjectId(memberId, subject.getId());
    }

    public Page<LikeSubjectDTO> getLikeSubjectsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // likeSubjectPage : 여러 개의 Subject 객체를 페이지 단위로 가지고 있다.
        Page<Subject> likeSubjectPage = likeSubjectRepository
                .findByMemberIdOrderBySubjectAsc(userId, pageable)
                .map(LikeSubject::getSubject);

        return likeSubjectPage.map(subject -> {
            // 해당 과목을 사용자가 수강신청했는지 확인
            boolean isRegistered = memberSubjectRepository
                    .findByMemberIdAndSubjectId(userId, subject.getId())
                    .isPresent();

            return LikeSubjectDTO.builder()
                    .subjectName(subject.getSubjectName())
                    .professorName(subject.getProfessorName())
                    .limitedNum(subject.getLimitedNum())
                    .code(subject.getCode())
                    .score(subject.getScore())
                    .subjectDay(subject.getSubjectDay())
                    .startTime(subject.getStartTime())
                    .endTime(subject.getEndTime())
                    .isRegistered(isRegistered)
                    .build();
        });
    }
}
