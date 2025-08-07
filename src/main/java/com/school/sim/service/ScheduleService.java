package com.school.sim.service;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.ScheduleResponse;
import com.school.sim.dto.response.TimetableResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for schedule management functionality
 * Handles timetable management, conflict detection, and availability checking
 */
public interface ScheduleService {

    /**
     * Create a new schedule
     */
    ScheduleResponse createSchedule(CreateScheduleRequest request);

    /**
     * Update an existing schedule
     */
    ScheduleResponse updateSchedule(Long scheduleId, UpdateScheduleRequest request);

    /**
     * Get schedule by ID
     */
    ScheduleResponse getScheduleById(Long scheduleId);

    /**
     * Delete schedule
     */
    void deleteSchedule(Long scheduleId);

    /**
     * Search and filter schedules
     */
    Page<ScheduleResponse> searchSchedules(ScheduleSearchRequest request, Pageable pageable);

    /**
     * Create multiple schedules in bulk
     */
    List<ScheduleResponse> createBulkSchedules(BulkScheduleRequest request);

    /**
     * Get schedules by class room
     */
    List<ScheduleResponse> getSchedulesByClassRoom(Long classRoomId, String academicYear, Integer semester);

    /**
     * Get schedules by teacher
     */
    List<ScheduleResponse> getSchedulesByTeacher(Long teacherId, String academicYear, Integer semester);

    /**
     * Get schedules by subject
     */
    List<ScheduleResponse> getSchedulesBySubject(Long subjectId, String academicYear, Integer semester);

