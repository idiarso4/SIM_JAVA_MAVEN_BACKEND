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
 * Request DTO for updating an extracurricular activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExtracurricularActivityRequest {

    @Size(max = 200, message = "Activity name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ActivityType type;

    private ActivityStatus status;

    private LocalDate activityDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maxParticipants;

    private LocalDate registrationDeadline;

    private Boolean isMandatory;

    private Boolean requiresPermission;

    @Pattern(regexp = "\\d{4}/\\d{4}", message = "Academic year must be in format YYYY/YYYY")
    private String academicYear;

    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    private Long supervisorId;

    private Long departmentId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Boolean isActive;

    // Validation flags for partial updates
    @Builder.Default
    private Boolean skipConflictCheck = false;

    @Builder.Default
    private Boolean allowPastDate = false;

    /**
     * Custom validation method for time range
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
    @AssertTrue(message = "Registration deadline must be before or on activity date")
    public boolean isValidRegistrationDeadline() {
        if (registrationDeadline == null || activityDate == null) {
            return true; // Skip validation if dates are not provided
        }
        return registrationDeadline.isBefore(activityDate) || registrationDeadline.equals(activityDate);
    }

    /**
     * Custom validation for activity date (only if allowPastDate is false)
     */
    @AssertTrue(message = "Activity date cannot be in the past")
    public boolean isValidActivityDate() {
        if (activityDate == null || allowPastDate) {
            return true; // Skip validation if date is not provided or past dates are allowed
        }
        return !activityDate.isBefore(LocalDate.now());
    }

    /**
     * Helper method to check if any field is being updated
     */
    public boolean hasUpdates() {
        return name != null || description != null || type != null || status != null ||
               activityDate != null || startTime != null || endTime != null ||
               location != null || maxParticipants != null || registrationDeadline != null ||
               isMandatory != null || requiresPermission != null || academicYear != null ||
               semester != null || supervisorId != null || departmentId != null ||
               notes != null || isActive != null;
    }
}