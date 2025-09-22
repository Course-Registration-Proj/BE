package com.practice.course_registration.domain.subject.dto;

import com.practice.course_registration.global.enums.SubjectDay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeSubjectDTO {
    private String subjectName;
    private String professorName;

    private Integer limitedNum;
    private Integer registeredNum;

    private String code;
    private SubjectDay subjectDay;

    private boolean isRegistered;
}
