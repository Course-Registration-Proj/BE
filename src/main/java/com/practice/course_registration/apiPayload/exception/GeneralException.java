package com.practice.course_registration.apiPayload.exception;

import com.practice.course_registration.apiPayload.code.BaseErrorCode;
import com.practice.course_registration.apiPayload.code.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ResponseDTO getErrorReason() {
        return this.code.getReason();
    }

    public ResponseDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}