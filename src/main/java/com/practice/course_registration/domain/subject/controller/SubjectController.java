package com.practice.course_registration.domain.subject.controller;

import com.practice.course_registration.domain.subject.dto.CourseFilterRequestDTO;
import com.practice.course_registration.domain.subject.dto.SubjectResponseDTO;
import com.practice.course_registration.domain.subject.dto.WaitPositionDTO;
import com.practice.course_registration.domain.subject.service.SubjectQueryService;
import com.practice.course_registration.domain.subject.service.SubjectService;
import com.practice.course_registration.global.apiPayload.exception.handler.ErrorHandler;
import com.practice.course_registration.global.kafka.KafkaProducer;
import com.practice.course_registration.global.redis.service.WaitQueueService;
import com.practice.course_registration.global.redis.utils.RedisKeyUtils;
import com.practice.course_registration.global.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
@Validated
@Slf4j
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
    @ResponseBody // 뷰가 아닌 데이터를 반환
    public ResponseEntity<Map<String, Object>> applyCourse(@RequestParam String code) {
        Long memberId = SecurityUtils.getUserId();
        Map<String, Object> response = new HashMap<>();

        try {
            // 대기열 등록
            subjectService.enqueueCourseRequest(memberId, code);

            // 성공 시 JSON 응답
            response.put("status", "WAITING");
            response.put("message", "대기열에 진입했습니다.");
            return ResponseEntity.ok(response);
        } catch (ErrorHandler e) {
            // 에러 시 JSON 응답
            response.put("status", "FAIL");
            response.put("message", e.getErrorReason().getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 대기 페이지
    @GetMapping("/apply/wait")
    public String waitPage(@RequestParam String code, Model model) {
        Long memberId = SecurityUtils.getUserId();
        WaitPositionDTO dto = subjectService.getWaitPosition(memberId, code);

        model.addAttribute("code", code);
        model.addAttribute("position", dto.getPosition());

        return "courses/apply-wait";
    }


    @GetMapping("/apply/try")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> tryApply(@RequestParam String code) {
        Long memberId = SecurityUtils.getUserId();
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 내 차례(토큰)가 왔는지 확인
            String tokenVal = waitQueueService.peekToken(memberId);

            if (tokenVal != null) {
                response.put("status", "ALLOWED");
            } else {
                // [대기 케이스] 토큰 없음 -> 현재 대기 순번 조회
                WaitPositionDTO dto = subjectService.getWaitPosition(memberId, code);

                response.put("status", "WAITING");
                response.put("position", dto.getPosition()); // 순번 전달
            }
            return ResponseEntity.ok(response);

        } catch (ErrorHandler e) {
            // [실패 케이스] 로직 수행 중 에러 (예: 정원 초과)
            response.put("status", "FAIL");
            response.put("message", e.getErrorReason().getMessage());
            return ResponseEntity.ok(response); // 200 OK로 보내되, 내용은 FAIL 처리
        }
    }

    @PostMapping("/apply/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmApply(@RequestParam String code) {
        Map<String, Object> response = new HashMap<>();
        Long memberId = SecurityUtils.getUserId();
        subjectService.applyCourseWithToken(memberId, code);

        response.put("status", "SUCCESS");
        response.put("message", "수강신청이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }
}
