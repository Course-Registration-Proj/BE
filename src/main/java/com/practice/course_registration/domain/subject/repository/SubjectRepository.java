package com.practice.course_registration.domain.subject.repository;

import com.practice.course_registration.domain.subject.domain.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {


    @Query("""
    SELECT s
    FROM Subject s
    WHERE (:code IS NULL OR :code = '' OR s.code = :code)
      AND (:professorName IS NULL OR :professorName = '' 
           OR LOWER(s.professorName) LIKE LOWER(CONCAT('%', :professorName, '%')))
      AND (:subjectName IS NULL OR :subjectName = '' 
           OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :subjectName, '%')))
    """)
    Page<Subject> findAllByCodeAndProfessorNameAndSubjectName(@Param("code") String code,
                                                              @Param("professorName") String professorName,
                                                              @Param("subjectName") String subjectName,
                                                              Pageable pageable);

    Optional<Subject> findByCode(String code);
}
