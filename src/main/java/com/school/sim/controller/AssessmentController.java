package com.school.sim.controller;

import com.school.sim.dto.request.AssessmentSearchRequest;
import com.school.sim.dto.request.CreateAssessmentRequest;
import com.school.sim.dto.request.GradeAssessmentRequest;
import com.school.sim.dto.request.UpdateAssessmentRequest;
import com.school.sim.dto.response.AssessmentResponse;
import com.school.sim.dto.response.StudentAssessmentResponse;
import com.school.sim.entity.AssessmentType;
import com.school.sim.service.AssessmentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for assessment management operations
 * Provides endpoints for assessment creation, grading, and evaluation
 */
@RestController
@RequestMapping("/api/v1/assessments")
@Tag(name = "Assessment Management", description = "Assessment management endpoints")
@Validated
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    @Autowired
    private AssessmentService assessmentService;

    /**
     * Create a new assessment
     */
    @PostMapping
    @Operation(summary = "Create assessment", description = "Create a new assessment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Assessment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        logger.info("Creating new assessment: {}", request.getTitle());
        
        try {
            AssessmentResponse response = assessmentService.createAssessment(request);
            logger.info("Successfully created assessment with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to create assessment: {}", request.getTitle(), e);
            throw e;
        }
    }

    /**
     * Get all assessments with pagination
     */
    @GetMapping
    @Operation(summary = "Get all assessments", description = "Retrieve all assessments with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AssessmentResponse>> getAllAssessments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Fetching all assessments - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AssessmentResponse> assessments = assessmentService.getAllAssessments(pageable);
        logger.debug("Retrieved {} assessments", assessments.getTotalElements());
        
        return ResponseEntity.ok(assessments);
    }

    /**
     * Get assessment by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get assessment by ID", description = "Retrieve assessment by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment found"),
        @ApiResponse(responseCode = "404", description = "Assessment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable("id") @NotNull Long assessmentId) {
        logger.debug("Fetching assessment by ID: {}", assessmentId);
        
        AssessmentResponse assessment = assessmentService.getAssessmentById(assessmentId);
        logger.debug("Assessment found with ID: {}", assessmentId);
        return ResponseEntity.ok(assessment);
    }

    /**
     * Update assessment
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update assessment", description = "Update assessment information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment updated successfully"),
        @ApiResponse(responseCode = "404", description = "Assessment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable("id") @NotNull Long assessmentId,
            @Valid @RequestBody UpdateAssessmentRequest request) {
        
        logger.info("Updating assessment with ID: {}", assessmentId);
        
        try {
            AssessmentResponse response = assessmentService.updateAssessment(assessmentId, request);
            logger.info("Successfully updated assessment with ID: {}", assessmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update assessment with ID: {}", assessmentId, e);
            throw e;
        }
    }

    /**
     * Delete assessment
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete assessment", description = "Delete assessment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Assessment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Assessment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAssessment(@PathVariable("id") @NotNull Long assessmentId) {
        logger.info("Deleting assessment with ID: {}", assessmentId);
        
        try {
            assessmentService.deleteAssessment(assessmentId);
            logger.info("Successfully deleted assessment with ID: {}", assessmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete assessment with ID: {}", assessmentId, e);
            throw e;
        }
    }

    /**
     * Search assessments with advanced criteria
     */
    @PostMapping("/search")
    @Operation(summary = "Search assessments", description = "Search assessments with advanced criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AssessmentResponse>> searchAssessments(
            @Valid @RequestBody AssessmentSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Searching assessments with criteria: {}", searchRequest);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AssessmentResponse> assessments = assessmentService.searchAssessments(searchRequest, pageable);
        logger.debug("Found {} assessments matching search criteria", assessments.getTotalElements());
        
        return ResponseEntity.ok(assessments);
    }

    /**
     * Get assessments by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get assessments by type", description = "Get all assessments of a specific type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AssessmentResponse>> getAssessmentsByType(
            @PathVariable("type") AssessmentType type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching assessments by type: {}", type);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AssessmentResponse> assessments = assessmentService.getAssessmentsByType(type, pageable);
        
        logger.debug("Retrieved {} assessments of type: {}", assessments.getTotalElements(), type);
        return ResponseEntity.ok(assessments);
    }

    /**
     * Get assessments by subject
     */
    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get assessments by subject", description = "Get all assessments for a specific subject")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AssessmentResponse>> getAssessmentsBySubject(
            @PathVariable("subjectId") @NotNull Long subjectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching assessments by subject ID: {}", subjectId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AssessmentResponse> assessments = assessmentService.getAssessmentsBySubject(subjectId, pageable);
        
        logger.debug("Retrieved {} assessments for subject: {}", assessments.getTotalElements(), subjectId);
        return ResponseEntity.ok(assessments);
    }

    /**
     * Get assessments by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get assessments by date range", description = "Get assessments within date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<AssessmentResponse>> getAssessmentsByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching assessments from {} to {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AssessmentResponse> assessments = assessmentService.getAssessmentsByDateRange(startDate, endDate, pageable);
        
        logger.debug("Retrieved {} assessments for date range", assessments.getTotalElements());
        return ResponseEntity.ok(assessments);
    }

    /**
     * Grade assessment for student
     */
    @PostMapping("/{id}/grade")
    @Operation(summary = "Grade assessment", description = "Grade assessment for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment graded successfully"),
        @ApiResponse(responseCode = "404", description = "Assessment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentAssessmentResponse>> gradeAssessment(
            @PathVariable("id") @NotNull Long assessmentId,
            @Valid @RequestBody GradeAssessmentRequest request) {
        
        logger.info("Grading assessment {} for {} students", assessmentId, request.getStudentGrades().size());
        
        try {
            List<StudentAssessmentResponse> responses = assessmentService.gradeAssessment(assessmentId, request);
            logger.info("Successfully graded assessment {} for {} students", assessmentId, responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Failed to grade assessment {}", assessmentId, e);
            throw e;
        }
    }

    /**
     * Get student grades for assessment
     */
    @GetMapping("/{id}/grades")
    @Operation(summary = "Get assessment grades", description = "Get all student grades for an assessment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Grades retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Assessment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentAssessmentResponse>> getAssessmentGrades(
            @PathVariable("id") @NotNull Long assessmentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching grades for assessment: {}", assessmentId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        Page<StudentAssessmentResponse> grades = assessmentService.getAssessmentGrades(assessmentId, pageable);
        
        logger.debug("Retrieved {} grades for assessment: {}", grades.getTotalElements(), assessmentId);
        return ResponseEntity.ok(grades);
    }

    /**
     * Get student assessments
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student assessments", description = "Get all assessments for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentAssessmentResponse>> getStudentAssessments(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching assessments for student: {}", studentId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StudentAssessmentResponse> assessments = assessmentService.getStudentAssessments(studentId, pageable);
        
        logger.debug("Retrieved {} assessments for student: {}", assessments.getTotalElements(), studentId);
        return ResponseEntity.ok(assessments);
    }

    /**
     * Get assessment statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get assessment statistics", description = "Get assessment statistics and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getAssessmentStatistics() {
        logger.debug("Fetching assessment statistics");
        
        try {
            Map<String, Object> statistics = assessmentService.getAssessmentStatistics();
            statistics.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved assessment statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get assessment statistics", e);
            throw e;
        }
    }

    /**
     * Calculate student GPA
     */
    @GetMapping("/student/{studentId}/gpa")
    @Operation(summary = "Calculate student GPA", description = "Calculate GPA for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GPA calculated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> calculateStudentGPA(
            @PathVariable("studentId") @NotNull Long studentId,
            @Parameter(description = "Academic year") @RequestParam(required = false) String academicYear,
            @Parameter(description = "Semester") @RequestParam(required = false) Integer semester) {
        
        logger.debug("Calculating GPA for student: {}", studentId);
        
        try {
            BigDecimal gpa = assessmentService.calculateStudentGPA(studentId, academicYear, semester);
            
            Map<String, Object> response = new HashMap<>();
            response.put("studentId", studentId);
            response.put("gpa", gpa);
            response.put("academicYear", academicYear);
            response.put("semester", semester);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Calculated GPA for student {}: {}", studentId, gpa);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to calculate GPA for student: {}", studentId, e);
            throw e;
        }
    }

    /**
     * Get upcoming assessments
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming assessments", description = "Get assessments scheduled for the near future")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming assessments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<AssessmentResponse>> getUpcomingAssessments(
            @Parameter(description = "Number of days ahead") @RequestParam(defaultValue = "7") @Min(1) int days) {
        
        logger.debug("Fetching upcoming assessments for next {} days", days);
        
        try {
            Page<AssessmentResponse> assessmentPage = assessmentService.getUpcomingAssessments(days, Pageable.unpaged());
            List<AssessmentResponse> assessments = assessmentPage.getContent();
            logger.debug("Retrieved {} upcoming assessments", assessments.size());
            return ResponseEntity.ok(assessments);
        } catch (Exception e) {
            logger.error("Failed to get upcoming assessments", e);
            throw e;
        }
    }

    /**
     * Bulk grade assessments
     */
    @PostMapping("/{id}/bulk-grade")
    @Operation(summary = "Bulk grade assessment", description = "Grade assessment for multiple students")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk grading completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> bulkGradeAssessment(
            @PathVariable("id") @NotNull Long assessmentId,
            @RequestBody List<GradeAssessmentRequest> gradeRequests) {
        
        logger.info("Bulk grading assessment {} for {} students", assessmentId, gradeRequests.size());
        
        try {
            List<StudentAssessmentResponse> responses = assessmentService.bulkGradeAssessment(assessmentId, gradeRequests);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk grading completed successfully");
            response.put("assessmentId", assessmentId);
            response.put("count", responses.size());
            response.put("grades", responses);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully bulk graded assessment {} for {} students", assessmentId, responses.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to bulk grade assessment {}", assessmentId, e);
            throw e;
        }
    }
}