package com.practice.course_registration.domain.subject.dto;

import com.practice.course_registration.global.enums.SubjectDay;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Builder
@Getter
public class MySubjectResponseDTO {

    private String subjectName;

    private String professorName;

    private Integer limitedNum; // 제한인원

    private String code;

    private SubjectDay subjectDay; // 수업 요일 (교양이라 하루만 한다는 전제)

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean registered;

    private Boolean liked;

}