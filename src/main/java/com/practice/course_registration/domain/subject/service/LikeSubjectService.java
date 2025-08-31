package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.dto.LikeSubjectDTO;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
import com.practice.course_registration.global.enums.SubjectDay;
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
    public LikeSubjectService(LikeSubjectRepository likeSubjectRepository) {
        this.likeSubjectRepository = likeSubjectRepository;
    }

    public Page<LikeSubjectDTO> getLikeSubjectsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // likeSubjectPage : 여러 개의 Subject 객체를 페이지 단위로 가지고 있다.
        Page<Subject> likeSubjectPage = likeSubjectRepository
                .findByMemberIdOrderBySubjectAsc(userId, pageable)
                .map(LikeSubject::getSubject);

        // Subject 객체를 DTO 단위로 변환한다. (페이지 안에 있는 객체 타입 변환이므로 페이징 정보 유지)
        return likeSubjectPage.map(subject -> LikeSubjectDTO.builder()
                .subjectName(subject.getSubjectName())
                .professorName(subject.getProfessorName())
                .limitedNum(subject.getLimitedNum())
//                    .registeredNum()
                .code(subject.getCode())
                .subjectDay(subject.getSubjectDay())
                .build());
    }
}
