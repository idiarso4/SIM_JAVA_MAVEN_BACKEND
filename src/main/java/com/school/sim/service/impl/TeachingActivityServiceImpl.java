package com.school.sim.service.impl;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.TeachingActivityResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.TeachingActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TeachingActivityService
 * Provides comprehensive teaching activity management functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeachingActivityServiceImpl implements TeachingActivityService {

    private final TeachingActivityRepository teachingActivityRepository;
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public TeachingActivityResponse createTeachingActivity(CreateTeachingActivityRequest request) {
        log.info("Creating new teaching activity for schedule: {}, date: {}", request.getScheduleId(), request.getDate());

        // Validate and get schedule
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + request.getScheduleId()));

        // Validate time constraints
        validateActivityTime(request.getStartTime(), request.getEndTime());

        // Check for conflicts
        List<Map<String, Object>> conflicts = validateActivityScheduleConflicts(request);
        if (!conflicts.isEmpty()) {
            throw new ValidationException("Teaching activity conflicts detected: " + conflicts.size() + " conflicts found");
        }

        // Create teaching activity entity
        TeachingActivity activity = new TeachingActivity();
        activity.setSchedule(schedule);
        activity.setDate(request.getDate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setTopic(request.getTopic());
        activity.setDescription(request.getDescription());
        activity.setNotes(request.getNotes());
        activity.setIsCompleted(request.getIsCompleted());

        // Use schedule defaults or overrides
        activity.setSubject(getSubjectForActivity(request.getSubjectId(), schedule));
        activity.setClassRoom(getClassRoomForActivity(request.getClassRoomId(), schedule));
        activity.setTeacher(getTeacherForActivity(request.getTeacherId(), schedule));

        TeachingActivity savedActivity = teachingActivityRepository.save(activity);
        
        log.info("Teaching activity created successfully with id: {}", savedActivity.getId());
        return mapToTeachingActivityResponse(savedActivity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public TeachingActivityResponse updateTeachingActivity(Long activityId, UpdateTeachingActivityRequest request) {
        log.info("Updating teaching activity with id: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        // Update fields if provided
        if (request.getDate() != null) {
            activity.setDate(request.getDate());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getTopic() != null) {
            activity.setTopic(request.getTopic());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getNotes() != null) {
            activity.setNotes(request.getNotes());
        }
        if (request.getIsCompleted() != null) {
            activity.setIsCompleted(request.getIsCompleted());
        }

        // Update related entities if provided
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
            activity.setSubject(subject);
        }
        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + request.getClassRoomId()));
            activity.setClassRoom(classRoom);
        }
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + request.getTeacherId()));
            activity.setTeacher(teacher);
        }

        // Validate updated time if both are provided
        if (request.getStartTime() != null && request.getEndTime() != null) {
            validateActivityTime(request.getStartTime(), request.getEndTime());
        }

        TeachingActivity updatedActivity = teachingActivityRepository.save(activity);
        log.info("Teaching activity updated successfully with id: {}", updatedActivity.getId());
        
        return mapToTeachingActivityResponse(updatedActivity);
    }

    @Override
    @Cacheable(value = "teachingActivities", key = "#activityId")
    public TeachingActivityResponse getTeachingActivityById(Long activityId) {
        log.info("Retrieving teaching activity with id: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        return mapToTeachingActivityResponse(activity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public void deleteTeachingActivity(Long activityId) {
        log.info("Deleting teaching activity with id: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        teachingActivityRepository.delete(activity);
        log.info("Teaching activity deleted successfully with id: {}", activityId);
    }

    @Override
    public Page<TeachingActivityResponse> searchTeachingActivities(TeachingActivitySearchRequest request, Pageable pageable) {
        log.info("Searching teaching activities with criteria: {}", request);

        // This would typically use Specification pattern for complex queries
        List<TeachingActivity> activities = teachingActivityRepository.findAll();
        
        // Apply filters
        List<TeachingActivity> filteredActivities = activities.stream()
                .filter(activity -> {
                    if (request.getScheduleId() != null && !activity.getSchedule().getId().equals(request.getScheduleId())) {
                        return false;
                    }
                    if (request.getSubjectId() != null && !activity.getSubject().getId().equals(request.getSubjectId())) {
                        return false;
                    }
                    if (request.getClassRoomId() != null && !activity.getClassRoom().getId().equals(request.getClassRoomId())) {
                        return false;
                    }
                    if (request.getTeacherId() != null && !activity.getTeacher().getId().equals(request.getTeacherId())) {
                        return false;
                    }
                    if (request.getDate() != null && !activity.getDate().equals(request.getDate())) {
                        return false;
                    }
                    if (request.getStartDate() != null && activity.getDate().isBefore(request.getStartDate())) {
                        return false;
                    }
                    if (request.getEndDate() != null && activity.getDate().isAfter(request.getEndDate())) {
                        return false;
                    }
                    if (request.getIsCompleted() != null && !activity.getIsCompleted().equals(request.getIsCompleted())) {
                        return false;
                    }
                    if (request.getTopic() != null && !activity.getTopic().toLowerCase().contains(request.getTopic().toLowerCase())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<TeachingActivityResponse> responses = filteredActivities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<TeachingActivityResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Cacheable(value = "teachingActivities", key = "'teacher-' + #teacherId + '-' + #startDate + '-' + #endDate")
    public List<TeachingActivityResponse> getTeachingActivitiesByTeacher(Long teacherId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving teaching activities for teacher: {}, period: {} to {}", teacherId, startDate, endDate);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<TeachingActivity> activities = teachingActivityRepository.findByTeacherAndDateBetween(teacher, startDate, endDate);

        return activities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "teachingActivities", key = "'classroom-' + #classRoomId + '-' + #startDate + '-' + #endDate")
    public List<TeachingActivityResponse> getTeachingActivitiesByClassRoom(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving teaching activities for classroom: {}, period: {} to {}", classRoomId, startDate, endDate);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<TeachingActivity> activities = teachingActivityRepository.findByClassRoomAndDateBetween(classRoom, startDate, endDate);

        return activities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "teachingActivities", key = "'subject-' + #subjectId + '-' + #startDate + '-' + #endDate")
    public List<TeachingActivityResponse> getTeachingActivitiesBySubject(Long subjectId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving teaching activities for subject: {}, period: {} to {}", subjectId, startDate, endDate);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        List<TeachingActivity> activities = teachingActivityRepository.findBySubjectAndDateBetween(subject, startDate, endDate);

        return activities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeachingActivityResponse> getTeachingActivitiesByDate(LocalDate date) {
        log.info("Retrieving teaching activities for date: {}", date);

        List<TeachingActivity> activities = teachingActivityRepository.findAll().stream()
                .filter(activity -> activity.getDate().equals(date))
                .collect(Collectors.toList());

        return activities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeachingActivityResponse> getTodaysActivitiesForTeacher(Long teacherId) {
        log.info("Retrieving today's activities for teacher: {}", teacherId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<TeachingActivity> activities = teachingActivityRepository.findByTeacherAndDate(teacher, LocalDate.now());

        return activities.stream()
                .map(this::mapToTeachingActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeachingActivityResponse> getUpcomingActivitiesForTeacher(Long teacherId, Integer days) {
        log.info("Retrieving upcoming activities for teacher: {} for next {} days", teacherId, days);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return getTeachingActivitiesByTeacher(teacherId, startDate, endDate);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public TeachingActivityResponse markActivityAsCompleted(Long activityId, String notes) {
        log.info("Marking teaching activity as completed: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        activity.setIsCompleted(true);
        if (notes != null) {
            activity.setNotes(notes);
        }

        TeachingActivity updatedActivity = teachingActivityRepository.save(activity);
        return mapToTeachingActivityResponse(updatedActivity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public TeachingActivityResponse markActivityAsIncomplete(Long activityId) {
        log.info("Marking teaching activity as incomplete: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        activity.setIsCompleted(false);

        TeachingActivity updatedActivity = teachingActivityRepository.save(activity);
        return mapToTeachingActivityResponse(updatedActivity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"teachingActivities", "activityStatistics"}, allEntries = true)
    public TeachingActivityResponse generateActivityFromSchedule(Long scheduleId, LocalDate date, String topic) {
        log.info("Generating teaching activity from schedule: {} for date: {}", scheduleId, date);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Check if activity already exists for this schedule and date
        Optional<TeachingActivity> existingActivity = teachingActivityRepository.findByScheduleIdAndDate(scheduleId, date);
        if (existingActivity.isPresent()) {
            throw new ValidationException("Teaching activity already exists for this schedule and date");
        }

        CreateTeachingActivityRequest request = CreateTeachingActivityRequest.builder()
                .scheduleId(scheduleId)
                .date(date)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .topic(topic)
                .isCompleted(false)
                .build();

        return createTeachingActivity(request);
    }

    @Override
    public TeachingActivityResponse.AttendanceSummary getAttendanceSummary(Long activityId) {
        log.info("Getting attendance summary for teaching activity: {}", activityId);

        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        List<Attendance> attendances = activity.getAttendances();
        
        if (attendances == null || attendances.isEmpty()) {
            return TeachingActivityResponse.AttendanceSummary.builder()
                    .totalStudents(0)
                    .presentCount(0)
                    .absentCount(0)
                    .lateCount(0)
                    .sickCount(0)
                    .permitCount(0)
                    .attendanceRate(0.0)
                    .isAttendanceRecorded(false)
                    .build();
        }

        int totalStudents = attendances.size();
        int presentCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int absentCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        int lateCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        int sickCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.SICK).count();
        int permitCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PERMIT).count();
        
        double attendanceRate = totalStudents > 0 ? (double) presentCount / totalStudents * 100 : 0.0;

        return TeachingActivityResponse.AttendanceSummary.builder()
                .totalStudents(totalStudents)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .sickCount(sickCount)
                .permitCount(permitCount)
                .attendanceRate(attendanceRate)
                .isAttendanceRecorded(true)
                .build();
    }

    @Override
    public Boolean isAttendanceRecorded(Long activityId) {
        TeachingActivity activity = teachingActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with id: " + activityId));

        return activity.getAttendances() != null && !activity.getAttendances().isEmpty();
    }

    // Helper methods
    private void validateActivityTime(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new ValidationException("Start time and end time are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Start time must be before end time");
        }
        if (ChronoUnit.MINUTES.between(startTime, endTime) < 30) {
            throw new ValidationException("Teaching activity must be at least 30 minutes long");
        }
    }

    private Subject getSubjectForActivity(Long subjectId, Schedule schedule) {
        if (subjectId != null) {
            return subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
        }
        return schedule.getSubject();
    }

    private ClassRoom getClassRoomForActivity(Long classRoomId, Schedule schedule) {
        if (classRoomId != null) {
            return classRoomRepository.findById(classRoomId)
                    .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));
        }
        return schedule.getClassRoom();
    }

    private User getTeacherForActivity(Long teacherId, Schedule schedule) {
        if (teacherId != null) {
            return userRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        }
        return schedule.getTeacher();
    }

    private TeachingActivityResponse mapToTeachingActivityResponse(TeachingActivity activity) {
        return TeachingActivityResponse.builder()
                .id(activity.getId())
                .date(activity.getDate())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .timeSlot(activity.getStartTime() + " - " + activity.getEndTime())
                .duration((int) ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime()))
                .topic(activity.getTopic())
                .description(activity.getDescription())
                .notes(activity.getNotes())
                .isCompleted(activity.getIsCompleted())
                .schedule(TeachingActivityResponse.ScheduleInfo.builder()
                        .id(activity.getSchedule().getId())
                        .dayOfWeek(activity.getSchedule().getDayOfWeek().name())
                        .academicYear(activity.getSchedule().getAcademicYear())
                        .semester(activity.getSchedule().getSemester())
                        .isActive(activity.getSchedule().getIsActive())
                        .build())
                .subject(TeachingActivityResponse.SubjectInfo.builder()
                        .id(activity.getSubject().getId())
                        .name(activity.getSubject().getName())
                        .code(activity.getSubject().getCode())
                        .credits(activity.getSubject().getCredits())
                        .build())
                .classRoom(TeachingActivityResponse.ClassRoomInfo.builder()
                        .id(activity.getClassRoom().getId())
                        .name(activity.getClassRoom().getName())
                        .code(activity.getClassRoom().getCode())
                        .capacity(activity.getClassRoom().getCapacity())
                        .location(activity.getClassRoom().getLocation())
                        .build())
                .teacher(TeachingActivityResponse.TeacherInfo.builder()
                        .id(activity.getTeacher().getId())
                        .firstName(activity.getTeacher().getFirstName())
                        .lastName(activity.getTeacher().getLastName())
                        .fullName(activity.getTeacher().getFirstName() + " " + activity.getTeacher().getLastName())
                        .username(activity.getTeacher().getUsername())
                        .email(activity.getTeacher().getEmail())
                        .build())
                .attendanceSummary(getAttendanceSummary(activity.getId()))
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    // Placeholder implementations for remaining methods
    @Override
    public List<TeachingActivityResponse> bulkGenerateActivitiesFromSchedules(List<Long> scheduleIds, 
                                                                             LocalDate startDate, 
                                                                             LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getTeacherActivityStatistics(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getClassRoomActivityStatistics(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getSubjectActivityStatistics(Long subjectId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public List<TeachingActivityResponse> getActivitiesWithPendingAttendance(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public List<TeachingActivityResponse> getCompletedActivities(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getTeachingWorkloadAnalysis(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getClassRoomUtilizationAnalysis(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateActivityReport(TeachingActivitySearchRequest criteria) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> validateActivityScheduleConflicts(CreateTeachingActivityRequest request) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getActivityNotifications(Long teacherId) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void sendActivityReminders(LocalDate date) {
        // Implementation would send reminders
    }

    @Override
    @Transactional
    public void archiveOldActivities(LocalDate beforeDate) {
        // Implementation would archive old activities
    }

    @Override
    public Map<String, Object> getTeacherActivityCalendar(Long teacherId, Integer year, Integer month) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getClassRoomActivityCalendar(Long classRoomId, Integer year, Integer month) {
        return new HashMap<>();
    }

    @Override
    public byte[] exportTeachingActivities(TeachingActivitySearchRequest criteria, String format) {
        return new byte[0];
    }

    @Override
    @Transactional
    public List<TeachingActivityResponse> importTeachingActivities(byte[] fileData) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getActivityPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getActivityTrends(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    @Transactional
    public TeachingActivityResponse rescheduleActivity(Long activityId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime) {
        UpdateTeachingActivityRequest request = UpdateTeachingActivityRequest.builder()
                .date(newDate)
                .startTime(newStartTime)
                .endTime(newEndTime)
                .build();
        return updateTeachingActivity(activityId, request);
    }

    @Override
    @Transactional
    public void cancelActivity(Long activityId, String reason) {
        // Implementation would mark activity as cancelled
        deleteTeachingActivity(activityId);
    }

    @Override
    public List<TeachingActivityResponse> getCancelledActivities(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }
}