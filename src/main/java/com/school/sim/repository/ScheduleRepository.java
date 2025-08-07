package com.school.sim.repository;

import com.school.sim.entity.Schedule;
import com.school.sim.entity.ClassRoom;
import com.school.sim.entity.Subject;
import com.school.sim.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for Schedule entity
 * Provides data access methods for class schedules and timetables
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    /**
     * Find schedules by class room
     */
    List<Schedule> findByClassRoomAndIsActiveTrue(ClassRoom classRoom);
    
    /**
     * Find schedules by teacher
     */
    List<Schedule> findByTeacherAndIsActiveTrue(User teacher);
    
    /**
     * Find schedules by subject
     */
    List<Schedule> findBySubjectAndIsActiveTrue(Subject subject);
    
    /**
     * Find schedules by academic year and semester
     */
    List<Schedule> findByAcademicYearAndSemesterAndIsActiveTrue(String academicYear, Integer semester);
    
    /**
     * Find schedules by day of week
     */
    List<Schedule> findByDayOfWeekAndIsActiveTrue(DayOfWeek dayOfWeek);
    
    /**
     * Find schedules by class room and academic period
     */
    List<Schedule> findByClassRoomAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
            ClassRoom classRoom, String academicYear, Integer semester);
    
    /**
     * Find schedules by teacher and academic period
     */
    List<Schedule> findByTeacherAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
            User teacher, String academicYear, Integer semester);
    
    /**
     * Find schedules by subject and academic period
     */
    List<Schedule> findBySubjectAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
            Subject subject, String academicYear, Integer semester);
    
    /**
     * Find schedules by teacher and day
     */
    List<Schedule> findByTeacherAndDayOfWeekAndAcademicYearAndSemester(
            User teacher, DayOfWeek dayOfWeek, String academicYear, Integer semester);
    
    /**
     * Find schedules by classroom and day
     */
    List<Schedule> findByClassRoomAndDayOfWeekAndAcademicYearAndSemester(
            ClassRoom classRoom, DayOfWeek dayOfWeek, String academicYear, Integer semester);
    
    /**
     * Check for schedule conflicts for a teacher
     */
    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacher AND s.dayOfWeek = :dayOfWeek " +
           "AND s.isActive = true AND s.academicYear = :academicYear AND s.semester = :semester " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findConflictingSchedulesForTeacher(
            @Param("teacher") User teacher,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Check for schedule conflicts for a classroom
     */
    @Query("SELECT s FROM Schedule s WHERE s.classRoom = :classRoom AND s.dayOfWeek = :dayOfWeek " +
           "AND s.isActive = true AND s.academicYear = :academicYear AND s.semester = :semester " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findConflictingSchedulesForClassRoom(
            @Param("classRoom") ClassRoom classRoom,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Find schedules for a specific class room and day
     */
    @Query("SELECT s FROM Schedule s WHERE s.classRoom = :classRoom AND s.dayOfWeek = :dayOfWeek " +
           "AND s.isActive = true AND s.academicYear = :academicYear AND s.semester = :semester " +
           "ORDER BY s.startTime")
    List<Schedule> findByClassRoomAndDayOfWeek(
            @Param("classRoom") ClassRoom classRoom,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("academicYear") String academicYear,
            @Param("semester") Integer semester
    );
    
    /**
     * Find schedules by academic year and semester (without active filter)
     */
    List<Schedule> findByAcademicYearAndSemester(String academicYear, Integer semester);
}
