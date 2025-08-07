package com.school.sim.controller;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.AttendanceReportResponse;
import com.school.sim.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for comprehensive reporting functionality
 * Provides endpoints for generating various types of reports
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Report Management", description = "Comprehensive reporting endpoints")
@Validated
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    // Academic Reports

    /**
     * Generate comprehensive academic report
     */
    @PostMapping("/academic")
    @Operation(summary = "Generate academic report", description = "Generate comprehensive academic report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AcademicReportResponse> generateAcademicReport(@Valid @RequestBody AcademicReportRequest request) {
        logger.info("Generating academic report for period: {} - {}", request.getAcademicYear(), request.getSemester());
        
        try {
            AcademicReportResponse response = reportService.generateAcademicReport(request);
            logger.info("Successfully generated academic report");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to generate academic report", e);
            throw e;
        }
    }

    /**
     * Generate student transcript
     */
    @GetMapping("/academic/transcript/student/{studentId}")
    @Operation(summary = "Generate student transcript", description = "Generate transcript for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transcript generated successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateStudentTranscript(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        logger.info("Generating transcript for student: {} for {}-{}", studentId, academicYear, semester);
        
        try {
            Map<String, Object> transcript = reportService.generateStudentTranscript(studentId, academicYear, semester);
            logger.info("Successfully generated transcript for student: {}", studentId);
            return ResponseEntity.ok(transcript);
        } catch (Exception e) {
            logger.error("Failed to generate transcript for student: {}", studentId, e);
            throw e;
        }
    }

    /**
     * Generate class performance report
     */
    @GetMapping("/academic/performance/class/{classRoomId}")
    @Operation(summary = "Generate class performance report", description = "Generate performance report for a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateClassPerformanceReport(
            @PathVariable("classRoomId") @NotNull Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        logger.info("Generating class performance report for class: {} for {}-{}", classRoomId, academicYear, semester);
        
        try {
            Map<String, Object> report = reportService.generateClassPerformanceReport(classRoomId, academicYear, semester);
            logger.info("Successfully generated class performance report for class: {}", classRoomId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate class performance report for class: {}", classRoomId, e);
            throw e;
        }
    }

    /**
     * Generate subject performance report
     */
    @GetMapping("/academic/performance/subject/{subjectId}")
    @Operation(summary = "Generate subject performance report", description = "Generate performance report for a specific subject")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "404", description = "Subject not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateSubjectPerformanceReport(
            @PathVariable("subjectId") @NotNull Long subjectId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        logger.info("Generating subject performance report for subject: {} for {}-{}", subjectId, academicYear, semester);
        
        try {
            Map<String, Object> report = reportService.generateSubjectPerformanceReport(subjectId, academicYear, semester);
            logger.info("Successfully generated subject performance report for subject: {}", subjectId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate subject performance report for subject: {}", subjectId, e);
            throw e;
        }
    }

    /**
     * Generate grade distribution report
     */
    @GetMapping("/academic/grade-distribution")
    @Operation(summary = "Generate grade distribution report", description = "Generate grade distribution analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateGradeDistributionReport(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        logger.info("Generating grade distribution report for {}-{}", academicYear, semester);
        
        try {
            Map<String, Object> report = reportService.generateGradeDistributionReport(academicYear, semester);
            logger.info("Successfully generated grade distribution report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate grade distribution report", e);
            throw e;
        }
    }

    /**
     * Generate top performers report
     */
    @GetMapping("/academic/top-performers")
    @Operation(summary = "Generate top performers report", description = "Generate report of top performing students")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateTopPerformersReport(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester,
            @Parameter(description = "Number of top performers") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.info("Generating top performers report for {}-{} (limit: {})", academicYear, semester, limit);
        
        try {
            Map<String, Object> report = reportService.generateTopPerformersReport(academicYear, semester, limit);
            logger.info("Successfully generated top performers report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate top performers report", e);
            throw e;
        }
    }

    /**
     * Generate students at risk report
     */
    @GetMapping("/academic/students-at-risk")
    @Operation(summary = "Generate students at risk report", description = "Generate report of students at academic risk")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateStudentsAtRiskReport(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester,
            @Parameter(description = "GPA threshold") @RequestParam(defaultValue = "2.0") Double threshold) {
        
        logger.info("Generating students at risk report for {}-{} (threshold: {})", academicYear, semester, threshold);
        
        try {
            Map<String, Object> report = reportService.generateStudentsAtRiskReport(academicYear, semester, threshold);
            logger.info("Successfully generated students at risk report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate students at risk report", e);
            throw e;
        }
    }

    // Attendance Reports

    /**
     * Generate comprehensive attendance report
     */
    @PostMapping("/attendance")
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
            AttendanceReportResponse response = reportService.generateAttendanceReport(request);
            logger.info("Successfully generated attendance report");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to generate attendance report", e);
            throw e;
        }
    }

    /**
     * Generate daily attendance summary
     */
    @GetMapping("/attendance/daily-summary")
    @Operation(summary = "Generate daily attendance summary", description = "Generate attendance summary for a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateDailyAttendanceSummary(
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.info("Generating daily attendance summary for: {}", date);
        
        try {
            Map<String, Object> summary = reportService.generateDailyAttendanceSummary(date);
            logger.info("Successfully generated daily attendance summary for: {}", date);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Failed to generate daily attendance summary for: {}", date, e);
            throw e;
        }
    }

    /**
     * Generate monthly attendance report
     */
    @GetMapping("/attendance/monthly")
    @Operation(summary = "Generate monthly attendance report", description = "Generate attendance report for a specific month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateMonthlyAttendanceReport(
            @Parameter(description = "Year") @RequestParam Integer year,
            @Parameter(description = "Month") @RequestParam @Min(1) Integer month) {
        
        logger.info("Generating monthly attendance report for {}/{}", year, month);
        
        try {
            Map<String, Object> report = reportService.generateMonthlyAttendanceReport(year, month);
            logger.info("Successfully generated monthly attendance report for {}/{}", year, month);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate monthly attendance report for {}/{}", year, month, e);
            throw e;
        }
    }

    /**
     * Generate class attendance report
     */
    @GetMapping("/attendance/class/{classRoomId}")
    @Operation(summary = "Generate class attendance report", description = "Generate attendance report for a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateClassAttendanceReport(
            @PathVariable("classRoomId") @NotNull Long classRoomId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.info("Generating class attendance report for class: {} from {} to {}", classRoomId, startDate, endDate);
        
        try {
            Map<String, Object> report = reportService.generateClassAttendanceReport(classRoomId, startDate, endDate);
            logger.info("Successfully generated class attendance report for class: {}", classRoomId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate class attendance report for class: {}", classRoomId, e);
            throw e;
        }
    }

    /**
     * Generate student attendance report
     */
    @GetMapping("/attendance/student/{studentId}")
    @Operation(summary = "Generate student attendance report", description = "Generate attendance report for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateStudentAttendanceReport(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.info("Generating student attendance report for student: {} from {} to {}", studentId, startDate, endDate);
        
        try {
            Map<String, Object> report = reportService.generateStudentAttendanceReport(studentId, startDate, endDate);
            logger.info("Successfully generated student attendance report for student: {}", studentId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate student attendance report for student: {}", studentId, e);
            throw e;
        }
    }

    // Administrative Reports

    /**
     * Generate enrollment report
     */
    @GetMapping("/administrative/enrollment")
    @Operation(summary = "Generate enrollment report", description = "Generate student enrollment report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> generateEnrollmentReport(
            @Parameter(description = "Academic year") @RequestParam String academicYear) {
        
        logger.info("Generating enrollment report for academic year: {}", academicYear);
        
        try {
            Map<String, Object> report = reportService.generateEnrollmentReport(academicYear);
            logger.info("Successfully generated enrollment report for academic year: {}", academicYear);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate enrollment report for academic year: {}", academicYear, e);
            throw e;
        }
    }

    /**
     * Generate demographic report
     */
    @GetMapping("/administrative/demographics")
    @Operation(summary = "Generate demographic report", description = "Generate student demographic analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> generateDemographicReport() {
        logger.info("Generating demographic report");
        
        try {
            Map<String, Object> report = reportService.generateDemographicReport();
            logger.info("Successfully generated demographic report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate demographic report", e);
            throw e;
        }
    }

    // Dashboard and Analytics

    /**
     * Generate dashboard summary
     */
    @GetMapping("/dashboard/summary")
    @Operation(summary = "Generate dashboard summary", description = "Generate comprehensive dashboard summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> generateDashboardSummary() {
        logger.info("Generating dashboard summary");
        
        try {
            Map<String, Object> summary = reportService.generateDashboardSummary();
            logger.info("Successfully generated dashboard summary");
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Failed to generate dashboard summary", e);
            throw e;
        }
    }

    /**
     * Generate KPI report
     */
    @GetMapping("/analytics/kpi")
    @Operation(summary = "Generate KPI report", description = "Generate Key Performance Indicators report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> generateKPIReport(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        logger.info("Generating KPI report for {}-{}", academicYear, semester);
        
        try {
            Map<String, Object> report = reportService.generateKPIReport(academicYear, semester);
            logger.info("Successfully generated KPI report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate KPI report", e);
            throw e;
        }
    }

    // Report Management

    /**
     * Get available report types
     */
    @GetMapping("/types")
    @Operation(summary = "Get available report types", description = "Get list of available report types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report types retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableReportTypes() {
        logger.info("Fetching available report types");
        
        try {
            List<Map<String, Object>> reportTypes = reportService.getAvailableReportTypes();
            logger.info("Successfully retrieved {} report types", reportTypes.size());
            return ResponseEntity.ok(reportTypes);
        } catch (Exception e) {
            logger.error("Failed to get available report types", e);
            throw e;
        }
    }

    /**
     * Clear report cache
     */
    @DeleteMapping("/cache")
    @Operation(summary = "Clear report cache", description = "Clear all cached report data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> clearReportCache() {
        logger.info("Clearing report cache");
        
        try {
            reportService.clearAllReportCache();
            
            Map<String, Object> response = Map.of(
                "message", "Report cache cleared successfully",
                "timestamp", System.currentTimeMillis()
            );
            
            logger.info("Successfully cleared report cache");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to clear report cache", e);
            throw e;
        }
    }
}