package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.subject.domain.MemberSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberSubjectRepository extends JpaRepository<MemberSubject, Long> {

    @Query("""
      SELECT ms.subjectId
      FROM MemberSubject ms
      WHERE ms.memberId = :memberId AND ms.subjectId IN :subjectIds
    """)
    Set<Long> findAllIdByMemberIdAndSubject(@Param("memberId") Long memberId, @Param("subjectIds") List<Long> subjectIds);

    Optional<MemberSubject> findByMemberIdAndSubjectId(Long memberId, Long subjectId);

    List<MemberSubject> findAllByMemberId(Long memberId);

    @Modifying
    @Query("delete from MemberSubject ms where ms.memberId = :memberId and ms.subjectId = :subjectId")
    void deleteByMemberIdAndSubjectId(@Param("memberId") Long memberId, @Param("subjectId") Long subjectId);

}
