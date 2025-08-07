package com.school.sim.service;

import com.school.sim.dto.request.CreateExtracurricularAttendanceRequest;
import com.school.sim.dto.request.ExtracurricularAttendanceSearchRequest;
import com.school.sim.dto.request.UpdateExtracurricularAttendanceRequest;
import com.school.sim.dto.response.ExtracurricularAttendanceResponse;
import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing extracurricular attendance
 * Provides comprehensive functionality for attendance tracking, reporting, and progress monitoring
 */
public interface ExtracurricularAttendanceService {

    // Core CRUD Operations
    
    /**
     * Record attendance for an extracurricular activity
     */
    ExtracurricularAttendanceResponse recordAttendance(CreateExtracurricularAttendanceRequest request);
    
    /**
     * Update attendance record
     */
    ExtracurricularAttendanceResponse updateAttendance(Long attendanceId, UpdateExtracurricularAttendanceRequest request);
    
    /**
     * Get attendance record by ID
     */
    ExtracurricularAttendanceResponse getAttendanceById(Long attendanceId);
    
    /**
     * Delete attendance record (soft delete)
     */
    void deleteAttendance(Long attendanceId);
    
    /**
     * Permanently delete attendance record
     */
    void permanentlyDeleteAttendance(Long attendanceId);
    
    // Search and Filtering
    
    /**
     * Search attendance records with advanced criteria
     */
    Page<ExtracurricularAttendanceResponse> searchAttendance(ExtracurricularAttendanceSearchRequest request, Pageable pageable);
    
    /**
     * Get attendance records by activity
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByActivity(Long activityId);
    
    /**
     * Get attendance records by student
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByStudent(Long studentId);
    
    /**
     * Get attendance records by activity and date
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByActivityAndDate(Long activityId, LocalDate date);
    
    /**
     * Get attendance records by student and date range
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get attendance records by activity and date range
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByActivityAndDateRange(Long activityId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get attendance records by status
     */
    List<ExtracurricularAttendanceResponse> getAttendanceByStatus(AttendanceStatus status);
    
    /**
     * Get recent attendance records
     */
    List<ExtracurricularAttendanceResponse> getRecentAttendance(int days);
    
    /**
     * Get attendance records that need review
     */
    List<ExtracurricularAttendanceResponse> getAttendanceNeedingReview();
    
    // Bulk Operations
    
    /**
     * Record bulk attendance for an activity
     */
    List<ExtracurricularAttendanceResponse> recordBulkAttendance(List<CreateExtracurricularAttendanceRequest> requests);
    
    /**
     * Update bulk attendance records
     */
    List<ExtracurricularAttendanceResponse> updateBulkAttendance(Map<Long, UpdateExtracurricularAttendanceRequest> updates);
    
    /**
     * Mark all registered students as present for an activity
     */
    List<ExtracurricularAttendanceResponse> markAllPresent(Long activityId, LocalDate date);
    
    /**
     * Mark all registered students as absent for an activity
     */
    List<ExtracurricularAttendanceResponse> markAllAbsent(Long activityId, LocalDate date);
    
    // Statistics and Analytics
    
    /**
     * Get attendance statistics for an activity
     */
    ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByActivity(Long activityId);
    
    /**
     * Get attendance statistics for a student
     */
    ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByStudent(Long studentId);
    
    /**
     * Get attendance statistics for a student in a specific activity
     */
    ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByStudentAndActivity(Long studentId, Long activityId);
    
    /**
     * Calculate attendance rate by activity
     */
    Double calculateAttendanceRateByActivity(Long activityId);
    
    /**
     * Calculate attendance rate by student
     */
    Double calculateAttendanceRateByStudent(Long studentId);
    
    /**
     * Calculate attendance rate by student and activity
     */
    Double calculateAttendanceRateByStudentAndActivity(Long studentId, Long activityId);
    
    /**
     * Get attendance trends by activity
     */
    List<ExtracurricularAttendanceResponse.AttendanceTrend> getAttendanceTrendsByActivity(Long activityId);
    
    /**
     * Get attendance trends by student
     */
    List<ExtracurricularAttendanceResponse.AttendanceTrend> getAttendanceTrendsByStudent(Long studentId);
    
    // Progress Tracking and Achievement
    
    /**
     * Get student progress in extracurricular activities
     */
    ExtracurricularAttendanceResponse.StudentProgress getStudentProgress(Long studentId);
    
    /**
     * Get student progress in a specific activity
     */
    ExtracurricularAttendanceResponse.StudentProgress getStudentProgressInActivity(Long studentId, Long activityId);
    
    /**
     * Get top performers by activity
     */
    List<ExtracurricularAttendanceResponse.StudentProgress> getTopPerformersByActivity(Long activityId, int limit);
    