    /**
     * Generate class timetable
     */
    TimetableResponse generateClassTimetable(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate teacher timetable
     */
    TimetableResponse generateTeacherTimetable(Long teacherId, String academicYear, Integer semester);

    /**
     * Generate subject timetable
     */
    TimetableResponse generateSubjectTimetable(Long subjectId, String academicYear, Integer semester);

    /**
     * Check for schedule conflicts
     */
    List<Map<String, Object>> checkScheduleConflicts(CreateScheduleRequest request);

    /**
     * Check for schedule conflicts when updating
     */
    List<Map<String, Object>> checkScheduleConflicts(Long scheduleId, UpdateScheduleRequest request);

    /**
     * Detect conflicts for existing schedules
     */
    List<Map<String, Object>> detectExistingConflicts(String academicYear, Integer semester);

    /**
     * Check teacher availability
     */
    Map<String, Object> checkTeacherAvailability(Long teacherId, DayOfWeek dayOfWeek, 
                                                LocalTime startTime, LocalTime endTime, 
                                                String academicYear, Integer semester);

    /**
     * Check classroom availability
     */
    Map<String, Object> checkClassroomAvailability(Long classRoomId, DayOfWeek dayOfWeek, 
                                                   LocalTime startTime, LocalTime endTime, 
                                                   String academicYear, Integer semester);

    /**
     * Get teacher's weekly schedule
     */
    Map<DayOfWeek, List<ScheduleResponse>> getTeacherWeeklySchedule(Long teacherId, 
                                                                   String academicYear, 
                                                                   Integer semester);

    /**
     * Get classroom's weekly schedule
     */
    Map<DayOfWeek, List<ScheduleResponse>> getClassroomWeeklySchedule(Long classRoomId, 
                                                                     String academicYear, 
                                                                     Integer semester);

    /**
     * Validate schedule constraints
     */
    Map<String, Object> validateScheduleConstraints(CreateScheduleRequest request);

    /**
     * Validate schedule constraints for update
     */
    Map<String, Object> validateScheduleConstraints(Long scheduleId, UpdateScheduleRequest request);

    /**
     * Get available time slots for a teacher
     */
    List<Map<String, Object>> getAvailableTimeSlots(Long teacherId, DayOfWeek dayOfWeek, 
                                                    String academicYear, Integer semester);

    /**
     * Get available time slots for a classroom
     */
    List<Map<String, Object>> getAvailableClassroomTimeSlots(Long classRoomId, DayOfWeek dayOfWeek, 
                                                            String academicYear, Integer semester);

    /**
     * Generate optimal schedule suggestions
     */
    List<Map<String, Object>> generateScheduleSuggestions(Long classRoomId, Long subjectId, 
                                                          Long teacherId, String academicYear, 
                                                          Integer semester);

    /**
     * Resolve schedule conflicts
     */
    List<ScheduleResponse> resolveScheduleConflicts(List<Long> conflictingScheduleIds, 
                                                   String resolutionStrategy);

    /**
     * Get schedule statistics
     */
    Map<String, Object> getScheduleStatistics(String academicYear, Integer semester);

    /**
     * Get teacher workload analysis
     */
    Map<String, Object> getTeacherWorkloadAnalysis(Long teacherId, String academicYear, Integer semester);

    /**
     * Get classroom utilization analysis
     */
    Map<String, Object> getClassroomUtilizationAnalysis(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate schedule optimization report
     */
    Map<String, Object> generateScheduleOptimizationReport(String academicYear, Integer semester);

    /**
     * Clone schedule to another academic period
     */
    List<ScheduleResponse> cloneSchedule(String fromAcademicYear, Integer fromSemester, 
                                        String toAcademicYear, Integer toSemester);

    /**
     * Clone class schedule to another class
     */
    List<ScheduleResponse> cloneClassSchedule(Long fromClassRoomId, Long toClassRoomId, 
                                             String academicYear, Integer semester);

    /**
     * Archive old schedules
     */
    void archiveOldSchedules(String academicYear);

    /**
     * Get schedule conflicts summary
     */
    Map<String, Object> getScheduleConflictsSummary(String academicYear, Integer semester);

    /**
     * Generate schedule change notifications
     */
    List<Map<String, Object>> generateScheduleChangeNotifications(Long scheduleId, 
                                                                 String changeType, 
                                                                 String changeDescription);

    /**
     * Validate time slot format
     */
    Boolean validateTimeSlot(LocalTime startTime, LocalTime endTime);

    /**
     * Calculate schedule duration
     */
    Integer calculateScheduleDuration(LocalTime startTime, LocalTime endTime);

    /**
     * Get overlapping schedules
     */
    List<ScheduleResponse> getOverlappingSchedules(DayOfWeek dayOfWeek, LocalTime startTime, 
                                                  LocalTime endTime, String academicYear, 
                                                  Integer semester);

    /**
     * Get free periods for a class
     */
    List<Map<String, Object>> getFreePeriods(Long classRoomId, DayOfWeek dayOfWeek, 
                                            String academicYear, Integer semester);

    /**
     * Get teacher's free periods
     */
    List<Map<String, Object>> getTeacherFreePeriods(Long teacherId, DayOfWeek dayOfWeek, 
                                                   String academicYear, Integer semester);

    /**
     * Generate schedule density report
     */
    Map<String, Object> generateScheduleDensityReport(String academicYear, Integer semester);

    /**
     * Get peak hours analysis
     */
    Map<String, Object> getPeakHoursAnalysis(String academicYear, Integer semester);

    /**
     * Generate schedule efficiency metrics
     */
    Map<String, Object> generateScheduleEfficiencyMetrics(String academicYear, Integer semester);

    /**
     * Get schedule change history
     */
    List<Map<String, Object>> getScheduleChangeHistory(Long scheduleId);

    /**
     * Validate academic period
     */
    Boolean validateAcademicPeriod(String academicYear, Integer semester);

    /**
     * Get schedule templates
     */
    List<Map<String, Object>> getScheduleTemplates();

    /**
     * Create schedule from template
     */
    List<ScheduleResponse> createScheduleFromTemplate(Long templateId, Long classRoomId, 
                                                     String academicYear, Integer semester);

    /**
     * Export schedule to various formats
     */
    byte[] exportSchedule(Long classRoomId, String academicYear, Integer semester, String format);

    /**
     * Import schedule from file
     */
    List<ScheduleResponse> importSchedule(byte[] fileData, String academicYear, Integer semester);

    /**
     * Generate schedule comparison report
     */
    Map<String, Object> generateScheduleComparisonReport(String academicYear1, Integer semester1, 
                                                        String academicYear2, Integer semester2);
}
