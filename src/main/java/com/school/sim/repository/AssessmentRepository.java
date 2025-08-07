package com.school.sim.repository;

import com.school.sim.entity.Assessment;
import com.school.sim.entity.AssessmentType;
import com.school.sim.entity.ClassRoom;
import com.school.sim.entity.Subject;
import com.school.sim.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Assessment entity
 * Provides data access methods for academic assessments
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    
    /**
     * Find assessments by subject
     */
    List<Assessment> findBySubjectAndIsActiveTrue(Subject subject);
    
    /**
     * Find assessments by class room
     */
    List<Assessment> findByClassRoomAndIsActiveTrue(ClassRoom classRoom);
    
    /**
     * Find assessments by teacher
     */
    List<Assessment> findByTeacherAndIsActiveTrue(User teacher);
    
    /**
     * Find assessments by type
     */
    List<Assessment> findByTypeAndIsActiveTrue(AssessmentType type);
    
    /**
     * Find assessments by academic year and semester
     */
    List<Assessment> findByAcademicYearAndSemesterAndIsActiveTrue(String academicYear, Integer semester);
    
    /**
     * Find assessments by class room, subject and academic period
     */
    @Query("SELECT a FROM Assessment a WHERE a.classRoom = :classRoom AND a.subject = :subject " +
           "AND a.academicYear = :academicYear AND a.semester = :semester AND a.isActive = true " +
           "ORDER BY a.dueDate, a.createdAt")
    List<Assessment> findByClassRoomAndSubjectAndAcademicPeriod(
            @Param("classRoom") ClassRoom classRoom,
            @Param("subject") Subject subject,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Find assessments with due date in range
     */
    @Query("SELECT a FROM Assessment a WHERE a.dueDate BETWEEN :startDate AND :endDate " +
           "AND a.isActive = true ORDER BY a.dueDate")
    List<Assessment> findByDueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find overdue assessments
     */
    @Query("SELECT a FROM Assessment a WHERE a.dueDate < :currentDate AND a.isActive = true")
    List<Assessment> findOverdueAssessments(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find upcoming assessments for a class room
     */
    @Query("SELECT a FROM Assessment a WHERE a.classRoom = :classRoom AND a.dueDate >= :currentDate " +
           "AND a.isActive = true ORDER BY a.dueDate LIMIT 10")
    List<Assessment> findUpcomingAssessmentsByClassRoom(@Param("classRoom") ClassRoom classRoom, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Count assessments by teacher and academic period
     */
    @Query("SELECT COUNT(a) FROM Assessment a WHERE a.teacher = :teacher " +
           "AND a.academicYear = :academicYear AND a.semester = :semester AND a.isActive = true")
    Long countByTeacherAndAcademicPeriod(
            @Param("teacher") User teacher,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
}
