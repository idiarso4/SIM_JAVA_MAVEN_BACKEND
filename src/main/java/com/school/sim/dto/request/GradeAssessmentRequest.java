package com.school.sim.dto.request;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for grading student assessments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeAssessmentRequest {

    @NotNull(message = "Assessment ID is required")
    private Long assessmentId;

    @NotEmpty(message = "Student grades list cannot be empty")
    private List<StudentGrade> studentGrades;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentGrade {
        
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @DecimalMin(value = "0.00", message = "Score cannot be negative")
        private BigDecimal score;

        @Size(max = 2, message = "Grade must not exceed 2 characters")
        private String grade;

        @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
        private String feedback;

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;

        @Builder.Default
        private Boolean isSubmitted = false;
    }
}
