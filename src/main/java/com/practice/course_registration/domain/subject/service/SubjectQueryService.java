package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.subject.domain.MemberSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.MyRegisteredSubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.MySubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubjectQueryService {

    private final SubjectRepository subjectRepository;
    private final MemberSubjectRepository memberSubjectRepository;
    private final LikeSubjectRepository likeSubjectRepository;

    public Page<SubjectResponseDTO> searchAllSubject(Long memberId, CourseFilterRequestDTO filters, Pageable pageable) {

        log.info("===========search 시작==============");

        String code = nullIfBlank(filters.getCode());
        String professorName = nullIfBlank(filters.getProfessorName());
        String subjectName = nullIfBlank(filters.getSubjectName());


        log.info("===========code: " + code + " =======professorName : " + professorName + " ========subjectName" + subjectName + " \n");
        Page<Subject> subjects
                = subjectRepository.findAllByCodeAndProfessorNameAndSubjectName(code, professorName, subjectName, pageable);


        if (subjects.isEmpty()) {
            return Page.empty(pageable);
        }

        // 조건에 맞는 과목의 id 조회
        List<Long> subjectIds = subjects.getContent().stream()
                .map(Subject::getId)
                .toList();
        log.info("===========페이지 크기 : " + subjectIds.size());

        Set<Long> registeredIds = memberSubjectRepository.findAllIdByMemberIdAndSubject(memberId, subjectIds);
        Set<Long> likedIds = likeSubjectRepository.findAllByMemberIdAndSubject(memberId, subjectIds);

        return subjects.map(subject -> SubjectResponseDTO.builder()
                .subjectName(subject.getSubjectName())
                .professorName(subject.getProfessorName())
                .limitedNum(subject.getLimitedNum())
                .registeredNum(subject.getRegisteredNum())
                .code(subject.getCode())
                .score(subject.getScore())
                .subjectDay(subject.getSubjectDay())
                .startTime(subject.getStartTime())
                .endTime(subject.getEndTime())
                .registered(registeredIds.contains(subject.getId()))
                .liked(likedIds.contains(subject.getId()))
                .build()
        );
    }

    public List<MyRegisteredSubjectResponseDTO> searchMySubject(Long memberId) {

        List<Long> subjectIds = memberSubjectRepository.findAllByMemberId(memberId).stream()
                .map(MemberSubject::getSubjectId)
                .toList();
        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        return subjects.stream()
                .map(subject -> MyRegisteredSubjectResponseDTO.builder()
                        .id(subject.getId())
                        .subjectName(subject.getSubjectName())
                        .professorName(subject.getProfessorName())
                        .limitedNum(subject.getLimitedNum())
                        .registeredNum(subject.getRegisteredNum())
                        .code(subject.getCode())
                        .score(subject.getScore())
                        .subjectDay(subject.getSubjectDay())
                        .startTime(subject.getStartTime())
                        .endTime(subject.getEndTime())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

}
