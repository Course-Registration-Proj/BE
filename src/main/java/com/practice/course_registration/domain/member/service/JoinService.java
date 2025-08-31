package com.practice.course_registration.domain.member.service;

import com.practice.course_registration.domain.member.dto.JoinDTO;
import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public JoinService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO) {
        // 자릿수, 특수문자 등은 추후에 추가.
        checkDuplicateMember(joinDTO);

        Member member = createMember(joinDTO);
        memberRepository.save(member);
    }

    private void checkDuplicateMember(JoinDTO joinDTO) {
        // DB에 이미 동일한 로그인용 ID(memberID)를 가진 회원이 존재하는지
        boolean duplicateMemberId = memberRepository.existsByLoginId(joinDTO.getMemberId());
        if (duplicateMemberId) {
            throw new IllegalArgumentException("동일한 로그인 ID를 가진 회원이 존재합니다.");
        }

        // DB에 이미 동일한 memberNumber(학번)을 가진 회원이 존재하는지
        boolean duplicateMemberNumber = memberRepository.existsByMemberNumber(joinDTO.getMemberNumber());
        if (duplicateMemberNumber) {
            throw new IllegalArgumentException("동일한 학번을 가진 회원이 존재합니다.");
        }
    }

    private Member createMember(JoinDTO joinDTO) {
        Member member = new Member(
                joinDTO.getMemberName(),
                joinDTO.getMemberNumber(),
                joinDTO.getGrade(),
                joinDTO.getMemberId(),
                bCryptPasswordEncoder.encode(joinDTO.getPassword())
        );

    //        if (joinDTO.getMemberEmail() != null)
    //            member.changeEmail(joinDTO.getMemberEmail());

        return member;
    }


}
