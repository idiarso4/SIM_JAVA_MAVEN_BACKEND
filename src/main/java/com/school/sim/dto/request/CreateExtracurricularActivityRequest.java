package com.school.sim.dto.request;

import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating a new extracurricular activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExtracurricularActivityRequest {

    @NotBlank(message = "Activity name is required")
    @Size(max = 200, message = "Activity name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Activity type is required")
    private ActivityType type;

    @NotNull(message = "Activity status is required")
    private ActivityStatus status;

    @NotNull(message = "Activity date is required")
    @Future(message = "Activity date must be in the future")
    private LocalDate activityDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maxParticipants;

    private LocalDate registrationDeadline;

    @Builder.Default
    private Boolean isMandatory = false;

    @Builder.Default
    private Boolean requiresPermission = false;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "\\d{4}/\\d{4}", message = "Academic year must be in format YYYY/YYYY")
    private String academicYear;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;

    private Long departmentId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Builder.Default
    private Boolean isActive = true;

    /**
     * Custom validation method
     */
    @AssertTrue(message = "End time must be after start time")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // Skip validation if times are not provided
        }
        return endTime.isAfter(startTime);
    }

    /**
     * Custom validation for registration deadline
     */
    @AssertTrue(message = "Registration deadline must be before activity date")
    public boolean isValidRegistrationDeadline() {
        if (registrationDeadline == null || activityDate == null) {
            return true; // Skip validation if dates are not provided
        }
        return registrationDeadline.isBefore(activityDate) || registrationDeadline.equals(activityDate);
    }

    /**
     * Custom validation for activity date
     */
    @AssertTrue(message = "Activity date cannot be in the past")
    public boolean isValidActivityDate() {
        if (activityDate == null) {
            return true; // Skip validation if date is not provided
        }
        return !activityDate.isBefore(LocalDate.now());
    }
}