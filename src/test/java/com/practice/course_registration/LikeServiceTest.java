package com.practice.course_registration;

import com.practice.course_registration.domain.member.domain.MemberEntity;
import com.practice.course_registration.domain.subject.domain.LikeSubject;
import com.practice.course_registration.domain.subject.domain.Subject;
import com.practice.course_registration.domain.subject.repository.LikeSubjectRepository;
import com.practice.course_registration.domain.subject.service.LikeSubjectService;
import com.practice.course_registration.global.enums.SubjectDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private LikeSubjectRepository likeSubjectRepository;

    @InjectMocks
    private LikeSubjectService likeSubjectService;

    private List<LikeSubject> mockLikeSubjects;

    @BeforeEach
    void setUp(){
        // 실제 MemberEntity 생성 (생성자 사용)
        MemberEntity mockMember = new MemberEntity("하냥이", "2022000001", 4, "hanyang", "erica");
        // MemberEntity ID 설정 (JPA @GeneratedValue 때문에 ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(mockMember, "id", 1L);

        mockLikeSubjects = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            // Subject 생성 시 ID는 Builder에서 설정하지 말고 ReflectionTestUtils 사용
            Subject mockSubject = Subject.builder()
                    .subjectName("하냥" + i)
                    .professorName(i + "교수")
                    .limitedNum(2)
                    .code("A" + i)
                    .subjectDay(SubjectDay.FRIDAY)
                    .startTime(LocalTime.of(9, 0)) // LocalTime.MIN보다는 실제 시간 사용 권장
                    .endTime(LocalTime.of(12, 0))   // LocalTime.MAX보다는 실제 시간 사용 권장
                    .memberSubjects(new ArrayList<>()) // 연관관계 초기화
                    .likeSubjects(new ArrayList<>())   // 연관관계 초기화
                    .build();
            // Subject ID 설정
            ReflectionTestUtils.setField(mockSubject, "id", (long) i);

            // LikeSubject 생성 시 ID는 Builder에서 설정하지 말고 ReflectionTestUtils 사용
            LikeSubject likeSubject = LikeSubject.builder()
                    .subject(mockSubject)
                    .member(mockMember)
                    .isRegistration(true)
                    .build();
            // LikeSubject ID 설정
            ReflectionTestUtils.setField(likeSubject, "id", (long) i);

            mockLikeSubjects.add(likeSubject);
        }
    }

    @Test
    @DisplayName("첫 번째 페이지 - 3개 항목 반환 테스트")
    void testGetLikeSubjectsByUserId_FirstPage() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 3;
        Pageable pageable = PageRequest.of(page, size);

        // 첫 번째 페이지: 0~2번 인덱스 (3개)
        List<LikeSubject> firstPageContent = mockLikeSubjects.subList(0, 3);
        Page<LikeSubject> mockPage = new PageImpl<>(firstPageContent, pageable, 9);

        when(likeSubjectRepository.findByMemberIdOrderBySubjectAsc(eq(userId), any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LikeSubject> result = likeSubjectService.getLikeSubjectsByUserId(userId, page, size);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getNumber()).isEqualTo(0); // 현재 페이지
        assertThat(result.getSize()).isEqualTo(3); // 페이지 크기
        assertThat(result.getTotalElements()).isEqualTo(9); // 전체 요소 수
        assertThat(result.getTotalPages()).isEqualTo(3); // 전체 페이지 수
        assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재
        assertThat(result.hasPrevious()).isFalse(); // 이전 페이지 없음

        // 실제 데이터 검증
        assertThat(result.getContent().get(0).getSubject().getSubjectName()).isEqualTo("하냥1");
        assertThat(result.getContent().get(1).getSubject().getSubjectName()).isEqualTo("하냥2");
        assertThat(result.getContent().get(2).getSubject().getSubjectName()).isEqualTo("하냥3");
    }

    @Test
    @DisplayName("두 번째 페이지 - 3개 항목 반환 테스트")
    void testGetLikeSubjectsByUserId_SecondPage() {
        // Given
        Long userId = 1L;
        int page = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(page, size);

        // 두 번째 페이지: 3~5번 인덱스 (3개)
        List<LikeSubject> secondPageContent = mockLikeSubjects.subList(3, 6);
        Page<LikeSubject> mockPage = new PageImpl<>(secondPageContent, pageable, 9);

        when(likeSubjectRepository.findByMemberIdOrderBySubjectAsc(eq(userId), any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LikeSubject> result = likeSubjectService.getLikeSubjectsByUserId(userId, page, size);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getNumber()).isEqualTo(1); // 현재 페이지
        assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재
        assertThat(result.hasPrevious()).isTrue(); // 이전 페이지 존재

        // 실제 데이터 검증
        assertThat(result.getContent().get(0).getSubject().getSubjectName()).isEqualTo("하냥4");
        assertThat(result.getContent().get(1).getSubject().getSubjectName()).isEqualTo("하냥5");
        assertThat(result.getContent().get(2).getSubject().getSubjectName()).isEqualTo("하냥6");
    }

    @Test
    @DisplayName("마지막 페이지 - 3개 항목 반환 테스트")
    void testGetLikeSubjectsByUserId_LastPage() {
        // Given
        Long userId = 1L;
        int page = 2;
        int size = 3;
        Pageable pageable = PageRequest.of(page, size);

        // 마지막 페이지: 6~8번 인덱스 (3개)
        List<LikeSubject> lastPageContent = mockLikeSubjects.subList(6, 9);
        Page<LikeSubject> mockPage = new PageImpl<>(lastPageContent, pageable, 9);

        when(likeSubjectRepository.findByMemberIdOrderBySubjectAsc(eq(userId), any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LikeSubject> result = likeSubjectService.getLikeSubjectsByUserId(userId, page, size);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getNumber()).isEqualTo(2); // 현재 페이지
        assertThat(result.hasNext()).isFalse(); // 다음 페이지 없음
        assertThat(result.hasPrevious()).isTrue(); // 이전 페이지 존재

        // 실제 데이터 검증
        assertThat(result.getContent().get(0).getSubject().getSubjectName()).isEqualTo("하냥7");
        assertThat(result.getContent().get(1).getSubject().getSubjectName()).isEqualTo("하냥8");
        assertThat(result.getContent().get(2).getSubject().getSubjectName()).isEqualTo("하냥9");
    }

    @Test
    @DisplayName("전체 페이지 순회 테스트 - 9개 데이터가 3개씩 3페이지로 나뉘는지 확인")
    void testGetLikeSubjectsByUserId_AllPagesIteration() {
        // Given
        Long userId = 1L;
        int size = 3;

        // 각 페이지별로 mock 설정
        for (int page = 0; page < 3; page++) {
            Pageable pageable = PageRequest.of(page, size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, mockLikeSubjects.size());

            List<LikeSubject> pageContent = mockLikeSubjects.subList(startIndex, endIndex);
            Page<LikeSubject> mockPage = new PageImpl<>(pageContent, pageable, 9);

            when(likeSubjectRepository.findByMemberIdOrderBySubjectAsc(eq(userId), eq(pageable)))
                    .thenReturn(mockPage);
        }

        // When & Then - 모든 페이지 순회
        for (int page = 0; page < 3; page++) {
            Page<LikeSubject> result = likeSubjectService.getLikeSubjectsByUserId(userId, page, size);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getNumber()).isEqualTo(page);
            assertThat(result.getTotalElements()).isEqualTo(9);
            assertThat(result.getTotalPages()).isEqualTo(3);

            // 각 페이지의 첫 번째 아이템 검증
            String expectedSubjectName = "하냥" + (page * 3 + 1);
            assertThat(result.getContent().get(0).getSubject().getSubjectName())
                    .isEqualTo(expectedSubjectName);
        }
    }
}