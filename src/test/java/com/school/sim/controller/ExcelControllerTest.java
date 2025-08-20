package com.school.sim.controller;

import com.school.sim.service.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ExcelController
 */
@WebMvcTest(ExcelController.class)
class ExcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelService excelService;

    private MockMultipartFile testFile;
    private ExcelService.ImportResult importResult;
    private ExcelService.ValidationResult validationResult;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
            "file", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test content".getBytes()
        );

        importResult = new ExcelService.ImportResult(
            1, 1, 0, new ArrayList<>(), new ArrayList<>()
        );

        validationResult = new ExcelService.ValidationResult(true, new ArrayList<>());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importStudents_ValidFile_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(excelService.validateExcelFile(any())).thenReturn(validationResult);
        when(excelService.importStudentsFromExcel(any())).thenReturn(importResult);

        // Act & Assert
        mockMvc.perform(multipart("/api/excel/import/students")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.successfulImports").value(1))
                .andExpect(jsonPath("$.failedImports").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importStudents_InvalidFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ExcelService.ValidationResult invalidResult = new ExcelService.ValidationResult(
            false, List.of("Invalid file format")
        );
        when(excelService.validateExcelFile(any())).thenReturn(invalidResult);

        // Act & Assert
        mockMvc.perform(multipart("/api/excel/import/students")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Invalid file format"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void importStudents_InsufficientRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/excel/import/students")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportAllStudents_ShouldReturnExcelFile() throws Exception {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("test excel content".getBytes());
        when(excelService.exportAllStudentsToExcel()).thenReturn(outputStream);

        // Act & Assert
        mockMvc.perform(get("/api/excel/export/students"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.containsString("attachment; filename=students_")))
                .andExpect(content().contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void exportStudentsByClassRoom_ShouldReturnExcelFile() throws Exception {
        // Arrange
        Long classRoomId = 1L;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("test excel content".getBytes());
        when(excelService.exportStudentsByClassRoomToExcel(classRoomId)).thenReturn(outputStream);

        // Act & Assert
        mockMvc.perform(get("/api/excel/export/students/classroom/{classRoomId}", classRoomId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.containsString("attachment; filename=students_classroom_1_")))
                .andExpect(content().contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadStudentTemplate_ShouldReturnTemplate() throws Exception {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("template content".getBytes());
        when(excelService.generateStudentImportTemplate()).thenReturn(outputStream);

        // Act & Assert
        mockMvc.perform(get("/api/excel/template/students"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    "attachment; filename=student_import_template.xlsx"))
                .andExpect(content().contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validateExcelFile_ValidFile_ShouldReturnValid() throws Exception {
        // Arrange
        when(excelService.validateExcelFile(any())).thenReturn(validationResult);

        // Act & Assert
        mockMvc.perform(multipart("/api/excel/validate")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validateExcelFile_InvalidFile_ShouldReturnInvalid() throws Exception {
        // Arrange
        ExcelService.ValidationResult invalidResult = new ExcelService.ValidationResult(
            false, List.of("File too large", "Invalid format")
        );
        when(excelService.validateExcelFile(any())).thenReturn(invalidResult);

        // Act & Assert
        mockMvc.perform(multipart("/api/excel/validate")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("File too large"))
                .andExpect(jsonPath("$.errors[1]").value("Invalid format"));
    }

    @Test
    void importStudents_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/excel/import/students")
                .file(testFile)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportAllStudents_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/excel/export/students"))
                .andExpect(status().isUnauthorized());
    }
}
