package com.school.sim.dto.request;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating new teaching activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeachingActivityRequest {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Topic is required")
    @Size(max = 200, message = "Topic must not exceed 200 characters")
    private String topic;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Builder.Default
    private Boolean isCompleted = false;

    // Optional overrides for schedule defaults
    private Long subjectId;
    private Long classRoomId;
    private Long teacherId;
}