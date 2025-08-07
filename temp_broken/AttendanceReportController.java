package com.school.sim.controller;

import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AttendanceReportResponse;
import com.school.sim.service.AttendanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for attendance reporting functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/reports")
@RequiredArgsConstructor
@Tag(name = "Attendance Reports", description = "Attendance reporting and analytics endpoints")
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;

    @PostMapping
    @Operation(summary = "Generate comprehensive attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateAttendanceReport(
            @Valid @RequestBody AttendanceReportRequest request) {
        log.info("Generating attendance report for period: {} to {}", request.getStartDate(), request.getEndDate());
        
        AttendanceReportResponse response = attendanceReportService.generateAttendanceReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Generate student attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateStudentAttendanceReport(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating student attendance report for student: {}", studentId);
        
        AttendanceReportResponse response = attendanceReportService.generateStudentAttendanceReport(
                studentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/class/{classRoomId}")
    @Operation(summary = "Generate class attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateClassAttendanceReport(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating class attendance report for class: {}", classRoomId);
        
        AttendanceReportResponse response = attendanceReportService.generateClassAttendanceReport(
                classRoomId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Generate teacher attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateTeacherAttendanceReport(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating teacher attendance report for teacher: {}", teacherId);
        
        AttendanceReportResponse response = attendanceReportService.generateTeacherAttendanceReport(
                teacherId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Generate subject attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateSubjectAttendanceReport(
            @Parameter(description = "Subject ID") @PathVariable Long subjectId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating subject attendance report for subject: {}", subjectId);
        
        AttendanceReportResponse response = attendanceReportService.generateSubjectAttendanceReport(
                subjectId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily")
    @Operation(summary = "Generate daily attendance summary")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateDailyAttendanceSummary(
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Generating daily attendance summary for date: {}", date);
        
        List<Map<String, Object>> summary = attendanceReportService.generateDailyAttendanceSummary(date);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/weekly")
    @Operation(summary = "Generate weekly attendance summary")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateWeeklyAttendanceSummary(
            @Parameter(description = "Week start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        log.info("Generating weekly attendance summary starting from: {}", startDate);
        
        List<Map<String, Object>> summary = attendanceReportService.generateWeeklyAttendanceSummary(startDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Generate monthly attendance summary")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateMonthlyAttendanceSummary(
            @Parameter(description = "Year") @RequestParam int year,
            @Parameter(description = "Month") @RequestParam int month) {
        
        log.info("Generating monthly attendance summary for {}/{}", year, month);
        
        List<Map<String, Object>> summary = attendanceReportService.generateMonthlyAttendanceSummary(year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Generate attendance statistics dashboard")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<Map<String, Object>> generateAttendanceStatisticsDashboard(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating attendance statistics dashboard for period: {} to {}", startDate, endDate);
        
        Map<String, Object> dashboard = attendanceReportService.generateAttendanceStatisticsDashboard(startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/trends")
    @Operation(summary = "Generate attendance trend analysis")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateAttendanceTrendAnalysis(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Group by") @RequestParam(defaultValue = "DAY") String groupBy) {
        
        log.info("Generating attendance trend analysis grouped by: {}", groupBy);
        
        List<Map<String, Object>> trends = attendanceReportService.generateAttendanceTrendAnalysis(startDate, endDate, groupBy);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/absenteeism")
    @Operation(summary = "Generate absenteeism report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generateAbsenteeismReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Minimum absences") @RequestParam(defaultValue = "3") int minAbsences) {
        
        log.info("Generating absenteeism report with minimum {} absences", minAbsences);
        
        AttendanceReportResponse response = attendanceReportService.generateAbsenteeismReport(startDate, endDate, minAbsences);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/perfect-attendance")
    @Operation(summary = "Generate perfect attendance report")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<AttendanceReportResponse> generatePerfectAttendanceReport(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating perfect attendance report");
        
        AttendanceReportResponse response = attendanceReportService.generatePerfectAttendanceReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Generate attendance analytics")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<Map<String, Object>> generateAttendanceAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating attendance analytics for period: {} to {}", startDate, endDate);
        
        Map<String, Object> analytics = attendanceReportService.generateAttendanceAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get attendance reports with pagination")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<Page<Map<String, Object>>> getAttendanceReports(
            @Valid AttendanceReportRequest request,
            Pageable pageable) {
        
        log.info("Getting paginated attendance reports");
        
        Page<Map<String, Object>> reports = attendanceReportService.getAttendanceReports(request, pageable);
        return ResponseEntity.ok(reports);
    }

    // Export endpoints
    @PostMapping("/export/excel")
    @Operation(summary = "Export attendance report to Excel")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<byte[]> exportAttendanceReportToExcel(
            @Valid @RequestBody AttendanceReportRequest request) {
        
        log.info("Exporting attendance report to Excel");
        
        ByteArrayOutputStream outputStream = attendanceReportService.exportAttendanceReportToExcel(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "attendance-report.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/daily/{date}/excel")
    @Operation(summary = "Export daily attendance to Excel")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<byte[]> exportDailyAttendanceToExcel(
            @Parameter(description = "Date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Exporting daily attendance to Excel for date: {}", date);
        
        ByteArrayOutputStream outputStream = attendanceReportService.exportDailyAttendanceToExcel(date);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "daily-attendance-" + date + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/monthly/{year}/{month}/excel")
    @Operation(summary = "Export monthly attendance to Excel")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<byte[]> exportMonthlyAttendanceToExcel(
            @Parameter(description = "Year") @PathVariable int year,
            @Parameter(description = "Month") @PathVariable int month) {
        
        log.info("Exporting monthly attendance to Excel for {}/{}", year, month);
        
        ByteArrayOutputStream outputStream = attendanceReportService.exportMonthlyAttendanceToExcel(year, month);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "monthly-attendance-" + year + "-" + month + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/student/{studentId}/excel")
    @Operation(summary = "Export student attendance summary to Excel")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<byte[]> exportStudentAttendanceSummaryToExcel(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Exporting student attendance summary to Excel for student: {}", studentId);
        
        ByteArrayOutputStream outputStream = attendanceReportService.exportStudentAttendanceSummaryToExcel(
                studentId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "student-attendance-" + studentId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/class/{classRoomId}/excel")
    @Operation(summary = "Export class attendance summary to Excel")
    @PreAuthorize("hasPermission('ATTENDANCE', 'READ')")
    public ResponseEntity<byte[]> exportClassAttendanceSummaryToExcel(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Exporting class attendance summary to Excel for class: {}", classRoomId);
        
        ByteArrayOutputStream outputStream = attendanceReportService.exportClassAttendanceSummaryToExcel(
                classRoomId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "class-attendance-" + classRoomId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
