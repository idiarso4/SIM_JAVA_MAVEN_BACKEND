package com.school.sim.service;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.TeachingActivityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for teaching activity management functionality
 * Handles teaching session management, activity tracking, and attendance integration
 */
public interface TeachingActivityService {

    /**
     * Create a new teaching activity
     */
    TeachingActivityResponse createTeachingActivity(CreateTeachingActivityRequest request);

    /**
     * Update an existing teaching activity
     */
    TeachingActivityResponse updateTeachingActivity(Long activityId, UpdateTeachingActivityRequest request);

    /**
     * Get teaching activity by ID
     */
    TeachingActivityResponse getTeachingActivityById(Long activityId);

    /**
     * Delete teaching activity
     */
    void deleteTeachingActivity(Long activityId);

    /**
     * Search and filter teaching activities
     */
    Page<TeachingActivityResponse> searchTeachingActivities(TeachingActivitySearchRequest request, Pageable pageable);

    /**
     * Get teaching activities by teacher and date range
     */
    List<TeachingActivityResponse> getTeachingActivitiesByTeacher(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activities by class room and date range
     */
    List<TeachingActivityResponse> getTeachingActivitiesByClassRoom(Long classRoomId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activities by subject and date range
     */
    List<TeachingActivityResponse> getTeachingActivitiesBySubject(Long subjectId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activities for a specific date
     */
    List<TeachingActivityResponse> getTeachingActivitiesByDate(LocalDate date);

    /**
     * Get today's teaching activities for a teacher
     */
    List<TeachingActivityResponse> getTodaysActivitiesForTeacher(Long teacherId);

    /**
     * Get upcoming teaching activities for a teacher
     */
    List<TeachingActivityResponse> getUpcomingActivitiesForTeacher(Long teacherId, Integer days);

    /**
     * Mark teaching activity as completed
     */
    TeachingActivityResponse markActivityAsCompleted(Long activityId, String notes);

    /**
     * Mark teaching activity as incomplete
     */
    TeachingActivityResponse markActivityAsIncomplete(Long activityId);

    /**
     * Generate teaching activity from schedule
     */
    TeachingActivityResponse generateActivityFromSchedule(Long scheduleId, LocalDate date, String topic);

    /**
     * Bulk generate activities from schedules for date range
     */
    List<TeachingActivityResponse> bulkGenerateActivitiesFromSchedules(List<Long> scheduleIds, 
                                                                       LocalDate startDate, 
                                                                       LocalDate endDate);

    /**
     * Get teaching activity statistics for teacher
     */
    Map<String, Object> getTeacherActivityStatistics(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activity statistics for class room
     */
    Map<String, Object> getClassRoomActivityStatistics(Long classRoomId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activity statistics for subject
     */
    Map<String, Object> getSubjectActivityStatistics(Long subjectId, LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance summary for teaching activity
     */
    TeachingActivityResponse.AttendanceSummary getAttendanceSummary(Long activityId);

    /**
     * Check if attendance is recorded for teaching activity
     */
    Boolean isAttendanceRecorded(Long activityId);

    /**
     * Get teaching activities with pending attendance
     */
    List<TeachingActivityResponse> getActivitiesWithPendingAttendance(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get completed teaching activities
     */
    List<TeachingActivityResponse> getCompletedActivities(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activity workload analysis
     */
    Map<String, Object> getTeachingWorkloadAnalysis(Long teacherId, LocalDate startDate, LocalDate endDate);

    /**
     * Get class room utilization analysis
     */
    Map<String, Object> getClassRoomUtilizationAnalysis(Long classRoomId, LocalDate startDate, LocalDate endDate);

    /**
     * Generate teaching activity report
     */
    Map<String, Object> generateActivityReport(TeachingActivitySearchRequest criteria);

    /**
     * Validate teaching activity schedule conflicts
     */
    List<Map<String, Object>> validateActivityScheduleConflicts(CreateTeachingActivityRequest request);

    /**
     * Get teaching activity notifications
     */
    List<Map<String, Object>> getActivityNotifications(Long teacherId);

    /**
     * Send activity reminders to teachers
     */
    void sendActivityReminders(LocalDate date);

    /**
     * Archive old teaching activities
     */
    void archiveOldActivities(LocalDate beforeDate);

    /**
     * Get teaching activity calendar for teacher
     */
    Map<String, Object> getTeacherActivityCalendar(Long teacherId, Integer year, Integer month);

    /**
     * Get teaching activity calendar for class room
     */
    Map<String, Object> getClassRoomActivityCalendar(Long classRoomId, Integer year, Integer month);

    /**
     * Export teaching activities to various formats
     */
    byte[] exportTeachingActivities(TeachingActivitySearchRequest criteria, String format);

    /**
     * Import teaching activities from file
     */
    List<TeachingActivityResponse> importTeachingActivities(byte[] fileData);

    /**
     * Get activity performance metrics
     */
    Map<String, Object> getActivityPerformanceMetrics(LocalDate startDate, LocalDate endDate);

    /**
     * Get teaching activity trends
     */
    Map<String, Object> getActivityTrends(LocalDate startDate, LocalDate endDate);

    /**
     * Reschedule teaching activity
     */
    TeachingActivityResponse rescheduleActivity(Long activityId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime);

    /**
     * Cancel teaching activity
     */
    void cancelActivity(Long activityId, String reason);

    /**
     * Get cancelled activities
     */
    List<TeachingActivityResponse> getCancelledActivities(LocalDate startDate, LocalDate endDate);
}