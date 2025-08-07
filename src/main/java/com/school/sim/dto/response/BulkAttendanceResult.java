package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result class for bulk attendance operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceResult {
    private List<AttendanceResponse> successfulRecords;
    private List<String> errors;
    private int totalProcessed;
    private int successCount;
    private int errorCount;
}