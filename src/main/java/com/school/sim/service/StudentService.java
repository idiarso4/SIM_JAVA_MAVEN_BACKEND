package com.school.sim.service;

import com.school.sim.dto.request.CreateStudentRequest;
import com.school.sim.dto.request.UpdateStudentRequest;
import com.school.sim.dto.request.StudentSearchRequest;
import com.school.sim.dto.response.StudentResponse;

import com.school.sim.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for student management operations
 * Provides methods for student lifecycle management, search, and class assignment
 */
public interface StudentService {

    /**
     * Create a new student
     */
    StudentResponse createStudent(CreateStudentRequest request);

    /**
     * Update an existing student
     */
    StudentResponse updateStudent(Long studentId, UpdateStudentRequest request);

    /**
     * Get student by ID
     */
    Optional<StudentResponse> getStudentById(Long studentId);

    /**
     * Get student by NIS
     */
    Optional<StudentResponse> getStudentByNis(String nis);

    /**
     * Get all students with pagination
     */
    Page<StudentResponse> getAllStudents(Pageable pageable);

    /**
     * Search students with multiple criteria
     */
    Page<StudentResponse> searchStudents(StudentSearchRequest searchRequest, Pageable pageable);

    /**
     * Get students by class room
     */
    Page<StudentResponse> getStudentsByClassRoom(Long classRoomId, Pageable pageable);

    /**
     * Get students by status
     */
    Page<StudentResponse> getStudentsByStatus(StudentStatus status, Pageable pageable);

    /**
     * Get students by major
     */
    Page<StudentResponse> getStudentsByMajor(Long majorId, Pageable pageable);

    /**
     * Get students by grade
     */
    Page<StudentResponse> getStudentsByGrade(Integer grade, Pageable pageable);

    /**
     * Get students by academic year
     */
    Page<StudentResponse> getStudentsByAcademicYear(String academicYear, Pageable pageable);

    /**
     * Delete student by ID
     */
    void deleteStudent(Long studentId);

    /**
     * Activate student
     */
    void activateStudent(Long studentId);

    /**
     * Deactivate student
     */
    void deactivateStudent(Long studentId);

    /**
     * Graduate student
     */
    void graduateStudent(Long studentId);

    /**
     * Assign student to class room
     */
    void assignToClassRoom(Long studentId, Long classRoomId);

    /**
     * Remove student from class room
     */
    void removeFromClassRoom(Long studentId);

    /**
     * Bulk assign students to class room
     */
    void bulkAssignToClassRoom(List<Long> studentIds, Long classRoomId);

    /**
     * Transfer student to different class room
     */
    void transferStudent(Long studentId, Long newClassRoomId, String reason);

    /**
     * Check if NIS exists
     */
    boolean existsByNis(String nis);

    /**
     * Get student statistics
     */
    Map<String, Object> getStudentStatistics();

    /**
     * Get students without class assignment
     */
    List<StudentResponse> getStudentsWithoutClassAssignment();

    /**
     * Get students without user account
     */
    List<StudentResponse> getStudentsWithoutUserAccount();

    /**
     * Create user account for student
     */
    void createUserAccountForStudent(Long studentId, String password);

    /**
     * Get student count by class room
     */
    long getStudentCountByClassRoom(Long classRoomId);

    /**
     * Get active student count by class room
     */
    long getActiveStudentCountByClassRoom(Long classRoomId);

    /**
     * Validate student data
     */
    void validateStudentData(CreateStudentRequest request);

    /**
     * Validate student update data
     */
    void validateStudentUpdateData(Long studentId, UpdateStudentRequest request);

    /**
     * Get students by birth year
     */
    List<StudentResponse> getStudentsByBirthYear(Integer birthYear);

    /**
     * Get students by age range
     */
    List<StudentResponse> getStudentsByAgeRange(Integer minAge, Integer maxAge);

    /**
     * Get students by gender
     */
    List<StudentResponse> getStudentsByGender(String gender);

    /**
     * Get students by religion
     */
    List<StudentResponse> getStudentsByReligion(String religion);

    /**
     * Get students by origin school
     */
    List<StudentResponse> getStudentsByOriginSchool(String originSchool);

    /**
     * Bulk update student status
     */
    void bulkUpdateStudentStatus(List<Long> studentIds, StudentStatus status);

    /**
     * Get student enrollment history
     */
    List<Map<String, Object>> getStudentEnrollmentHistory(Long studentId);

    /**
     * Check class room capacity before assignment
     */
    boolean canAssignToClassRoom(Long classRoomId, int additionalStudents);

    /**
     * Get available class rooms for student assignment
     */
    List<Map<String, Object>> getAvailableClassRooms(Integer grade, Long majorId);

    /**
     * Generate student report card data
     */
    Map<String, Object> generateStudentReportCard(Long studentId, String academicYear, Integer semester);

    /**
     * Get student attendance summary
     */
    Map<String, Object> getStudentAttendanceSummary(Long studentId, String startDate, String endDate);

    /**
     * Get student assessment summary
     */
    Map<String, Object> getStudentAssessmentSummary(Long studentId, String academicYear, Integer semester);
}
