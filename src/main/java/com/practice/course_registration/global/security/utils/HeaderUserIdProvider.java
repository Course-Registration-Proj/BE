package com.practice.course_registration.global.security.utils;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Profile("loadtest") // 부하테스트 프로파일에서만 활성화 (요청 헤더 userId 사용, 로그인 불필요)
public class HeaderUserIdProvider implements UserIdProvider {
    @Override
    public Long getUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String userId = request.getHeader("userId");
        if (userId == null || userId.isEmpty()){
            throw new ErrorHandler(ErrorStatus._UNAUTHORIZED);
        }

        try{
            return Long.parseLong(userId);
        }
        catch (NumberFormatException e){
            throw new ErrorHandler(ErrorStatus._BAD_REQUEST);
        }
    }
}
