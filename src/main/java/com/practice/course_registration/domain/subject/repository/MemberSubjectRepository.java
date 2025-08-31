package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.subject.domain.MemberSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberSubjectRepository extends JpaRepository<MemberSubject, Long> {

    @Query("""
      SELECT ms.subject.id
      FROM MemberSubject ms
      WHERE ms.member = :member AND ms.subject.id IN :subjectIds
    """)
    Set<Long> findAllByMemberAndSubject(@Param("member") Member member, @Param("subjectIds") List<Long> subjectIds);
}
