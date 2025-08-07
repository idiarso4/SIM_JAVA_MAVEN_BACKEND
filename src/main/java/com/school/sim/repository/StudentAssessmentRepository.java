package com.school.sim.repository;

import com.school.sim.entity.StudentAssessment;
import com.school.sim.entity.Assessment;
import com.school.sim.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StudentAssessment entity
 * Provides data access methods for student assessment scores and evaluations
 */
@Repository
public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {
    
    /**
     * Find student assessment by assessment and student
     */
    Optional<StudentAssessment> findByAssessmentAndStudent(Assessment assessment, Student student);
    
    /**
     * Find all assessments for a student
     */
    List<StudentAssessment> findByStudent(Student student);
    
    /**
     * Find all student assessments for an assessment
     */
    List<StudentAssessment> findByAssessment(Assessment assessment);
    
    /**
     * Find student assessments by student and academic period
     */
    @Query("SELECT sa FROM StudentAssessment sa JOIN sa.assessment a WHERE sa.student = :student " +
           "AND a.academicYear = :academicYear AND a.semester = :semester " +
           "ORDER BY a.dueDate, a.createdAt")
    List<StudentAssessment> findByStudentAndAcademicPeriod(
            @Param("student") Student student,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Find student assessments by subject
     */
    @Query("SELECT sa FROM StudentAssessment sa JOIN sa.assessment a WHERE sa.student = :student " +
           "AND a.subject.id = :subjectId ORDER BY a.dueDate")
    List<StudentAssessment> findByStudentAndSubject(@Param("student") Student student, @Param("subjectId") Long subjectId);
    
    /**
     * Find submitted assessments
     */
    List<StudentAssessment> findByIsSubmittedTrue();
    
    /**
     * Find unsubmitted assessments
     */
    List<StudentAssessment> findByIsSubmittedFalse();
    
    /**
     * Find graded assessments
     */
    @Query("SELECT sa FROM StudentAssessment sa WHERE sa.gradedAt IS NOT NULL")
    List<StudentAssessment> findGradedAssessments();
    
    /**
     * Find ungraded assessments
     */
    @Query("SELECT sa FROM StudentAssessment sa WHERE sa.gradedAt IS NULL AND sa.isSubmitted = true")
    List<StudentAssessment> findUngradedAssessments();
    
    /**
     * Calculate average score for student in subject
     */
    @Query("SELECT AVG(sa.score) FROM StudentAssessment sa JOIN sa.assessment a " +
           "WHERE sa.student = :student AND a.subject.id = :subjectId AND sa.score IS NOT NULL")
    BigDecimal calculateAverageScoreByStudentAndSubject(@Param("student") Student student, @Param("subjectId") Long subjectId);
    
    /**
     * Calculate weighted average for student in academic period
     */
    @Query("SELECT SUM(sa.score * a.weight) / SUM(a.weight) FROM StudentAssessment sa JOIN sa.assessment a " +
           "WHERE sa.student = :student AND a.academicYear = :academicYear AND a.semester = :semester " +
           "AND sa.score IS NOT NULL")
    BigDecimal calculateWeightedAverageByStudentAndAcademicPeriod(
            @Param("student") Student student,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Find top performers for an assessment
     */
    @Query("SELECT sa FROM StudentAssessment sa WHERE sa.assessment = :assessment " +
           "AND sa.score IS NOT NULL ORDER BY sa.score DESC")
    List<StudentAssessment> findTopPerformersByAssessment(@Param("assessment") Assessment assessment);
    
    /**
     * Count assessments by grade
     */
    @Query("SELECT sa.grade, COUNT(sa) FROM StudentAssessment sa WHERE sa.assessment = :assessment " +
           "AND sa.grade IS NOT NULL GROUP BY sa.grade")
    List<Object[]> countAssessmentsByGrade(@Param("assessment") Assessment assessment);
    
    /**
     * Check if student assessment exists
     */
    boolean existsByAssessmentAndStudent(Assessment assessment, Student student);
}
