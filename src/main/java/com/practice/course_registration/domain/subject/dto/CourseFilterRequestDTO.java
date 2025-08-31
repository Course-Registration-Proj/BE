package com.practice.course_registration.domain.subject.dto;

import lombok.*;

@Data
@NoArgsConstructor
public class CourseFilterRequestDTO {
    private String code; // 학수번호
    private String subjectName; // 교과명
    private String professorName; // 교수명
}
