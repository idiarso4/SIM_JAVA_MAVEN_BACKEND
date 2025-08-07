package com.school.sim.repository;

import com.school.sim.entity.ExtracurricularActivity;
import com.school.sim.entity.ExtracurricularAttendance;
import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import com.school.sim.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExtracurricularAttendance entity
 * Provides data access methods for extracurricular attendance management
 */
@Repository
public interface ExtracurricularAttendanceRepository extends JpaRepository<ExtracurricularAttendance, Long> {

    /**
     * Find attendance by activity and student
     */
    Optional<ExtracurricularAttendance> findByActivityAndStudentAndAttendanceDate(
            ExtracurricularActivity activity, Student student, LocalDate attendanceDate);

    /**
     * Find all attendance records for an activity
     */
    List<ExtracurricularAttendance> findByActivityAndIsActiveTrueOrderByAttendanceDateDesc(ExtracurricularActivity activity);

    /**
     * Find all attendance records for a student
     */
    List<ExtracurricularAttendance> findByStudentAndIsActiveTrueOrderByAttendanceDateDesc(Student student);

    /**
     * Find attendance by activity and date
     */
    List<ExtracurricularAttendance> findByActivityAndAttendanceDateAndIsActiveTrueOrderByStudentNamaLengkapAsc(
            ExtracurricularActivity activity, LocalDate attendanceDate);

    /**
     * Find attendance by student and date range
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.student = :student " +
           "AND ea.attendanceDate BETWEEN :startDate AND :endDate AND ea.isActive = true " +
           "ORDER BY ea.attendanceDate DESC")
    List<ExtracurricularAttendance> findByStudentAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find attendance by activity and date range
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.activity = :activity " +
           "AND ea.attendanceDate BETWEEN :startDate AND :endDate AND ea.isActive = true " +
           "ORDER BY ea.attendanceDate DESC, ea.student.namaLengkap ASC")
    List<ExtracurricularAttendance> findByActivityAndDateRange(
            @Param("activity") ExtracurricularActivity activity,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find attendance by status
     */
    List<ExtracurricularAttendance> findByStatusAndIsActiveTrueOrderByAttendanceDateDesc(AttendanceStatus status);

    /**
     * Find attendance by activity and status
     */
    List<ExtracurricularAttendance> findByActivityAndStatusAndIsActiveTrueOrderByAttendanceDateDesc(
            ExtracurricularActivity activity, AttendanceStatus status);

    /**
     * Find attendance by student and status
     */
    List<ExtracurricularAttendance> findByStudentAndStatusAndIsActiveTrueOrderByAttendanceDateDesc(
            Student student, AttendanceStatus status);

    /**
     * Count attendance by activity
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularAttendance ea WHERE ea.activity = :activity AND ea.isActive = true")
    Long countByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Count attendance by student
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularAttendance ea WHERE ea.student = :student AND ea.isActive = true")
    Long countByStudent(@Param("student") Student student);

    /**
     * Count attendance by status
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularAttendance ea WHERE ea.status = :status AND ea.isActive = true")
    Long countByStatus(@Param("status") AttendanceStatus status);

    /**
     * Count present attendance by activity
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularAttendance ea WHERE ea.activity = :activity " +
           "AND ea.status IN ('PRESENT', 'LATE', 'PARTIAL') AND ea.isActive = true")
    Long countPresentByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Count present attendance by student
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularAttendance ea WHERE ea.student = :student " +
           "AND ea.status IN ('PRESENT', 'LATE', 'PARTIAL') AND ea.isActive = true")
    Long countPresentByStudent(@Param("student") Student student);

    /**
     * Calculate attendance rate by activity
     */
    @Query("SELECT CAST(COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) AS DOUBLE) / " +
           "CAST(COUNT(ea) AS DOUBLE) * 100 FROM ExtracurricularAttendance ea " +
           "WHERE ea.activity = :activity AND ea.isActive = true")
    Double calculateAttendanceRateByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Calculate attendance rate by student
     */
    @Query("SELECT CAST(COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) AS DOUBLE) / " +
           "CAST(COUNT(ea) AS DOUBLE) * 100 FROM ExtracurricularAttendance ea " +
           "WHERE ea.student = :student AND ea.isActive = true")
    Double calculateAttendanceRateByStudent(@Param("student") Student student);

    /**
     * Calculate attendance rate by student and activity
     */
    @Query("SELECT CAST(COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) AS DOUBLE) / " +
           "CAST(COUNT(ea) AS DOUBLE) * 100 FROM ExtracurricularAttendance ea " +
           "WHERE ea.student = :student AND ea.activity = :activity AND ea.isActive = true")
    Double calculateAttendanceRateByStudentAndActivity(
            @Param("student") Student student, @Param("activity") ExtracurricularActivity activity);

    /**
     * Find students with perfect attendance for an activity
     */
    @Query("SELECT ea.student FROM ExtracurricularAttendance ea WHERE ea.activity = :activity " +
           "AND ea.isActive = true GROUP BY ea.student " +
           "HAVING COUNT(CASE WHEN ea.status NOT IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) = 0")
    List<Student> findStudentsWithPerfectAttendance(@Param("activity") ExtracurricularActivity activity);

    /**
     * Find students with poor attendance (below threshold)
     */
    @Query("SELECT ea.student FROM ExtracurricularAttendance ea WHERE ea.activity = :activity " +
           "AND ea.isActive = true GROUP BY ea.student " +
           "HAVING (CAST(COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) AS DOUBLE) / " +
           "CAST(COUNT(ea) AS DOUBLE) * 100) < :threshold")
    List<Student> findStudentsWithPoorAttendance(
            @Param("activity") ExtracurricularActivity activity, @Param("threshold") Double threshold);

