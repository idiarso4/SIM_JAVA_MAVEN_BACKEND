package com.school.sim.service.impl;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.ScheduleResponse;
import com.school.sim.dto.response.TimetableResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ScheduleService
 * Provides comprehensive schedule management and timetable functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    // Time slot constants
    private static final LocalTime SCHOOL_START_TIME = LocalTime.of(7, 0);
    private static final LocalTime SCHOOL_END_TIME = LocalTime.of(17, 0);
    private static final int MIN_SESSION_DURATION = 30; // minutes
    private static final int MAX_SESSION_DURATION = 180; // minutes

    @Override
    @Transactional
    @CacheEvict(value = { "schedules", "timetables" }, allEntries = true)
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        log.info("Creating new schedule for class: {}, subject: {}, teacher: {}",
                request.getClassRoomId(), request.getSubjectId(), request.getTeacherId());

        // Validate related entities
        ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ClassRoom not found with id: " + request.getClassRoomId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Teacher not found with id: " + request.getTeacherId()));

        // Validate schedule constraints
        validateScheduleData(request);

        // Check for conflicts if not skipped
        if (!request.getSkipConflictCheck()) {
            List<Map<String, Object>> conflicts = checkScheduleConflicts(request);
            if (!conflicts.isEmpty() && !request.getAllowOverlap()) {
                throw new ValidationException("Schedule conflicts detected: " + conflicts.size() + " conflicts found");
            }
        }

        // Create schedule entity
        Schedule schedule = new Schedule();
        schedule.setClassRoom(classRoom);
        schedule.setSubject(subject);
        schedule.setTeacher(teacher);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setAcademicYear(request.getAcademicYear());
        schedule.setSemester(request.getSemester());
        schedule.setNotes(request.getNotes());
        schedule.setIsActive(request.getIsActive());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Schedule created successfully with id: {}", savedSchedule.getId());
        return mapToScheduleResponse(savedSchedule);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "schedules", "timetables" }, allEntries = true)
    public ScheduleResponse updateSchedule(Long scheduleId, UpdateScheduleRequest request) {
        log.info("Updating schedule with id: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Update fields if provided
        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ClassRoom not found with id: " + request.getClassRoomId()));
            schedule.setClassRoom(classRoom);
        }
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subject not found with id: " + request.getSubjectId()));
            schedule.setSubject(subject);
        }
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Teacher not found with id: " + request.getTeacherId()));
            schedule.setTeacher(teacher);
        }
        if (request.getDayOfWeek() != null) {
            schedule.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getAcademicYear() != null) {
            schedule.setAcademicYear(request.getAcademicYear());
        }
        if (request.getSemester() != null) {
            schedule.setSemester(request.getSemester());
        }
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule updated successfully with id: {}", updatedSchedule.getId());

        return mapToScheduleResponse(updatedSchedule);
    }

    @Override
    @Cacheable(value = "schedules", key = "#scheduleId")
    public ScheduleResponse getScheduleById(Long scheduleId) {
        log.info("Retrieving schedule with id: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        return mapToScheduleResponse(schedule);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "schedules", "timetables" }, allEntries = true)
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule with id: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        scheduleRepository.delete(schedule);
        log.info("Schedule deleted successfully with id: {}", scheduleId);
    }

    @Override
    public Page<ScheduleResponse> searchSchedules(ScheduleSearchRequest request, Pageable pageable) {
        log.info("Searching schedules with criteria: {}", request);

        List<Schedule> schedules = scheduleRepository.findAll();

        // Apply basic filters
        List<Schedule> filteredSchedules = schedules.stream()
                .filter(schedule -> {
                    if (request.getClassRoomId() != null
                            && !schedule.getClassRoom().getId().equals(request.getClassRoomId())) {
                        return false;
                    }
                    if (request.getSubjectId() != null
                            && !schedule.getSubject().getId().equals(request.getSubjectId())) {
                        return false;
                    }
                    if (request.getTeacherId() != null
                            && !schedule.getTeacher().getId().equals(request.getTeacherId())) {
                        return false;
                    }
                    if (request.getAcademicYear() != null
                            && !schedule.getAcademicYear().equals(request.getAcademicYear())) {
                        return false;
                    }
                    if (request.getSemester() != null && !schedule.getSemester().equals(request.getSemester())) {
                        return false;
                    }
                    if (request.getDayOfWeek() != null && !schedule.getDayOfWeek().equals(request.getDayOfWeek())) {
                        return false;
                    }
                    if (request.getIsActive() != null && !schedule.getIsActive().equals(request.getIsActive())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<ScheduleResponse> responses = filteredSchedules.stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ScheduleResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "schedules", "timetables" }, allEntries = true)
    public List<ScheduleResponse> createBulkSchedules(BulkScheduleRequest request) {
        log.info("Creating {} schedules in bulk", request.getSchedules().size());

        List<ScheduleResponse> responses = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < request.getSchedules().size(); i++) {
            CreateScheduleRequest scheduleRequest = request.getSchedules().get(i);
            try {
                scheduleRequest.setSkipConflictCheck(request.getSkipConflictCheck());
                scheduleRequest.setAllowOverlap(request.getAllowOverlap());

                ScheduleResponse response = createSchedule(scheduleRequest);
                responses.add(response);
            } catch (Exception e) {
                String error = String.format("Error creating schedule %d: %s", i + 1, e.getMessage());
                errors.add(error);
                log.error(error, e);

                if (request.getStopOnFirstError()) {
                    throw new ValidationException("Bulk schedule creation failed: " + error);
                }
            }
        }

        log.info("Bulk schedule creation completed. Created: {}, Errors: {}", responses.size(), errors.size());
        return responses;
    }

    @Override
    @Cacheable(value = "schedules", key = "#classRoomId + '-' + #academicYear + '-' + #semester")
    public List<ScheduleResponse> getSchedulesByClassRoom(Long classRoomId, String academicYear, Integer semester) {
        log.info("Retrieving schedules for class room: {}, period: {}/{}", classRoomId, academicYear, semester);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<Schedule> schedules = scheduleRepository
                .findByClassRoomAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
                        classRoom, academicYear, semester);

        return schedules.stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "schedules", key = "'teacher-' + #teacherId + '-' + #academicYear + '-' + #semester")
    public List<ScheduleResponse> getSchedulesByTeacher(Long teacherId, String academicYear, Integer semester) {
        log.info("Retrieving schedules for teacher: {}, period: {}/{}", teacherId, academicYear, semester);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<Schedule> schedules = scheduleRepository
                .findByTeacherAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
                        teacher, academicYear, semester);

        return schedules.stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "schedules", key = "'subject-' + #subjectId + '-' + #academicYear + '-' + #semester")
    public List<ScheduleResponse> getSchedulesBySubject(Long subjectId, String academicYear, Integer semester) {
        log.info("Retrieving schedules for subject: {}, period: {}/{}", subjectId, academicYear, semester);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        List<Schedule> schedules = scheduleRepository
                .findBySubjectAndAcademicYearAndSemesterOrderByDayOfWeekAscStartTimeAsc(
                        subject, academicYear, semester);

        return schedules.stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "timetables", key = "'class-' + #classRoomId + '-' + #academicYear + '-' + #semester")
    public TimetableResponse generateClassTimetable(Long classRoomId, String academicYear, Integer semester) {
        log.info("Generating class timetable for class: {}, period: {}/{}", classRoomId, academicYear, semester);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<ScheduleResponse> schedules = getSchedulesByClassRoom(classRoomId, academicYear, semester);

        Map<DayOfWeek, List<TimetableResponse.TimeSlot>> weeklySchedule = buildWeeklySchedule(schedules);
        TimetableResponse.TimetableStatistics statistics = calculateTimetableStatistics(schedules);

        return TimetableResponse.builder()
                .title("Class Timetable")
                .type("CLASS")
                .academicYear(academicYear)
                .semester(semester)
                .entityId(classRoomId)
                .entityName(classRoom.getName())
                .entityCode(classRoom.getCode())
                .weeklySchedule(weeklySchedule)
                .statistics(statistics)
                .generatedAt(new Date().toString())
                .totalHours(statistics.getTotalHours())
                .totalSessions(statistics.getTotalSessions())
                .build();
    }

    @Override
    @Cacheable(value = "timetables", key = "'teacher-' + #teacherId + '-' + #academicYear + '-' + #semester")
    public TimetableResponse generateTeacherTimetable(Long teacherId, String academicYear, Integer semester) {
        log.info("Generating teacher timetable for teacher: {}, period: {}/{}", teacherId, academicYear, semester);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<ScheduleResponse> schedules = getSchedulesByTeacher(teacherId, academicYear, semester);

        Map<DayOfWeek, List<TimetableResponse.TimeSlot>> weeklySchedule = buildWeeklySchedule(schedules);
        TimetableResponse.TimetableStatistics statistics = calculateTimetableStatistics(schedules);

        return TimetableResponse.builder()
                .title("Teacher Timetable")
                .type("TEACHER")
                .academicYear(academicYear)
                .semester(semester)
                .entityId(teacherId)
                .entityName(teacher.getFirstName() + " " + teacher.getLastName())
                .entityCode(teacher.getUsername())
                .weeklySchedule(weeklySchedule)
                .statistics(statistics)
                .generatedAt(new Date().toString())
                .totalHours(statistics.getTotalHours())
                .totalSessions(statistics.getTotalSessions())
                .build();
    }

    @Override
    @Cacheable(value = "timetables", key = "'subject-' + #subjectId + '-' + #academicYear + '-' + #semester")
    public TimetableResponse generateSubjectTimetable(Long subjectId, String academicYear, Integer semester) {
        log.info("Generating subject timetable for subject: {}, period: {}/{}", subjectId, academicYear, semester);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        List<ScheduleResponse> schedules = getSchedulesBySubject(subjectId, academicYear, semester);

        Map<DayOfWeek, List<TimetableResponse.TimeSlot>> weeklySchedule = buildWeeklySchedule(schedules);
        TimetableResponse.TimetableStatistics statistics = calculateTimetableStatistics(schedules);

        return TimetableResponse.builder()
                .title("Subject Timetable")
                .type("SUBJECT")
                .academicYear(academicYear)
                .semester(semester)
                .entityId(subjectId)
                .entityName(subject.getName())
                .entityCode(subject.getCode())
                .weeklySchedule(weeklySchedule)
                .statistics(statistics)
                .generatedAt(new Date().toString())
                .totalHours(statistics.getTotalHours())
                .totalSessions(statistics.getTotalSessions())
                .build();
    }

    @Override
    public List<Map<String, Object>> checkScheduleConflicts(CreateScheduleRequest request) {
        log.info("Checking schedule conflicts for new schedule");

        List<Map<String, Object>> conflicts = new ArrayList<>();

        // Check teacher conflicts
        List<Schedule> teacherSchedules = scheduleRepository.findByTeacherAndDayOfWeekAndAcademicYearAndSemester(
                userRepository.findById(request.getTeacherId()).orElse(null),
                request.getDayOfWeek(),
                request.getAcademicYear(),
                request.getSemester());

        for (Schedule existingSchedule : teacherSchedules) {
            if (isTimeOverlap(request.getStartTime(), request.getEndTime(),
                    existingSchedule.getStartTime(), existingSchedule.getEndTime())) {
                conflicts.add(createConflictInfo("TEACHER_CONFLICT", existingSchedule,
                        "Teacher has another class at this time"));
            }
        }

        // Check classroom conflicts
        List<Schedule> classroomSchedules = scheduleRepository.findByClassRoomAndDayOfWeekAndAcademicYearAndSemester(
                classRoomRepository.findById(request.getClassRoomId()).orElse(null),
                request.getDayOfWeek(),
                request.getAcademicYear(),
                request.getSemester());

        for (Schedule existingSchedule : classroomSchedules) {
            if (isTimeOverlap(request.getStartTime(), request.getEndTime(),
                    existingSchedule.getStartTime(), existingSchedule.getEndTime())) {
                conflicts.add(createConflictInfo("CLASSROOM_CONFLICT", existingSchedule,
                        "Classroom is already occupied at this time"));
            }
        }

        return conflicts;
    }

    @Override
    public List<Map<String, Object>> checkScheduleConflicts(Long scheduleId, UpdateScheduleRequest request) {
        log.info("Checking schedule conflicts for schedule update: {}", scheduleId);

        Schedule currentSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Create a temporary request with current values and updated values
        CreateScheduleRequest tempRequest = CreateScheduleRequest.builder()
                .classRoomId(request.getClassRoomId() != null ? request.getClassRoomId()
                        : currentSchedule.getClassRoom().getId())
                .subjectId(
                        request.getSubjectId() != null ? request.getSubjectId() : currentSchedule.getSubject().getId())
                .teacherId(
                        request.getTeacherId() != null ? request.getTeacherId() : currentSchedule.getTeacher().getId())
                .dayOfWeek(request.getDayOfWeek() != null ? request.getDayOfWeek() : currentSchedule.getDayOfWeek())
                .startTime(request.getStartTime() != null ? request.getStartTime() : currentSchedule.getStartTime())
                .endTime(request.getEndTime() != null ? request.getEndTime() : currentSchedule.getEndTime())
                .academicYear(request.getAcademicYear() != null ? request.getAcademicYear()
                        : currentSchedule.getAcademicYear())
                .semester(request.getSemester() != null ? request.getSemester() : currentSchedule.getSemester())
                .build();

        List<Map<String, Object>> conflicts = checkScheduleConflicts(tempRequest);

        // Filter out conflicts with the current schedule itself
        return conflicts.stream()
                .filter(conflict -> !scheduleId.equals(conflict.get("conflictingScheduleId")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> detectExistingConflicts(String academicYear, Integer semester) {
        log.info("Detecting existing conflicts for period: {}/{}", academicYear, semester);

        List<Schedule> allSchedules = scheduleRepository.findByAcademicYearAndSemester(academicYear, semester);
        List<Map<String, Object>> conflicts = new ArrayList<>();

        for (int i = 0; i < allSchedules.size(); i++) {
            for (int j = i + 1; j < allSchedules.size(); j++) {
                Schedule schedule1 = allSchedules.get(i);
                Schedule schedule2 = allSchedules.get(j);

                if (schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek()) &&
                        isTimeOverlap(schedule1.getStartTime(), schedule1.getEndTime(),
                                schedule2.getStartTime(), schedule2.getEndTime())) {

                    // Check if same teacher or same classroom
                    if (schedule1.getTeacher().getId().equals(schedule2.getTeacher().getId())) {
                        conflicts.add(createConflictInfo("TEACHER_CONFLICT", schedule2,
                                "Teacher has conflicting schedules"));
                    }

                    if (schedule1.getClassRoom().getId().equals(schedule2.getClassRoom().getId())) {
                        conflicts.add(createConflictInfo("CLASSROOM_CONFLICT", schedule2,
                                "Classroom has conflicting schedules"));
                    }
                }
            }
        }

        return conflicts;
    }

    @Override
    public Map<String, Object> checkTeacherAvailability(Long teacherId, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime,
            String academicYear, Integer semester) {
        log.info("Checking teacher availability for teacher: {} on {}", teacherId, dayOfWeek);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<Schedule> existingSchedules = scheduleRepository.findByTeacherAndDayOfWeekAndAcademicYearAndSemester(
                teacher, dayOfWeek, academicYear, semester);

        boolean isAvailable = existingSchedules.stream()
                .noneMatch(schedule -> isTimeOverlap(startTime, endTime,
                        schedule.getStartTime(), schedule.getEndTime()));

        Map<String, Object> availability = new HashMap<>();
        availability.put("teacherId", teacherId);
        availability.put("teacherName", teacher.getFirstName() + " " + teacher.getLastName());
        availability.put("dayOfWeek", dayOfWeek);
        availability.put("requestedStartTime", startTime);
        availability.put("requestedEndTime", endTime);
        availability.put("isAvailable", isAvailable);
        availability.put("conflictingSchedules", existingSchedules.stream()
                .filter(schedule -> isTimeOverlap(startTime, endTime,
                        schedule.getStartTime(), schedule.getEndTime()))
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList()));

        return availability;
    }

    @Override
    public Map<String, Object> checkClassroomAvailability(Long classRoomId, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime,
            String academicYear, Integer semester) {
        log.info("Checking classroom availability for classroom: {} on {}", classRoomId, dayOfWeek);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<Schedule> existingSchedules = scheduleRepository.findByClassRoomAndDayOfWeekAndAcademicYearAndSemester(
                classRoom, dayOfWeek, academicYear, semester);

        boolean isAvailable = existingSchedules.stream()
                .noneMatch(schedule -> isTimeOverlap(startTime, endTime,
                        schedule.getStartTime(), schedule.getEndTime()));

        Map<String, Object> availability = new HashMap<>();
        availability.put("classRoomId", classRoomId);
        availability.put("classRoomName", classRoom.getName());
        availability.put("dayOfWeek", dayOfWeek);
        availability.put("requestedStartTime", startTime);
        availability.put("requestedEndTime", endTime);
        availability.put("isAvailable", isAvailable);
        availability.put("conflictingSchedules", existingSchedules.stream()
                .filter(schedule -> isTimeOverlap(startTime, endTime,
                        schedule.getStartTime(), schedule.getEndTime()))
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList()));

        return availability;
    }

    @Override
    public Map<DayOfWeek, List<ScheduleResponse>> getTeacherWeeklySchedule(Long teacherId,
            String academicYear,
            Integer semester) {
        log.info("Getting teacher weekly schedule for teacher: {}", teacherId);

        List<ScheduleResponse> schedules = getSchedulesByTeacher(teacherId, academicYear, semester);

        return schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResponse::getDayOfWeek));
    }

    @Override
    public Map<DayOfWeek, List<ScheduleResponse>> getClassroomWeeklySchedule(Long classRoomId,
            String academicYear,
            Integer semester) {
        log.info("Getting classroom weekly schedule for classroom: {}", classRoomId);

        List<ScheduleResponse> schedules = getSchedulesByClassRoom(classRoomId, academicYear, semester);

        return schedules.stream()
                .collect(Collectors.groupingBy(ScheduleResponse::getDayOfWeek));
    }

    @Override
    public Map<String, Object> validateScheduleConstraints(CreateScheduleRequest request) {
        log.info("Validating schedule constraints for new schedule");

        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate time slot
        if (!validateTimeSlot(request.getStartTime(), request.getEndTime())) {
            errors.add("Invalid time slot: start time must be before end time");
        }

        // Check if time is within school hours
        if (request.getStartTime().isBefore(SCHOOL_START_TIME) ||
                request.getEndTime().isAfter(SCHOOL_END_TIME)) {
            warnings.add("Schedule is outside normal school hours");
        }

        // Check session duration
        int duration = calculateScheduleDuration(request.getStartTime(), request.getEndTime());
        if (duration < MIN_SESSION_DURATION) {
            errors.add("Session duration is too short (minimum " + MIN_SESSION_DURATION + " minutes)");
        }
        if (duration > MAX_SESSION_DURATION) {
            warnings.add("Session duration is very long (maximum recommended " + MAX_SESSION_DURATION + " minutes)");
        }

        validation.put("isValid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("duration", duration);

        return validation;
    }

    @Override
    public Map<String, Object> validateScheduleConstraints(Long scheduleId, UpdateScheduleRequest request) {
        log.info("Validating schedule constraints for schedule update: {}", scheduleId);

        Schedule currentSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Create temporary request with merged values
        CreateScheduleRequest tempRequest = CreateScheduleRequest.builder()
                .startTime(request.getStartTime() != null ? request.getStartTime() : currentSchedule.getStartTime())
                .endTime(request.getEndTime() != null ? request.getEndTime() : currentSchedule.getEndTime())
                .academicYear(request.getAcademicYear() != null ? request.getAcademicYear()
                        : currentSchedule.getAcademicYear())
                .semester(request.getSemester() != null ? request.getSemester() : currentSchedule.getSemester())
                .build();

        return validateScheduleConstraints(tempRequest);
    }

    @Override
    public Boolean validateTimeSlot(LocalTime startTime, LocalTime endTime) {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    @Override
    public Integer calculateScheduleDuration(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(startTime, endTime);
    }

    @Override
    public Boolean validateAcademicPeriod(String academicYear, Integer semester) {
        if (academicYear == null || !academicYear.matches("\\d{4}/\\d{4}")) {
            return false;
        }
        return semester != null && semester >= 1 && semester <= 2;
    }

    // Helper methods
    private void validateScheduleData(CreateScheduleRequest request) {
        // Validate time slot
        if (!validateTimeSlot(request.getStartTime(), request.getEndTime())) {
            throw new ValidationException(
                    "Invalid time slot: start time must be before end time and within school hours");
        }

        // Validate academic year format
        if (!request.getAcademicYear().matches("\\d{4}/\\d{4}")) {
            throw new ValidationException("Academic year must be in format YYYY/YYYY");
        }

        // Validate semester
        if (request.getSemester() < 1 || request.getSemester() > 2) {
            throw new ValidationException("Semester must be 1 or 2");
        }
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private Map<String, Object> createConflictInfo(String type, Schedule conflictingSchedule, String description) {
        Map<String, Object> conflict = new HashMap<>();
        conflict.put("type", type);
        conflict.put("description", description);
        conflict.put("conflictingScheduleId", conflictingSchedule.getId());
        conflict.put("conflictingEntity", getConflictingEntityName(conflictingSchedule, type));
        conflict.put("conflictStartTime", conflictingSchedule.getStartTime());
        conflict.put("conflictEndTime", conflictingSchedule.getEndTime());
        conflict.put("severity", "HIGH");
        return conflict;
    }

    private String getConflictingEntityName(Schedule schedule, String conflictType) {
        switch (conflictType) {
            case "TEACHER_CONFLICT":
                return schedule.getTeacher().getFirstName() + " " + schedule.getTeacher().getLastName();
            case "CLASSROOM_CONFLICT":
                return schedule.getClassRoom().getName();
            default:
                return "Unknown";
        }
    }

    private ScheduleResponse mapToScheduleResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .dayName(schedule.getDayOfWeek().name())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .timeSlot(schedule.getStartTime() + " - " + schedule.getEndTime())
                .duration(calculateScheduleDuration(schedule.getStartTime(), schedule.getEndTime()))
                .classRoom(ScheduleResponse.ClassRoomInfo.builder()
                        .id(schedule.getClassRoom().getId())
                        .name(schedule.getClassRoom().getName())
                        .code(schedule.getClassRoom().getCode())
                        .capacity(schedule.getClassRoom().getCapacity())
                        .location(schedule.getClassRoom().getLocation())
                        .build())
                .subject(ScheduleResponse.SubjectInfo.builder()
                        .id(schedule.getSubject().getId())
                        .name(schedule.getSubject().getName())
                        .code(schedule.getSubject().getCode())
                        .credits(schedule.getSubject().getCredits())
                        .build())
                .teacher(ScheduleResponse.TeacherInfo.builder()
                        .id(schedule.getTeacher().getId())
                        .firstName(schedule.getTeacher().getFirstName())
                        .lastName(schedule.getTeacher().getLastName())
                        .fullName(schedule.getTeacher().getFirstName() + " " + schedule.getTeacher().getLastName())
                        .username(schedule.getTeacher().getUsername())
                        .email(schedule.getTeacher().getEmail())
                        .build())
                .academicYear(schedule.getAcademicYear())
                .semester(schedule.getSemester())
                .isActive(schedule.getIsActive())
                .notes(schedule.getNotes())
                .conflicts(new ArrayList<>()) // Simplified for now
                .hasConflicts(false)
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private Map<DayOfWeek, List<TimetableResponse.TimeSlot>> buildWeeklySchedule(List<ScheduleResponse> schedules) {
        Map<DayOfWeek, List<TimetableResponse.TimeSlot>> weeklySchedule = new HashMap<>();

        for (ScheduleResponse schedule : schedules) {
            DayOfWeek day = schedule.getDayOfWeek();
            weeklySchedule.computeIfAbsent(day, k -> new ArrayList<>());

            TimetableResponse.TimeSlot timeSlot = TimetableResponse.TimeSlot.builder()
                    .scheduleId(schedule.getId())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .timeRange(schedule.getTimeSlot())
                    .duration(schedule.getDuration())
                    .subjectName(schedule.getSubject().getName())
                    .subjectCode(schedule.getSubject().getCode())
                    .teacherName(schedule.getTeacher().getFullName())
                    .teacherCode(schedule.getTeacher().getUsername())
                    .classRoomName(schedule.getClassRoom().getName())
                    .classRoomCode(schedule.getClassRoom().getCode())
                    .notes(schedule.getNotes())
                    .isActive(schedule.getIsActive())
                    .conflicts(new ArrayList<>())
                    .build();

            weeklySchedule.get(day).add(timeSlot);
        }

        // Sort time slots by start time for each day
        weeklySchedule.values().forEach(
                daySchedule -> daySchedule.sort(Comparator.comparing(TimetableResponse.TimeSlot::getStartTime)));

        return weeklySchedule;
    }

    private TimetableResponse.TimetableStatistics calculateTimetableStatistics(List<ScheduleResponse> schedules) {
        int totalSessions = schedules.size();
        int totalHours = schedules.stream()
                .mapToInt(ScheduleResponse::getDuration)
                .sum() / 60; // Convert minutes to hours

        Map<DayOfWeek, Integer> sessionsByDay = schedules.stream()
                .collect(Collectors.groupingBy(
                        ScheduleResponse::getDayOfWeek,
                        Collectors.summingInt(s -> 1)));

        Map<String, Integer> subjectHours = schedules.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSubject().getName(),
                        Collectors.summingInt(s -> s.getDuration() / 60)));

        return TimetableResponse.TimetableStatistics.builder()
                .totalSessions(totalSessions)
                .totalHours(totalHours)
                .totalMinutes(schedules.stream().mapToInt(ScheduleResponse::getDuration).sum())
                .sessionsByDay(sessionsByDay)
                .subjectHours(subjectHours)
                .averageSessionsPerDay(totalSessions / 5.0) // Assuming 5 school days
                .conflictCount(0)
                .conflictSummary(new ArrayList<>())
                .build();
    }

    // Placeholder implementations for remaining methods
    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(Long teacherId, DayOfWeek dayOfWeek,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAvailableClassroomTimeSlots(Long classRoomId, DayOfWeek dayOfWeek,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> generateScheduleSuggestions(Long classRoomId, Long subjectId,
            Long teacherId, String academicYear,
            Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduleResponse> resolveScheduleConflicts(List<Long> conflictingScheduleIds,
            String resolutionStrategy) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getScheduleStatistics(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getTeacherWorkloadAnalysis(Long teacherId, String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getClassroomUtilizationAnalysis(Long classRoomId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateScheduleOptimizationReport(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    @Transactional
    public List<ScheduleResponse> cloneSchedule(String fromAcademicYear, Integer fromSemester,
            String toAcademicYear, Integer toSemester) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<ScheduleResponse> cloneClassSchedule(Long fromClassRoomId, Long toClassRoomId,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void archiveOldSchedules(String academicYear) {
        // Implementation would archive old schedules
    }

    @Override
    public Map<String, Object> getScheduleConflictsSummary(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateScheduleChangeNotifications(Long scheduleId,
            String changeType,
            String changeDescription) {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduleResponse> getOverlappingSchedules(DayOfWeek dayOfWeek, LocalTime startTime,
            LocalTime endTime, String academicYear,
            Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getFreePeriods(Long classRoomId, DayOfWeek dayOfWeek,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getTeacherFreePeriods(Long teacherId, DayOfWeek dayOfWeek,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateScheduleDensityReport(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getPeakHoursAnalysis(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateScheduleEfficiencyMetrics(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getScheduleChangeHistory(Long scheduleId) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getScheduleTemplates() {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<ScheduleResponse> createScheduleFromTemplate(Long templateId, Long classRoomId,
            String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public byte[] exportSchedule(Long classRoomId, String academicYear, Integer semester, String format) {
        return new byte[0];
    }

    @Override
    @Transactional
    public List<ScheduleResponse> importSchedule(byte[] fileData, String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateScheduleComparisonReport(String academicYear1, Integer semester1,
            String academicYear2, Integer semester2) {
        return new HashMap<>();
    }
}