package com.school.sim.controller;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.TranscriptResponse;
import com.school.sim.service.AcademicReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for academic reporting functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/academic/reports")
@RequiredArgsConstructor
@Tag(name = "Academic Reports", description = "Academic reporting and analytics endpoints")
public class AcademicReportController {

    private final AcademicReportService academicReportService;

    @PostMapping
    @Operation(summary = "Generate comprehensive academic report")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<AcademicReportResponse> generateAcademicReport(
            @Valid @RequestBody AcademicReportRequest request) {
        log.info("Generating academic report for academic year: {}, semester: {}", 
                request.getAcademicYear(), request.getSemester());
        
        AcademicReportResponse response = academicReportService.generateAcademicReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transcript/student/{studentId}")
    @Operation(summary = "Generate student transcript")
    @PreAuthorize("hasPermission('TRANSCRIPT', 'READ')")
    public ResponseEntity<TranscriptResponse> generateStudentTranscript(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating transcript for student: {}, period: {}/{}", studentId, academicYear, semester);
        
        TranscriptResponse response = academicReportService.generateStudentTranscript(studentId, academicYear, semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transcript/student/{studentId}/complete")
    @Operation(summary = "Generate complete student transcript")
    @PreAuthorize("hasPermission('TRANSCRIPT', 'READ')")
    public ResponseEntity<TranscriptResponse> generateCompleteStudentTranscript(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        
        log.info("Generating complete transcript for student: {}", studentId);
        
        TranscriptResponse response = academicReportService.generateCompleteStudentTranscript(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/class/{classRoomId}")
    @Operation(summary = "Generate class academic report")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<AcademicReportResponse> generateClassAcademicReport(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating class academic report for class: {}", classRoomId);
        
        AcademicReportResponse response = academicReportService.generateClassAcademicReport(
                classRoomId, academicYear, semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Generate subject performance report")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<AcademicReportResponse> generateSubjectPerformanceReport(
            @Parameter(description = "Subject ID") @PathVariable Long subjectId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating subject performance report for subject: {}", subjectId);
        
        AcademicReportResponse response = academicReportService.generateSubjectPerformanceReport(
                subjectId, academicYear, semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Generate teacher performance report")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<AcademicReportResponse> generateTeacherPerformanceReport(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating teacher performance report for teacher: {}", teacherId);
        
        AcademicReportResponse response = academicReportService.generateTeacherPerformanceReport(
                teacherId, academicYear, semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gpa/student/{studentId}")
    @Operation(summary = "Calculate student GPA")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> calculateStudentGPA(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Calculating GPA for student: {}", studentId);
        
        BigDecimal gpa = academicReportService.calculateStudentGPA(studentId, academicYear, semester);
        Map<String, Object> response = Map.of(
                "studentId", studentId,
                "academicYear", academicYear,
                "semester", semester,
                "gpa", gpa
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gpa/student/{studentId}/cumulative")
    @Operation(summary = "Calculate cumulative GPA")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> calculateCumulativeGPA(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        
        log.info("Calculating cumulative GPA for student: {}", studentId);
        
        BigDecimal cumulativeGPA = academicReportService.calculateCumulativeGPA(studentId);
        Map<String, Object> response = Map.of(
                "studentId", studentId,
                "cumulativeGPA", cumulativeGPA
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/average/class/{classRoomId}")
    @Operation(summary = "Calculate class average")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> calculateClassAverage(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Calculating class average for class: {}", classRoomId);
        
        BigDecimal average = academicReportService.calculateClassAverage(classRoomId, academicYear, semester);
        Map<String, Object> response = Map.of(
                "classRoomId", classRoomId,
                "academicYear", academicYear,
                "semester", semester,
                "average", average
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rankings/class/{classRoomId}")
    @Operation(summary = "Generate student rankings")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<List<AcademicReportResponse.StudentRanking>> generateStudentRankings(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating student rankings for class: {}", classRoomId);
        
        List<AcademicReportResponse.StudentRanking> rankings = academicReportService.generateStudentRankings(
                classRoomId, academicYear, semester);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/grade-distribution/class/{classRoomId}")
    @Operation(summary = "Generate grade distribution analysis")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> generateGradeDistributionAnalysis(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating grade distribution analysis for class: {}", classRoomId);
        
        Map<String, Object> analysis = academicReportService.generateGradeDistributionAnalysis(
                classRoomId, academicYear, semester);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/pass-fail-rates/class/{classRoomId}")
    @Operation(summary = "Calculate pass/fail rates")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> calculatePassFailRates(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Calculating pass/fail rates for class: {}", classRoomId);
        
        Map<String, Object> rates = academicReportService.calculatePassFailRates(
                classRoomId, academicYear, semester);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/honor-roll")
    @Operation(summary = "Generate honor roll list")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateHonorRollList(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester,
            @Parameter(description = "Minimum GPA") @RequestParam(defaultValue = "3.5") BigDecimal minimumGPA) {
        
        log.info("Generating honor roll list for {}/{}", academicYear, semester);
        
        List<Map<String, Object>> honorRoll = academicReportService.generateHonorRollList(
                academicYear, semester, minimumGPA);
        return ResponseEntity.ok(honorRoll);
    }

    @GetMapping("/deans-list")
    @Operation(summary = "Generate dean's list")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateDeansList(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester,
            @Parameter(description = "Minimum GPA") @RequestParam(defaultValue = "3.8") BigDecimal minimumGPA) {
        
        log.info("Generating dean's list for {}/{}", academicYear, semester);
        
        List<Map<String, Object>> deansList = academicReportService.generateDeansList(
                academicYear, semester, minimumGPA);
        return ResponseEntity.ok(deansList);
    }

    @GetMapping("/probation-list")
    @Operation(summary = "Generate academic probation list")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generateAcademicProbationList(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester,
            @Parameter(description = "Maximum GPA") @RequestParam(defaultValue = "2.0") BigDecimal maximumGPA) {
        
        log.info("Generating academic probation list for {}/{}", academicYear, semester);
        
        List<Map<String, Object>> probationList = academicReportService.generateAcademicProbationList(
                academicYear, semester, maximumGPA);
        return ResponseEntity.ok(probationList);
    }

    @GetMapping("/analytics/dashboard")
    @Operation(summary = "Generate academic analytics dashboard")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> generateAcademicAnalyticsDashboard(
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Generating academic analytics dashboard for {}/{}", academicYear, semester);
        
        Map<String, Object> dashboard = academicReportService.generateAcademicAnalyticsDashboard(
                academicYear, semester);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/progress/student/{studentId}")
    @Operation(summary = "Generate progress tracking report")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Map<String, Object>> generateProgressTrackingReport(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Academic year") @RequestParam String academicYear) {
        
        log.info("Generating progress tracking report for student: {}", studentId);
        
        Map<String, Object> progress = academicReportService.generateProgressTrackingReport(
                studentId, academicYear);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/trends/class/{classRoomId}")
    @Operation(summary = "Generate performance trends")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<List<Map<String, Object>>> generatePerformanceTrends(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear) {
        
        log.info("Generating performance trends for class: {}", classRoomId);
        
        List<Map<String, Object>> trends = academicReportService.generatePerformanceTrends(
                classRoomId, academicYear);
        return ResponseEntity.ok(trends);
    }

    // Export endpoints
    @PostMapping("/export/excel")
    @Operation(summary = "Export academic report to Excel")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<byte[]> exportAcademicReportToExcel(
            @Valid @RequestBody AcademicReportRequest request) {
        
        log.info("Exporting academic report to Excel");
        
        ByteArrayOutputStream outputStream = academicReportService.exportAcademicReportToExcel(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "academic-report.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/transcript/student/{studentId}/pdf")
    @Operation(summary = "Export transcript to PDF")
    @PreAuthorize("hasPermission('TRANSCRIPT', 'READ')")
    public ResponseEntity<byte[]> exportTranscriptToPDF(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Exporting transcript to PDF for student: {}", studentId);
        
        ByteArrayOutputStream outputStream = academicReportService.exportTranscriptToPDF(
                studentId, academicYear, semester);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transcript-" + studentId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/class/{classRoomId}/excel")
    @Operation(summary = "Export class report to Excel")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<byte[]> exportClassReportToExcel(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Exporting class report to Excel for class: {}", classRoomId);
        
        ByteArrayOutputStream outputStream = academicReportService.exportClassReportToExcel(
                classRoomId, academicYear, semester);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "class-report-" + classRoomId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/export/rankings/class/{classRoomId}/excel")
    @Operation(summary = "Export rankings to Excel")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<byte[]> exportRankingsToExcel(
            @Parameter(description = "Class room ID") @PathVariable Long classRoomId,
            @Parameter(description = "Academic year") @RequestParam String academicYear,
            @Parameter(description = "Semester") @RequestParam Integer semester) {
        
        log.info("Exporting rankings to Excel for class: {}", classRoomId);
        
        ByteArrayOutputStream outputStream = academicReportService.exportRankingsToExcel(
                classRoomId, academicYear, semester);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "rankings-" + classRoomId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get academic reports with pagination")
    @PreAuthorize("hasPermission('ACADEMIC_REPORT', 'READ')")
    public ResponseEntity<Page<Map<String, Object>>> getAcademicReports(
            @Valid AcademicReportRequest request,
            Pageable pageable) {
        
        log.info("Getting paginated academic reports");
        
        Page<Map<String, Object>> reports = academicReportService.getAcademicReports(request, pageable);
        return ResponseEntity.ok(reports);
    }
}
