package com.practice.course_registration.domain.subject.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WaitPositionDTO {
    Long subjectId;
    Long position;
}