    /**
     * Get students needing attention by activity
     */
    List<ExtracurricularAttendanceResponse.StudentProgress> getStudentsNeedingAttention(Long activityId, Double threshold);
    
    /**
     * Get students with perfect attendance
     */
    List<ExtracurricularAttendanceResponse.StudentProgress> getStudentsWithPerfectAttendance(Long activityId);
    
    /**
     * Calculate total achievement points by student
     */
    Long calculateTotalAchievementPointsByStudent(Long studentId);
    
    /**
     * Calculate total achievement points by activity
     */
    Long calculateTotalAchievementPointsByActivity(Long activityId);
    
    /**
     * Award achievement points to student
     */
    ExtracurricularAttendanceResponse awardAchievementPoints(Long attendanceId, Integer points, String reason);
    
    // Reporting
    
    /**
     * Generate activity participation report
     */
    ExtracurricularAttendanceResponse.ActivityParticipationReport generateActivityParticipationReport(Long activityId);
    
    /**
     * Generate student attendance report
     */
    Map<String, Object> generateStudentAttendanceReport(Long studentId);
    
    /**
     * Generate comprehensive attendance report
     */
    Map<String, Object> generateComprehensiveAttendanceReport(ExtracurricularAttendanceSearchRequest searchRequest);
    
    /**
     * Generate monthly attendance summary
     */
    Map<String, Object> generateMonthlyAttendanceSummary(int year, int month);
    
    /**
     * Generate semester attendance summary
     */
    Map<String, Object> generateSemesterAttendanceSummary(String academicYear, Integer semester);
    
    // Export Functionality
    
    /**
     * Export attendance records to Excel
     */
    byte[] exportAttendanceToExcel(ExtracurricularAttendanceSearchRequest searchRequest);
    
    /**
     * Export activity participation report to Excel
     */
    byte[] exportActivityParticipationReportToExcel(Long activityId);
    
    /**
     * Export student progress report to Excel
     */
    byte[] exportStudentProgressReportToExcel(Long studentId);
    
    /**
     * Export attendance statistics to Excel
     */
    byte[] exportAttendanceStatisticsToExcel(LocalDate startDate, LocalDate endDate);
    
    /**
     * Import attendance records from Excel
     */
    List<ExtracurricularAttendanceResponse> importAttendanceFromExcel(byte[] excelData);
    
    // Validation and Business Logic
    
    /**
     * Check if attendance can be recorded
     */
    Map<String, Object> validateAttendanceRecording(Long activityId, Long studentId, LocalDate date);
    
    /**
     * Check if student is registered for activity
     */
    Boolean isStudentRegisteredForActivity(Long activityId, Long studentId);
    
    /**
     * Check if attendance already exists
     */
    Boolean attendanceExists(Long activityId, Long studentId, LocalDate date);
    
    /**
     * Get attendance conflicts
     */
    List<Map<String, Object>> getAttendanceConflicts(Long studentId, LocalDate date);
    
    // Notification and Communication
    
    /**
     * Send attendance notifications
     */
    List<Map<String, Object>> sendAttendanceNotifications(Long activityId, LocalDate date);
    
    /**
     * Send absence notifications to parents/guardians
     */
    List<Map<String, Object>> sendAbsenceNotifications(Long studentId, Long activityId, LocalDate date);
    
    /**
     * Send achievement notifications
     */
    List<Map<String, Object>> sendAchievementNotifications(Long studentId, String achievement);
    
    /**
     * Send progress reports
     */
    List<Map<String, Object>> sendProgressReports(Long activityId);
    
    // Dashboard and Analytics
    
    /**
     * Get attendance dashboard data
     */
    Map<String, Object> getAttendanceDashboardData();
    
    /**
     * Get activity dashboard data
     */
    Map<String, Object> getActivityDashboardData(Long activityId);
    
    /**
     * Get student dashboard data
     */
    Map<String, Object> getStudentDashboardData(Long studentId);
    
    /**
     * Get supervisor dashboard data
     */
    Map<String, Object> getSupervisorDashboardData(Long supervisorId);
    
    // Utility Methods
    
    /**
     * Calculate participation score based on attendance quality
     */
    Integer calculateParticipationScore(Long attendanceId);
    
    /**
     * Update participation scores for activity
     */
    List<ExtracurricularAttendanceResponse> updateParticipationScoresForActivity(Long activityId);
    
    /**
     * Generate attendance certificates
     */
    byte[] generateAttendanceCertificate(Long studentId, Long activityId);
    
    /**
     * Generate achievement certificates
     */
    byte[] generateAchievementCertificate(Long studentId, String achievement);
    
    /**
     * Archive old attendance records
     */
    void archiveOldAttendanceRecords(String academicYear);
    
    /**
     * Clean up inactive attendance records
     */
    void cleanupInactiveAttendanceRecords();
}