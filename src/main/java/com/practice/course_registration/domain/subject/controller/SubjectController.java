package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.WaitPositionDTO;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.kafka.KafkaProducer;
import com.practice.course_registration.global.redis.service.WaitQueueService;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
@Validated
public class SubjectController {

    private final SubjectQueryService subjectQueryService;
    private final SubjectService subjectService;
    private final WaitQueueService waitQueueService;


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

        Long memberId = SecurityUtils.getUserId();

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
    public String applyCourse(@RequestParam String code,
                              RedirectAttributes redirectAttributes) {

        Long memberId = SecurityUtils.getUserId();
        try {
            subjectService.enqueueCourseRequest(memberId, code);
            redirectAttributes.addFlashAttribute("message", "수강신청이 정상적으로 접수됐습니다");
            return "redirect:/courses/apply/wait?code=" + code;
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorReason().getMessage());
        }

        return "redirect:/courses/search";
    }

    // 대기 페이지(SSR)
    @GetMapping("/apply/wait")
    public String waitPage(@RequestParam String code, Model model) {
        Long memberId = SecurityUtils.getUserId();
        WaitPositionDTO dto = subjectService.getWaitPosition(memberId, code);

        model.addAttribute("code", code);
        model.addAttribute("position", dto.getPosition()); // null이면 이미 토큰 발급/소비됐을 가능성

        // 이 뷰에서 meta refresh로 /courses/apply/try 호출
        return "courses/apply-wait";
    }


    @GetMapping("/apply/try")
    public String tryApply(@RequestParam String code, RedirectAttributes ra) {
        Long memberId = SecurityUtils.getUserId();
        try {
            // 토큰 존재하면 확정 처리
            String tokenVal = waitQueueService.peekToken(memberId);
            if (tokenVal != null) {
                subjectService.applyCourseWithToken(memberId, code);
                ra.addFlashAttribute("message", "수강신청이 완료되었습니다.");
                return "redirect:/courses/search";
            }
            // 토큰이 아직 없으면 대기 페이지로
            return "redirect:/courses/apply/wait?code=" + code;

        } catch (ErrorHandler e) {
            ra.addFlashAttribute("errorMessage", e.getErrorReason().getMessage());
            return "redirect:/courses/search";
        }
    }
}
