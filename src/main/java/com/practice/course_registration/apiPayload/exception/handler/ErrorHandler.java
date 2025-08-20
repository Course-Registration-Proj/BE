package com.practice.course_registration.apiPayload.exception.handler;

import com.practice.course_registration.apiPayload.code.BaseErrorCode;
import com.practice.course_registration.apiPayload.exception.GeneralException;

public class ErrorHandler extends GeneralException {
    public ErrorHandler(BaseErrorCode code) {
        super(code);
    }
}
