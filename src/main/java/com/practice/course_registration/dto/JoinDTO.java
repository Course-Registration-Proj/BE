package com.practice.course_registration.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO {
    private String memberName;
    private String memberNumber;

    private int grade; // 학년

    private String memberEmail;

    private String memberId; // 로그인용 ID
    private String password; // 로그인용 PW
}
