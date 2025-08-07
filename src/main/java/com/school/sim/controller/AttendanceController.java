package com.school.sim.controller;

import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.request.BulkAttendanceRequest;
import com.school.sim.dto.request.CreateAttendanceRequest;
import com.school.sim.dto.request.UpdateAttendanceRequest;
import com.school.sim.dto.response.AttendanceReportResponse;
import com.school.sim.dto.response.AttendanceResponse;

import com.school.sim.entity.AttendanceStatus;
import com.school.sim.service.AttendanceReportService;
import com.school.sim.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for attendance management operations
 * Provides endpoints for attendance recording, tracking, and reporting
 */
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Management", description = "Attendance management endpoints")
@Validated
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceReportService attendanceReportService;

    /**
     * Record attendance
     */
    @PostMapping
    @Operation(summary = "Record attendance", description = "Record attendance for a student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Attendance recorded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Attendance already exists")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceResponse> recordAttendance(@Valid @RequestBody CreateAttendanceRequest request) {
        logger.info("Recording attendance for student {} in teaching activity {}", request.getStudentId(), request.getTeachingActivityId());
        
        try {
            AttendanceResponse response = attendanceService.recordAttendance(request);
            logger.info("Successfully recorded attendance with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to record attendance for student {} in teaching activity {}", request.getStudentId(), request.getTeachingActivityId(), e);
            throw e;
        }
    }

    /**
     * Get all attendance records with pagination
     */
    @GetMapping
    @Operation(summary = "Get all attendance", description = "Retrieve all attendance records with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AttendanceResponse>> getAllAttendance(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Fetching all attendance records - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AttendanceResponse> attendance = attendanceService.getAllAttendance(pageable);
        logger.debug("Retrieved {} attendance records", attendance.getTotalElements());
        
        return ResponseEntity.ok(attendance);
    }

    /**
     * Get attendance by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID", description = "Retrieve attendance record by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance record found"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable("id") @NotNull Long attendanceId) {
        logger.debug("Fetching attendance record by ID: {}", attendanceId);
        
        Optional<AttendanceResponse> attendance = attendanceService.getAttendanceById(attendanceId);
        if (attendance.isPresent()) {
            logger.debug("Attendance record found with ID: {}", attendanceId);
            return ResponseEntity.ok(attendance.get());
        } else {
            logger.debug("Attendance record not found with ID: {}", attendanceId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update attendance record
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update attendance", description = "Update attendance record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance updated successfully"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable("id") @NotNull Long attendanceId,
            @Valid @RequestBody UpdateAttendanceRequest request) {
        
        logger.info("Updating attendance record with ID: {}", attendanceId);
        
        try {
            AttendanceResponse response = attendanceService.updateAttendance(attendanceId, request);
            logger.info("Successfully updated attendance record with ID: {}", attendanceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update attendance record with ID: {}", attendanceId, e);
            throw e;
        }
    }

    /**
     * Delete attendance record
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance", description = "Delete attendance record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Attendance deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Attendance record not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable("id") @NotNull Long attendanceId) {
        logger.info("Deleting attendance record with ID: {}", attendanceId);
        
        try {
            attendanceService.deleteAttendance(attendanceId);
            logger.info("Successfully deleted attendance record with ID: {}", attendanceId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete attendance record with ID: {}", attendanceId, e);
            throw e;
        }
    }

    /**
     * Get attendance by student
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get attendance by student", description = "Get all attendance records for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceByStudent(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching attendance records for student: {}", studentId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceResponse> attendance = attendanceService.getAttendanceByStudent(studentId, pageable);
        
        logger.debug("Retrieved {} attendance records for student: {}", attendance.getTotalElements(), studentId);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Get attendance by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get attendance by date range", description = "Get attendance records within date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching attendance records from {} to {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceResponse> attendance = attendanceService.getAttendanceByDateRange(startDate, endDate, pageable);
        
        logger.debug("Retrieved {} attendance records for date range", attendance.getTotalElements());
        return ResponseEntity.ok(attendance);
    }

    /**
     * Get attendance by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get attendance by status", description = "Get attendance records by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceByStatus(
            @PathVariable("status") AttendanceStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching attendance records by status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceResponse> attendance = attendanceService.getAttendanceByStatus(status, pageable);
        
        logger.debug("Retrieved {} attendance records with status: {}", attendance.getTotalElements(), status);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Record bulk attendance
     */
    @PostMapping("/bulk")
    @Operation(summary = "Record bulk attendance", description = "Record attendance for multiple students")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk attendance recorded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> recordBulkAttendance(@Valid @RequestBody BulkAttendanceRequest request) {
        logger.info("Recording bulk attendance for {} students", request.getStudentAttendances().size());
        
        try {
            AttendanceService.BulkAttendanceResult result = attendanceService.bulkRecordAttendance(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk attendance recorded successfully");
            response.put("totalProcessed", result.getTotalProcessed());
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            response.put("successfulRecords", result.getSuccessfulRecords());
            response.put("errors", result.getErrors());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully processed bulk attendance for {} students", result.getTotalProcessed());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to record bulk attendance", e);
            throw e;
        }
    }

    /**
     * Get attendance statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get attendance statistics", description = "Get attendance statistics and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Fetching attendance statistics");
        
        try {
            Map<String, Object> statistics = attendanceService.getAttendanceStatistics(startDate, endDate);
            statistics.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved attendance statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get attendance statistics", e);
            throw e;
        }
    }

    /**
     * Generate attendance report
     */
    @PostMapping("/reports")
    @Operation(summary = "Generate attendance report", description = "Generate comprehensive attendance report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AttendanceReportResponse> generateAttendanceReport(@Valid @RequestBody AttendanceReportRequest request) {
        logger.info("Generating attendance report for period: {} to {}", request.getStartDate(), request.getEndDate());
        
        try {
            AttendanceReportResponse response = attendanceReportService.generateAttendanceReport(request);
            logger.info("Successfully generated attendance report");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to generate attendance report", e);
            throw e;
        }
    }

    /**
     * Export attendance report to Excel
     */
    @PostMapping("/reports/export")
    @Operation(summary = "Export attendance report", description = "Export attendance report to Excel")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report exported successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportAttendanceReport(@Valid @RequestBody AttendanceReportRequest request) {
        logger.info("Exporting attendance report to Excel");
        
        try {
            ByteArrayOutputStream outputStream = attendanceReportService.exportAttendanceReportToExcel(request);
            byte[] excelData = outputStream.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "attendance_report.xlsx");
            headers.setContentLength(excelData.length);
            
            logger.info("Successfully exported attendance report to Excel");
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
        } catch (Exception e) {
            logger.error("Failed to export attendance report to Excel", e);
            throw e;
        }
    }

    /**
     * Get daily attendance summary
     */
    @GetMapping("/daily-summary")
    @Operation(summary = "Get daily attendance summary", description = "Get attendance summary for a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getDailyAttendanceSummary(
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.debug("Fetching daily attendance summary for: {}", date);
        
        try {
            Map<String, Object> summary = attendanceService.getDailyAttendanceSummary(date);
            summary.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved daily attendance summary for: {}", date);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Failed to get daily attendance summary for: {}", date, e);
            throw e;
        }
    }

    /**
     * Get student attendance rate
     */
    @GetMapping("/student/{studentId}/rate")
    @Operation(summary = "Get student attendance rate", description = "Calculate attendance rate for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance rate calculated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceRate(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Calculating attendance rate for student: {}", studentId);
        
        try {
            Double attendanceRate = attendanceService.calculateStudentAttendanceRate(studentId, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("studentId", studentId);
            response.put("attendanceRate", attendanceRate);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Calculated attendance rate for student {}: {}%", studentId, attendanceRate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to calculate attendance rate for student: {}", studentId, e);
            throw e;
        }
    }
}