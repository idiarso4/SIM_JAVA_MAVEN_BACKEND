package com.school.sim.service.impl;

import com.school.sim.dto.request.CreateStudentRequest;
import com.school.sim.dto.request.UpdateStudentRequest;
import com.school.sim.dto.request.StudentSearchRequest;
import com.school.sim.dto.response.StudentResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.StudentService;
import com.school.sim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of StudentService for student lifecycle management
 * Provides comprehensive student CRUD operations, search, and class assignment
 */
@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentAssessmentRepository studentAssessmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public StudentResponse createStudent(CreateStudentRequest request) {
        logger.info("Creating new student with NIS: {}", request.getNis());

        // Validate student data
        validateStudentData(request);

        // Create new student entity
        Student student = new Student();
        student.setNis(request.getNis());
        student.setNamaLengkap(request.getNamaLengkap());
        student.setTempatLahir(request.getTempatLahir());
        student.setTanggalLahir(request.getTanggalLahir());
        student.setJenisKelamin(request.getJenisKelamin());
        student.setAgama(request.getAgama());
        student.setAlamat(request.getAlamat());
        student.setNamaAyah(request.getNamaAyah());
        student.setNamaIbu(request.getNamaIbu());
        student.setPekerjaanAyah(request.getPekerjaanAyah());
        student.setPekerjaanIbu(request.getPekerjaanIbu());
        student.setNoHpOrtu(request.getNoHpOrtu());
        student.setAlamatOrtu(request.getAlamatOrtu());
        student.setTahunMasuk(request.getTahunMasuk());
        student.setAsalSekolah(request.getAsalSekolah());
        student.setStatus(request.getStatus());

        // Assign to class room if provided
        if (request.getClassRoomId() != null) {
            ClassRoom classRoom = findClassRoomById(request.getClassRoomId());
            student.setClassRoom(classRoom);
        }

        Student savedStudent = studentRepository.save(student);
        logger.info("Successfully created student with ID: {}", savedStudent.getId());

        return StudentResponse.from(savedStudent);
    }

    @Override
    public StudentResponse updateStudent(Long studentId, UpdateStudentRequest request) {
        logger.info("Updating student with ID: {}", studentId);

        Student student = findStudentById(studentId);

        // Validate update data
        validateStudentUpdateData(studentId, request);

        // Update student fields
        if (StringUtils.hasText(request.getNamaLengkap())) {
            student.setNamaLengkap(request.getNamaLengkap());
        }
        if (StringUtils.hasText(request.getTempatLahir())) {
            student.setTempatLahir(request.getTempatLahir());
        }
        if (request.getTanggalLahir() != null) {
            student.setTanggalLahir(request.getTanggalLahir());
        }
        if (request.getJenisKelamin() != null) {
            student.setJenisKelamin(request.getJenisKelamin());
        }
        if (StringUtils.hasText(request.getAgama())) {
            student.setAgama(request.getAgama());
        }
        if (StringUtils.hasText(request.getAlamat())) {
            student.setAlamat(request.getAlamat());
        }
        if (StringUtils.hasText(request.getNamaAyah())) {
            student.setNamaAyah(request.getNamaAyah());
        }
        if (StringUtils.hasText(request.getNamaIbu())) {
            student.setNamaIbu(request.getNamaIbu());
        }
        if (StringUtils.hasText(request.getPekerjaanAyah())) {
            student.setPekerjaanAyah(request.getPekerjaanAyah());
        }
        if (StringUtils.hasText(request.getPekerjaanIbu())) {
            student.setPekerjaanIbu(request.getPekerjaanIbu());
        }
        if (StringUtils.hasText(request.getNoHpOrtu())) {
            student.setNoHpOrtu(request.getNoHpOrtu());
        }
        if (StringUtils.hasText(request.getAlamatOrtu())) {
            student.setAlamatOrtu(request.getAlamatOrtu());
        }
        if (request.getTahunMasuk() != null) {
            student.setTahunMasuk(request.getTahunMasuk());
        }
        if (StringUtils.hasText(request.getAsalSekolah())) {
            student.setAsalSekolah(request.getAsalSekolah());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }

        // Update class room assignment
        if (request.getClassRoomId() != null) {
            if (student.getClassRoom() == null || !request.getClassRoomId().equals(student.getClassRoom().getId())) {
                ClassRoom classRoom = findClassRoomById(request.getClassRoomId());
                student.setClassRoom(classRoom);
            }
        }

        student.setUpdatedAt(LocalDateTime.now());

        Student updatedStudent = studentRepository.save(student);
        logger.info("Successfully updated student with ID: {}", updatedStudent.getId());

        return StudentResponse.from(updatedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentResponse> getStudentById(Long studentId) {
        logger.debug("Fetching student by ID: {}", studentId);
        return studentRepository.findById(studentId)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentResponse> getStudentByNis(String nis) {
        logger.debug("Fetching student by NIS: {}", nis);
        return studentRepository.findByNis(nis)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        logger.debug("Fetching all students with pagination: {}", pageable);
        return studentRepository.findAll(pageable)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> searchStudents(StudentSearchRequest searchRequest, Pageable pageable) {
        logger.debug("Searching students with criteria: {}", searchRequest);
        
        return studentRepository.advancedSearchStudents(
            searchRequest.getName(),
            searchRequest.getNis(),
            searchRequest.getClassRoomId(),
            searchRequest.getStatus(),
            searchRequest.getTahunMasuk(),
            searchRequest.getAgama(),
            searchRequest.getAsalSekolah(),
            searchRequest.getParentName(),
            pageable
        ).map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByClassRoom(Long classRoomId, Pageable pageable) {
        logger.debug("Fetching students by class room ID: {}", classRoomId);
        return studentRepository.findByClassRoomId(classRoomId, pageable)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByStatus(StudentStatus status, Pageable pageable) {
        logger.debug("Fetching students by status: {}", status);
        return studentRepository.findByStatus(status, pageable)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByMajor(Long majorId, Pageable pageable) {
        logger.debug("Fetching students by major ID: {}", majorId);
        return studentRepository.findByMajorId(majorId, pageable)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByGrade(Integer grade, Pageable pageable) {
        logger.debug("Fetching students by grade: {}", grade);
        return studentRepository.findByGrade(grade, pageable)
            .map(StudentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByAcademicYear(String academicYear, Pageable pageable) {
        logger.debug("Fetching students by academic year: {}", academicYear);
        return studentRepository.findByAcademicYear(academicYear, pageable)
            .map(StudentResponse::from);
    }

    @Override
    public void deleteStudent(Long studentId) {
        logger.info("Deleting student with ID: {}", studentId);

        Student student = findStudentById(studentId);
        
        // Check if student has attendance or assessment records
        if (hasAttendanceRecords(studentId) || hasAssessmentRecords(studentId)) {
            throw new ValidationException("Cannot delete student with existing attendance or assessment records");
        }

        studentRepository.delete(student);
        logger.info("Successfully deleted student with ID: {}", studentId);
    }

    @Override
    public void activateStudent(Long studentId) {
        logger.info("Activating student with ID: {}", studentId);
        updateStudentStatus(studentId, StudentStatus.ACTIVE);
    }

    @Override
    public void deactivateStudent(Long studentId) {
        logger.info("Deactivating student with ID: {}", studentId);
        updateStudentStatus(studentId, StudentStatus.INACTIVE);
    }

    @Override
    public void graduateStudent(Long studentId) {
        logger.info("Graduating student with ID: {}", studentId);
        updateStudentStatus(studentId, StudentStatus.GRADUATED);
    }

    @Override
    public void assignToClassRoom(Long studentId, Long classRoomId) {
        logger.info("Assigning student {} to class room {}", studentId, classRoomId);

        Student student = findStudentById(studentId);
        ClassRoom classRoom = findClassRoomById(classRoomId);

        // Check class room capacity
        if (!canAssignToClassRoom(classRoomId, 1)) {
            throw new ValidationException("Class room has reached maximum capacity");
        }

        student.setClassRoom(classRoom);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        logger.info("Successfully assigned student {} to class room {}", studentId, classRoomId);
    }

    @Override
    public void removeFromClassRoom(Long studentId) {
        logger.info("Removing student {} from class room", studentId);

        Student student = findStudentById(studentId);
        student.setClassRoom(null);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        logger.info("Successfully removed student {} from class room", studentId);
    }

    @Override
    public void bulkAssignToClassRoom(List<Long> studentIds, Long classRoomId) {
        logger.info("Bulk assigning {} students to class room {}", studentIds.size(), classRoomId);

        ClassRoom classRoom = findClassRoomById(classRoomId);

        // Check class room capacity
        if (!canAssignToClassRoom(classRoomId, studentIds.size())) {
            throw new ValidationException("Class room does not have enough capacity for all students");
        }

        List<Student> students = studentRepository.findAllById(studentIds);
        students.forEach(student -> {
            student.setClassRoom(classRoom);
            student.setUpdatedAt(LocalDateTime.now());
        });

        studentRepository.saveAll(students);
        logger.info("Successfully assigned {} students to class room {}", students.size(), classRoomId);
    }

    @Override
    public void transferStudent(Long studentId, Long newClassRoomId, String reason) {
        logger.info("Transferring student {} to class room {} with reason: {}", studentId, newClassRoomId, reason);

        Student student = findStudentById(studentId);
        ClassRoom newClassRoom = findClassRoomById(newClassRoomId);

        // Check new class room capacity
        if (!canAssignToClassRoom(newClassRoomId, 1)) {
            throw new ValidationException("Target class room has reached maximum capacity");
        }

        Long oldClassRoomId = student.getClassRoom() != null ? student.getClassRoom().getId() : null;
        
        student.setClassRoom(newClassRoom);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        logger.info("Successfully transferred student {} from class room {} to class room {}", 
                   studentId, oldClassRoomId, newClassRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNis(String nis) {
        return studentRepository.existsByNis(nis);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics() {
        logger.debug("Generating student statistics");

        Map<String, Object> statistics = new HashMap<>();

        // Total students
        statistics.put("totalStudents", studentRepository.count());

        // Students by status
        List<Object[]> statusStats = studentRepository.getStudentStatisticsByStatus();
        Map<String, Long> statusMap = statusStats.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
        statistics.put("studentsByStatus", statusMap);

        // Students by grade
        List<Object[]> gradeStats = studentRepository.getStudentStatisticsByGrade();
        Map<String, Long> gradeMap = gradeStats.stream()
            .collect(Collectors.toMap(
                row -> "Grade " + row[0].toString(),
                row -> (Long) row[1]
            ));
        statistics.put("studentsByGrade", gradeMap);

        // Students by major
        List<Object[]> majorStats = studentRepository.getStudentStatisticsByMajor();
        Map<String, Long> majorMap = majorStats.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
        statistics.put("studentsByMajor", majorMap);

        // Students without class assignment
        long studentsWithoutClass = studentRepository.findByClassRoomIsNull().size();
        statistics.put("studentsWithoutClass", studentsWithoutClass);

        // Students without user account
        long studentsWithoutUser = studentRepository.findStudentsWithoutUserAccount().size();
        statistics.put("studentsWithoutUserAccount", studentsWithoutUser);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsWithoutClassAssignment() {
        logger.debug("Fetching students without class assignment");
        return studentRepository.findByClassRoomIsNull().stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsWithoutUserAccount() {
        logger.debug("Fetching students without user account");
        return studentRepository.findStudentsWithoutUserAccount().stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public void createUserAccountForStudent(Long studentId, String password) {
        logger.info("Creating user account for student: {}", studentId);

        Student student = findStudentById(studentId);

        if (student.getUser() != null) {
            throw new ValidationException("Student already has a user account");
        }

        // Create user account
        User user = new User();
        user.setName(student.getNamaLengkap());
        user.setEmail(generateEmailForStudent(student));
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(UserType.STUDENT);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Link user to student
        student.setUser(savedUser);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        logger.info("Successfully created user account for student: {}", studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getStudentCountByClassRoom(Long classRoomId) {
        return studentRepository.countByClassRoomId(classRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveStudentCountByClassRoom(Long classRoomId) {
        return studentRepository.countByClassRoomIdAndStatus(classRoomId, StudentStatus.ACTIVE);
    }

    @Override
    public void validateStudentData(CreateStudentRequest request) {
        // Check if NIS already exists
        if (existsByNis(request.getNis())) {
            throw new ValidationException("NIS already exists: " + request.getNis());
        }

        // Validate class room capacity if assigned
        if (request.getClassRoomId() != null) {
            if (!canAssignToClassRoom(request.getClassRoomId(), 1)) {
                throw new ValidationException("Class room has reached maximum capacity");
            }
        }
    }

    @Override
    public void validateStudentUpdateData(Long studentId, UpdateStudentRequest request) {
        // Validate class room capacity if changing assignment
        if (request.getClassRoomId() != null) {
            Student student = findStudentById(studentId);
            if (student.getClassRoom() == null || !request.getClassRoomId().equals(student.getClassRoom().getId())) {
                if (!canAssignToClassRoom(request.getClassRoomId(), 1)) {
                    throw new ValidationException("Class room has reached maximum capacity");
                }
            }
        }
    }

    // Additional methods implementation continues...
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByBirthYear(Integer birthYear) {
        return studentRepository.findByBirthYear(birthYear).stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByAgeRange(Integer minAge, Integer maxAge) {
        return studentRepository.findByAgeRange(minAge, maxAge).stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByGender(String gender) {
        return studentRepository.findByGender(gender).stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByReligion(String religion) {
        return studentRepository.findByAgama(religion).stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByOriginSchool(String originSchool) {
        return studentRepository.findByAsalSekolah(originSchool).stream()
            .map(StudentResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public void bulkUpdateStudentStatus(List<Long> studentIds, StudentStatus status) {
        logger.info("Bulk updating status for {} students to {}", studentIds.size(), status);

        List<Student> students = studentRepository.findAllById(studentIds);
        students.forEach(student -> {
            student.setStatus(status);
            student.setUpdatedAt(LocalDateTime.now());
        });

        studentRepository.saveAll(students);
        logger.info("Successfully updated status for {} students", students.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentEnrollmentHistory(Long studentId) {
        // This would typically involve a separate enrollment history table
        // For now, return basic information
        Student student = findStudentById(studentId);
        
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("studentId", studentId);
        enrollment.put("currentClassRoom", student.getClassRoom());
        enrollment.put("enrollmentDate", student.getCreatedAt());
        enrollment.put("status", student.getStatus());
        
        return List.of(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAssignToClassRoom(Long classRoomId, int additionalStudents) {
        ClassRoom classRoom = findClassRoomById(classRoomId);
        long currentCount = getActiveStudentCountByClassRoom(classRoomId);
        
        // Assuming maximum capacity of 40 students per class
        int maxCapacity = classRoom.getCapacity() != null ? classRoom.getCapacity() : 40;
        
        return (currentCount + additionalStudents) <= maxCapacity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableClassRooms(Integer grade, Long majorId) {
        // This would involve querying class rooms with available capacity
        // Implementation would depend on specific business rules
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateStudentReportCard(Long studentId, String academicYear, Integer semester) {
        // This would involve complex queries across multiple tables
        // Implementation would depend on specific report requirements
        Map<String, Object> reportCard = new HashMap<>();
        reportCard.put("studentId", studentId);
        reportCard.put("academicYear", academicYear);
        reportCard.put("semester", semester);
        return reportCard;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentAttendanceSummary(Long studentId, String startDate, String endDate) {
        // Implementation would involve attendance calculations
        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("period", startDate + " to " + endDate);
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentAssessmentSummary(Long studentId, String academicYear, Integer semester) {
        // Implementation would involve assessment calculations
        Map<String, Object> summary = new HashMap<>();
        summary.put("studentId", studentId);
        summary.put("academicYear", academicYear);
        summary.put("semester", semester);
        return summary;
    }

    // Helper methods

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    }

    private ClassRoom findClassRoomById(Long classRoomId) {
        return classRoomRepository.findById(classRoomId)
            .orElseThrow(() -> new ResourceNotFoundException("Class room not found with ID: " + classRoomId));
    }

    private void updateStudentStatus(Long studentId, StudentStatus status) {
        Student student = findStudentById(studentId);
        student.setStatus(status);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);
        logger.info("Successfully updated student {} status to {}", studentId, status);
    }

    private boolean hasAttendanceRecords(Long studentId) {
        Student student = findStudentById(studentId);
        return student.getAttendances() != null && !student.getAttendances().isEmpty();
    }

    private boolean hasAssessmentRecords(Long studentId) {
        Student student = findStudentById(studentId);
        return student.getAssessments() != null && !student.getAssessments().isEmpty();
    }

    private String generateEmailForStudent(Student student) {
        // Generate email based on NIS and school domain
        return student.getNis() + "@student.school.edu";
    }
}
