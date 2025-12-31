package com.practice.course_registration.domain.subject.controller;


import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.apiPayload.ApiCommonResponse;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/courses")
@Validated
public class SubjectTestController {
    private final SubjectQueryService subjectQueryService;
    private final SubjectService subjectService;

    @PostMapping("/apply")
    public ApiCommonResponse<String> applyCourse(@RequestParam String code, @RequestParam Long memberId) {

        subjectService.apply(memberId, code);
        return ApiCommonResponse.onSuccess("ok");
    }

//    @GetMapping("/search")
//    public String search(CourseFilterRequestDTO filters,
//                         Model model,
//                         @PageableDefault(size = 15, sort = "subjectName", direction = Sort.Direction.ASC) Pageable pageable) {
//
//        Long memberId = 3L;
//
//        Page<SubjectResponseDTO> subjects = subjectQueryService.searchAllSubject(memberId, filters, pageable);
//
//        model.addAttribute("subjects", subjects.getContent());
//        model.addAttribute("filters", filters);
//        model.addAttribute("page", subjects);
//        model.addAttribute("hasSearched", true);
//        model.addAttribute("activeTab", "register");
//
//        return "courses/register-form";
//    }
}
