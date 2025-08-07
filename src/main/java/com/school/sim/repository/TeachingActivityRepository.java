package com.school.sim.repository;

import com.school.sim.entity.TeachingActivity;
import com.school.sim.entity.ClassRoom;
import com.school.sim.entity.User;
import com.school.sim.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TeachingActivity entity
 * Provides data access methods for teaching activities and sessions
 */
@Repository
public interface TeachingActivityRepository extends JpaRepository<TeachingActivity, Long> {
    
    /**
     * Find teaching activities by teacher and date
     */
    List<TeachingActivity> findByTeacherAndDate(User teacher, LocalDate date);
    
    /**
     * Find teaching activities by class room and date
     */
    List<TeachingActivity> findByClassRoomAndDate(ClassRoom classRoom, LocalDate date);
    
    /**
     * Find teaching activities by subject and date range
     */
    @Query("SELECT ta FROM TeachingActivity ta WHERE ta.subject = :subject " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<TeachingActivity> findBySubjectAndDateBetween(
            @Param("subject") Subject subject,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find teaching activities by teacher and date range
     */
    @Query("SELECT ta FROM TeachingActivity ta WHERE ta.teacher = :teacher " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<TeachingActivity> findByTeacherAndDateBetween(
            @Param("teacher") User teacher,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find teaching activities by class room and date range
     */
    @Query("SELECT ta FROM TeachingActivity ta WHERE ta.classRoom = :classRoom " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<TeachingActivity> findByClassRoomAndDateBetween(
            @Param("classRoom") ClassRoom classRoom,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find completed teaching activities
     */
    List<TeachingActivity> findByIsCompletedTrue();
    
    /**
     * Find pending teaching activities
     */
    List<TeachingActivity> findByIsCompletedFalse();
    
    /**
     * Check if teaching activity exists for specific schedule and date
     */
    @Query("SELECT ta FROM TeachingActivity ta WHERE ta.schedule.id = :scheduleId AND ta.date = :date")
    Optional<TeachingActivity> findByScheduleIdAndDate(@Param("scheduleId") Long scheduleId, @Param("date") LocalDate date);
    
    /**
     * Count teaching activities by teacher in date range
     */
    @Query("SELECT COUNT(ta) FROM TeachingActivity ta WHERE ta.teacher = :teacher " +
           "AND ta.date BETWEEN :startDate AND :endDate")
    Long countByTeacherAndDateBetween(
            @Param("teacher") User teacher,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
