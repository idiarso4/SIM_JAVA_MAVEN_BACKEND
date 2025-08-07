package com.school.sim.controller;

import com.school.sim.dto.request.CreateStudentRequest;
import com.school.sim.dto.request.StudentSearchRequest;
import com.school.sim.dto.request.UpdateStudentRequest;
import com.school.sim.dto.response.StudentResponse;
import com.school.sim.entity.StudentStatus;
import com.school.sim.service.ExcelService;
import com.school.sim.service.StudentService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for student management operations
 * Provides endpoints for student CRUD operations, search, filtering, and Excel import/export
 */
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student Management", description = "Student management endpoints")
@Validated
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExcelService excelService;

    /**
     * Create a new student
     */
    @PostMapping
    @Operation(summary = "Create student", description = "Create a new student record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Student created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Student already exists")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        logger.info("Creating new student with NIS: {}", request.getNis());
        
        try {
            StudentResponse response = studentService.createStudent(request);
            logger.info("Successfully created student with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to create student with NIS: {}", request.getNis(), e);
            throw e;
        }
    }

    /**
     * Get all students with pagination
     */
    @GetMapping
    @Operation(summary = "Get all students", description = "Retrieve all students with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "namaLengkap") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Fetching all students - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<StudentResponse> students = studentService.getAllStudents(pageable);
        logger.debug("Retrieved {} students", students.getTotalElements());
        
        return ResponseEntity.ok(students);
    }

    /**
     * Get student by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID", description = "Retrieve student information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable("id") @NotNull Long studentId) {
        logger.debug("Fetching student by ID: {}", studentId);
        
        Optional<StudentResponse> student = studentService.getStudentById(studentId);
        if (student.isPresent()) {
            logger.debug("Student found with ID: {}", studentId);
            return ResponseEntity.ok(student.get());
        } else {
            logger.debug("Student not found with ID: {}", studentId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get student by NIS
     */
    @GetMapping("/nis/{nis}")
    @Operation(summary = "Get student by NIS", description = "Retrieve student information by NIS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> getStudentByNis(@PathVariable("nis") String nis) {
        logger.debug("Fetching student by NIS: {}", nis);
        
        Optional<StudentResponse> student = studentService.getStudentByNis(nis);
        if (student.isPresent()) {
            logger.debug("Student found with NIS: {}", nis);
            return ResponseEntity.ok(student.get());
        } else {
            logger.debug("Student not found with NIS: {}", nis);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update student
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Update student information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student updated successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable("id") @NotNull Long studentId,
            @Valid @RequestBody UpdateStudentRequest request) {
        
        logger.info("Updating student with ID: {}", studentId);
        
        try {
            StudentResponse response = studentService.updateStudent(studentId, request);
            logger.info("Successfully updated student with ID: {}", studentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update student with ID: {}", studentId, e);
            throw e;
        }
    }

    /**
     * Delete student
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Delete student record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") @NotNull Long studentId) {
        logger.info("Deleting student with ID: {}", studentId);
        
        try {
            studentService.deleteStudent(studentId);
            logger.info("Successfully deleted student with ID: {}", studentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete student with ID: {}", studentId, e);
            throw e;
        }
    }

    /**
     * Search students with advanced criteria
     */
    @PostMapping("/search")
    @Operation(summary = "Search students", description = "Search students with advanced criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentResponse>> searchStudents(
            @Valid @RequestBody StudentSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "namaLengkap") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Searching students with criteria: {}", searchRequest);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<StudentResponse> students = studentService.searchStudents(searchRequest, pageable);
        logger.debug("Found {} students matching search criteria", students.getTotalElements());
        
        return ResponseEntity.ok(students);
    }

    /**
     * Get students by class room
     */
    @GetMapping("/class/{classRoomId}")
    @Operation(summary = "Get students by class", description = "Get all students in a specific class room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getStudentsByClassRoom(
            @PathVariable("classRoomId") @NotNull Long classRoomId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching students by class room ID: {}", classRoomId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "namaLengkap"));
        Page<StudentResponse> students = studentService.getStudentsByClassRoom(classRoomId, pageable);
        
        logger.debug("Retrieved {} students from class room: {}", students.getTotalElements(), classRoomId);
        return ResponseEntity.ok(students);
    }

    /**
     * Get students by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get students by status", description = "Get all students with specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getStudentsByStatus(
            @PathVariable("status") StudentStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching students by status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "namaLengkap"));
        Page<StudentResponse> students = studentService.getStudentsByStatus(status, pageable);
        
        logger.debug("Retrieved {} students with status: {}", students.getTotalElements(), status);
        return ResponseEntity.ok(students);
    }

    /**
     * Assign student to class room
     */
    @PostMapping("/{id}/assign-class/{classRoomId}")
    @Operation(summary = "Assign to class", description = "Assign student to class room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Student or class room not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> assignToClassRoom(
            @PathVariable("id") @NotNull Long studentId,
            @PathVariable("classRoomId") @NotNull Long classRoomId) {
        
        logger.info("Assigning student {} to class room {}", studentId, classRoomId);
        
        try {
            studentService.assignToClassRoom(studentId, classRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student assigned to class room successfully");
            response.put("studentId", studentId);
            response.put("classRoomId", classRoomId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully assigned student {} to class room {}", studentId, classRoomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to assign student {} to class room {}", studentId, classRoomId, e);
            throw e;
        }
    }

    /**
     * Remove student from class room
     */
    @PostMapping("/{id}/remove-class")
    @Operation(summary = "Remove from class", description = "Remove student from class room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student removed successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> removeFromClassRoom(@PathVariable("id") @NotNull Long studentId) {
        logger.info("Removing student {} from class room", studentId);
        
        try {
            studentService.removeFromClassRoom(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student removed from class room successfully");
            response.put("studentId", studentId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully removed student {} from class room", studentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to remove student {} from class room", studentId, e);
            throw e;
        }
    }

    /**
     * Get student statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get student statistics", description = "Get student count and distribution statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStudentStatistics() {
        logger.debug("Fetching student statistics");
        
        try {
            Map<String, Object> statistics = studentService.getStudentStatistics();
            statistics.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved student statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get student statistics", e);
            throw e;
        }
    }

    /**
     * Import students from Excel
     */
    @PostMapping("/excel/import")
    @Operation(summary = "Import from Excel", description = "Import students from Excel file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> importStudentsFromExcel(@RequestParam("file") MultipartFile file) {
        logger.info("Importing students from Excel file: {}", file.getOriginalFilename());
        
        try {
            ExcelService.ImportResult importResult = excelService.importStudentsFromExcel(file);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalRows", importResult.getTotalRows());
            result.put("successfulImports", importResult.getSuccessfulImports());
            result.put("failedImports", importResult.getFailedImports());
            result.put("errors", importResult.getErrors());
            result.put("importedStudents", importResult.getImportedStudents());
            result.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully imported students from Excel file: {}", file.getOriginalFilename());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to import students from Excel file: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }

    /**
     * Export students to Excel
     */
    @PostMapping("/excel/export")
    @Operation(summary = "Export to Excel", description = "Export students to Excel file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportStudentsToExcel(@RequestBody(required = false) StudentSearchRequest searchRequest) {
        logger.info("Exporting students to Excel");
        
        try {
            // Get students based on search criteria or all students
            List<StudentResponse> students;
            if (searchRequest != null) {
                Page<StudentResponse> studentPage = studentService.searchStudents(searchRequest, Pageable.unpaged());
                students = studentPage.getContent();
            } else {
                Page<StudentResponse> studentPage = studentService.getAllStudents(Pageable.unpaged());
                students = studentPage.getContent();
            }
            
            ByteArrayOutputStream outputStream = excelService.exportStudentsToExcel(students);
            byte[] excelData = outputStream.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "students_export.xlsx");
            headers.setContentLength(excelData.length);
            
            logger.info("Successfully exported students to Excel");
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
        } catch (Exception e) {
            logger.error("Failed to export students to Excel", e);
            throw e;
        }
    }

    /**
     * Download Excel template
     */
    @GetMapping("/excel/template")
    @Operation(summary = "Download template", description = "Download Excel template for student import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template downloaded successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        logger.info("Downloading Excel template for student import");
        
        try {
            ByteArrayOutputStream outputStream = excelService.generateStudentImportTemplate();
            byte[] templateData = outputStream.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "student_import_template.xlsx");
            headers.setContentLength(templateData.length);
            
            logger.info("Successfully generated Excel template");
            return ResponseEntity.ok()
                .headers(headers)
                .body(templateData);
        } catch (Exception e) {
            logger.error("Failed to generate Excel template", e);
            throw e;
        }
    }

    /**
     * Bulk assign students to class room
     */
    @PostMapping("/bulk/assign-class/{classRoomId}")
    @Operation(summary = "Bulk assign to class", description = "Assign multiple students to class room")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Students assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkAssignToClassRoom(
            @PathVariable("classRoomId") @NotNull Long classRoomId,
            @RequestBody List<Long> studentIds) {
        
        logger.info("Bulk assigning {} students to class room {}", studentIds.size(), classRoomId);
        
        try {
            studentService.bulkAssignToClassRoom(studentIds, classRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Students assigned to class room successfully");
            response.put("count", studentIds.size());
            response.put("classRoomId", classRoomId);
            response.put("studentIds", studentIds);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully bulk assigned {} students to class room {}", studentIds.size(), classRoomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to bulk assign students to class room {}", classRoomId, e);
            throw e;
        }
    }
}