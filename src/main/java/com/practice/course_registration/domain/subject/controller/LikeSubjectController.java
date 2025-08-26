package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.service.LikeSubjectService;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
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
                       @RequestParam(defaultValue = "5") int size){
        // 세션에서 사용자 정보 가져오기
        Long id = getUserId();

        // 페이징 적용
        Page<LikeSubject> likeSubjectPage = likeSubjectService.getLikeSubjectsByUserId(id, page, size);

        model.addAttribute("likeSubjectPage", likeSubjectPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", likeSubjectPage.getTotalPages());
        model.addAttribute("totalItems", likeSubjectPage.getTotalElements());
        model.addAttribute("hasNextPage", likeSubjectPage.hasNext());
        model.addAttribute("hasPreviousPage", likeSubjectPage.hasPrevious());

        return "like";
    }

    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        return customUserDetails.getID();
    }
}
