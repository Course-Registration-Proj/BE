package com.practice.course_registration.domain.member.repository;

import com.practice.course_registration.domain.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByMemberNumber(String memberNumber);

    Member findByLoginId(String loginId);

        @Query("select m from Member m " +
                "left join fetch m.memberSubjects ms " +
                "left join fetch ms.subject " + // 어차피 membersubject에서 eager로 subject를 들고와서 N+1 이 생긴다면, 한번에 fetch join 하는 게?
                "where m.id = :id")
        Optional<Member> findWithSubjectsById(@Param("id") Long id);

        Optional<Member> findById(long id);
}
