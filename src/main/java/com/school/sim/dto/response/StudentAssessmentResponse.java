package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for student assessment data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssessmentResponse {

    private Long id;
    
    // Assessment info
    private AssessmentInfo assessment;
    
    // Student info
    private StudentInfo student;
    
    // Grading info
    private BigDecimal score;
    private String grade;
    private Boolean isSubmitted;
    private LocalDateTime submissionDate;
    private String feedback;
    private String notes;
    
    // Grader info
    private GraderInfo gradedBy;
    private LocalDateTime gradedAt;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentInfo {
        private Long id;
        private String title;
        private String type;
        private BigDecimal maxScore;
        private BigDecimal weight;
        private LocalDateTime dueDate;
        private String academicYear;
        private Integer semester;
    }

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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraderInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String username;
    }
}
