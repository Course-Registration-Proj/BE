package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.dto.LikeSubjectDTO;
import com.practice.course_registration.domain.subject.service.LikeSubjectService;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@Controller
public class LikeSubjectController {
    private final LikeSubjectService likeSubjectService;
    public LikeSubjectController(LikeSubjectService likeSubjectService) {
        this.likeSubjectService =  likeSubjectService;
    }

    @GetMapping("/like")
    public String like(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size){

        // 세션에서 사용자 정보 가져오기
        Long id = SecurityUtils.getUserId();

        // 페이징 적용
        Page<LikeSubjectDTO> likeSubjectPage = likeSubjectService.getLikeSubjectsByUserId(id, page, size);

        model.addAttribute("likeSubjectPage", likeSubjectPage);

        return "like";
    }
}
