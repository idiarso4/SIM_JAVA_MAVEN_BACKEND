package com.school.sim.dto.request;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for updating existing teaching activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeachingActivityRequest {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Size(max = 200, message = "Topic must not exceed 200 characters")
    private String topic;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private Boolean isCompleted;

    // Optional overrides
    private Long subjectId;
    private Long classRoomId;
    private Long teacherId;
}