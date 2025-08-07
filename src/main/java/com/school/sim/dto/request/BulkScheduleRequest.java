package com.school.sim.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk schedule operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkScheduleRequest {

    @NotEmpty(message = "Schedule list cannot be empty")
    @Valid
    private List<CreateScheduleRequest> schedules;

    private Boolean skipConflictCheck = false;
    private Boolean allowOverlap = false;
    private Boolean validateAll = true;
    private Boolean stopOnFirstError = true;
}
