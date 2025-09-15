package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.dto.MyRegisteredSubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.MySubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.repository.MemberSubjectRepository;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
@Validated
public class MemberSubjectController {

    private final SubjectQueryService subjectQueryService;

    @GetMapping("/courses")
    public String myCourses(Model model) {

        Long memberId = 1L;

        List<MyRegisteredSubjectResponseDTO> subjects = subjectQueryService.searchMySubject(memberId);

        model.addAttribute("applies", subjects);
        model.addAttribute("activeTab", "applied");

        return "courses/applied";

    }

}
