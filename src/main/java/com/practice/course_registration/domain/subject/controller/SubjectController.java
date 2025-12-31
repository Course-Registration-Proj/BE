package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.WaitPositionDTO;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
@Validated
@Slf4j
public class SubjectController {

    private final SubjectQueryService subjectQueryService;
    private final SubjectService subjectService;

    /*
    * 기본 수강신청 페이지
    * */
    @GetMapping
    public String page(Model model){
        model.addAttribute("subjects", List.of());
        model.addAttribute("filters", new CourseFilterRequestDTO());
        model.addAttribute("hasSearched", false);
        model.addAttribute("activeTab", "register");
        return "courses/register-form";
    }

    /*
    * 필터링 목록 포함해서 과목 조회 페이지
    * */
    @GetMapping("/search")
    public String search(CourseFilterRequestDTO filters,
                         Model model,
                         @PageableDefault(size = 15, sort = "subjectName", direction = Sort.Direction.ASC) Pageable pageable) {

        Long memberId = 3L;

        Page<SubjectResponseDTO> subjects = subjectQueryService.searchAllSubject(memberId, filters, pageable);

        model.addAttribute("subjects", subjects.getContent());
        model.addAttribute("filters", filters);
        model.addAttribute("page", subjects);
        model.addAttribute("hasSearched", true);
        model.addAttribute("activeTab", "register");

        return "courses/register-form";
    }

    // 수강신청
    @PostMapping("/apply")
    public String applyCourse(@RequestParam String code,@RequestParam Long memberId,
                              RedirectAttributes redirectAttributes) {

        try {
            subjectService.apply(memberId, code);
            redirectAttributes.addFlashAttribute("message", "수강신청이 정상적으로 성공했습니다");
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorReason().getMessage());
        }

        return "redirect:/courses/search";
    }
}
