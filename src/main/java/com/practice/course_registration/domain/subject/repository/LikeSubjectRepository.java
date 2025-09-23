package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.subject.domain.LikeSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    Set<Long> findAllByMemberAndSubject(@Param("member") Member member, @Param("subjectIds") List<Long> subjectIds);

         
    List<LikeSubject> findByMemberId(Long memberId);

    @Query("SELECT ls FROM LikeSubject ls JOIN FETCH ls.subject s WHERE ls.member.id = :memberId ORDER BY s.subjectName")
    Page<LikeSubject> findByMemberIdOrderBySubjectAsc(@Param("memberId") Long memberId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM LikeSubject ls WHERE ls.member.id = :memberId AND ls.subject.id = :subjectId")
    void deleteByMemberIdAndSubjectId(@Param("memberId") Long memberId, @Param("subjectId") Long subjectId);

    Optional<LikeSubject> findByMemberIdAndSubjectId(Long memberId, Long subjectId);
}
