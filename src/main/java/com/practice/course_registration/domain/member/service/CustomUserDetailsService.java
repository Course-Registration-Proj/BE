package com.practice.course_registration.domain.member.service;

import com.practice.course_registration.global.security.domain.CustomUserDetails;
import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // SpringBoot가 알아서 UserDetailsService로 등록
public class CustomUserDetailsService implements UserDetailsService {
    // UserDetailsService가 인터페이스이므로, Custom class로 구현.
    private final MemberRepository memberRepository;
    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(userId);

        if (member == null)
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);

        return new CustomUserDetails(member);
    }
}
