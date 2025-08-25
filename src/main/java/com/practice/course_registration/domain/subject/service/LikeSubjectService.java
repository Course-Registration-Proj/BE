package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
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

    public List<LikeSubject> getLikeSubjectsByUserId(Long userId) {
        List<LikeSubject> likeSubjects = likeSubjectRepository.findByMemberId(userId);

        return likeSubjects.stream().filter(LikeSubject::getIsRegistration).collect(Collectors.toList());
    }
}
