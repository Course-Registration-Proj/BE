package com.practice.course_registration.global.security.utils;

import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    private static UserIdProvider userIdProvider;
    public SecurityUtils(UserIdProvider userIdProvider) {
        this.userIdProvider = userIdProvider;
    }

    public static Long getUserId(){
        return userIdProvider.getUserId();
    }

    public static void setUserIdProvider(UserIdProvider userIdProvider){
        SecurityUtils.userIdProvider = userIdProvider;
    }
}
