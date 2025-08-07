package com.school.sim.service;

import com.school.sim.dto.request.CreateExtracurricularActivityRequest;
import com.school.sim.dto.request.ExtracurricularActivitySearchRequest;
import com.school.sim.dto.request.UpdateExtracurricularActivityRequest;
import com.school.sim.dto.response.ExtracurricularActivityResponse;
import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing extracurricular activities
 * Provides comprehensive functionality for activity management, registration, and reporting
 */
public interface ExtracurricularActivityService {

    // Core CRUD Operations
    
    /**
     * Create a new extracurricular activity
     */
    ExtracurricularActivityResponse createActivity(CreateExtracurricularActivityRequest request);
    
    /**
     * Update an existing extracurricular activity
     */
    ExtracurricularActivityResponse updateActivity(Long activityId, UpdateExtracurricularActivityRequest request);
    
    /**
     * Get activity by ID
     */
    ExtracurricularActivityResponse getActivityById(Long activityId);
    
    /**
     * Delete an activity (soft delete)
     */
    void deleteActivity(Long activityId);
    
    /**
     * Permanently delete an activity
     */
    void permanentlyDeleteActivity(Long activityId);
    
    // Search and Filtering
    
    /**
     * Search activities with advanced criteria
     */
    Page<ExtracurricularActivityResponse> searchActivities(ExtracurricularActivitySearchRequest request, Pageable pageable);
    
    /**
     * Get all active activities
     */
    List<ExtracurricularActivityResponse> getAllActiveActivities();
    
    /**
     * Get activities by type
     */
    List<ExtracurricularActivityResponse> getActivitiesByType(ActivityType type);
    
    /**
     * Get activities by status
     */
    List<ExtracurricularActivityResponse> getActivitiesByStatus(ActivityStatus status);
    
    /**
     * Get activities by supervisor
     */
    List<ExtracurricularActivityResponse> getActivitiesBySupervisor(Long supervisorId);
    
    /**
     * Get activities by date range
     */
    List<ExtracurricularActivityResponse> getActivitiesByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get activities by specific date
     */
    List<ExtracurricularActivityResponse> getActivitiesByDate(LocalDate date);
    
    /**
     * Get upcoming activities
     */
    List<ExtracurricularActivityResponse> getUpcomingActivities();
    
    /**
     * Get today's activities
     */
    List<ExtracurricularActivityResponse> getTodaysActivities();
    
    /**
     * Get activities open for registration
     */
    List<ExtracurricularActivityResponse> getActivitiesOpenForRegistration();
    
    /**
     * Get mandatory activities
     */
    List<ExtracurricularActivityResponse> getMandatoryActivities();
    
    /**
     * Get activities requiring permission
     */
    List<ExtracurricularActivityResponse> getActivitiesRequiringPermission();
    
    // Student Registration Management
    
    /**
     * Register student for activity
     */
    ExtracurricularActivityResponse registerStudentForActivity(Long activityId, Long studentId);
    
    /**
     * Unregister student from activity
     */
    ExtracurricularActivityResponse unregisterStudentFromActivity(Long activityId, Long studentId);
    
    /**
     * Get activities for a specific student
     */
    List<ExtracurricularActivityResponse> getStudentActivities(Long studentId);
    
    /**
     * Get student's upcoming activities
     */
    List<ExtracurricularActivityResponse> getStudentUpcomingActivities(Long studentId);
    
    /**
     * Get student's completed activities
     */
    List<ExtracurricularActivityResponse> getStudentCompletedActivities(Long studentId);
    
    /**
     * Check if student is registered for activity
     */
    Boolean isStudentRegistered(Long activityId, Long studentId);
    
    /**
     * Get registration information for student
     */
    ExtracurricularActivityResponse.RegistrationInfo getRegistrationInfo(Long activityId, Long studentId);
    
    // Activity Status Management
    
    /**
     * Update activity status
     */
    ExtracurricularActivityResponse updateActivityStatus(Long activityId, ActivityStatus status);
    
    /**
     * Open activity for registration
     */
    ExtracurricularActivityResponse openForRegistration(Long activityId);
    
    /**
     * Close activity registration
     */
    ExtracurricularActivityResponse closeRegistration(Long activityId);
    
    /**
     * Start activity (mark as in progress)
     */
    ExtracurricularActivityResponse startActivity(Long activityId);
    
    /**
     * Complete activity
     */
    ExtracurricularActivityResponse completeActivity(Long activityId);
    
    /**
     * Cancel activity
     */
    ExtracurricularActivityResponse cancelActivity(Long activityId, String reason);
    
    /**
     * Postpone activity
     */
    ExtracurricularActivityResponse postponeActivity(Long activityId, LocalDate newDate, String reason);
    
    // Validation and Conflict Detection
    
