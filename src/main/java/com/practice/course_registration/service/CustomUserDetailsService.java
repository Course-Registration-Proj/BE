package com.practice.course_registration.service;

import com.practice.course_registration.entity.CustomUserDetails;
import com.practice.course_registration.entity.MemberEntity;
import com.practice.course_registration.repository.MemberRepository;
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
        MemberEntity member = memberRepository.findByMemberId(userId);

        if (member == null)
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);

        return new CustomUserDetails(member);
    }
}
