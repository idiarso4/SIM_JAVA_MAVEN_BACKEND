package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for attendance reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportResponse {

    private String reportId;
    private String reportType;
    private String title;
    private String description;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    private String generatedBy;
    
    // Summary statistics
    private AttendanceStatistics statistics;
    
    // Detailed data
    private List<AttendanceReportItem> items;
    
    // Charts and analytics data
    private Map<String, Object> chartData;
    private Map<String, Object> analytics;
    
    // Metadata
    private Integer totalRecords;
    private Integer totalPages;
    private Integer currentPage;
    private Map<String, Object> filters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceStatistics {
        private Long totalStudents;
        private Long totalClasses;
        private Long totalAttendanceRecords;
        
        private Long presentCount;
        private Long absentCount;
        private Long lateCount;
        private Long excusedCount;
        
        private Double overallAttendanceRate;
        private Double averageAttendanceRate;
        private Double absenteeismRate;
        private Double lateArrivalRate;
        
        private Map<String, Long> statusBreakdown;
        private Map<String, Double> ratesByPeriod;
        private Map<String, Object> trends;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceReportItem {
        private Long id;
        private String type; // STUDENT, CLASS, SUBJECT, TEACHER
        
        // Entity information
        private Long entityId;
        private String entityName;
        private String entityCode;
        private Map<String, Object> entityDetails;
        
        // Attendance data
        private Long totalSessions;
        private Long presentSessions;
        private Long absentSessions;
        private Long lateSessions;
        private Long excusedSessions;
        
        private Double attendanceRate;
        private Double absenteeismRate;
        private Double lateRate;
        
        // Time-based breakdown
        private Map<String, Long> dailyBreakdown;
        private Map<String, Long> weeklyBreakdown;
        private Map<String, Long> monthlyBreakdown;
        
        // Additional metrics
        private Integer consecutiveAbsences;
        private LocalDate lastAttendanceDate;
        private String attendancePattern;
        private Map<String, Object> additionalMetrics;
    }
}