    /**
     * Check for activity conflicts
     */
    List<Map<String, Object>> checkActivityConflicts(CreateExtracurricularActivityRequest request);
    
    /**
     * Check for activity conflicts during update
     */
    List<Map<String, Object>> checkActivityConflicts(Long activityId, UpdateExtracurricularActivityRequest request);
    
    /**
     * Validate activity constraints
     */
    Map<String, Object> validateActivityConstraints(CreateExtracurricularActivityRequest request);
    
    /**
     * Validate activity constraints for update
     */
    Map<String, Object> validateActivityConstraints(Long activityId, UpdateExtracurricularActivityRequest request);
    
    /**
     * Check supervisor availability
     */
    Map<String, Object> checkSupervisorAvailability(Long supervisorId, LocalDate date, 
                                                   LocalTime startTime, LocalTime endTime);
    
    /**
     * Check location availability
     */
    Map<String, Object> checkLocationAvailability(String location, LocalDate date, 
                                                 LocalTime startTime, LocalTime endTime);
    
    // Statistics and Reporting
    
    /**
     * Get activity statistics
     */
    ExtracurricularActivityResponse.ActivityStatistics getActivityStatistics();
    
    /**
     * Get activity statistics by type
     */
    Map<ActivityType, Long> getActivityStatisticsByType();
    
    /**
     * Get activity statistics by status
     */
    Map<ActivityStatus, Long> getActivityStatisticsByStatus();
    
    /**
     * Get supervisor workload analysis
     */
    Map<String, Object> getSupervisorWorkloadAnalysis(Long supervisorId);
    
    /**
     * Get popular activities
     */
    List<ExtracurricularActivityResponse> getPopularActivities(int limit);
    
    /**
     * Get activity participation trends
     */
    Map<String, Object> getActivityParticipationTrends(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get department activity summary
     */
    Map<String, Object> getDepartmentActivitySummary(Long departmentId);
    
    // Notification and Communication
    
    /**
     * Send activity notifications
     */
    List<Map<String, Object>> sendActivityNotifications(Long activityId, String notificationType, String message);
    
    /**
     * Send registration reminders
     */
    List<Map<String, Object>> sendRegistrationReminders(Long activityId);
    
    /**
     * Send activity updates
     */
    List<Map<String, Object>> sendActivityUpdates(Long activityId, String updateMessage);
    
    // Bulk Operations
    
    /**
     * Create multiple activities
     */
    List<ExtracurricularActivityResponse> createBulkActivities(List<CreateExtracurricularActivityRequest> requests);
    
    /**
     * Update multiple activities
     */
    List<ExtracurricularActivityResponse> updateBulkActivities(Map<Long, UpdateExtracurricularActivityRequest> updates);
    
    /**
     * Register multiple students for activity
     */
    ExtracurricularActivityResponse registerMultipleStudents(Long activityId, List<Long> studentIds);
    
    /**
     * Unregister multiple students from activity
     */
    ExtracurricularActivityResponse unregisterMultipleStudents(Long activityId, List<Long> studentIds);
    
    // Import/Export
    
    /**
     * Export activities to Excel
     */
    byte[] exportActivitiesToExcel(ExtracurricularActivitySearchRequest searchRequest);
    
    /**
     * Export activity participants to Excel
     */
    byte[] exportActivityParticipantsToExcel(Long activityId);
    
    /**
     * Import activities from Excel
     */
    List<ExtracurricularActivityResponse> importActivitiesFromExcel(byte[] excelData);
    
    // Academic Integration
    
    /**
     * Get activities by academic year and semester
     */
    List<ExtracurricularActivityResponse> getActivitiesByAcademicPeriod(String academicYear, Integer semester);
    
    /**
     * Clone activities to new academic period
     */
    List<ExtracurricularActivityResponse> cloneActivitiesToNewPeriod(String fromAcademicYear, Integer fromSemester,
                                                                    String toAcademicYear, Integer toSemester);
    
    /**
     * Archive old activities
     */
    void archiveOldActivities(String academicYear);
    
    // Utility Methods
    
    /**
     * Get activities with upcoming deadlines
     */
    List<ExtracurricularActivityResponse> getActivitiesWithUpcomingDeadlines(int days);
    
    /**
     * Get activities with available spots
     */
    List<ExtracurricularActivityResponse> getActivitiesWithAvailableSpots();
    
    /**
     * Search activities by name
     */
    List<ExtracurricularActivityResponse> searchActivitiesByName(String name);
    
    /**
     * Search activities by location
     */
    List<ExtracurricularActivityResponse> searchActivitiesByLocation(String location);
    
    /**
     * Get activity calendar data
     */
    Map<String, Object> getActivityCalendarData(LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate activity report
     */
    Map<String, Object> generateActivityReport(ExtracurricularActivitySearchRequest searchRequest);
}