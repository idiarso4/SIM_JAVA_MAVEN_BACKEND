package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for academic reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportResponse {

    private String reportId;
    private String reportType;
    private String title;
    private String description;
    
    private String academicYear;
    private Integer semester;
    private LocalDateTime generatedAt;
    private String generatedBy;
    
    // Summary statistics
    private AcademicStatistics statistics;
    
    // Detailed data
    private List<AcademicReportItem> items;
    
    // Rankings and analytics
    private List<StudentRanking> rankings;
    private Map<String, Object> analytics;
    private Map<String, Object> gradeDistribution;
    
    // Metadata
    private Integer totalRecords;
    private Integer totalPages;
    private Integer currentPage;
    private Map<String, Object> filters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicStatistics {
        private Long totalStudents;
        private Long totalSubjects;
        private Long totalAssessments;
        
        private BigDecimal overallAverage;
        private BigDecimal highestGrade;
        private BigDecimal lowestGrade;
        private BigDecimal medianGrade;
        
        private Double passRate;
        private Double failureRate;
        private Double excellenceRate; // Students with A grades
        
        private Map<String, Long> gradeBreakdown;
        private Map<String, BigDecimal> subjectAverages;
        private Map<String, Object> trends;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicReportItem {
        private Long id;
        private String type; // STUDENT, CLASS, SUBJECT
        
        // Entity information
        private Long entityId;
        private String entityName;
        private String entityCode;
        private Map<String, Object> entityDetails;
        
        // Academic performance
        private BigDecimal overallGrade;
        private String letterGrade;
        private Integer rank;
        private BigDecimal gpa;
        
        // Subject grades
        private List<SubjectGrade> subjectGrades;
        
        // Progress tracking
        private ProgressData progressData;
        
        // Additional metrics
        private Map<String, Object> additionalMetrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectGrade {
        private Long subjectId;
        private String subjectName;
        private String subjectCode;
        private BigDecimal finalGrade;
        private String letterGrade;
        private BigDecimal weightedGrade;
        private Integer credits;
        private List<AssessmentGrade> assessmentGrades;
        private String status; // PASSED, FAILED, INCOMPLETE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentGrade {
        private Long assessmentId;
        private String assessmentTitle;
        private String assessmentType;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private String grade;
        private BigDecimal weight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRanking {
        private Long studentId;
        private String studentName;
        private String studentNumber;
        private String className;
        private Integer rank;
        private BigDecimal overallGrade;
        private BigDecimal gpa;
        private String letterGrade;
        private Integer totalCredits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressData {
        private BigDecimal currentGrade;
        private BigDecimal previousGrade;
        private BigDecimal improvement;
        private String trend; // IMPROVING, DECLINING, STABLE
        private List<ProgressPoint> progressHistory;
        private Map<String, Object> milestones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressPoint {
        private String period;
        private BigDecimal grade;
        private Integer rank;
        private LocalDateTime recordedAt;
    }
}
