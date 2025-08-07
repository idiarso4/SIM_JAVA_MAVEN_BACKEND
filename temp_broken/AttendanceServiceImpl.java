package com.school.sim.service.impl;

import com.school.sim.dto.request.CreateAttendanceRequest;
import com.school.sim.dto.request.UpdateAttendanceRequest;
import com.school.sim.dto.request.BulkAttendanceRequest;
import com.school.sim.dto.response.AttendanceResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AttendanceService for attendance management operations
 * Provides comprehensive attendance CRUD operations, bulk recording, and caching
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TeachingActivityRepository teachingActivityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public AttendanceResponse recordAttendance(CreateAttendanceRequest request) {
        logger.info("Recording attendance for student {} in teaching activity {}", 
                   request.getStudentId(), request.getTeachingActivityId());

        // Validate request
        validateAttendanceConflicts(request);

        // Get entities
        TeachingActivity teachingActivity = findTeachingActivityById(request.getTeachingActivityId());
        Student student = findStudentById(request.getStudentId());
        User recordedBy = request.getRecordedBy() != null ? 
            findUserById(request.getRecordedBy()) : null;

        // Check if attendance already exists
        if (attendanceExists(request.getTeachingActivityId(), request.getStudentId())) {
            throw new ValidationException("Attendance already recorded for this student in this teaching activity");
        }

        // Create attendance record
        Attendance attendance = new Attendance();
        attendance.setTeachingActivity(teachingActivity);
        attendance.setStudent(student);
        attendance.setStatus(request.getStatus());
        attendance.setKeterangan(request.getKeterangan());
        attendance.setRecordedBy(recordedBy);
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("Successfully recorded attendance with ID: {}", savedAttendance.getId());

        return AttendanceResponse.from(savedAttendance);
    }

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public AttendanceResponse updateAttendance(Long attendanceId, UpdateAttendanceRequest request) {
        logger.info("Updating attendance with ID: {}", attendanceId);

        Attendance attendance = findAttendanceById(attendanceId);

        // Update fields
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getKeterangan() != null) {
            attendance.setKeterangan(request.getKeterangan());
        }
        if (request.getRecordedBy() != null) {
            User recordedBy = findUserById(request.getRecordedBy());
            attendance.setRecordedBy(recordedBy);
        }

        attendance.setUpdatedAt(LocalDateTime.now());

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        logger.info("Successfully updated attendance with ID: {}", updatedAttendance.getId());

        return AttendanceResponse.from(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendance", key = "#attendanceId")
    public Optional<AttendanceResponse> getAttendanceById(Long attendanceId) {
        logger.debug("Fetching attendance by ID: {}", attendanceId);
        return attendanceRepository.findById(attendanceId)
            .map(AttendanceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendanceByTeachingActivity", key = "#teachingActivityId")
    public List<AttendanceResponse> getAttendanceByTeachingActivity(Long teachingActivityId) {
        logger.debug("Fetching attendance by teaching activity ID: {}", teachingActivityId);
        
        TeachingActivity teachingActivity = findTeachingActivityById(teachingActivityId);
        return attendanceRepository.findByTeachingActivity(teachingActivity).stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceByStudentAndDateRange(Long studentId, 
                                                                      LocalDate startDate, 
                                                                      LocalDate endDate, 
                                                                      Pageable pageable) {
        logger.debug("Fetching attendance by student {} and date range {} to {}", 
                    studentId, startDate, endDate);
        
        Student student = findStudentById(studentId);
        return attendanceRepository.findByStudentAndDateBetween(student, startDate, endDate, pageable)
            .map(AttendanceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByStudentAndDate(Long studentId, LocalDate date) {
        logger.debug("Fetching attendance by student {} and date {}", studentId, date);
        
        Student student = findStudentById(studentId);
        return attendanceRepository.findByStudentAndDate(student, date).stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendanceByClassRoomAndDate", key = "#classRoomId + '_' + #date")
    public List<AttendanceResponse> getAttendanceByClassRoomAndDate(Long classRoomId, LocalDate date) {
        logger.debug("Fetching attendance by class room {} and date {}", classRoomId, date);
        
        return attendanceRepository.findByClassRoomAndDate(classRoomId, date).stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceByClassRoomAndDateRange(Long classRoomId, 
                                                                        LocalDate startDate, 
                                                                        LocalDate endDate, 
                                                                        Pageable pageable) {
        logger.debug("Fetching attendance by class room {} and date range {} to {}", 
                    classRoomId, startDate, endDate);
        
        return attendanceRepository.findByClassRoomAndDateBetween(classRoomId, startDate, endDate, pageable)
            .map(AttendanceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceByTeacherAndDateRange(Long teacherId, 
                                                                      LocalDate startDate, 
                                                                      LocalDate endDate, 
                                                                      Pageable pageable) {
        logger.debug("Fetching attendance by teacher {} and date range {} to {}", 
                    teacherId, startDate, endDate);
        
        return attendanceRepository.findByTeacherAndDateBetween(teacherId, startDate, endDate, pageable)
            .map(AttendanceResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceBySubjectAndDateRange(Long subjectId, 
                                                                      LocalDate startDate, 
                                                                      LocalDate endDate, 
                                                                      Pageable pageable) {
        logger.debug("Fetching attendance by subject {} and date range {} to {}", 
                    subjectId, startDate, endDate);
        
        return attendanceRepository.findBySubjectAndDateBetween(subjectId, startDate, endDate, pageable)
            .map(AttendanceResponse::from);
    }

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public BulkAttendanceResult bulkRecordAttendance(BulkAttendanceRequest request) {
        logger.info("Bulk recording attendance for {} students in teaching activity {}", 
                   request.getStudentAttendances().size(), request.getTeachingActivityId());

        List<String> errors = new ArrayList<>();
        List<AttendanceResponse> recordedAttendances = new ArrayList<>();
        int totalRecords = request.getStudentAttendances().size();
        int successfulRecords = 0;
        int failedRecords = 0;

        // Get teaching activity and recorded by user
        TeachingActivity teachingActivity = findTeachingActivityById(request.getTeachingActivityId());
        User recordedBy = request.getRecordedBy() != null ? 
            findUserById(request.getRecordedBy()) : null;

        for (BulkAttendanceRequest.StudentAttendanceRecord studentRecord : request.getStudentAttendances()) {
            try {
                // Check if attendance already exists
                if (attendanceExists(request.getTeachingActivityId(), studentRecord.getStudentId())) {
                    errors.add("Attendance already exists for student ID: " + studentRecord.getStudentId());
                    failedRecords++;
                    continue;
                }

                // Get student
                Student student = findStudentById(studentRecord.getStudentId());

                // Create attendance record
                Attendance attendance = new Attendance();
                attendance.setTeachingActivity(teachingActivity);
                attendance.setStudent(student);
                attendance.setStatus(studentRecord.getStatus());
                attendance.setKeterangan(studentRecord.getKeterangan());
                attendance.setRecordedBy(recordedBy);
                attendance.setCreatedAt(LocalDateTime.now());
                attendance.setUpdatedAt(LocalDateTime.now());

                Attendance savedAttendance = attendanceRepository.save(attendance);
                recordedAttendances.add(AttendanceResponse.from(savedAttendance));
                successfulRecords++;

            } catch (Exception e) {
                logger.error("Error recording attendance for student {}: {}", 
                           studentRecord.getStudentId(), e.getMessage());
                errors.add("Error for student ID " + studentRecord.getStudentId() + ": " + e.getMessage());
                failedRecords++;
            }
        }

        logger.info("Bulk attendance recording completed. Success: {}, Failed: {}", 
                   successfulRecords, failedRecords);

        return new BulkAttendanceResult(totalRecords, successfulRecords, failedRecords, 
                                       errors, recordedAttendances);
    }

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public void deleteAttendance(Long attendanceId) {
        logger.info("Deleting attendance with ID: {}", attendanceId);

        Attendance attendance = findAttendanceById(attendanceId);
        attendanceRepository.delete(attendance);

        logger.info("Successfully deleted attendance with ID: {}", attendanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean attendanceExists(Long teachingActivityId, Long studentId) {
        TeachingActivity teachingActivity = findTeachingActivityById(teachingActivityId);
        Student student = findStudentById(studentId);
        return attendanceRepository.existsByTeachingActivityAndStudent(teachingActivity, student);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendanceStats", key = "#studentId + '_' + #startDate + '_' + #endDate")
    public Map<AttendanceStatus, Long> getAttendanceStatsByStudent(Long studentId, 
                                                                  LocalDate startDate, 
                                                                  LocalDate endDate) {
        logger.debug("Getting attendance stats for student {} from {} to {}", 
                    studentId, startDate, endDate);
        
        Student student = findStudentById(studentId);
        List<Object[]> stats = attendanceRepository.getAttendanceStatsByStudentAndDateBetween(
            student, startDate, endDate);
        
        return stats.stream()
            .collect(Collectors.toMap(
                row -> (AttendanceStatus) row[0],
                row -> (Long) row[1]
            ));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendanceSummary", key = "#classRoomId + '_' + #startDate + '_' + #endDate")
    public Map<AttendanceStatus, Long> getAttendanceSummaryByClassRoom(Long classRoomId, 
                                                                      LocalDate startDate, 
                                                                      LocalDate endDate) {
        logger.debug("Getting attendance summary for class room {} from {} to {}", 
                    classRoomId, startDate, endDate);
        
        List<Object[]> summary = attendanceRepository.getAttendanceSummaryByClassRoomAndDateBetween(
            classRoomId, startDate, endDate);
        
        return summary.stream()
            .collect(Collectors.toMap(
                row -> (AttendanceStatus) row[0],
                row -> (Long) row[1]
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyAttendanceStatistics(LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting daily attendance statistics from {} to {}", startDate, endDate);
        
        List<Object[]> stats = attendanceRepository.getDailyAttendanceStatistics(startDate, endDate);
        
        return stats.stream()
            .map(row -> {
                Map<String, Object> dailyStat = new HashMap<>();
                dailyStat.put("date", row[0]);
                dailyStat.put("status", row[1]);
                dailyStat.put("count", row[2]);
                return dailyStat;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRateForStudent(Long studentId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Calculating attendance rate for student {} from {} to {}", 
                    studentId, startDate, endDate);
        
        Student student = findStudentById(studentId);
        return attendanceRepository.calculateAttendanceRateForStudent(student, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAttendanceRateForClassRoom(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Calculating attendance rate for class room {} from {} to {}", 
                    classRoomId, startDate, endDate);
        
        return attendanceRepository.calculateAttendanceRateForClassRoom(classRoomId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findStudentsWithPerfectAttendance(LocalDate startDate, LocalDate endDate) {
        logger.debug("Finding students with perfect attendance from {} to {}", startDate, endDate);
        
        List<Student> students = attendanceRepository.findStudentsWithPerfectAttendance(startDate, endDate);
        
        return students.stream()
            .map(student -> {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", student.getId());
                studentInfo.put("nis", student.getNis());
                studentInfo.put("namaLengkap", student.getNamaLengkap());
                studentInfo.put("classRoom", student.getClassRoom() != null ? 
                    student.getClassRoom().getName() : null);
                return studentInfo;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findStudentsWithPoorAttendance(LocalDate startDate, 
                                                                   LocalDate endDate, 
                                                                   Long minAbsences) {
        logger.debug("Finding students with poor attendance from {} to {} with min absences {}", 
                    startDate, endDate, minAbsences);
        
        List<Object[]> results = attendanceRepository.findStudentsWithPoorAttendance(
            startDate, endDate, minAbsences);
        
        return results.stream()
            .map(row -> {
                Student student = (Student) row[0];
                Long absenceCount = (Long) row[1];
                
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", student.getId());
                studentInfo.put("nis", student.getNis());
                studentInfo.put("namaLengkap", student.getNamaLengkap());
                studentInfo.put("classRoom", student.getClassRoom() != null ? 
                    student.getClassRoom().getName() : null);
                studentInfo.put("absenceCount", absenceCount);
                return studentInfo;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceNeedingFollowUp(LocalDate fromDate) {
        logger.debug("Getting attendance records needing follow-up from {}", fromDate);
        
        return attendanceRepository.findAttendanceNeedingFollowUp(fromDate).stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyAttendanceReport(LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting monthly attendance report from {} to {}", startDate, endDate);
        
        List<Object[]> report = attendanceRepository.getMonthlyAttendanceReport(startDate, endDate);
        
        return report.stream()
            .map(row -> {
                Map<String, Object> monthlyData = new HashMap<>();
                monthlyData.put("year", row[0]);
                monthlyData.put("month", row[1]);
                monthlyData.put("status", row[2]);
                monthlyData.put("count", row[3]);
                return monthlyData;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void validateAttendanceConflicts(CreateAttendanceRequest request) {
        // Check if teaching activity exists and is not completed
        TeachingActivity teachingActivity = findTeachingActivityById(request.getTeachingActivityId());
        
        if (teachingActivity.getIsCompleted()) {
            throw new ValidationException("Cannot record attendance for completed teaching activity");
        }

        // Check if student is enrolled in the class
        Student student = findStudentById(request.getStudentId());
        if (student.getClassRoom() == null || 
            !student.getClassRoom().getId().equals(teachingActivity.getClassRoom().getId())) {
            throw new ValidationException("Student is not enrolled in the class for this teaching activity");
        }

        // Check if student is active
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new ValidationException("Cannot record attendance for inactive student");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getRecentAttendance(int hours) {
        logger.debug("Getting recent attendance records from last {} hours", hours);
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return attendanceRepository.findRecentAttendance(since).stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public List<AttendanceResponse> autoGenerateAttendanceForTeachingActivity(Long teachingActivityId, 
                                                                             AttendanceStatus defaultStatus) {
        logger.info("Auto-generating attendance for teaching activity {} with default status {}", 
                   teachingActivityId, defaultStatus);

        TeachingActivity teachingActivity = findTeachingActivityById(teachingActivityId);
        
        // Get all active students in the class
        List<Student> students = studentRepository.findByClassRoomIdAndStatus(
            teachingActivity.getClassRoom().getId(), StudentStatus.ACTIVE);

        List<AttendanceResponse> generatedAttendances = new ArrayList<>();

        for (Student student : students) {
            // Skip if attendance already exists
            if (attendanceExists(teachingActivityId, student.getId())) {
                continue;
            }

            // Create attendance record
            Attendance attendance = new Attendance();
            attendance.setTeachingActivity(teachingActivity);
            attendance.setStudent(student);
            attendance.setStatus(defaultStatus);
            attendance.setKeterangan("Auto-generated");
            attendance.setCreatedAt(LocalDateTime.now());
            attendance.setUpdatedAt(LocalDateTime.now());

            Attendance savedAttendance = attendanceRepository.save(attendance);
            generatedAttendances.add(AttendanceResponse.from(savedAttendance));
        }

        logger.info("Auto-generated {} attendance records", generatedAttendances.size());
        return generatedAttendances;
    }

    @Override
    @CacheEvict(value = "attendance", allEntries = true)
    public List<AttendanceResponse> copyAttendanceFromPreviousSession(Long currentTeachingActivityId, 
                                                                     Long previousTeachingActivityId) {
        logger.info("Copying attendance from teaching activity {} to {}", 
                   previousTeachingActivityId, currentTeachingActivityId);

        TeachingActivity currentActivity = findTeachingActivityById(currentTeachingActivityId);
        TeachingActivity previousActivity = findTeachingActivityById(previousTeachingActivityId);

        // Get previous attendance records
        List<Attendance> previousAttendances = attendanceRepository.findByTeachingActivity(previousActivity);
        List<AttendanceResponse> copiedAttendances = new ArrayList<>();

        for (Attendance previousAttendance : previousAttendances) {
            // Skip if attendance already exists for current activity
            if (attendanceExists(currentTeachingActivityId, previousAttendance.getStudent().getId())) {
                continue;
            }

            // Create new attendance record
            Attendance newAttendance = new Attendance();
            newAttendance.setTeachingActivity(currentActivity);
            newAttendance.setStudent(previousAttendance.getStudent());
            newAttendance.setStatus(previousAttendance.getStatus());
            newAttendance.setKeterangan("Copied from previous session");
            newAttendance.setCreatedAt(LocalDateTime.now());
            newAttendance.setUpdatedAt(LocalDateTime.now());

            Attendance savedAttendance = attendanceRepository.save(newAttendance);
            copiedAttendances.add(AttendanceResponse.from(savedAttendance));
        }

        logger.info("Copied {} attendance records", copiedAttendances.size());
        return copiedAttendances;
    }

    // Helper methods

    private Attendance findAttendanceById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));
    }

    private TeachingActivity findTeachingActivityById(Long teachingActivityId) {
        return teachingActivityRepository.findById(teachingActivityId)
            .orElseThrow(() -> new ResourceNotFoundException("Teaching activity not found with ID: " + teachingActivityId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
