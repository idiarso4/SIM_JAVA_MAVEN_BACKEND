package com.school.sim.dto.request;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for updating existing schedules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {

    private Long classRoomId;
    private Long subjectId;
    private Long teacherId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    @Pattern(regexp = "\\d{4}/\\d{4}", message = "Academic year must be in format YYYY/YYYY")
    private String academicYear;

    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    private Integer semester;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private Boolean isActive;

    // Validation flags
    @Builder.Default
    private Boolean skipConflictCheck = false;
    @Builder.Default
    private Boolean allowOverlap = false;
}
