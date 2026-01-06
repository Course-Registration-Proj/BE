package com.practice.course_registration.global.security.utils;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Primary
public class HeaderUserIdProvider implements UserIdProvider {
    @Override
    public Long getUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String userId = request.getHeader("userId");
        System.out.println("userId = " + userId);
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
