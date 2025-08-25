package com.practice.course_registration;

import com.practice.course_registration.domain.member.dto.JoinDTO;
import com.practice.course_registration.domain.member.domain.MemberEntity;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.member.service.JoinService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class JoinServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private JoinService joinService;

    private JoinDTO joinDTO;

    @BeforeEach
    void setUp() {
        joinDTO = new JoinDTO();
        joinDTO.setMemberName("하냥이");
        joinDTO.setMemberId("hanyang");
        joinDTO.setPassword("20250816");
        joinDTO.setGrade(4);
        joinDTO.setMemberEmail("hanyang@hanyang.ac.kr");
        joinDTO.setMemberNumber("2025001002");
    }

    @Test
    @DisplayName("회원 가입 성공")
    void joinProcess_Success() {
        //given
        when(memberRepository.existsByMemberId(joinDTO.getMemberId())).thenReturn(false);
        when(memberRepository.existsByMemberNumber(joinDTO.getMemberNumber())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(joinDTO.getPassword())).thenReturn("encodedPassword");

        //when
        joinService.joinProcess(joinDTO);

        //then
        verify(memberRepository, times(1)).existsByMemberId(joinDTO.getMemberId());
        verify(memberRepository, times(1)).existsByMemberNumber(joinDTO.getMemberNumber());
        verify(bCryptPasswordEncoder, times(1)).encode(joinDTO.getPassword());
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("학번 중복으로 회원가입 실패")
    void joinProcess_DuplicateMemberNumber_Fail() {
        //given
        when(memberRepository.existsByMemberId(joinDTO.getMemberId())).thenReturn(false);
        when(memberRepository.existsByMemberNumber(joinDTO.getMemberNumber())).thenReturn(true);

        // when & then
        Assertions.assertThatThrownBy(() -> joinService.joinProcess(joinDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일한 학번을 가진 회원이 존재합니다."); // 문자열로 비교하는 것을 나중에 에러 핸들링하면서 바꾸는 게 좋을듯

        // 학번 중복이므로 save 호출 X
        verify(memberRepository, never()).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("ID 중복으로 회원가입 실패")
    void joinProcess_DuplicateMemberId_Fail() {
        // given
        when(memberRepository.existsByMemberId(joinDTO.getMemberId())).thenReturn(true);

        // when & then
        Assertions.assertThatThrownBy(() -> joinService.joinProcess(joinDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일한 로그인 ID를 가진 회원이 존재합니다.");

        verify(memberRepository, never()).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("이메일 없는 회원가입 성공")
    void joinProcess_WithoutEmail_Success(){
        //given
        joinDTO.setMemberEmail(null);
        when(memberRepository.existsByMemberId(joinDTO.getMemberId())).thenReturn(false);
        when(memberRepository.existsByMemberNumber(joinDTO.getMemberNumber())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(joinDTO.getPassword())).thenReturn("encodedPassword");


        // when
        joinService.joinProcess(joinDTO);

        //then
        verify(memberRepository, times(1)).existsByMemberId(joinDTO.getMemberId());
        verify(memberRepository, times(1)).existsByMemberNumber(joinDTO.getMemberNumber());
        verify(bCryptPasswordEncoder, times(1)).encode(joinDTO.getPassword());
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }


}
