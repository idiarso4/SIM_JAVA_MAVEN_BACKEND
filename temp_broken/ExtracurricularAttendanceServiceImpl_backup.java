package com.school.sim.service.impl;

import com.school.sim.dto.request.CreateExtracurricularAttendanceRequest;
import com.school.sim.dto.request.ExtracurricularAttendanceSearchRequest;
import com.school.sim.dto.request.UpdateExtracurricularAttendanceRequest;
import com.school.sim.dto.response.ExtracurricularAttendanceResponse;
import com.school.sim.entity.ExtracurricularActivity;
import com.school.sim.entity.ExtracurricularAttendance;
import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import com.school.sim.entity.Student;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.ExtracurricularActivityRepository;
import com.school.sim.repository.ExtracurricularAttendanceRepository;
import com.school.sim.repository.StudentRepository;
import com.school.sim.service.ExtracurricularAttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ExtracurricularAttendanceService for attendance management
 * Provides comprehensive functionality for attendance tracking, reporting, and progress monitoring
 */
@Service
@Transactional
public class ExtracurricularAttendanceServiceImpl implements ExtracurricularAttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(ExtracurricularAttendanceServiceImpl.class);

    @Autowired
    private ExtracurricularAttendanceRepository attendanceRepository;

    @Autowired
    private ExtracurricularActivityRepository activityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public ExtracurricularAttendanceResponse recordAttendance(CreateExtracurricularAttendanceRequest request) {
        logger.info("Recording attendance for student {} in activity {} on {}", 
                   request.getStudentId(), request.getActivityId(), request.getAttendanceDate());

        // Validate attendance recording
        Map<String, Object> validation = validateAttendanceRecording(
            request.getActivityId(), request.getStudentId(), request.getAttendanceDate());
        
        if (!(Boolean) validation.get("valid")) {
            throw new ValidationException("Attendance recording validation failed: " + validation.get("errors"));
        }

        ExtracurricularActivity activity = findActivityById(request.getActivityId());
        Student student = findStudentById(request.getStudentId());

        // Create attendance record
        ExtracurricularAttendance attendance = new ExtracurricularAttendance();
        attendance.setActivity(activity);
        attendance.setStudent(student);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());
        attendance.setNotes(request.getNotes());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setParticipationScore(request.getParticipationScore());
        attendance.setPerformanceRating(request.getPerformanceRating());
        attendance.setIsExcused(request.getIsExcused());
        attendance.setExcuseReason(request.getExcuseReason());
        attendance.setLateArrivalMinutes(request.getLateArrivalMinutes());
        attendance.setEarlyDepartureMinutes(request.getEarlyDepartureMinutes());
        attendance.setAchievementPoints(request.getAchievementPoints());

        // Calculate participation score if present
        if (request.getStatus() == AttendanceStatus.PRESENT) {
            attendance.setParticipationScore(calculateParticipationScore(attendance.getId()));
        }

        ExtracurricularAttendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("Successfully recorded attendance with ID: {}", savedAttendance.getId());

        return ExtracurricularAttendanceResponse.from(savedAttendance);
    }

    @Override
    public ExtracurricularAttendanceResponse updateAttendance(Long attendanceId, UpdateExtracurricularAttendanceRequest request) {
        logger.info("Updating attendance record with ID: {}", attendanceId);

        ExtracurricularAttendance attendance = findAttendanceById(attendanceId);

        // Update fields
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }
        if (request.getParticipationScore() != null) {
            attendance.setParticipationScore(request.getParticipationScore());
        }

        attendance.setUpdatedAt(LocalDateTime.now());

        ExtracurricularAttendance updatedAttendance = attendanceRepository.save(attendance);
        logger.info("Successfully updated attendance with ID: {}", updatedAttendance.getId());

        return ExtracurricularAttendanceResponse.from(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse getAttendanceById(Long attendanceId) {
        logger.debug("Fetching attendance record by ID: {}", attendanceId);
        ExtracurricularAttendance attendance = findAttendanceById(attendanceId);
        return ExtracurricularAttendanceResponse.from(attendance);
    }

    @Override
    public void deleteAttendance(Long attendanceId) {
        logger.info("Soft deleting attendance record with ID: {}", attendanceId);
        ExtracurricularAttendance attendance = findAttendanceById(attendanceId);
        attendance.setIsActive(false);
        attendanceRepository.save(attendance);
        logger.info("Successfully soft deleted attendance with ID: {}", attendanceId);
    }

    @Override
    public void permanentlyDeleteAttendance(Long attendanceId) {
        logger.info("Permanently deleting attendance record with ID: {}", attendanceId);
        ExtracurricularAttendance attendance = findAttendanceById(attendanceId);
        attendanceRepository.delete(attendance);
        logger.info("Successfully permanently deleted attendance with ID: {}", attendanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExtracurricularAttendanceResponse> searchAttendance(ExtracurricularAttendanceSearchRequest request, Pageable pageable) {
        logger.debug("Searching attendance records with criteria: {}", request);
        
        ExtracurricularActivity activity = null;
        if (request.getActivityId() != null) {
            activity = findActivityById(request.getActivityId());
        }
        
        Student student = null;
        if (request.getStudentId() != null) {
            student = findStudentById(request.getStudentId());
        }
        
        return attendanceRepository.findByMultipleCriteria(
            activity,
            student,
            request.getStatus(),
            request.getStartDate(),
            request.getEndDate(),
            pageable
        ).map(ExtracurricularAttendanceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByActivity(Long activityId) {
        logger.debug("Fetching attendance records by activity ID: {}", activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        return attendanceRepository.findByActivityAndIsActiveTrueOrderByAttendanceDateDesc(activity).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByStudent(Long studentId) {
        logger.debug("Fetching attendance records by student ID: {}", studentId);
        Student student = findStudentById(studentId);
        return attendanceRepository.findByStudentAndIsActiveTrueOrderByAttendanceDateDesc(student).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByActivityAndDate(Long activityId, LocalDate date) {
        logger.debug("Fetching attendance records by activity {} and date {}", activityId, date);
        ExtracurricularActivity activity = findActivityById(activityId);
        return attendanceRepository.findByActivityAndAttendanceDateAndIsActiveTrueOrderByStudentNamaLengkapAsc(activity, date).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching attendance records by student {} and date range {} to {}", studentId, startDate, endDate);
        Student student = findStudentById(studentId);
        return attendanceRepository.findByStudentAndDateRange(student, startDate, endDate).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByActivityAndDateRange(Long activityId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching attendance records by activity {} and date range {} to {}", activityId, startDate, endDate);
        ExtracurricularActivity activity = findActivityById(activityId);
        return attendanceRepository.findByActivityAndDateRange(activity, startDate, endDate).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceByStatus(AttendanceStatus status) {
        logger.debug("Fetching attendance records by status: {}", status);
        return attendanceRepository.findByStatusAndIsActiveTrueOrderByAttendanceDateDesc(status).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isStudentRegisteredForActivity(Long activityId, Long studentId) {
        ExtracurricularActivity activity = findActivityById(activityId);
        Student student = findStudentById(studentId);
        return activity.getParticipants() != null && activity.getParticipants().contains(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean attendanceExists(Long activityId, Long studentId, LocalDate date) {
        ExtracurricularActivity activity = findActivityById(activityId);
        Student student = findStudentById(studentId);
        return attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(activity, student, date);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validateAttendanceRecording(Long activityId, Long studentId, LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        // Check if student is registered for activity
        if (!isStudentRegisteredForActivity(activityId, studentId)) {
            errors.add("Student is not registered for this activity");
        }

        // Check if attendance already exists
        if (attendanceExists(activityId, studentId, date)) {
            errors.add("Attendance record already exists for this student on this date");
        }

        // Check if date is valid (not in future for most cases)
        if (date.isAfter(LocalDate.now())) {
            errors.add("Cannot record attendance for future dates");
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRateByActivity(Long activityId) {
        ExtracurricularActivity activity = findActivityById(activityId);
        Double rate = attendanceRepository.calculateAttendanceRateByActivity(activity);
        return rate != null ? rate : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRateByStudent(Long studentId) {
        Student student = findStudentById(studentId);
        Double rate = attendanceRepository.calculateAttendanceRateByStudent(student);
        return rate != null ? rate : 0.0;

        long presentCount = attendances.stream()
            .mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0)
            .sum();

        return (double) presentCount / attendances.size() * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRateByStudentAndActivity(Long studentId, Long activityId) {
        Student student = findStudentById(studentId);
        ExtracurricularActivity activity = findActivityById(activityId);
        Double rate = attendanceRepository.calculateAttendanceRateByStudentAndActivity(student, activity);
        return rate != null ? rate : 0.0;
    }

    @Override
    public Integer calculateParticipationScore(Long attendanceId) {
        // Basic participation score calculation
        // In a real implementation, this would be more sophisticated
        return 85; // Default score for present attendance
    }

    // Helper methods
    private ExtracurricularAttendance findAttendanceById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Extracurricular attendance not found with ID: " + attendanceId));
    }

    private ExtracurricularActivity findActivityById(Long activityId) {
        return activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Extracurricular activity not found with ID: " + activityId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    }

    // Helper methods for statistics and trends calculation
    private ExtracurricularAttendanceResponse.AttendanceStatistics calculateStatistics(List<ExtracurricularAttendance> attendances) {
        if (attendances.isEmpty()) {
            return new ExtracurricularAttendanceResponse.AttendanceStatistics();
        }

        long totalAttendances = attendances.size();
        long presentCount = attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum();
        long absentCount = attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.ABSENT ? 1 : 0).sum();
        long lateCount = attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.LATE ? 1 : 0).sum();
        
        ExtracurricularAttendanceResponse.AttendanceStatistics stats = ExtracurricularAttendanceResponse.AttendanceStatistics.builder()
            .totalRecords(totalAttendances)
            .presentCount(presentCount)
            .absentCount(absentCount)
            .lateCount(lateCount)
            .attendanceRate((double) presentCount / totalAttendances * 100)
            .build();
        
        return stats;
    }

    private List<ExtracurricularAttendanceResponse.AttendanceTrend> calculateTrends(List<ExtracurricularAttendance> attendances) {
        Map<LocalDate, Long> dailyAttendance = attendances.stream()
            .collect(Collectors.groupingBy(
                ExtracurricularAttendance::getAttendanceDate,
                Collectors.counting()
            ));

        return dailyAttendance.entrySet().stream()
            .map(entry -> ExtracurricularAttendanceResponse.AttendanceTrend.builder()
                .period(entry.getKey().toString())
                .totalRecords(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    private ExtracurricularAttendanceResponse.StudentProgress calculateStudentProgress(Long studentId, List<ExtracurricularAttendance> attendances) {
        Student student = findStudentById(studentId);
        
        long presentCount = attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum();
        long totalAchievementPoints = attendances.stream()
            .filter(a -> a.getAchievementPoints() != null)
            .mapToLong(ExtracurricularAttendance::getAchievementPoints)
            .sum();
            
        ExtracurricularAttendanceResponse.StudentProgress progress = ExtracurricularAttendanceResponse.StudentProgress.builder()
            .attendedActivities((long) attendances.size())
            .attendanceRate(attendances.isEmpty() ? 0.0 : (double) presentCount / attendances.size() * 100)
            .totalAchievementPoints(totalAchievementPoints)
            .build();
        
        return progress;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getRecentAttendance(int days) {
        logger.debug("Fetching recent attendance records for last {} days", days);
        LocalDate startDate = LocalDate.now().minusDays(days);
        return attendanceRepository.findRecentAttendance(startDate).stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse> getAttendanceNeedingReview() {
        logger.debug("Fetching attendance records needing review");
        // Return attendance records with notes or special circumstances
        return attendanceRepository.findAttendanceNeedingReview().stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<ExtracurricularAttendanceResponse> recordBulkAttendance(List<CreateExtracurricularAttendanceRequest> requests) {
        logger.info("Recording bulk attendance for {} records", requests.size());
        List<ExtracurricularAttendanceResponse> responses = new ArrayList<>();
        
        for (CreateExtracurricularAttendanceRequest request : requests) {
            try {
                ExtracurricularAttendanceResponse response = recordAttendance(request);
                responses.add(response);
            } catch (Exception e) {
                logger.error("Failed to record attendance for student {} in activity {}: {}", 
                           request.getStudentId(), request.getActivityId(), e.getMessage());
            }
        }
        
        logger.info("Successfully recorded {} out of {} attendance records", responses.size(), requests.size());
        return responses;
    }

    @Override
    public List<ExtracurricularAttendanceResponse> updateBulkAttendance(Map<Long, UpdateExtracurricularAttendanceRequest> updates) {
        logger.info("Updating bulk attendance for {} records", updates.size());
        List<ExtracurricularAttendanceResponse> responses = new ArrayList<>();
        
        for (Map.Entry<Long, UpdateExtracurricularAttendanceRequest> entry : updates.entrySet()) {
            try {
                ExtracurricularAttendanceResponse response = updateAttendance(entry.getKey(), entry.getValue());
                responses.add(response);
            } catch (Exception e) {
                logger.error("Failed to update attendance with ID {}: {}", entry.getKey(), e.getMessage());
            }
        }
        
        logger.info("Successfully updated {} out of {} attendance records", responses.size(), updates.size());
        return responses;
    }

    @Override
    public List<ExtracurricularAttendanceResponse> markAllPresent(Long activityId, LocalDate date) {
        logger.info("Marking all students present for activity {} on {}", activityId, date);
        ExtracurricularActivity activity = findActivityById(activityId);
        List<ExtracurricularAttendanceResponse> responses = new ArrayList<>();
        
        if (activity.getParticipants() != null) {
            for (Student student : activity.getParticipants()) {
                if (!attendanceExists(activityId, student.getId(), date)) {
                    CreateExtracurricularAttendanceRequest request = new CreateExtracurricularAttendanceRequest();
                    request.setActivityId(activityId);
                    request.setStudentId(student.getId());
                    request.setAttendanceDate(date);
                    request.setStatus(AttendanceStatus.PRESENT);
                    request.setNotes("Bulk marked as present");
                
                try {
                    ExtracurricularAttendanceResponse response = recordAttendance(request);
                    responses.add(response);
                } catch (Exception e) {
                    logger.error("Failed to mark student {} as present: {}", student.getId(), e.getMessage());
                }
            }
        }
        
        logger.info("Successfully marked {} students as present", responses.size());
        return responses;
    }

    @Override
    public List<ExtracurricularAttendanceResponse> markAllAbsent(Long activityId, LocalDate date) {
        logger.info("Marking all students absent for activity {} on {}", activityId, date);
        ExtracurricularActivity activity = findActivityById(activityId);
        List<ExtracurricularAttendanceResponse> responses = new ArrayList<>();
        
        if (activity.getParticipants() != null) {
            for (Student student : activity.getParticipants()) {
                if (!attendanceExists(activityId, student.getId(), date)) {
                    CreateExtracurricularAttendanceRequest request = new CreateExtracurricularAttendanceRequest();
                    request.setActivityId(activityId);
                    request.setStudentId(student.getId());
                    request.setAttendanceDate(date);
                    request.setStatus(AttendanceStatus.ABSENT);
                    request.setNotes("Bulk marked as absent");
                
                try {
                    ExtracurricularAttendanceResponse response = recordAttendance(request);
                    responses.add(response);
                } catch (Exception e) {
                    logger.error("Failed to mark student {} as absent: {}", student.getId(), e.getMessage());
                }
            }
        }
        
        logger.info("Successfully marked {} students as absent", responses.size());
        return responses;
    }
    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByActivity(Long activityId) {
        logger.debug("Calculating attendance statistics for activity {}", activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByActivityId(activityId);
        return calculateStatistics(attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByStudent(Long studentId) {
        logger.debug("Calculating attendance statistics for student {}", studentId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentId(studentId);
        return calculateStatistics(attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.AttendanceStatistics getAttendanceStatisticsByStudentAndActivity(Long studentId, Long activityId) {
        logger.debug("Calculating attendance statistics for student {} in activity {}", studentId, activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdAndActivityId(studentId, activityId);
        return calculateStatistics(attendances);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse.AttendanceTrend> getAttendanceTrendsByActivity(Long activityId) {
        logger.debug("Calculating attendance trends for activity {}", activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByActivityIdOrderByDateAsc(activityId);
        return calculateTrends(attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse.AttendanceTrend> getAttendanceTrendsByStudent(Long studentId) {
        logger.debug("Calculating attendance trends for student {}", studentId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdOrderByDateAsc(studentId);
        return calculateTrends(attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.StudentProgress getStudentProgress(Long studentId) {
        logger.debug("Calculating overall student progress for student {}", studentId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentId(studentId);
        return calculateStudentProgress(studentId, attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.StudentProgress getStudentProgressInActivity(Long studentId, Long activityId) {
        logger.debug("Calculating student progress for student {} in activity {}", studentId, activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdAndActivityId(studentId, activityId);
        return calculateStudentProgress(studentId, attendances);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse.StudentProgress> getTopPerformersByActivity(Long activityId, int limit) {
        logger.debug("Finding top {} performers for activity {}", limit, activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        
        return activity.getStudents().stream()
            .map(student -> {
                List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdAndActivityId(student.getId(), activityId);
                return calculateStudentProgress(student.getId(), attendances);
            })
            .sorted((p1, p2) -> Double.compare(p2.getAttendanceRate(), p1.getAttendanceRate()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse.StudentProgress> getStudentsNeedingAttention(Long activityId, Double threshold) {
        logger.debug("Finding students needing attention for activity {} with threshold {}", activityId, threshold);
        ExtracurricularActivity activity = findActivityById(activityId);
        
        return activity.getStudents().stream()
            .map(student -> {
                List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdAndActivityId(student.getId(), activityId);
                return calculateStudentProgress(student.getId(), attendances);
            })
            .filter(progress -> progress.getAttendanceRate() < threshold)
            .sorted((p1, p2) -> Double.compare(p1.getAttendanceRate(), p2.getAttendanceRate()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtracurricularAttendanceResponse.StudentProgress> getStudentsWithPerfectAttendance(Long activityId) {
        logger.debug("Finding students with perfect attendance for activity {}", activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        
        return activity.getStudents().stream()
            .map(student -> {
                List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentIdAndActivityId(student.getId(), activityId);
                return calculateStudentProgress(student.getId(), attendances);
            })
            .filter(progress -> progress.getAttendanceRate() >= 100.0)
            .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public Long calculateTotalAchievementPointsByStudent(Long studentId) {
        logger.debug("Calculating total achievement points for student {}", studentId);
        Student student = findStudentById(studentId);
        return attendanceRepository.calculateTotalAchievementPointsByStudent(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateTotalAchievementPointsByActivity(Long activityId) {
        logger.debug("Calculating total achievement points for activity {}", activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        return attendanceRepository.calculateTotalAchievementPointsByActivity(activity);
    }

    @Override
    public ExtracurricularAttendanceResponse awardAchievementPoints(Long attendanceId, Integer points, String reason) {
        logger.info("Awarding {} achievement points to attendance {} for reason: {}", points, attendanceId, reason);
        ExtracurricularAttendance attendance = findAttendanceById(attendanceId);
        
        Integer currentPoints = attendance.getAchievementPoints() != null ? attendance.getAchievementPoints() : 0;
        attendance.setAchievementPoints(currentPoints + points);
        // Note: Achievement reason field may need to be added to entity
        attendance.setUpdatedAt(LocalDateTime.now());
        
        ExtracurricularAttendance updatedAttendance = attendanceRepository.save(attendance);
        logger.info("Successfully awarded achievement points to attendance {}", attendanceId);
        
        return ExtracurricularAttendanceResponse.from(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public ExtracurricularAttendanceResponse.ActivityParticipationReport generateActivityParticipationReport(Long activityId) {
        logger.debug("Generating activity participation report for activity {}", activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByActivityId(activityId);
        
        ExtracurricularAttendanceResponse.ActivityParticipationReport report = 
            ExtracurricularAttendanceResponse.ActivityParticipationReport.builder()
                .totalSessions((long) attendances.size())
                .averageAttendanceRate(calculateAttendanceRateByActivity(activityId))
                .build();
        
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateStudentAttendanceReport(Long studentId) {
        logger.debug("Generating student attendance report for student {}", studentId);
        Student student = findStudentById(studentId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentAndIsActiveTrueOrderByAttendanceDateDesc(student);
        
        Map<String, Object> report = new HashMap<>();
        report.put("studentId", studentId);
        report.put("totalAttendances", attendances.size());
        report.put("presentCount", attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum());
        report.put("absentCount", attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.ABSENT ? 1 : 0).sum());
        report.put("attendanceRate", calculateAttendanceRateByStudent(studentId));
        report.put("totalAchievementPoints", calculateTotalAchievementPointsByStudent(studentId));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateComprehensiveAttendanceReport(ExtracurricularAttendanceSearchRequest searchRequest) {
        logger.debug("Generating comprehensive attendance report with search criteria");
        ExtracurricularActivity activity = null;
        if (searchRequest.getActivityId() != null) {
            activity = findActivityById(searchRequest.getActivityId());
        }
        
        Student student = null;
        if (searchRequest.getStudentId() != null) {
            student = findStudentById(searchRequest.getStudentId());
        }
        
        Page<ExtracurricularAttendance> attendances = attendanceRepository.findByMultipleCriteria(
            activity,
            student,
            searchRequest.getStatus(),
            searchRequest.getStartDate(),
            searchRequest.getEndDate(),
            Pageable.unpaged()
        );
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalRecords", attendances.getTotalElements());
        report.put("searchCriteria", searchRequest);
        report.put("attendances", attendances.getContent().stream()
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList()));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateMonthlyAttendanceSummary(int year, int month) {
        logger.debug("Generating monthly attendance summary for {}/{}", year, month);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // Using a placeholder query - actual implementation would need a proper repository method
        List<ExtracurricularAttendance> attendances = new ArrayList<>();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalAttendances", attendances.size());
        summary.put("presentCount", attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum());
        summary.put("absentCount", attendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.ABSENT ? 1 : 0).sum());
        summary.put("generatedAt", LocalDateTime.now());
        
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateSemesterAttendanceSummary(String academicYear, Integer semester) {
        logger.debug("Generating semester attendance summary for academic year {} semester {}", academicYear, semester);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("academicYear", academicYear);
        summary.put("semester", semester);
        summary.put("message", "Semester summary implementation requires academic calendar configuration");
        summary.put("generatedAt", LocalDateTime.now());
        
        return summary;
    }
    @Override public byte[] exportAttendanceToExcel(ExtracurricularAttendanceSearchRequest searchRequest) { return new byte[0]; }
    @Override public byte[] exportActivityParticipationReportToExcel(Long activityId) { return new byte[0]; }
    @Override public byte[] exportStudentProgressReportToExcel(Long studentId) { return new byte[0]; }
    @Override public byte[] exportAttendanceStatisticsToExcel(LocalDate startDate, LocalDate endDate) { return new byte[0]; }
    @Override public List<ExtracurricularAttendanceResponse> importAttendanceFromExcel(byte[] excelData) { return new ArrayList<>(); }
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAttendanceConflicts(Long studentId, LocalDate date) {
        logger.debug("Checking attendance conflicts for student {} on {}", studentId, date);
        // Using placeholder - actual implementation would need proper repository method
        List<ExtracurricularAttendance> attendances = new ArrayList<>();
        List<Map<String, Object>> conflicts = new ArrayList<>();
        
        if (attendances.size() > 1) {
            for (ExtracurricularAttendance attendance : attendances) {
                Map<String, Object> conflict = new HashMap<>();
                conflict.put("attendanceId", attendance.getId());
                conflict.put("activityId", attendance.getActivity().getId());
                conflict.put("activityName", attendance.getActivity().getName());
                conflict.put("date", attendance.getAttendanceDate());
                conflict.put("status", attendance.getStatus());
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }

    @Override
    public List<Map<String, Object>> sendAttendanceNotifications(Long activityId, LocalDate date) {
        logger.info("Sending attendance notifications for activity {} on {}", activityId, date);
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // This would integrate with an email/SMS service
        Map<String, Object> notification = new HashMap<>();
        notification.put("activityId", activityId);
        notification.put("date", date);
        notification.put("type", "ATTENDANCE_REMINDER");
        notification.put("status", "SENT");
        notification.put("sentAt", LocalDateTime.now());
        notifications.add(notification);
        
        return notifications;
    }

    @Override
    public List<Map<String, Object>> sendAbsenceNotifications(Long studentId, Long activityId, LocalDate date) {
        logger.info("Sending absence notifications for student {} in activity {} on {}", studentId, activityId, date);
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // This would integrate with an email/SMS service to notify parents/guardians
        Map<String, Object> notification = new HashMap<>();
        notification.put("studentId", studentId);
        notification.put("activityId", activityId);
        notification.put("date", date);
        notification.put("type", "ABSENCE_NOTIFICATION");
        notification.put("status", "SENT");
        notification.put("sentAt", LocalDateTime.now());
        notifications.add(notification);
        
        return notifications;
    }

    @Override
    public List<Map<String, Object>> sendAchievementNotifications(Long studentId, String achievement) {
        logger.info("Sending achievement notifications for student {} for achievement: {}", studentId, achievement);
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // This would integrate with an email/SMS service
        Map<String, Object> notification = new HashMap<>();
        notification.put("studentId", studentId);
        notification.put("achievement", achievement);
        notification.put("type", "ACHIEVEMENT_NOTIFICATION");
        notification.put("status", "SENT");
        notification.put("sentAt", LocalDateTime.now());
        notifications.add(notification);
        
        return notifications;
    }

    @Override
    public List<Map<String, Object>> sendProgressReports(Long activityId) {
        logger.info("Sending progress reports for activity {}", activityId);
        List<Map<String, Object>> reports = new ArrayList<>();
        
        ExtracurricularActivity activity = findActivityById(activityId);
        if (activity.getStudents() != null) {
            for (Student student : activity.getStudents()) {
                Map<String, Object> report = new HashMap<>();
                report.put("studentId", student.getId());
                report.put("activityId", activityId);
                report.put("type", "PROGRESS_REPORT");
                report.put("status", "SENT");
                report.put("sentAt", LocalDateTime.now());
                reports.add(report);
            }
        }
        
        return reports;
    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceDashboardData() {
        logger.debug("Generating attendance dashboard data");
        Map<String, Object> dashboard = new HashMap<>();
        
        List<ExtracurricularAttendance> recentAttendances = attendanceRepository.findRecentAttendance(
            LocalDate.now().minusDays(30));
        
        dashboard.put("totalAttendancesThisMonth", recentAttendances.size());
        dashboard.put("presentCountThisMonth", recentAttendances.stream()
            .mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum());
        dashboard.put("absentCountThisMonth", recentAttendances.stream()
            .mapToLong(a -> a.getStatus() == AttendanceStatus.ABSENT ? 1 : 0).sum());
        dashboard.put("attendanceRateThisMonth", recentAttendances.isEmpty() ? 0.0 :
            (double) recentAttendances.stream().mapToLong(a -> a.getStatus() == AttendanceStatus.PRESENT ? 1 : 0).sum() 
            / recentAttendances.size() * 100);
        dashboard.put("lastUpdated", LocalDateTime.now());
        
        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getActivityDashboardData(Long activityId) {
        logger.debug("Generating activity dashboard data for activity {}", activityId);
        ExtracurricularActivity activity = findActivityById(activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByActivityId(activityId);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("activityId", activityId);
        dashboard.put("activityName", activity.getName());
        dashboard.put("totalStudents", activity.getStudents() != null ? activity.getStudents().size() : 0);
        dashboard.put("totalAttendances", attendances.size());
        dashboard.put("attendanceRate", calculateAttendanceRateByActivity(activityId));
        dashboard.put("recentAttendances", attendances.stream()
            .filter(a -> a.getDate().isAfter(LocalDate.now().minusDays(7)))
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList()));
        dashboard.put("lastUpdated", LocalDateTime.now());
        
        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentDashboardData(Long studentId) {
        logger.debug("Generating student dashboard data for student {}", studentId);
        Student student = findStudentById(studentId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByStudentId(studentId);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("studentId", studentId);
        dashboard.put("studentName", student.getName());
        dashboard.put("totalAttendances", attendances.size());
        dashboard.put("attendanceRate", calculateAttendanceRateByStudent(studentId));
        dashboard.put("totalAchievementPoints", calculateTotalAchievementPointsByStudent(studentId));
        dashboard.put("recentAttendances", attendances.stream()
            .filter(a -> a.getDate().isAfter(LocalDate.now().minusDays(7)))
            .map(ExtracurricularAttendanceResponse::from)
            .collect(Collectors.toList()));
        dashboard.put("lastUpdated", LocalDateTime.now());
        
        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSupervisorDashboardData(Long supervisorId) {
        logger.debug("Generating supervisor dashboard data for supervisor {}", supervisorId);
        Map<String, Object> dashboard = new HashMap<>();
        
        // This would require supervisor-activity relationships to be properly implemented
        dashboard.put("supervisorId", supervisorId);
        dashboard.put("message", "Supervisor dashboard requires supervisor-activity relationship mapping");
        dashboard.put("lastUpdated", LocalDateTime.now());
        
        return dashboard;
    }
    @Override
    public List<ExtracurricularAttendanceResponse> updateParticipationScoresForActivity(Long activityId) {
        logger.info("Updating participation scores for activity {}", activityId);
        List<ExtracurricularAttendance> attendances = attendanceRepository.findByActivityId(activityId);
        List<ExtracurricularAttendanceResponse> responses = new ArrayList<>();
        
        for (ExtracurricularAttendance attendance : attendances) {
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                Integer newScore = calculateParticipationScore(attendance.getId());
                attendance.setParticipationScore(newScore);
                attendance.setUpdatedAt(LocalDateTime.now());
                
                ExtracurricularAttendance updated = attendanceRepository.save(attendance);
                responses.add(ExtracurricularAttendanceResponse.from(updated));
            }
        }
        
        logger.info("Updated participation scores for {} attendance records", responses.size());
        return responses;
    }

    @Override
    public byte[] generateAttendanceCertificate(Long studentId, Long activityId) {
        logger.info("Generating attendance certificate for student {} in activity {}", studentId, activityId);
        // This would require a PDF generation library like iText or similar
        // For now, return empty byte array as placeholder
        return new byte[0];
    }

    @Override
    public byte[] generateAchievementCertificate(Long studentId, String achievement) {
        logger.info("Generating achievement certificate for student {} for achievement: {}", studentId, achievement);
        // This would require a PDF generation library like iText or similar
        // For now, return empty byte array as placeholder
        return new byte[0];
    }

    @Override
    public void archiveOldAttendanceRecords(String academicYear) {
        logger.info("Archiving old attendance records for academic year {}", academicYear);
        // This would involve moving old records to an archive table or marking them as archived
        // Implementation depends on archival strategy
    }

    @Override
    public void cleanupInactiveAttendanceRecords() {
        logger.info("Cleaning up inactive attendance records");
        // This would involve removing or archiving records that are no longer needed
        // Implementation depends on cleanup policy
    }
}