package com.school.sim.service;

import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AttendanceReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for attendance reporting functionality
 * Provides methods for generating various attendance reports and analytics
 */
public interface AttendanceReportService {

    /**
     * Generate comprehensive attendance report
     */
    AttendanceReportResponse generateAttendanceReport(AttendanceReportRequest request);

    /**
     * Generate student attendance report
     */
    AttendanceReportResponse generateStudentAttendanceReport(Long studentId, 
                                                            LocalDate startDate, 
                                                            LocalDate endDate);

    /**
     * Generate class attendance report
     */
    AttendanceReportResponse generateClassAttendanceReport(Long classRoomId, 
                                                          LocalDate startDate, 
                                                          LocalDate endDate);

    /**
     * Generate teacher attendance report
     */
    AttendanceReportResponse generateTeacherAttendanceReport(Long teacherId, 
                                                            LocalDate startDate, 
                                                            LocalDate endDate);

    /**
     * Generate subject attendance report
     */
    AttendanceReportResponse generateSubjectAttendanceReport(Long subjectId, 
                                                            LocalDate startDate, 
                                                            LocalDate endDate);

    /**
     * Generate daily attendance summary
     */
    List<Map<String, Object>> generateDailyAttendanceSummary(LocalDate date);

    /**
     * Generate weekly attendance summary
     */
    List<Map<String, Object>> generateWeeklyAttendanceSummary(LocalDate startDate);

    /**
     * Generate monthly attendance summary
     */
    List<Map<String, Object>> generateMonthlyAttendanceSummary(int year, int month);

    /**
     * Generate attendance statistics dashboard
     */
    Map<String, Object> generateAttendanceStatisticsDashboard(LocalDate startDate, LocalDate endDate);

    /**
     * Generate attendance trend analysis
     */
    List<Map<String, Object>> generateAttendanceTrendAnalysis(LocalDate startDate, 
                                                             LocalDate endDate, 
                                                             String groupBy);

    /**
     * Generate absenteeism report
     */
    AttendanceReportResponse generateAbsenteeismReport(LocalDate startDate, 
                                                      LocalDate endDate, 
                                                      int minAbsences);

    /**
     * Generate perfect attendance report
     */
    AttendanceReportResponse generatePerfectAttendanceReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generate attendance rate comparison report
     */
    List<Map<String, Object>> generateAttendanceRateComparison(LocalDate startDate, 
                                                              LocalDate endDate, 
                                                              String compareBy);

    /**
     * Generate late arrival report
     */
    AttendanceReportResponse generateLateArrivalReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generate attendance follow-up report
     */
    AttendanceReportResponse generateAttendanceFollowUpReport(LocalDate fromDate);

    /**
     * Export attendance report to Excel
     */
    ByteArrayOutputStream exportAttendanceReportToExcel(AttendanceReportRequest request);

    /**
     * Export daily attendance to Excel
     */
    ByteArrayOutputStream exportDailyAttendanceToExcel(LocalDate date);

    /**
     * Export monthly attendance to Excel
     */
    ByteArrayOutputStream exportMonthlyAttendanceToExcel(int year, int month);

    /**
     * Export student attendance summary to Excel
     */
    ByteArrayOutputStream exportStudentAttendanceSummaryToExcel(Long studentId, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate);

    /**
     * Export class attendance summary to Excel
     */
    ByteArrayOutputStream exportClassAttendanceSummaryToExcel(Long classRoomId, 
                                                             LocalDate startDate, 
                                                             LocalDate endDate);

    /**
     * Generate attendance analytics for dashboard
     */
    Map<String, Object> generateAttendanceAnalytics(LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance reports with pagination
     */
    Page<Map<String, Object>> getAttendanceReports(AttendanceReportRequest request, Pageable pageable);

    /**
     * Generate custom attendance report based on filters
     */
    AttendanceReportResponse generateCustomAttendanceReport(Map<String, Object> filters, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate);

    /**
     * Calculate attendance metrics
     */
    Map<String, Double> calculateAttendanceMetrics(LocalDate startDate, LocalDate endDate);

    /**
     * Generate attendance heatmap data
     */
    List<Map<String, Object>> generateAttendanceHeatmapData(LocalDate startDate, 
                                                           LocalDate endDate, 
                                                           String granularity);

    /**
     * Generate attendance pattern analysis
     */
    Map<String, Object> generateAttendancePatternAnalysis(Long studentId, 
                                                         LocalDate startDate, 
                                                         LocalDate endDate);

    /**
     * Generate class comparison report
     */
    List<Map<String, Object>> generateClassComparisonReport(List<Long> classRoomIds, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate);

    /**
     * Generate attendance forecast
     */
    Map<String, Object> generateAttendanceForecast(LocalDate startDate, 
                                                   LocalDate endDate, 
                                                   int forecastDays);

    /**
     * Generate attendance alert report
     */
    List<Map<String, Object>> generateAttendanceAlertReport(LocalDate startDate, 
                                                           LocalDate endDate, 
                                                           Map<String, Object> thresholds);
}
