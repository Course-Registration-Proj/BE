package com.practice.course_registration.global.init;

import com.practice.course_registration.domain.member.domain.Member;
import com.practice.course_registration.domain.member.repository.MemberRepository;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.repository.SubjectRepository;
import com.practice.course_registration.global.enums.SubjectDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 부하테스트용 시드 데이터 생성기.
 * app.seed.enabled=true 일 때만 동작하며, 데이터가 비어있을 때만 생성한다(재실행 안전).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.seed.member-count:100}")
    private int memberCount;

    @Value("${app.seed.member-password:test1234}")
    private String memberPassword;

    @Override
    public void run(String... args) {
        if (subjectRepository.count() == 0) {
            seedSubjects();
        }
        if (memberRepository.count() == 0) {
            seedMembers();
        }
    }

    private void seedSubjects() {
        SubjectDay[] days = SubjectDay.values();
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            subjects.add(Subject.builder()
                    .subjectName("테스트과목" + i)
                    .professorName("교수" + i)
                    .code(String.format("SUBJ%03d", i))
                    .limitedNum(30)          // 정원을 작게 두어 동시성 경합 유도
                    .registeredNum(0)
                    .score(3)
                    .subjectDay(days[i % days.length])
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 30))
                    .build());
        }
        subjectRepository.saveAll(subjects);
        log.info("[seed] 과목 {}개 생성", subjects.size());
    }

    private void seedMembers() {
        // bcrypt는 비용이 커서 동일 비밀번호는 한 번만 인코딩하여 재사용
        String encoded = passwordEncoder.encode(memberPassword);
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= memberCount; i++) {
            members.add(new Member(
                    "user" + i,                    // memberName (unique)
                    String.format("2024%06d", i),  // memberNumber (unique)
                    (i % 4) + 1,                    // grade 1~4
                    "user" + i,                     // loginId (unique)
                    encoded                         // password (bcrypt)
            ));
        }
        memberRepository.saveAll(members);
        log.info("[seed] 테스트 회원 {}명 생성 (loginId: user1~user{}, pw: {})",
                members.size(), memberCount, memberPassword);
    }
}
