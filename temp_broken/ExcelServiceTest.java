package com.school.sim.service;

import com.school.sim.dto.response.StudentResponse;
import com.school.sim.entity.Gender;
import com.school.sim.entity.StudentStatus;
import com.school.sim.service.impl.ExcelServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExcelService
 */
@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private ExcelServiceImpl excelService;

    private StudentResponse testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new StudentResponse();
        testStudent.setId(1L);
        testStudent.setNis("12345");
        testStudent.setNamaLengkap("Test Student");
        testStudent.setTempatLahir("Jakarta");
        testStudent.setTanggalLahir(LocalDate.of(2005, 1, 1));
        testStudent.setJenisKelamin(Gender.MALE);
        testStudent.setAgama("Islam");
        testStudent.setAlamat("Test Address");
        testStudent.setNamaAyah("Test Father");
        testStudent.setNamaIbu("Test Mother");
        testStudent.setPekerjaanAyah("Engineer");
        testStudent.setPekerjaanIbu("Teacher");
        testStudent.setNoHpOrtu("081234567890");
        testStudent.setAlamatOrtu("Parent Address");
        testStudent.setTahunMasuk(2023);
        testStudent.setAsalSekolah("SMP Test");
        testStudent.setStatus(StudentStatus.ACTIVE);
        testStudent.setCreatedAt(LocalDateTime.now());
        testStudent.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void exportStudentsToExcel_ValidStudents_ShouldCreateExcelFile() throws IOException {
        // Arrange
        List<StudentResponse> students = List.of(testStudent);

        // Act
        ByteArrayOutputStream result = excelService.exportStudentsToExcel(students);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);

        // Verify Excel content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Students", sheet.getSheetName());
            
            // Check header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("NIS", headerRow.getCell(0).getStringCellValue());
            assertEquals("Nama Lengkap", headerRow.getCell(1).getStringCellValue());
            
            // Check data row
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("12345", dataRow.getCell(0).getStringCellValue());
            assertEquals("Test Student", dataRow.getCell(1).getStringCellValue());
        }
    }

    @Test
    void exportStudentsByClassRoomToExcel_ValidClassRoom_ShouldExportStudents() {
        // Arrange
        Long classRoomId = 1L;
        List<StudentResponse> students = List.of(testStudent);
        Page<StudentResponse> studentPage = new PageImpl<>(students);
        
        when(studentService.getStudentsByClassRoom(eq(classRoomId), any(PageRequest.class)))
            .thenReturn(studentPage);

        // Act
        ByteArrayOutputStream result = excelService.exportStudentsByClassRoomToExcel(classRoomId);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(studentService).getStudentsByClassRoom(eq(classRoomId), any(PageRequest.class));
    }

    @Test
    void exportAllStudentsToExcel_ShouldExportAllStudents() {
        // Arrange
        List<StudentResponse> students = List.of(testStudent);
        Page<StudentResponse> studentPage = new PageImpl<>(students);
        
        when(studentService.getAllStudents(any(PageRequest.class))).thenReturn(studentPage);

        // Act
        ByteArrayOutputStream result = excelService.exportAllStudentsToExcel();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(studentService).getAllStudents(any(PageRequest.class));
    }

    @Test
    void generateStudentImportTemplate_ShouldCreateTemplate() throws IOException {
        // Act
        ByteArrayOutputStream result = excelService.generateStudentImportTemplate();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);

        // Verify template content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.toByteArray()))) {
            // Check main sheet
            Sheet templateSheet = workbook.getSheet("Student Template");
            assertNotNull(templateSheet);
            
            // Check header row
            Row headerRow = templateSheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("NIS", headerRow.getCell(0).getStringCellValue());
            assertEquals("Nama Lengkap", headerRow.getCell(1).getStringCellValue());
            
            // Check sample data row
            Row sampleRow = templateSheet.getRow(1);
            assertNotNull(sampleRow);
            assertEquals("12345", sampleRow.getCell(0).getStringCellValue());
            assertEquals("John Doe", sampleRow.getCell(1).getStringCellValue());
            
            // Check instructions sheet
            Sheet instructionsSheet = workbook.getSheet("Instructions");
            assertNotNull(instructionsSheet);
            
            Row instructionRow = instructionsSheet.getRow(0);
            assertNotNull(instructionRow);
            assertEquals("STUDENT IMPORT INSTRUCTIONS", instructionRow.getCell(0).getStringCellValue());
        }
    }

    @Test
    void validateExcelFile_ValidFile_ShouldReturnValid() throws IOException {
        // Arrange
        MultipartFile validFile = createValidExcelFile();

        // Act
        ExcelService.ValidationResult result = excelService.validateExcelFile(validFile);

        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validateExcelFile_InvalidFileType_ShouldReturnInvalid() {
        // Arrange
        MultipartFile invalidFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", "test content".getBytes());

        // Act
        ExcelService.ValidationResult result = excelService.validateExcelFile(invalidFile);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("File must be an Excel file (.xlsx or .xls)"));
    }

    @Test
    void validateExcelFile_EmptyFile_ShouldReturnInvalid() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
            "file", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            new byte[0]);

        // Act
        ExcelService.ValidationResult result = excelService.validateExcelFile(emptyFile);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("File cannot be empty"));
    }

    @Test
    void validateExcelFile_LargeFile_ShouldReturnInvalid() {
        // Arrange
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile largeFile = new MockMultipartFile(
            "file", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            largeContent);

        // Act
        ExcelService.ValidationResult result = excelService.validateExcelFile(largeFile);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("File size must not exceed 10MB"));
    }

    @Test
    void importStudentsFromExcel_ValidFile_ShouldImportSuccessfully() throws IOException {
        // Arrange
        MultipartFile validFile = createValidExcelFileWithData();
        when(studentService.existsByNis("12345")).thenReturn(false);
        when(studentService.createStudent(any())).thenReturn(testStudent);

        // Act
        ExcelService.ImportResult result = excelService.importStudentsFromExcel(validFile);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessfulImports());
        assertEquals(0, result.getFailedImports());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(1, result.getImportedStudents().size());
        
        verify(studentService).createStudent(any());
    }

    @Test
    void importStudentsFromExcel_DuplicateNIS_ShouldReportError() throws IOException {
        // Arrange
        MultipartFile validFile = createValidExcelFileWithData();
        when(studentService.existsByNis("12345")).thenReturn(true);

        // Act
        ExcelService.ImportResult result = excelService.importStudentsFromExcel(validFile);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessfulImports());
        assertEquals(1, result.getFailedImports());
        assertFalse(result.getErrors().isEmpty());
        
        ExcelService.ImportError error = result.getErrors().get(0);
        assertEquals(2, error.getRowNumber()); // Row 2 (after header)
        assertEquals("NIS", error.getField());
        assertEquals("NIS already exists", error.getErrorMessage());
        
        verify(studentService, never()).createStudent(any());
    }

    // Helper methods

    private MultipartFile createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "NIS", "Nama Lengkap", "Tempat Lahir", "Tanggal Lahir", "Jenis Kelamin",
                "Agama", "Alamat", "Nama Ayah", "Nama Ibu", "Pekerjaan Ayah", "Pekerjaan Ibu",
                "No HP Orang Tua", "Alamat Orang Tua", "Tahun Masuk", "Asal Sekolah", "Status"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return new MockMultipartFile(
                "file", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                outputStream.toByteArray());
        }
    }

    private MultipartFile createValidExcelFileWithData() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "NIS", "Nama Lengkap", "Tempat Lahir", "Tanggal Lahir", "Jenis Kelamin",
                "Agama", "Alamat", "Nama Ayah", "Nama Ibu", "Pekerjaan Ayah", "Pekerjaan Ibu",
                "No HP Orang Tua", "Alamat Orang Tua", "Tahun Masuk", "Asal Sekolah", "Status"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Create data row
            Row dataRow = sheet.createRow(1);
            String[] data = {
                "12345", "Test Student", "Jakarta", "01/01/2005", "MALE",
                "Islam", "Test Address", "Test Father", "Test Mother", "Engineer", "Teacher",
                "081234567890", "Parent Address", "2023", "SMP Test", "ACTIVE"
            };
            
            for (int i = 0; i < data.length; i++) {
                dataRow.createCell(i).setCellValue(data[i]);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return new MockMultipartFile(
                "file", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                outputStream.toByteArray());
        }
    }
}
