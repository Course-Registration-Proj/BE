package com.practice.course_registration.apiPayload.code.status;

import com.practice.course_registration.apiPayload.code.BaseErrorCode;
import com.practice.course_registration.apiPayload.code.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {


    /*
    * @ 일반적인 응답
    * */
    // 400번대
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 500번대
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),



    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ResponseDTO getReason() {
        return ResponseDTO.builder()
                .isSuccess(false)
                .message(message)
                .code(code)
                .build()
                ;
    }

    @Override
    public ResponseDTO getReasonHttpStatus() {
        return ResponseDTO.builder()
                .isSuccess(false)
                .message(message)
                .code(code)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
