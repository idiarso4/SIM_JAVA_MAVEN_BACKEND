package com.school.sim.service;

import com.school.sim.dto.request.CreateAttendanceRequest;
import com.school.sim.dto.request.UpdateAttendanceRequest;
import com.school.sim.dto.request.BulkAttendanceRequest;
import com.school.sim.dto.response.AttendanceResponse;
import com.school.sim.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Result class for bulk attendance operations
 */
class BulkAttendanceResult {
    private List<AttendanceResponse> successfulRecords;
    private List<String> errors;
    private int totalProcessed;
    private int successCount;
    private int errorCount;
    
    // Constructors, getters, and setters would be here
    public BulkAttendanceResult() {}
    
    public List<AttendanceResponse> getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(List<AttendanceResponse> successfulRecords) { this.successfulRecords = successfulRecords; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }
    
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
}

/**
 * Service interface for attendance management operations
 * Provides methods for attendance CRUD operations, bulk recording, and validation
 */
public interface AttendanceService {

    /**
     * Get all attendance records with pagination
     */
    Page<AttendanceResponse> getAllAttendance(Pageable pageable);

    /**
     * Get attendance records by student with pagination
     */
    Page<AttendanceResponse> getAttendanceByStudent(Long studentId, Pageable pageable);

    /**
     * Get attendance records by date range with pagination
     */
    Page<AttendanceResponse> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get attendance records by status with pagination
     */
    Page<AttendanceResponse> getAttendanceByStatus(AttendanceStatus status, Pageable pageable);

    /**
     * Get attendance statistics for date range
     */
    Map<String, Object> getAttendanceStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Get daily attendance summary
     */
    Map<String, Object> getDailyAttendanceSummary(LocalDate date);

    /**
     * Calculate student attendance rate
     */
    Double calculateStudentAttendanceRate(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Record attendance for a student in a teaching activity
     */
    AttendanceResponse recordAttendance(CreateAttendanceRequest request);

    /**
     * Update existing attendance record
     */
    AttendanceResponse updateAttendance(Long attendanceId, UpdateAttendanceRequest request);

    /**
     * Get attendance by ID
     */
    Optional<AttendanceResponse> getAttendanceById(Long attendanceId);

    /**
     * Get attendance records by teaching activity
     */
    List<AttendanceResponse> getAttendanceByTeachingActivity(Long teachingActivityId);

    /**
     * Get attendance records by student and date range
     */
    Page<AttendanceResponse> getAttendanceByStudentAndDateRange(Long studentId, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate, 
                                                               Pageable pageable);

    /**
     * Get attendance records by student and specific date
     */
    List<AttendanceResponse> getAttendanceByStudentAndDate(Long studentId, LocalDate date);

    /**
     * Get attendance records by class room and date
     */
    List<AttendanceResponse> getAttendanceByClassRoomAndDate(Long classRoomId, LocalDate date);

    /**
     * Get attendance records by class room and date range
     */
    Page<AttendanceResponse> getAttendanceByClassRoomAndDateRange(Long classRoomId, 
                                                                 LocalDate startDate, 
                                                                 LocalDate endDate, 
                                                                 Pageable pageable);

    /**
     * Get attendance records by teacher and date range
     */
    Page<AttendanceResponse> getAttendanceByTeacherAndDateRange(Long teacherId, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate, 
                                                               Pageable pageable);

    /**
     * Get attendance records by subject and date range
     */
    Page<AttendanceResponse> getAttendanceBySubjectAndDateRange(Long subjectId, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate, 
                                                               Pageable pageable);

    /**
     * Bulk record attendance for multiple students
     */
    BulkAttendanceResult bulkRecordAttendance(BulkAttendanceRequest request);

    /**
     * Delete attendance record
     */
    void deleteAttendance(Long attendanceId);

    /**
     * Check if attendance exists for teaching activity and student
     */
    boolean attendanceExists(Long teachingActivityId, Long studentId);

    /**
     * Get attendance statistics for a student in date range
     */
    Map<AttendanceStatus, Long> getAttendanceStatsByStudent(Long studentId, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate);

    /**
     * Get attendance summary for class room in date range
     */
    Map<AttendanceStatus, Long> getAttendanceSummaryByClassRoom(Long classRoomId, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate);

    /**
     * Get daily attendance statistics
     */
    List<Map<String, Object>> getDailyAttendanceStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Calculate attendance rate for student
     */
    Double calculateAttendanceRateForStudent(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate attendance rate for class room
     */
    Double calculateAttendanceRateForClassRoom(Long classRoomId, LocalDate startDate, LocalDate endDate);

    /**
     * Find students with perfect attendance
     */
    List<Map<String, Object>> findStudentsWithPerfectAttendance(LocalDate startDate, LocalDate endDate);

    /**
     * Find students with poor attendance
     */
    List<Map<String, Object>> findStudentsWithPoorAttendance(LocalDate startDate, 
                                                             LocalDate endDate, 
                                                             Long minAbsences);

    /**
     * Get attendance records needing follow-up
     */
    List<AttendanceResponse> getAttendanceNeedingFollowUp(LocalDate fromDate);

    /**
     * Get monthly attendance report
     */
    List<Map<String, Object>> getMonthlyAttendanceReport(LocalDate startDate, LocalDate endDate);

    /**
     * Validate attendance conflicts
     */
    void validateAttendanceConflicts(CreateAttendanceRequest request);

    /**
     * Get recent attendance records
     */
    List<AttendanceResponse> getRecentAttendance(int hours);

    /**
     * Auto-generate attendance records for teaching activity
     */
    List<AttendanceResponse> autoGenerateAttendanceForTeachingActivity(Long teachingActivityId, 
                                                                      AttendanceStatus defaultStatus);

    /**
     * Copy attendance from previous session
     */
    List<AttendanceResponse> copyAttendanceFromPreviousSession(Long currentTeachingActivityId, 
                                                              Long previousTeachingActivityId);

    /**
     * Bulk attendance result container
     */
    class BulkAttendanceResult {
        private List<AttendanceResponse> successfulRecords;
        private List<String> errors;
        private int totalProcessed;
        private int successCount;
        private int errorCount;

        // Constructors
        public BulkAttendanceResult() {}

        public BulkAttendanceResult(List<AttendanceResponse> successfulRecords, List<String> errors, 
                                   int totalProcessed, int successCount, int errorCount) {
            this.successfulRecords = successfulRecords;
            this.errors = errors;
            this.totalProcessed = totalProcessed;
            this.successCount = successCount;
            this.errorCount = errorCount;
        }

        // Getters and setters
        public List<AttendanceResponse> getSuccessfulRecords() { return successfulRecords; }
        public void setSuccessfulRecords(List<AttendanceResponse> successfulRecords) { this.successfulRecords = successfulRecords; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public int getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    }
}
