package com.practice.course_registration.domain.member.domain;

import com.practice.course_registration.global.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String memberName;

    @Column(unique = true, nullable = false)
    private String memberNumber; // 학번

    private int grade; // 학년

//    @Column(unique = true, nullable = true)
//    private String memberEmail; // 인증용 메일(추후 기능 추가)

    @Column(unique = true, nullable = false)
    private String memberId; // 로그인용 ID

    @Column(nullable = false)
    private String password; // 로그인용 PW

    @Enumerated(EnumType.STRING)
    private Role role;

    public MemberEntity(String memberName, String memberNumber, int grade, String memberId, String password) {
        this.memberName = memberName;
        this.memberNumber = memberNumber;
        this.grade = grade;
        this.memberId = memberId;
        this.password = password;
        this.role = Role.STUDENT; // 기본값
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeGrade(int grade) {
        this.grade = grade;
    }

//    public void changeEmail(String memberEmail) {
//        this.memberEmail = memberEmail;
//    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }
}
