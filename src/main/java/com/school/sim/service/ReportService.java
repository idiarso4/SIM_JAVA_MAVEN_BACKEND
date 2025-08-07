package com.school.sim.service;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.AttendanceReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for comprehensive reporting functionality
 * Provides methods for generating various types of reports with caching and template management
 */
public interface ReportService {

    // Academic Reports
    
    /**
     * Generate comprehensive academic report
     */
    AcademicReportResponse generateAcademicReport(AcademicReportRequest request);
    
    /**
     * Generate student transcript
     */
    Map<String, Object> generateStudentTranscript(Long studentId, String academicYear, Integer semester);
    
    /**
     * Generate class performance report
     */
    Map<String, Object> generateClassPerformanceReport(Long classRoomId, String academicYear, Integer semester);
    
    /**
     * Generate subject performance report
     */
    Map<String, Object> generateSubjectPerformanceReport(Long subjectId, String academicYear, Integer semester);
    
    /**
     * Generate grade distribution report
     */
    Map<String, Object> generateGradeDistributionReport(String academicYear, Integer semester);
    
    /**
     * Generate top performers report
     */
    Map<String, Object> generateTopPerformersReport(String academicYear, Integer semester, Integer limit);
    
    /**
     * Generate students at risk report
     */
    Map<String, Object> generateStudentsAtRiskReport(String academicYear, Integer semester, Double threshold);
    
    // Attendance Reports
    
    /**
     * Generate comprehensive attendance report
     */
    AttendanceReportResponse generateAttendanceReport(AttendanceReportRequest request);
    
    /**
     * Generate daily attendance summary
     */
    Map<String, Object> generateDailyAttendanceSummary(LocalDate date);
    
    /**
     * Generate monthly attendance report
     */
    Map<String, Object> generateMonthlyAttendanceReport(Integer year, Integer month);
    
    /**
     * Generate class attendance report
     */
    Map<String, Object> generateClassAttendanceReport(Long classRoomId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate student attendance report
     */
    Map<String, Object> generateStudentAttendanceReport(Long studentId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate attendance trends report
     */
    Map<String, Object> generateAttendanceTrendsReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate chronic absenteeism report
     */
    Map<String, Object> generateChronicAbsenteeismReport(LocalDate startDate, LocalDate endDate, Double threshold);
    
    // Administrative Reports
    
    /**
     * Generate enrollment report
     */
    Map<String, Object> generateEnrollmentReport(String academicYear);
    
    /**
     * Generate demographic report
     */
    Map<String, Object> generateDemographicReport();
    
    /**
     * Generate teacher workload report
     */
    Map<String, Object> generateTeacherWorkloadReport(String academicYear, Integer semester);
    
    /**
     * Generate facility utilization report
     */
    Map<String, Object> generateFacilityUtilizationReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate extracurricular participation report
     */
    Map<String, Object> generateExtracurricularParticipationReport(String academicYear, Integer semester);
    
    // Financial Reports (if applicable)
    
    /**
     * Generate fee collection report
     */
    Map<String, Object> generateFeeCollectionReport(String academicYear, Integer semester);
    
    /**
     * Generate outstanding fees report
     */
    Map<String, Object> generateOutstandingFeesReport();
    
    // Custom Reports
    
    /**
     * Generate custom report based on parameters
     */
    Map<String, Object> generateCustomReport(String reportType, Map<String, Object> parameters);
    
    /**
     * Get available report types
     */
    List<Map<String, Object>> getAvailableReportTypes();
    
    /**
     * Get report parameters for a specific report type
     */
    Map<String, Object> getReportParameters(String reportType);
    
    // Report Templates
    
    /**
     * Create report template
     */
    Map<String, Object> createReportTemplate(String templateName, String templateContent, Map<String, Object> metadata);
    
    /**
     * Update report template
     */
    Map<String, Object> updateReportTemplate(Long templateId, String templateContent, Map<String, Object> metadata);
    
    /**
     * Delete report template
     */
    void deleteReportTemplate(Long templateId);
    
    /**
     * Get report template by ID
     */
    Map<String, Object> getReportTemplate(Long templateId);
    
    /**
     * Get all report templates
     */
    List<Map<String, Object>> getAllReportTemplates();
    
    /**
     * Apply template to report data
     */
    Map<String, Object> applyTemplateToReport(Long templateId, Map<String, Object> reportData);
    
    // Report Caching
    
    /**
     * Cache report result
     */
    void cacheReportResult(String cacheKey, Map<String, Object> reportData, Integer ttlMinutes);
    
    /**
     * Get cached report result
     */
    Map<String, Object> getCachedReportResult(String cacheKey);
    
    /**
     * Invalidate report cache
     */
    void invalidateReportCache(String cacheKey);
    
    /**
     * Clear all report cache
     */
    void clearAllReportCache();
    
    /**
     * Generate cache key for report
     */
    String generateReportCacheKey(String reportType, Map<String, Object> parameters);
    
    // Report Scheduling
    
    /**
     * Schedule report generation
     */
    Map<String, Object> scheduleReport(String reportType, Map<String, Object> parameters, String cronExpression);
    
    /**
     * Cancel scheduled report
     */
    void cancelScheduledReport(Long scheduleId);
    
    /**
     * Get scheduled reports
     */
    List<Map<String, Object>> getScheduledReports();
    
    /**
     * Execute scheduled report
     */
    Map<String, Object> executeScheduledReport(Long scheduleId);
    
    // Report History and Audit
    
    /**
     * Get report generation history
     */
    List<Map<String, Object>> getReportHistory(String reportType, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get report by history ID
     */
    Map<String, Object> getHistoricalReport(Long historyId);
    
    /**
     * Delete old report history
     */
    void cleanupOldReportHistory(Integer daysToKeep);
    
    // Dashboard and Analytics
    
    /**
     * Generate dashboard summary
     */
    Map<String, Object> generateDashboardSummary();
    
    /**
     * Generate KPI report
     */
    Map<String, Object> generateKPIReport(String academicYear, Integer semester);
    
    /**
     * Generate trend analysis report
     */
    Map<String, Object> generateTrendAnalysisReport(String metric, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate comparative analysis report
     */
    Map<String, Object> generateComparativeAnalysisReport(String metric, List<String> academicYears);
    
    // Report Validation and Quality
    
    /**
     * Validate report data
     */
    Map<String, Object> validateReportData(String reportType, Map<String, Object> reportData);
    
    /**
     * Check data quality for report
     */
    Map<String, Object> checkDataQuality(String reportType, Map<String, Object> parameters);
    
    /**
     * Generate data quality report
     */
    Map<String, Object> generateDataQualityReport();
    
    // Utility Methods
    
    /**
     * Get report metadata
     */
    Map<String, Object> getReportMetadata(String reportType);
    
    /**
     * Calculate report execution time
     */
    Long calculateReportExecutionTime(String reportType, Map<String, Object> parameters);
    
    /**
     * Estimate report size
     */
    Map<String, Object> estimateReportSize(String reportType, Map<String, Object> parameters);
    
    /**
     * Get report generation status
     */
    Map<String, Object> getReportGenerationStatus(String jobId);
    
    /**
     * Cancel report generation
     */
    void cancelReportGeneration(String jobId);
}