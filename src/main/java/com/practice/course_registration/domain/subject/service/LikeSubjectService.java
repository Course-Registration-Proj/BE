package com.practice.course_registration.domain.subject.service;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
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

    public Page<LikeSubject> getLikeSubjectsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LikeSubject> likeSubjectPage = likeSubjectRepository.findByMemberIdOrderBySubjectAsc(userId, pageable);
        return likeSubjectPage;
    }
}
