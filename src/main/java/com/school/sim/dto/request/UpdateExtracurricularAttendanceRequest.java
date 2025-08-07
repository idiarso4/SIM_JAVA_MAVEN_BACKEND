package com.school.sim.dto.request;

import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalTime;

/**
 * Request DTO for updating extracurricular attendance record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExtracurricularAttendanceRequest {

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private AttendanceStatus status;

    @Min(value = 0, message = "Participation score must be between 0 and 100")
    @Max(value = 100, message = "Participation score must be between 0 and 100")
    private Integer participationScore;

    @Min(value = 1, message = "Performance rating must be between 1 and 5")
    @Max(value = 5, message = "Performance rating must be between 1 and 5")
    private Integer performanceRating;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Boolean isExcused;

    @Size(max = 500, message = "Excuse reason must not exceed 500 characters")
    private String excuseReason;

    @Min(value = 0, message = "Late arrival minutes cannot be negative")
    private Integer lateArrivalMinutes;

    @Min(value = 0, message = "Early departure minutes cannot be negative")
    private Integer earlyDepartureMinutes;

    @Min(value = 0, message = "Achievement points cannot be negative")
    private Integer achievementPoints;

    private Boolean isActive;

    /**
     * Custom validation for check-in and check-out times
     */
    @AssertTrue(message = "Check-out time must be after check-in time")
    public boolean isValidTimeRange() {
        if (checkInTime == null || checkOutTime == null) {
            return true; // Skip validation if times are not provided
        }
        return checkOutTime.isAfter(checkInTime);
    }

    /**
     * Custom validation for excuse reason when excused
     */
    @AssertTrue(message = "Excuse reason is required when attendance is excused")
    public boolean isValidExcuseReason() {
        if (isExcused != null && isExcused && status == AttendanceStatus.EXCUSED) {
            return excuseReason != null && !excuseReason.trim().isEmpty();
        }
        return true;
    }

    /**
     * Helper method to check if any field is being updated
     */
    public boolean hasUpdates() {
        return checkInTime != null || checkOutTime != null || status != null ||
               participationScore != null || performanceRating != null || notes != null ||
               isExcused != null || excuseReason != null || lateArrivalMinutes != null ||
               earlyDepartureMinutes != null || achievementPoints != null || isActive != null;
    }
}