package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for student academic transcript
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResponse {

    // Student information
    private StudentInfo student;
    
    // Academic periods
    private List<AcademicPeriod> academicPeriods;
    
    // Overall summary
    private TranscriptSummary summary;
    
    // Generation metadata
    private LocalDateTime generatedAt;
    private String generatedBy;
    private String transcriptId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String studentNumber;
        private String className;
        private String majorName;
        private String departmentName;
        private LocalDate enrollmentDate;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicPeriod {
        private String academicYear;
        private Integer semester;
        private List<SubjectRecord> subjects;
        private BigDecimal semesterGPA;
        private BigDecimal cumulativeGPA;
        private Integer totalCredits;
        private Integer earnedCredits;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectRecord {
        private String subjectCode;
        private String subjectName;
        private Integer credits;
        private BigDecimal finalGrade;
        private String letterGrade;
        private String status; // PASSED, FAILED, INCOMPLETE, WITHDRAWN
        private List<AssessmentRecord> assessments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentRecord {
        private String title;
        private String type;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private String grade;
        private BigDecimal weight;
        private LocalDate assessmentDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranscriptSummary {
        private BigDecimal overallGPA;
        private Integer totalCreditsAttempted;
        private Integer totalCreditsEarned;
        private BigDecimal completionRate;
        private String academicStanding; // EXCELLENT, GOOD, SATISFACTORY, PROBATION
        private List<Honor> honors;
        private List<String> remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Honor {
        private String type; // DEAN_LIST, HONOR_ROLL, MAGNA_CUM_LAUDE, etc.
        private String academicYear;
        private Integer semester;
        private String description;
    }
}
