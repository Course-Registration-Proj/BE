package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
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
      SELECT ls.subjectId
      FROM LikeSubject ls
      WHERE ls.memberId = :memberId AND ls.subjectId IN :subjectIds
    """)
    Set<Long> findAllByMemberIdAndSubject(@Param("memberId") Long memberId, @Param("subjectIds") List<Long> subjectIds);


    List<LikeSubject> findByMemberId(Long memberId);

    // 연관관계 없이 id로 조인하여 찜한 과목을 과목명순으로 조회
    @Query(value = "SELECT s FROM Subject s, LikeSubject ls WHERE ls.subjectId = s.id AND ls.memberId = :memberId ORDER BY s.subjectName",
            countQuery = "SELECT count(ls) FROM LikeSubject ls WHERE ls.memberId = :memberId")
    Page<Subject> findLikedSubjectsByMemberIdOrderByName(@Param("memberId") Long memberId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM LikeSubject ls WHERE ls.memberId = :memberId AND ls.subjectId = :subjectId")
    void deleteByMemberIdAndSubjectId(@Param("memberId") Long memberId, @Param("subjectId") Long subjectId);

    Optional<LikeSubject> findByMemberIdAndSubjectId(Long memberId, Long subjectId);
}
