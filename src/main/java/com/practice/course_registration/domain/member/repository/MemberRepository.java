package com.practice.course_registration.domain.member.repository;

import com.practice.course_registration.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByMemberNumber(String memberNumber);

    Member findByLoginId(String loginId);
}
