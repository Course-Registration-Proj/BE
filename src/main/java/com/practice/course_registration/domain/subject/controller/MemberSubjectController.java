package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.dto.MyRegisteredSubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.MySubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
@Validated
public class MemberSubjectController {

    private final SubjectQueryService subjectQueryService;
    private final SubjectService subjectService;

    @GetMapping("/courses")
    public String myCourses(Model model) {

        Long memberId = SecurityUtils.getUserId();

        List<MyRegisteredSubjectResponseDTO> subjects = subjectQueryService.searchMySubject(memberId);

        model.addAttribute("applies", subjects);
        model.addAttribute("activeTab", "applied");

        return "courses/applied";

    }

    @PostMapping("/cancel")
    public String cancelCourses(@RequestParam Long subjectId) {

        Long memberId = SecurityUtils.getUserId();

        subjectService.cancelCourse(memberId, subjectId);

        return "redirect:/my/courses";
    }
}
