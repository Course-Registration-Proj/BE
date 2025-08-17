package com.practice.course_registration.repository;

import com.practice.course_registration.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    boolean existsByMemberId(String memberId);
    boolean existsByMemberNumber(String memberNumber);

    MemberEntity findByMemberId(String memberId);
}
