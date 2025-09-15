package com.practice.course_registration.global.apiPayload.code.status;

import com.practice.course_registration.global.apiPayload.code.BaseErrorCode;
import com.practice.course_registration.global.apiPayload.code.ResponseDTO;
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

    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "해당 멤버를 찾을 수 없습니다."),


    // subject
    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBJECT4001", "해당 과목을 찾을 수 없습니다."),
    ALREADY_APPLY_SUBJECT(HttpStatus.BAD_REQUEST, "SUBJECT4002", "이미 신청한 과목입니다."),
    CONFLICT_COURSE_TIME(HttpStatus.BAD_REQUEST, "SUBJECT4003", "동일한 시간대에 신청한 과목이 있습니다."),
    CAPACITY_FULL(HttpStatus.BAD_REQUEST, "SUBJECT4004", "제한 인원이 가득 찼습니다.")

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
