package com.practice.course_registration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SessionController {

    @GetMapping("/session/expired")
    public String sessionExpired() {
        return "session-expired"; // session-expired.html 템플릿 반환
    }
}