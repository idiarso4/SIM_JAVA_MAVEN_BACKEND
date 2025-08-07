package com.school.sim.service;

import com.school.sim.dto.request.*;
import com.school.sim.dto.response.AssessmentResponse;
import com.school.sim.dto.response.StudentAssessmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for assessment management functionality
 * Handles assessment lifecycle, grading, and evaluation operations
 */
public interface AssessmentService {

    /**
     * Get all assessments with pagination
     */
    Page<AssessmentResponse> getAllAssessments(Pageable pageable);

    /**
     * Get assessments by type
     */
    Page<AssessmentResponse> getAssessmentsByType(com.school.sim.entity.AssessmentType type, Pageable pageable);

    /**
     * Get assessments by date range
     */
    Page<AssessmentResponse> getAssessmentsByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable);

    /**
     * Grade assessment for multiple students
     */
    List<StudentAssessmentResponse> gradeAssessment(Long assessmentId, GradeAssessmentRequest request);

    /**
     * Get assessment grades for an assessment
     */
    Page<StudentAssessmentResponse> getAssessmentGrades(Long assessmentId, Pageable pageable);

    /**
     * Get student assessments
     */
    Page<StudentAssessmentResponse> getStudentAssessments(Long studentId, Pageable pageable);

    /**
     * Get assessment statistics
     */
    Map<String, Object> getAssessmentStatistics();

    /**
     * Calculate student GPA
     */
    BigDecimal calculateStudentGPA(Long studentId, String academicYear, Integer semester);

    /**
     * Bulk grade assessment
     */
    List<StudentAssessmentResponse> bulkGradeAssessment(Long assessmentId, List<GradeAssessmentRequest> requests);

    /**
     * Create a new assessment
     */
    AssessmentResponse createAssessment(CreateAssessmentRequest request);

    /**
     * Update an existing assessment
     */
    AssessmentResponse updateAssessment(Long assessmentId, UpdateAssessmentRequest request);

    /**
     * Get assessment by ID
     */
    AssessmentResponse getAssessmentById(Long assessmentId);

    /**
     * Delete assessment
     */
    void deleteAssessment(Long assessmentId);

    /**
     * Search and filter assessments
     */
    Page<AssessmentResponse> searchAssessments(AssessmentSearchRequest request, Pageable pageable);

    /**
     * Get assessments by teacher
     */
    Page<AssessmentResponse> getAssessmentsByTeacher(Long teacherId, Pageable pageable);

    /**
     * Get assessments by class
     */
    Page<AssessmentResponse> getAssessmentsByClass(Long classRoomId, Pageable pageable);

    /**
     * Get assessments by subject
     */
    Page<AssessmentResponse> getAssessmentsBySubject(Long subjectId, Pageable pageable);

    /**
     * Get assessments by academic year and semester
     */
    Page<AssessmentResponse> getAssessmentsByAcademicPeriod(String academicYear, Integer semester, Pageable pageable);

    /**
     * Grade student assessments
     */
    List<StudentAssessmentResponse> gradeAssessments(GradeAssessmentRequest request);

    /**
     * Grade single student assessment
     */
    StudentAssessmentResponse gradeStudentAssessment(Long assessmentId, Long studentId, 
                                                    BigDecimal score, String grade, 
                                                    String feedback, String notes);

    /**
     * Get student assessment by ID
     */
    StudentAssessmentResponse getStudentAssessmentById(Long studentAssessmentId);

    /**
     * Get student assessments for a specific assessment
     */
    Page<StudentAssessmentResponse> getStudentAssessmentsByAssessment(Long assessmentId, Pageable pageable);

    /**
     * Get student assessments for a specific student
     */
    Page<StudentAssessmentResponse> getStudentAssessmentsByStudent(Long studentId, Pageable pageable);

    /**
     * Get student assessments by student and academic period
     */
    Page<StudentAssessmentResponse> getStudentAssessmentsByStudentAndPeriod(Long studentId, 
                                                                           String academicYear, 
                                                                           Integer semester, 
                                                                           Pageable pageable);

    /**
     * Calculate student's overall grade for a subject
     */
    Map<String, Object> calculateStudentSubjectGrade(Long studentId, Long subjectId, 
                                                     String academicYear, Integer semester);

    /**
     * Calculate class average for an assessment
     */
    Map<String, Object> calculateAssessmentClassAverage(Long assessmentId);

    /**
     * Generate assessment statistics
     */
    Map<String, Object> generateAssessmentStatistics(Long assessmentId);

    /**
     * Generate class assessment statistics
     */
    Map<String, Object> generateClassAssessmentStatistics(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate subject assessment statistics
     */
    Map<String, Object> generateSubjectAssessmentStatistics(Long subjectId, String academicYear, Integer semester);

    /**
     * Generate teacher assessment statistics
     */
    Map<String, Object> generateTeacherAssessmentStatistics(Long teacherId, String academicYear, Integer semester);

    /**
     * Get assessment grade distribution
     */
    List<Map<String, Object>> getAssessmentGradeDistribution(Long assessmentId);

    /**
     * Get class grade distribution for a subject
     */
    List<Map<String, Object>> getClassSubjectGradeDistribution(Long classRoomId, Long subjectId, 
                                                              String academicYear, Integer semester);

    /**
     * Calculate assessment rubric scores
     */
    Map<String, Object> calculateRubricScores(Long assessmentId, Map<String, Object> rubricCriteria);

    /**
     * Validate assessment criteria
     */
    Map<String, Object> validateAssessmentCriteria(CreateAssessmentRequest request);

    /**
     * Get overdue assessments
     */
    Page<AssessmentResponse> getOverdueAssessments(Pageable pageable);

    /**
     * Get upcoming assessments
     */
    Page<AssessmentResponse> getUpcomingAssessments(int days, Pageable pageable);

    /**
     * Get ungraded assessments for teacher
     */
    Page<AssessmentResponse> getUngradedAssessmentsByTeacher(Long teacherId, Pageable pageable);

    /**
     * Get assessment completion status
     */
    Map<String, Object> getAssessmentCompletionStatus(Long assessmentId);

    /**
     * Generate assessment progress report
     */
    Map<String, Object> generateAssessmentProgressReport(Long assessmentId);

    /**
     * Bulk update assessment grades
     */
    List<StudentAssessmentResponse> bulkUpdateGrades(Long assessmentId, 
                                                    List<GradeAssessmentRequest.StudentGrade> grades);

    /**
     * Export assessment results
     */
    byte[] exportAssessmentResults(Long assessmentId, String format);

    /**
     * Import assessment grades from file
     */
    List<StudentAssessmentResponse> importAssessmentGrades(Long assessmentId, byte[] fileData);

    /**
     * Clone assessment to another class
     */
    AssessmentResponse cloneAssessment(Long assessmentId, Long targetClassRoomId);

    /**
     * Archive old assessments
     */
    void archiveOldAssessments(String academicYear);

    /**
     * Get assessment analytics dashboard
     */
    Map<String, Object> getAssessmentAnalyticsDashboard(String academicYear, Integer semester);

    /**
     * Generate assessment performance trends
     */
    List<Map<String, Object>> generatePerformanceTrends(Long classRoomId, Long subjectId, 
                                                        String academicYear, Integer semester);

    /**
     * Calculate weighted final grades
     */
    Map<String, Object> calculateWeightedFinalGrades(Long classRoomId, Long subjectId, 
                                                     String academicYear, Integer semester);

    /**
     * Get assessment feedback summary
     */
    Map<String, Object> getAssessmentFeedbackSummary(Long assessmentId);

    /**
     * Generate assessment quality metrics
     */
    Map<String, Object> generateAssessmentQualityMetrics(Long assessmentId);
}
