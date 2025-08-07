package com.school.sim.dto.request;

import com.school.sim.entity.AssessmentType;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating new assessments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentRequest {

    @NotBlank(message = "Assessment title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Class room ID is required")
    private Long classRoomId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Maximum score is required")
    @DecimalMin(value = "0.01", message = "Maximum score must be greater than 0")
    @DecimalMax(value = "999.99", message = "Maximum score must not exceed 999.99")
    private BigDecimal maxScore;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "1.00", message = "Weight must not exceed 1.00")
    private BigDecimal weight;

    private LocalDate dueDate;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "\\d{4}/\\d{4}", message = "Academic year must be in format YYYY/YYYY")
    private String academicYear;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @Size(max = 2000, message = "Instructions must not exceed 2000 characters")
    private String instructions;

    @Builder.Default
    private Boolean isActive = true;
}
