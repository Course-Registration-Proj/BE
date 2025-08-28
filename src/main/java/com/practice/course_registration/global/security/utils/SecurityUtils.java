package com.practice.course_registration.global.security.utils;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()){
            throw new ErrorHandler(ErrorStatus._UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)){
            throw new ErrorHandler(ErrorStatus._UNAUTHORIZED);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) principal;

        return customUserDetails.getID();
    }
}
