package com.practice.course_registration.global.kafka;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RegistrationMessage {
    // 기본 정보
    private Long userId;
    private String courseId;
    private Long timestamp;
    private String requestId;
}
