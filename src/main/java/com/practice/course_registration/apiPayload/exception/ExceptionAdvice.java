package com.practice.course_registration.apiPayload.exception;

import com.practice.course_registration.apiPayload.code.ResponseDTO;
import com.practice.course_registration.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.NoHandlerFoundException;


@Slf4j
@ControllerAdvice(annotations = {Controller.class})
public class ExceptionAdvice {

    /** 1) 도메인/비즈니스 예외: throw new ErrorHandler(ErrorStatus._FORBIDDEN) */
    @ExceptionHandler(GeneralException.class)
    public ModelAndView handleGeneral(GeneralException e, HttpServletRequest req) {
        ResponseDTO dto = e.getErrorReasonHttpStatus(); // httpStatus 포함된 DTO
        HttpStatus status = dto.getHttpStatus() != null ? dto.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;

        ModelMap model = new ModelMap();
        model.addAttribute("httpStatus", status.value());     // 숫자 상태코드(뷰에서 쓰기 편함)
        model.addAttribute("code", dto.getCode());
        model.addAttribute("message", dto.getMessage());
        model.addAttribute("isSuccess", dto.getIsSuccess());
        model.addAttribute("path", req.getRequestURI());

        ModelAndView mv = new ModelAndView("error/common");
        mv.setStatus(status); // 실제 HTTP 상태코드 설정
        mv.addAllObjects(model);
        return mv;
    }

    /** 2) 폼 바인딩 오류 (폼 화면으로 돌려보내지 않고 공통 에러 페이지로 보낼 때) */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ModelAndView handleValidation(Exception e, HttpServletRequest req) {
        var ec = ErrorStatus._BAD_REQUEST;

        String msg;
        if (e instanceof MethodArgumentNotValidException manv) {
            msg = manv.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b).orElse("Validation failed");
        } else if (e instanceof BindException be) {
            msg = be.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b).orElse("Binding failed");
        } else {
            msg = "Validation error";
        }

        ModelAndView mv = new ModelAndView("error/common");
        mv.setStatus(ec.getHttpStatus());
        mv.addObject("httpStatus", ec.getHttpStatus().value());
        mv.addObject("code", ec.getCode());
        mv.addObject("message", msg);
        mv.addObject("isSuccess", false);
        mv.addObject("path", req.getRequestURI());
        return mv;
    }

    /** 3) 파라미터 타입 불일치 (?page=abc 등) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        var ec = ErrorStatus._BAD_REQUEST;

        ModelAndView mv = new ModelAndView("error/common");
        mv.setStatus(ec.getHttpStatus());
        mv.addObject("httpStatus", ec.getHttpStatus().value());
        mv.addObject("code", ec.getCode());
        mv.addObject("message", "Invalid parameter: " + e.getName());
        mv.addObject("isSuccess", false);
        mv.addObject("path", req.getRequestURI());
        return mv;
    }

    /** 4) 404 (핸들러 없음) */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handle404(NoHandlerFoundException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ModelAndView mv = new ModelAndView("error/common");
        mv.setStatus(status);
        mv.addObject("httpStatus", status.value());
        mv.addObject("code", "COMMON404");
        mv.addObject("message", "요청한 페이지를 찾을 수 없습니다.");
        mv.addObject("isSuccess", false);
        mv.addObject("path", req.getRequestURI());
        return mv;
    }

    /** 5) 예상치 못한 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception e, HttpServletRequest req) {
        log.error("Unexpected error", e);
        var ec = ErrorStatus._INTERNAL_SERVER_ERROR;

        ModelAndView mv = new ModelAndView("error/common");
        mv.setStatus(ec.getHttpStatus());
        mv.addObject("httpStatus", ec.getHttpStatus().value());
        mv.addObject("code", ec.getCode());
        mv.addObject("message", ec.getMessage());
        mv.addObject("isSuccess", false);
        mv.addObject("path", req.getRequestURI());
        return mv;
    }
}

