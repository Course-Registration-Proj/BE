package com.practice.course_registration.global.security.domain;

import com.practice.course_registration.domain.member.domain.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails {

    private MemberEntity memberEntity;
    public CustomUserDetails(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + memberEntity.getRole().name()));
    }

    @Override
    public String getPassword() {
        return memberEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return memberEntity.getMemberId();
    }

    // 계정의 상태가 바뀔 일은 아직 없으니 모두 true 반환
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails)) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return this.getUsername().equals(that.getUsername());
    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }

    public Long getID(){
        return memberEntity.getId();
    }

}
