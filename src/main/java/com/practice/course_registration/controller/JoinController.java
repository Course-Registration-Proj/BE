package com.practice.course_registration.controller;

import com.practice.course_registration.dto.JoinDTO;
import com.practice.course_registration.service.JoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class JoinController {
    private final JoinService joinService;
    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @GetMapping("/join")
    public String joinPage(){
        return "join";
    }

    @PostMapping("/join")
    public String joinProcess(JoinDTO joinDTO){
        log.info("[joinProcess] joinDTO: {}", joinDTO);

        joinService.joinProcess(joinDTO);

        return "redirect:/login";
    }

}
