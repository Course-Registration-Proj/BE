package com.practice.course_registration.apiPayload.code.status;

import com.practice.course_registration.apiPayload.code.BaseCode;
import com.practice.course_registration.apiPayload.code.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.awt.desktop.UserSessionEvent;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 성공 응답
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),



    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ResponseDTO getReason() {
        return ResponseDTO.builder()
                .isSuccess(true)
                .message(message)
                .code(code)
                .build()
        ;
    }

    @Override
    public ResponseDTO getReasonHttpStatus() {
        return ResponseDTO.builder()
                .isSuccess(true)
                .message(message)
                .code(code)
                .httpStatus(httpStatus)
                .build()
        ;
    }
}
