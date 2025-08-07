package com.school.sim.controller;

import com.school.sim.service.ExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for Excel import/export operations
 * Provides endpoints for importing and exporting student data
 */
@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ExcelService excelService;

    /**
     * Import students from Excel file
     */
    @PostMapping("/import/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) {
        logger.info("Importing students from Excel file: {}", file.getOriginalFilename());

        try {
            // Validate file first
            ExcelService.ValidationResult validation = excelService.validateExcelFile(file);
            if (!validation.isValid()) {
                return ResponseEntity.badRequest().body(validation);
            }

            // Import students
            ExcelService.ImportResult result = excelService.importStudentsFromExcel(file);
            
            logger.info("Excel import completed. Success: {}, Failed: {}", 
                       result.getSuccessfulImports(), result.getFailedImports());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error importing students from Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error importing students: " + e.getMessage());
        }
    }

    /**
     * Export all students to Excel
     */
    @GetMapping("/export/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ByteArrayResource> exportAllStudents() {
        logger.info("Exporting all students to Excel");

        try {
            ByteArrayOutputStream outputStream = excelService.exportAllStudentsToExcel();
            
            String filename = "students_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.add(HttpHeaders.CONTENT_TYPE, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            logger.info("Successfully exported all students to Excel: {}", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);

        } catch (Exception e) {
            logger.error("Error exporting students to Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export students by class room to Excel
     */
    @GetMapping("/export/students/classroom/{classRoomId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ByteArrayResource> exportStudentsByClassRoom(@PathVariable Long classRoomId) {
        logger.info("Exporting students by class room {} to Excel", classRoomId);

        try {
            ByteArrayOutputStream outputStream = excelService.exportStudentsByClassRoomToExcel(classRoomId);
            
            String filename = "students_classroom_" + classRoomId + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.add(HttpHeaders.CONTENT_TYPE, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            logger.info("Successfully exported students by class room {} to Excel: {}", classRoomId, filename);

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);

        } catch (Exception e) {
            logger.error("Error exporting students by class room to Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download student import template
     */
    @GetMapping("/template/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ByteArrayResource> downloadStudentTemplate() {
        logger.info("Generating student import template");

        try {
            ByteArrayOutputStream outputStream = excelService.generateStudentImportTemplate();
            
            String filename = "student_import_template.xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.add(HttpHeaders.CONTENT_TYPE, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            logger.info("Successfully generated student import template");

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);

        } catch (Exception e) {
            logger.error("Error generating student template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validate Excel file before import
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> validateExcelFile(@RequestParam("file") MultipartFile file) {
        logger.info("Validating Excel file: {}", file.getOriginalFilename());

        try {
            ExcelService.ValidationResult result = excelService.validateExcelFile(file);
            
            logger.info("Excel file validation completed. Valid: {}", result.isValid());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error validating Excel file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error validating file: " + e.getMessage());
        }
    }
}
