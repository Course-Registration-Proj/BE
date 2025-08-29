package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.member.domain.MemberEntity;
import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeSubjectRepository extends JpaRepository<LikeSubject, Long> {





    @Query("""
      SELECT ls.subject.id
      FROM LikeSubject ls
      WHERE ls.member = :member AND ls.subject.id IN :subjectIds
    """)
    Set<Long> findAllByMemberAndSubject(@Param("member") MemberEntity member, @Param("subjectIds") List<Long> subjectIds);
}
