package com.practice.course_registration.global.apiPayload.exception.handler;

import com.practice.course_registration.global.apiPayload.code.BaseErrorCode;
import com.practice.course_registration.global.apiPayload.exception.GeneralException;

public class ErrorHandler extends GeneralException {
    public ErrorHandler(BaseErrorCode code) {
        super(code);
    }
}
