package com.practice.course_registration.domain.subject.domain;

import com.practice.course_registration.global.common.BaseEntity;
import com.practice.course_registration.global.enums.SubjectDay;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Subject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subjectName;

    private String professorName;

    private Integer limitedNum; // 제한인원

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer registeredNum; // 현재신청인원

    private String code;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    private SubjectDay subjectDay; // 수업 요일 (교양이라 하루만 한다는 전제)

    private LocalTime startTime;

    private LocalTime endTime;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MemberSubject> memberSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LikeSubject> likeSubjects = new ArrayList<>();


    public boolean conflictCheck(Subject subject) {
        return this.getStartTime().isBefore(subject.getEndTime())
                && this.getEndTime().isAfter(subject.getStartTime());
    }
}
