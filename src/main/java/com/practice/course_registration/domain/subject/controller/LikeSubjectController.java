package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.LikeSubjectDTO;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.domain.subject.service.LikeSubjectService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.apiPayload.code.status.ErrorStatus;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.security.domain.CustomUserDetails;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Slf4j
@Controller
public class LikeSubjectController {
    private final LikeSubjectService likeSubjectService;
    private final SubjectService subjectService;
    private final SubjectRepository subjectRepository;
    public LikeSubjectController(LikeSubjectService likeSubjectService,
                                 SubjectService subjectService,
                                 SubjectRepository subjectRepository) {
        this.likeSubjectService =  likeSubjectService;
        this.subjectService = subjectService;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping("/courses/wish")
    public String addWishSubject(@RequestParam String code,
                                 RedirectAttributes redirectAttributes) {
        Long memberId = SecurityUtils.getUserId();
        try {

            likeSubjectService.addLikeSubject(memberId, code);
            redirectAttributes.addFlashAttribute("message", "희망과목에 추가되었습니다");
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/search";
    }

    @GetMapping("/courses/wishlist")
    public String like(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "3") int size){

        Long id = SecurityUtils.getUserId();
        Page<LikeSubjectDTO> likeSubjectPage = likeSubjectService.getLikeSubjectsByUserId(id, page, size);

        model.addAttribute("likeSubjectPage", likeSubjectPage);
        model.addAttribute("activeTab", "wishlist");
        model.addAttribute("subjects", List.of());
        model.addAttribute("filters", new CourseFilterRequestDTO());
        model.addAttribute("hasSearched", false);

        return "courses/register-form";
    }

    @PostMapping("/apply")
    public String applyCourse(@RequestParam String code,
                              RedirectAttributes redirectAttributes) {

        Long memberId = SecurityUtils.getUserId();
        try {
            subjectService.applyCourse(memberId, code);
            redirectAttributes.addFlashAttribute("message", "수강신청이 정상적으로 성공했습니다");
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/wishlist";
    }

    @PostMapping("/cancel")
    public String cancelCourse(@RequestParam String code,
                               RedirectAttributes redirectAttributes) {
        Long memberId = SecurityUtils.getUserId();
        try {
            // SubjectService.cancelCourse()는 subjectId를 받으므로
            // code로 Subject를 먼저 찾기
            Subject subject = subjectRepository.findByCode(code)
                    .orElseThrow(() -> new ErrorHandler(ErrorStatus.SUBJECT_NOT_FOUND));

            subjectService.cancelCourse(memberId, subject.getId());
            redirectAttributes.addFlashAttribute("message", "수강취소가 정상적으로 처리되었습니다");
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/wishlist";
    }

    @PostMapping("/remove-wish")
    public String removeWishSubject(@RequestParam String code,
                                    RedirectAttributes redirectAttributes) {
        Long memberId = SecurityUtils.getUserId();
        try {
            likeSubjectService.removeLikeSubject(memberId, code);
            redirectAttributes.addFlashAttribute("message", "희망과목에서 삭제되었습니다");
        } catch (ErrorHandler e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/wishlist";
    }
}