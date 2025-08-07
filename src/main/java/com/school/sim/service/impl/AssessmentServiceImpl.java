package com.school.sim.service.impl;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.AssessmentResponse;
import com.school.sim.dto.response.StudentAssessmentResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;

import com.school.sim.repository.*;
import com.school.sim.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AssessmentService for assessment management
 * Provides comprehensive assessment management and grading functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentAssessmentRepository studentAssessmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;

    @Override
    public Page<AssessmentResponse> getAllAssessments(Pageable pageable) {
        log.debug("Fetching all assessments with pagination: {}", pageable);
        Page<Assessment> assessments = assessmentRepository.findAll(pageable);
        return assessments.map(this::mapToAssessmentResponse);
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsByType(AssessmentType type, Pageable pageable) {
        log.debug("Fetching assessments by type: {} with pagination: {}", type, pageable);
        Page<Assessment> allAssessments = assessmentRepository.findAll(pageable);
        List<Assessment> filteredAssessments = allAssessments.getContent().stream()
                .filter(assessment -> assessment.getType().equals(type))
                .collect(Collectors.toList());
        return new PageImpl<>(filteredAssessments.stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList()), pageable, filteredAssessments.size());
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsByDateRange(LocalDate startDate, LocalDate endDate,
            Pageable pageable) {
        log.debug("Fetching assessments between {} and {} with pagination: {}", startDate, endDate, pageable);
        List<Assessment> assessments = assessmentRepository.findByDueDateBetween(startDate, endDate);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), assessments.size());
        List<Assessment> pageContent = assessments.subList(start, end);

        return new PageImpl<>(pageContent.stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList()), pageable, assessments.size());
    }

    @Override
    @Transactional
    public List<StudentAssessmentResponse> gradeAssessment(Long assessmentId, GradeAssessmentRequest request) {
        log.info("Grading assessment {} for {} students", assessmentId, request.getStudentGrades().size());
        Assessment assessment = findAssessmentById(assessmentId);
        List<StudentAssessmentResponse> responses = new ArrayList<>();

        for (GradeAssessmentRequest.StudentGrade studentGrade : request.getStudentGrades()) {
            try {
                Student student = findStudentById(studentGrade.getStudentId());

                Optional<StudentAssessment> existingAssessment = studentAssessmentRepository
                        .findByAssessmentAndStudent(assessment, student);

                StudentAssessment studentAssessment;
                if (existingAssessment.isPresent()) {
                    studentAssessment = existingAssessment.get();
                } else {
                    studentAssessment = new StudentAssessment();
                    studentAssessment.setStudent(student);
                    studentAssessment.setAssessment(assessment);
                    studentAssessment.setCreatedAt(LocalDateTime.now());
                }

                studentAssessment.setScore(studentGrade.getScore());
                studentAssessment.setGrade(studentGrade.getGrade());
                studentAssessment.setFeedback(studentGrade.getFeedback());
                studentAssessment.setNotes(studentGrade.getNotes());
                studentAssessment.setGradedAt(LocalDateTime.now());
                studentAssessment.setUpdatedAt(LocalDateTime.now());

                StudentAssessment saved = studentAssessmentRepository.save(studentAssessment);
                responses.add(mapToStudentAssessmentResponse(saved));
            } catch (Exception e) {
                log.error("Failed to grade assessment for student {}: {}", studentGrade.getStudentId(), e.getMessage());
            }
        }

        log.info("Successfully graded {} out of {} students", responses.size(), request.getStudentGrades().size());
        return responses;
    }

    @Override
    public Page<StudentAssessmentResponse> getAssessmentGrades(Long assessmentId, Pageable pageable) {
        log.debug("Fetching grades for assessment: {}", assessmentId);
        Assessment assessment = findAssessmentById(assessmentId);
        List<StudentAssessment> grades = studentAssessmentRepository.findByAssessment(assessment);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), grades.size());
        List<StudentAssessment> pageContent = grades.subList(start, end);

        return new PageImpl<>(pageContent.stream()
                .map(this::mapToStudentAssessmentResponse)
                .collect(Collectors.toList()), pageable, grades.size());
    }

    @Override
    public Page<StudentAssessmentResponse> getStudentAssessments(Long studentId, Pageable pageable) {
        log.debug("Fetching assessments for student: {}", studentId);
        Student student = findStudentById(studentId);
        List<StudentAssessment> assessments = studentAssessmentRepository.findByStudent(student);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), assessments.size());
        List<StudentAssessment> pageContent = assessments.subList(start, end);

        return new PageImpl<>(pageContent.stream()
                .map(this::mapToStudentAssessmentResponse)
                .collect(Collectors.toList()), pageable, assessments.size());
    }

    @Override
    public Map<String, Object> getAssessmentStatistics() {
        log.debug("Fetching assessment statistics");
        Map<String, Object> stats = new HashMap<>();

        long totalAssessments = assessmentRepository.count();
        long gradedAssessments = studentAssessmentRepository.count();

        stats.put("totalAssessments", totalAssessments);
        stats.put("gradedAssessments", gradedAssessments);
        stats.put("averageScore", BigDecimal.ZERO);

        return stats;
    }

    @Override
    public BigDecimal calculateStudentGPA(Long studentId, String academicYear, Integer semester) {
        log.debug("Calculating GPA for student {} in {} semester {}", studentId, academicYear, semester);
        // Student student = findStudentById(studentId); // Commented out unused variable
        return BigDecimal.ZERO; // Placeholder implementation
    }

    @Override
    @Transactional
    public List<StudentAssessmentResponse> bulkGradeAssessment(Long assessmentId,
            List<GradeAssessmentRequest> requests) {
        log.info("Bulk grading assessment {} for {} requests", assessmentId, requests.size());
        List<StudentAssessmentResponse> responses = new ArrayList<>();

        for (GradeAssessmentRequest request : requests) {
            try {
                List<StudentAssessmentResponse> batchResponses = gradeAssessment(assessmentId, request);
                responses.addAll(batchResponses);
            } catch (Exception e) {
                log.error("Failed to process bulk grading request: {}", e.getMessage());
            }
        }

        log.info("Successfully processed {} bulk grading requests", responses.size());
        return responses;
    }

    @Override
    @Transactional
    public AssessmentResponse createAssessment(CreateAssessmentRequest request) {
        log.info("Creating new assessment: {}", request.getTitle());

        Subject subject = findSubjectById(request.getSubjectId());
        ClassRoom classRoom = findClassRoomById(request.getClassRoomId());
        User teacher = findUserById(request.getTeacherId());

        Assessment assessment = new Assessment();
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setType(request.getType());
        assessment.setSubject(subject);
        assessment.setClassRoom(classRoom);
        assessment.setTeacher(teacher);
        assessment.setDueDate(request.getDueDate());
        assessment.setMaxScore(request.getMaxScore());
        assessment.setAcademicYear(request.getAcademicYear());
        assessment.setSemester(request.getSemester());
        assessment.setIsActive(true);
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());

        Assessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Successfully created assessment with ID: {}", savedAssessment.getId());

        return mapToAssessmentResponse(savedAssessment);
    }

    @Override
    @Transactional
    public AssessmentResponse updateAssessment(Long assessmentId, UpdateAssessmentRequest request) {
        log.info("Updating assessment with ID: {}", assessmentId);

        Assessment assessment = findAssessmentById(assessmentId);

        if (request.getTitle() != null) {
            assessment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assessment.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            assessment.setType(request.getType());
        }
        if (request.getDueDate() != null) {
            assessment.setDueDate(request.getDueDate());
        }
        if (request.getMaxScore() != null) {
            assessment.setMaxScore(request.getMaxScore());
        }
        assessment.setUpdatedAt(LocalDateTime.now());

        Assessment updatedAssessment = assessmentRepository.save(assessment);
        log.info("Successfully updated assessment with ID: {}", updatedAssessment.getId());

        return mapToAssessmentResponse(updatedAssessment);
    }

    @Override
    public AssessmentResponse getAssessmentById(Long assessmentId) {
        log.debug("Fetching assessment with ID: {}", assessmentId);
        Assessment assessment = findAssessmentById(assessmentId);
        return mapToAssessmentResponse(assessment);
    }

    @Override
    @Transactional
    public void deleteAssessment(Long assessmentId) {
        log.info("Deleting assessment with ID: {}", assessmentId);
        Assessment assessment = findAssessmentById(assessmentId);
        assessment.setIsActive(false);
        assessment.setUpdatedAt(LocalDateTime.now());
        assessmentRepository.save(assessment);
        log.info("Successfully deleted assessment with ID: {}", assessmentId);
    }

    // Placeholder implementations for all other required methods
    @Override
    public Page<AssessmentResponse> searchAssessments(AssessmentSearchRequest request, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsByTeacher(Long teacherId, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsByClass(Long classRoomId, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsBySubject(Long subjectId, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getAssessmentsByAcademicPeriod(String academicYear, Integer semester,
            Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getUpcomingAssessments(int days, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getOverdueAssessments(Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public Page<AssessmentResponse> getUngradedAssessmentsByTeacher(Long teacherId, Pageable pageable) {
        return getAllAssessments(pageable);
    }

    @Override
    public StudentAssessmentResponse getStudentAssessmentById(Long studentAssessmentId) {
        return StudentAssessmentResponse.builder().build();
    }

    @Override
    public Page<StudentAssessmentResponse> getStudentAssessmentsByStudent(Long studentId, Pageable pageable) {
        return getStudentAssessments(studentId, pageable);
    }

    @Override
    public Page<StudentAssessmentResponse> getStudentAssessmentsByAssessment(Long assessmentId, Pageable pageable) {
        return getAssessmentGrades(assessmentId, pageable);
    }

    @Override
    public Page<StudentAssessmentResponse> getStudentAssessmentsByStudentAndPeriod(Long studentId, String academicYear,
            Integer semester, Pageable pageable) {
        return getStudentAssessments(studentId, pageable);
    }

    @Override
    public List<StudentAssessmentResponse> gradeAssessments(GradeAssessmentRequest request) {
        return gradeAssessment(request.getAssessmentId(), request);
    }

    @Override
    public StudentAssessmentResponse gradeStudentAssessment(Long assessmentId, Long studentId, BigDecimal score,
            String grade, String feedback, String notes) {
        return StudentAssessmentResponse.builder().build();
    }

    @Override
    public List<StudentAssessmentResponse> bulkUpdateGrades(Long assessmentId,
            List<GradeAssessmentRequest.StudentGrade> studentGrades) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAssessmentStatistics(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateClassAssessmentStatistics(Long classRoomId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateSubjectAssessmentStatistics(Long subjectId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateTeacherAssessmentStatistics(Long teacherId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAssessmentAnalyticsDashboard(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAssessmentCompletionStatus(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAssessmentFeedbackSummary(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getAssessmentGradeDistribution(Long assessmentId) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getClassSubjectGradeDistribution(Long classRoomId, Long subjectId, String academicYear,
            Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> calculateAssessmentClassAverage(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateStudentSubjectGrade(Long studentId, Long subjectId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateWeightedFinalGrades(Long classRoomId, Long subjectId, String academicYear,
            Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateRubricScores(Long assessmentId, Map<String, Object> rubricData) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generatePerformanceTrends(Long classRoomId, Long subjectId, String academicYear,
            Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAssessmentProgressReport(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateAssessmentQualityMetrics(Long assessmentId) {
        return new HashMap<>();
    }

    @Override
    public AssessmentResponse cloneAssessment(Long assessmentId, Long targetClassRoomId) {
        Assessment original = findAssessmentById(assessmentId);
        return mapToAssessmentResponse(original);
    }

    @Override
    public byte[] exportAssessmentResults(Long assessmentId, String format) {
        return new byte[0];
    }

    @Override
    public List<StudentAssessmentResponse> importAssessmentGrades(Long assessmentId, byte[] fileData) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> validateAssessmentCriteria(CreateAssessmentRequest request) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        return validation;
    }

    @Override
    public void archiveOldAssessments(String academicYear) {
        log.info("Archiving assessments for academic year: {}", academicYear);
    }

    // Helper methods
    private Assessment findAssessmentById(Long assessmentId) {
        return assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    }

    private Subject findSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));
    }

    private ClassRoom findClassRoomById(Long classRoomId) {
        return classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with ID: " + classRoomId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .type(assessment.getType())
                .maxScore(assessment.getMaxScore())
                .dueDate(assessment.getDueDate())
                .academicYear(assessment.getAcademicYear())
                .semester(assessment.getSemester())
                .isActive(assessment.getIsActive())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }

    private StudentAssessmentResponse mapToStudentAssessmentResponse(StudentAssessment studentAssessment) {
        return StudentAssessmentResponse.builder()
                .id(studentAssessment.getId())
                .score(studentAssessment.getScore())
                .grade(studentAssessment.getGrade())
                .feedback(studentAssessment.getFeedback())
                .notes(studentAssessment.getNotes())
                .gradedAt(studentAssessment.getGradedAt())
                .createdAt(studentAssessment.getCreatedAt())
                .updatedAt(studentAssessment.getUpdatedAt())
                .build();
    }
}