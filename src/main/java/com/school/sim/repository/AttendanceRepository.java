package com.school.sim.repository;

import com.school.sim.entity.Attendance;
import com.school.sim.entity.AttendanceStatus;
import com.school.sim.entity.Student;
import com.school.sim.entity.TeachingActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity
 * Provides data access methods for student attendance records
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * Find attendance by teaching activity and student
     */
    Optional<Attendance> findByTeachingActivityAndStudent(TeachingActivity teachingActivity, Student student);
    
    /**
     * Find all attendance records for a teaching activity
     */
    List<Attendance> findByTeachingActivity(TeachingActivity teachingActivity);
    
    /**
     * Find attendance records by student and date range
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE a.student = :student " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<Attendance> findByStudentAndDateBetween(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find attendance records by student and specific date
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE a.student = :student " +
           "AND ta.date = :date ORDER BY ta.startTime")
    List<Attendance> findByStudentAndDate(@Param("student") Student student, @Param("date") LocalDate date);
    
    /**
     * Find attendance records by status
     */
    List<Attendance> findByStatus(AttendanceStatus status);
    
    /**
     * Count attendance by student and status in date range
     */
    @Query("SELECT COUNT(a) FROM Attendance a JOIN a.teachingActivity ta WHERE a.student = :student " +
           "AND a.status = :status AND ta.date BETWEEN :startDate AND :endDate")
    Long countByStudentAndStatusAndDateBetween(
            @Param("student") Student student,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Get attendance statistics for a student in date range
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE a.student = :student AND ta.date BETWEEN :startDate AND :endDate " +
           "GROUP BY a.status")
    List<Object[]> getAttendanceStatsByStudentAndDateBetween(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find attendance records by class room and date
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE ta.classRoom.id = :classRoomId " +
           "AND ta.date = :date ORDER BY a.student.namaLengkap")
    List<Attendance> findByClassRoomAndDate(@Param("classRoomId") Long classRoomId, @Param("date") LocalDate date);
    
    /**
     * Find recent attendance records
     */
    @Query("SELECT a FROM Attendance a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Attendance> findRecentAttendance(@Param("since") LocalDateTime since);
    
    /**
     * Check if attendance exists for teaching activity and student
     */
    boolean existsByTeachingActivityAndStudent(TeachingActivity teachingActivity, Student student);

    /**
     * Find attendance records by class room and date range
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE ta.classRoom.id = :classRoomId " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, a.student.namaLengkap")
    List<Attendance> findByClassRoomAndDateBetween(@Param("classRoomId") Long classRoomId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find attendance records by teacher and date range
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE ta.teacher.id = :teacherId " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<Attendance> findByTeacherAndDateBetween(@Param("teacherId") Long teacherId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Find attendance records by subject and date range
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta WHERE ta.subject.id = :subjectId " +
           "AND ta.date BETWEEN :startDate AND :endDate ORDER BY ta.date, ta.startTime")
    List<Attendance> findBySubjectAndDateBetween(@Param("subjectId") Long subjectId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Get attendance summary by class room and date range
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.classRoom.id = :classRoomId AND ta.date BETWEEN :startDate AND :endDate " +
           "GROUP BY a.status")
    List<Object[]> getAttendanceSummaryByClassRoomAndDateBetween(@Param("classRoomId") Long classRoomId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    /**
     * Get daily attendance statistics
     */
    @Query("SELECT ta.date, a.status, COUNT(a) FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ta.date, a.status ORDER BY ta.date")
    List<Object[]> getDailyAttendanceStatistics(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Find students with perfect attendance in date range
     */
    @Query("SELECT s FROM Student s WHERE s.id NOT IN (" +
           "SELECT DISTINCT a.student.id FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.date BETWEEN :startDate AND :endDate AND a.status IN ('ABSENT', 'LATE')" +
           ") AND s.id IN (" +
           "SELECT DISTINCT a2.student.id FROM Attendance a2 JOIN a2.teachingActivity ta2 " +
           "WHERE ta2.date BETWEEN :startDate AND :endDate" +
           ")")
    List<Student> findStudentsWithPerfectAttendance(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Find students with poor attendance (high absence rate)
     */
    @Query("SELECT a.student, COUNT(a) as absenceCount FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.date BETWEEN :startDate AND :endDate AND a.status = 'ABSENT' " +
           "GROUP BY a.student HAVING COUNT(a) >= :minAbsences ORDER BY COUNT(a) DESC")
    List<Object[]> findStudentsWithPoorAttendance(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("minAbsences") Long minAbsences);

    /**
     * Calculate attendance rate for student in date range
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a) " +
           "FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE a.student = :student AND ta.date BETWEEN :startDate AND :endDate")
    Double calculateAttendanceRateForStudent(@Param("student") Student student,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Calculate attendance rate for class room in date range
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a) " +
           "FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.classRoom.id = :classRoomId AND ta.date BETWEEN :startDate AND :endDate")
    Double calculateAttendanceRateForClassRoom(@Param("classRoomId") Long classRoomId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Find attendance records that need follow-up (absent or sick students)
     */
    @Query("SELECT a FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE a.status IN ('ABSENT', 'SICK') AND ta.date >= :fromDate " +
           "ORDER BY ta.date DESC, a.student.namaLengkap")
    List<Attendance> findAttendanceNeedingFollowUp(@Param("fromDate") LocalDate fromDate);

    /**
     * Get monthly attendance report
     */
    @Query("SELECT YEAR(ta.date), MONTH(ta.date), a.status, COUNT(a) " +
           "FROM Attendance a JOIN a.teachingActivity ta " +
           "WHERE ta.date BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(ta.date), MONTH(ta.date), a.status " +
           "ORDER BY YEAR(ta.date), MONTH(ta.date)")
    List<Object[]> getMonthlyAttendanceReport(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
