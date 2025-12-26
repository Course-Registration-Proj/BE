package com.practice.course_registration;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import com.practice.course_registration.global.security.utils.SecurityContextUserIdProvider;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @Mock
    private Authentication authentication;

    private SecurityContextUserIdProvider securityContextUserIdProvider =  new SecurityContextUserIdProvider();

    @Mock
    private CustomUserDetails customUserDetails;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        new SecurityUtils(securityContextUserIdProvider);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정상적인 사용자 ID 반환 테스트")
    void getUserId_Success() {
        // Given
        Long expectedUserId = 123L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getID()).thenReturn(expectedUserId);

        // When
        Long actualUserId = SecurityUtils.getUserId();

        // Then
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }

    @Test
    @DisplayName("Authentication이 null인 경우 예외 발생")
    void getUserId_WhenAuthenticationIsNull_ThrowsException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getUserId())
                .isInstanceOf(ErrorHandler.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus._UNAUTHORIZED);
    }

    @Test
    @DisplayName("인증되지 않은 사용자인 경우 예외 발생")
    void getUserId_WhenNotAuthenticated_ThrowsException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getUserId())
                .isInstanceOf(ErrorHandler.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus._UNAUTHORIZED);
    }

    @Test
    @DisplayName("Principal이 null인 경우 예외 발생")
    void getUserId_WhenPrincipalIsNull_ThrowsException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getUserId())
                .isInstanceOf(ErrorHandler.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus._UNAUTHORIZED);
    }

    @Test
    @DisplayName("Principal이 CustomUserDetails가 아닌 경우 예외 발생")
    void getUserId_WhenPrincipalIsNotCustomUserDetails_ThrowsException() {
        // Given
        String invalidPrincipal = "invalidPrincipal";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(invalidPrincipal);

        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getUserId())
                .isInstanceOf(ErrorHandler.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus._UNAUTHORIZED);
    }

    @Test
    @DisplayName("CustomUserDetails의 ID가 null인 경우")
    void getUserId_WhenUserIdIsNull_ReturnsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getID()).thenReturn(null);

        // When
        Long actualUserId = SecurityUtils.getUserId();

        // Then
        assertThat(actualUserId).isNull();
    }
}

