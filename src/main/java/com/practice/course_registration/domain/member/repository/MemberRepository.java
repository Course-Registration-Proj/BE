package com.practice.course_registration.domain.member.repository;

import com.practice.course_registration.domain.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    boolean existsByMemberId(String memberId);
    boolean existsByMemberNumber(String memberNumber);

    MemberEntity findByMemberId(String memberId);
}
