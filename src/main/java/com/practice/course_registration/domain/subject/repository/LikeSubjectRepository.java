package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeSubjectRepository extends JpaRepository<LikeSubject, Long> {
    List<LikeSubject> findByMemberId(Long memberId);

    @Query("SELECT l FROM LikeSubject l where l.member.id = :memberId and l.isRegistration = true order by l.subject.id ASC")
    Page<LikeSubject> findByMemberIdOrderBySubjectAsc(@Param("memberId") Long memberId, Pageable pageable);
}
