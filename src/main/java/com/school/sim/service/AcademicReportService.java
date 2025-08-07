package com.school.sim.service;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.TranscriptResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for academic reporting functionality
 * Provides methods for generating academic reports, transcripts, and analytics
 */
public interface AcademicReportService {

    /**
     * Generate comprehensive academic report
     */
    AcademicReportResponse generateAcademicReport(AcademicReportRequest request);

    /**
     * Generate student transcript
     */
    TranscriptResponse generateStudentTranscript(Long studentId, String academicYear, Integer semester);

    /**
     * Generate complete student transcript (all periods)
     */
    TranscriptResponse generateCompleteStudentTranscript(Long studentId);

    /**
     * Generate class academic report
     */
    AcademicReportResponse generateClassAcademicReport(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate subject performance report
     */
    AcademicReportResponse generateSubjectPerformanceReport(Long subjectId, String academicYear, Integer semester);

    /**
     * Generate teacher performance report
     */
    AcademicReportResponse generateTeacherPerformanceReport(Long teacherId, String academicYear, Integer semester);

    /**
     * Calculate student GPA
     */
    BigDecimal calculateStudentGPA(Long studentId, String academicYear, Integer semester);

    /**
     * Calculate cumulative GPA
     */
    BigDecimal calculateCumulativeGPA(Long studentId);

    /**
     * Calculate class average
     */
    BigDecimal calculateClassAverage(Long classRoomId, String academicYear, Integer semester);

    /**
     * Calculate subject average
     */
    BigDecimal calculateSubjectAverage(Long subjectId, String academicYear, Integer semester);

    /**
     * Generate student rankings
     */
    List<AcademicReportResponse.StudentRanking> generateStudentRankings(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate subject rankings
     */
    List<Map<String, Object>> generateSubjectRankings(String academicYear, Integer semester);

    /**
     * Generate class rankings
     */
    List<Map<String, Object>> generateClassRankings(String academicYear, Integer semester);

    /**
     * Calculate weighted final grades
     */
    Map<String, BigDecimal> calculateWeightedFinalGrades(Long studentId, String academicYear, Integer semester);

    /**
     * Generate grade distribution analysis
     */
    Map<String, Object> generateGradeDistributionAnalysis(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate progress tracking report
     */
    Map<String, Object> generateProgressTrackingReport(Long studentId, String academicYear);

    /**
     * Generate academic performance trends
     */
    List<Map<String, Object>> generatePerformanceTrends(Long classRoomId, String academicYear);

    /**
     * Generate academic analytics dashboard
     */
    Map<String, Object> generateAcademicAnalyticsDashboard(String academicYear, Integer semester);

    /**
     * Calculate pass/fail rates
     */
    Map<String, Object> calculatePassFailRates(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate honor roll list
     */
    List<Map<String, Object>> generateHonorRollList(String academicYear, Integer semester, BigDecimal minimumGPA);

    /**
     * Generate dean's list
     */
    List<Map<String, Object>> generateDeansList(String academicYear, Integer semester, BigDecimal minimumGPA);

    /**
     * Generate academic probation list
     */
    List<Map<String, Object>> generateAcademicProbationList(String academicYear, Integer semester, BigDecimal maximumGPA);

    /**
     * Calculate credit completion rates
     */
    Map<String, Object> calculateCreditCompletionRates(Long studentId);

    /**
     * Generate graduation eligibility report
     */
    Map<String, Object> generateGraduationEligibilityReport(Long studentId);

    /**
     * Generate academic standing report
     */
    Map<String, Object> generateAcademicStandingReport(Long studentId);

    /**
     * Calculate semester statistics
     */
    Map<String, Object> calculateSemesterStatistics(String academicYear, Integer semester);

    /**
     * Generate comparative analysis
     */
    Map<String, Object> generateComparativeAnalysis(List<Long> classRoomIds, String academicYear, Integer semester);

    /**
     * Generate improvement recommendations
     */
    List<Map<String, Object>> generateImprovementRecommendations(Long studentId, String academicYear, Integer semester);

    /**
     * Calculate grade point averages by period
     */
    List<Map<String, Object>> calculateGPAByPeriod(Long studentId);

    /**
     * Generate academic calendar performance
     */
    Map<String, Object> generateAcademicCalendarPerformance(String academicYear);

    /**
     * Export academic report to Excel
     */
    ByteArrayOutputStream exportAcademicReportToExcel(AcademicReportRequest request);

    /**
     * Export transcript to PDF
     */
    ByteArrayOutputStream exportTranscriptToPDF(Long studentId, String academicYear, Integer semester);

    /**
     * Export class report to Excel
     */
    ByteArrayOutputStream exportClassReportToExcel(Long classRoomId, String academicYear, Integer semester);

    /**
     * Export rankings to Excel
     */
    ByteArrayOutputStream exportRankingsToExcel(Long classRoomId, String academicYear, Integer semester);

    /**
     * Export grade distribution to Excel
     */
    ByteArrayOutputStream exportGradeDistributionToExcel(String academicYear, Integer semester);

    /**
     * Get academic reports with pagination
     */
    Page<Map<String, Object>> getAcademicReports(AcademicReportRequest request, Pageable pageable);

    /**
     * Generate custom academic report
     */
    AcademicReportResponse generateCustomAcademicReport(Map<String, Object> criteria);

    /**
     * Calculate academic metrics
     */
    Map<String, Object> calculateAcademicMetrics(String academicYear, Integer semester);

    /**
     * Generate performance benchmarks
     */
    Map<String, Object> generatePerformanceBenchmarks(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate academic quality indicators
     */
    Map<String, Object> generateAcademicQualityIndicators(String academicYear, Integer semester);

    /**
     * Calculate retention rates
     */
    Map<String, Object> calculateRetentionRates(String academicYear);

    /**
     * Generate academic success predictors
     */
    Map<String, Object> generateAcademicSuccessPredictors(Long studentId);

    /**
     * Generate curriculum effectiveness report
     */
    Map<String, Object> generateCurriculumEffectivenessReport(Long subjectId, String academicYear);

    /**
     * Calculate learning outcomes assessment
     */
    Map<String, Object> calculateLearningOutcomesAssessment(Long classRoomId, String academicYear, Integer semester);

    /**
     * Generate academic intervention recommendations
     */
    List<Map<String, Object>> generateAcademicInterventionRecommendations(String academicYear, Integer semester);
}
