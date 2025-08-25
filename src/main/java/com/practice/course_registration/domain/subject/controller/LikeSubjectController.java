package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.service.LikeSubjectService;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class LikeSubjectController {
    private final LikeSubjectService likeSubjectService;
    public LikeSubjectController(LikeSubjectService likeSubjectService) {
        this.likeSubjectService =  likeSubjectService;
    }

    @GetMapping("/like")
    public String like(Model model){
        // 세션에서 사용자 정보 가져오기
        Long id = getUserId();
        List<LikeSubject> likeSubjects = likeSubjectService.getLikeSubjectsByUserId(id);
        model.addAttribute("likeSubjects", likeSubjects);

        return "like";
    }

    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        return customUserDetails.getID();
    }
}
