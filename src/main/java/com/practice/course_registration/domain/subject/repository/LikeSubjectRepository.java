package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeSubjectRepository extends JpaRepository<LikeSubject, Long> {
    List<LikeSubject> findByMemberId(Long memberId);
}