    /**
     * Find top performers by participation score
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.activity = :activity " +
           "AND ea.participationScore IS NOT NULL AND ea.isActive = true " +
           "ORDER BY ea.participationScore DESC")
    List<ExtracurricularAttendance> findTopPerformersByActivity(
            @Param("activity") ExtracurricularActivity activity, Pageable pageable);

    /**
     * Calculate average participation score by activity
     */
    @Query("SELECT AVG(ea.participationScore) FROM ExtracurricularAttendance ea " +
           "WHERE ea.activity = :activity AND ea.participationScore IS NOT NULL AND ea.isActive = true")
    Double calculateAverageParticipationScoreByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Calculate average participation score by student
     */
    @Query("SELECT AVG(ea.participationScore) FROM ExtracurricularAttendance ea " +
           "WHERE ea.student = :student AND ea.participationScore IS NOT NULL AND ea.isActive = true")
    Double calculateAverageParticipationScoreByStudent(@Param("student") Student student);

    /**
     * Find attendance with high achievement points
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.achievementPoints > :minPoints " +
           "AND ea.isActive = true ORDER BY ea.achievementPoints DESC, ea.attendanceDate DESC")
    List<ExtracurricularAttendance> findHighAchievementAttendance(@Param("minPoints") Integer minPoints);

    /**
     * Calculate total achievement points by student
     */
    @Query("SELECT COALESCE(SUM(ea.achievementPoints), 0) FROM ExtracurricularAttendance ea " +
           "WHERE ea.student = :student AND ea.isActive = true")
    Long calculateTotalAchievementPointsByStudent(@Param("student") Student student);

    /**
     * Calculate total achievement points by activity
     */
    @Query("SELECT COALESCE(SUM(ea.achievementPoints), 0) FROM ExtracurricularAttendance ea " +
           "WHERE ea.activity = :activity AND ea.isActive = true")
    Long calculateTotalAchievementPointsByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Find recent attendance records
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.attendanceDate >= :fromDate " +
           "AND ea.isActive = true ORDER BY ea.attendanceDate DESC, ea.createdAt DESC")
    List<ExtracurricularAttendance> findRecentAttendance(@Param("fromDate") LocalDate fromDate);

    /**
     * Find attendance records that need review (no participation score)
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.participationScore IS NULL " +
           "AND ea.status IN ('PRESENT', 'LATE', 'PARTIAL') AND ea.isActive = true " +
           "ORDER BY ea.attendanceDate DESC")
    List<ExtracurricularAttendance> findAttendanceNeedingReview();

    /**
     * Find attendance by multiple criteria
     */
    @Query("SELECT ea FROM ExtracurricularAttendance ea WHERE ea.isActive = true " +
           "AND (:activity IS NULL OR ea.activity = :activity) " +
           "AND (:student IS NULL OR ea.student = :student) " +
           "AND (:status IS NULL OR ea.status = :status) " +
           "AND (:startDate IS NULL OR ea.attendanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR ea.attendanceDate <= :endDate) " +
           "ORDER BY ea.attendanceDate DESC, ea.student.namaLengkap ASC")
    Page<ExtracurricularAttendance> findByMultipleCriteria(
            @Param("activity") ExtracurricularActivity activity,
            @Param("student") Student student,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Get attendance statistics for an activity
     */
    @Query("SELECT new map(" +
           "COUNT(ea) as totalRecords, " +
           "COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) as presentCount, " +
           "COUNT(CASE WHEN ea.status = 'ABSENT' THEN 1 END) as absentCount, " +
           "COUNT(CASE WHEN ea.status = 'LATE' THEN 1 END) as lateCount, " +
           "COUNT(CASE WHEN ea.status = 'EXCUSED' THEN 1 END) as excusedCount, " +
           "AVG(ea.participationScore) as avgParticipationScore, " +
           "SUM(ea.achievementPoints) as totalAchievementPoints" +
           ") FROM ExtracurricularAttendance ea WHERE ea.activity = :activity AND ea.isActive = true")
    List<Object> getAttendanceStatisticsByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Get attendance statistics for a student
     */
    @Query("SELECT new map(" +
           "COUNT(ea) as totalRecords, " +
           "COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) as presentCount, " +
           "COUNT(CASE WHEN ea.status = 'ABSENT' THEN 1 END) as absentCount, " +
           "COUNT(CASE WHEN ea.status = 'LATE' THEN 1 END) as lateCount, " +
           "COUNT(CASE WHEN ea.status = 'EXCUSED' THEN 1 END) as excusedCount, " +
           "AVG(ea.participationScore) as avgParticipationScore, " +
           "SUM(ea.achievementPoints) as totalAchievementPoints" +
           ") FROM ExtracurricularAttendance ea WHERE ea.student = :student AND ea.isActive = true")
    List<Object> getAttendanceStatisticsByStudent(@Param("student") Student student);

    /**
     * Find attendance trends by month
     */
    @Query("SELECT EXTRACT(YEAR FROM ea.attendanceDate) as year, " +
           "EXTRACT(MONTH FROM ea.attendanceDate) as month, " +
           "COUNT(ea) as totalRecords, " +
           "COUNT(CASE WHEN ea.status IN ('PRESENT', 'LATE', 'PARTIAL') THEN 1 END) as presentCount " +
           "FROM ExtracurricularAttendance ea WHERE ea.activity = :activity AND ea.isActive = true " +
           "GROUP BY EXTRACT(YEAR FROM ea.attendanceDate), EXTRACT(MONTH FROM ea.attendanceDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> findAttendanceTrendsByActivity(@Param("activity") ExtracurricularActivity activity);

    /**
     * Check if attendance record exists
     */
    boolean existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
            ExtracurricularActivity activity, Student student, LocalDate attendanceDate);
}