package com.school.sim.service.impl;

import com.school.sim.dto.request.CreateStudentRequest;
import com.school.sim.dto.response.StudentResponse;
import com.school.sim.entity.Gender;
import com.school.sim.entity.StudentStatus;
import com.school.sim.exception.ValidationException;
import com.school.sim.service.ExcelService;
import com.school.sim.service.StudentService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ExcelService for Excel import/export operations
 * Uses Apache POI for Excel file processing
 */
@Service
public class ExcelServiceImpl implements ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelServiceImpl.class);

    // Excel column headers
    private static final String[] STUDENT_HEADERS = {
        "NIS", "Nama Lengkap", "Tempat Lahir", "Tanggal Lahir", "Jenis Kelamin",
        "Agama", "Alamat", "Nama Ayah", "Nama Ibu", "Pekerjaan Ayah", "Pekerjaan Ibu",
        "No HP Orang Tua", "Alamat Orang Tua", "Tahun Masuk", "Asal Sekolah", "Status"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private StudentService studentService;

    @Override
    public ImportResult importStudentsFromExcel(MultipartFile file) {
        logger.info("Starting Excel import for file: {}", file.getOriginalFilename());

        List<ImportError> errors = new ArrayList<>();
        List<StudentResponse> importedStudents = new ArrayList<>();
        int totalRows = 0;
        int successfulImports = 0;
        int failedImports = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();

            // Skip header row
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    CreateStudentRequest studentRequest = parseRowToStudentRequest(row, i + 1);
                    
                    // Validate student data
                    validateStudentRequest(studentRequest, i + 1, errors);
                    
                    if (errors.stream().noneMatch(error -> error.getRowNumber() == i + 1)) {
                        StudentResponse student = studentService.createStudent(studentRequest);
                        importedStudents.add(student);
                        successfulImports++;
                        logger.debug("Successfully imported student: {} at row {}", student.getNis(), i + 1);
                    } else {
                        failedImports++;
                    }

                } catch (Exception e) {
                    logger.error("Error importing student at row {}: {}", i + 1, e.getMessage());
                    errors.add(new ImportError(i + 1, "General", "", e.getMessage()));
                    failedImports++;
                }
            }

        } catch (IOException e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            throw new ValidationException("Error reading Excel file: " + e.getMessage());
        }

        logger.info("Excel import completed. Total: {}, Success: {}, Failed: {}", 
                   totalRows, successfulImports, failedImports);

        return new ImportResult(totalRows, successfulImports, failedImports, errors, importedStudents);
    }

    @Override
    public ByteArrayOutputStream exportStudentsToExcel(List<StudentResponse> students) {
        logger.info("Exporting {} students to Excel", students.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < STUDENT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(STUDENT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int i = 0; i < students.size(); i++) {
                Row row = sheet.createRow(i + 1);
                StudentResponse student = students.get(i);
                populateStudentRow(row, student);
            }

            // Auto-size columns
            for (int i = 0; i < STUDENT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("Successfully exported {} students to Excel", students.size());
            return outputStream;

        } catch (IOException e) {
            logger.error("Error creating Excel file: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayOutputStream exportStudentsByClassRoomToExcel(Long classRoomId) {
        logger.info("Exporting students by class room ID: {}", classRoomId);
        
        List<StudentResponse> students = studentService.getStudentsByClassRoom(classRoomId, 
            PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        
        return exportStudentsToExcel(students);
    }

    @Override
    public ByteArrayOutputStream exportAllStudentsToExcel() {
        logger.info("Exporting all students to Excel");
        
        List<StudentResponse> students = studentService.getAllStudents(
            PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        
        return exportStudentsToExcel(students);
    }

    @Override
    public ByteArrayOutputStream generateStudentImportTemplate() {
        logger.info("Generating student import template");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student Template");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < STUDENT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(STUDENT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create sample data row
            Row sampleRow = sheet.createRow(1);
            String[] sampleData = {
                "12345", "John Doe", "Jakarta", "01/01/2005", "MALE",
                "Islam", "Jl. Contoh No. 123", "John Sr.", "Jane Doe", "Engineer", "Teacher",
                "081234567890", "Jl. Contoh No. 123", "2023", "SMP Negeri 1", "ACTIVE"
            };

            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(sampleData[i]);
            }

            // Auto-size columns
            for (int i = 0; i < STUDENT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add instructions sheet
            createInstructionsSheet(workbook);

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("Successfully generated student import template");
            return outputStream;

        } catch (IOException e) {
            logger.error("Error creating template file: {}", e.getMessage());
            throw new RuntimeException("Error creating template file: " + e.getMessage());
        }
    }

    @Override
    public ValidationResult validateExcelFile(MultipartFile file) {
        logger.info("Validating Excel file: {}", file.getOriginalFilename());

        List<String> errors = new ArrayList<>();

        // Check file type
        if (!isExcelFile(file)) {
            errors.add("File must be an Excel file (.xlsx or .xls)");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            errors.add("File size must not exceed 10MB");
        }

        // Check if file is empty
        if (file.isEmpty()) {
            errors.add("File cannot be empty");
        }

        // Validate Excel structure
        if (errors.isEmpty()) {
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                
                if (sheet.getLastRowNum() < 1) {
                    errors.add("Excel file must contain at least one data row");
                }

                // Validate headers
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    errors.add("Excel file must contain header row");
                } else {
                    validateHeaders(headerRow, errors);
                }

            } catch (IOException e) {
                errors.add("Error reading Excel file: " + e.getMessage());
            }
        }

        boolean isValid = errors.isEmpty();
        logger.info("Excel file validation completed. Valid: {}, Errors: {}", isValid, errors.size());

        return new ValidationResult(isValid, errors);
    }

    // Helper methods

    private CreateStudentRequest parseRowToStudentRequest(Row row, int rowNumber) {
        CreateStudentRequest request = new CreateStudentRequest();

        request.setNis(getCellValueAsString(row.getCell(0)));
        request.setNamaLengkap(getCellValueAsString(row.getCell(1)));
        request.setTempatLahir(getCellValueAsString(row.getCell(2)));
        
        // Parse date
        String dateStr = getCellValueAsString(row.getCell(3));
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                request.setTanggalLahir(LocalDate.parse(dateStr, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format at row {}: {}", rowNumber, dateStr);
            }
        }

        // Parse gender
        String genderStr = getCellValueAsString(row.getCell(4));
        if (genderStr != null) {
            try {
                request.setJenisKelamin(Gender.valueOf(genderStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid gender at row {}: {}", rowNumber, genderStr);
            }
        }

        request.setAgama(getCellValueAsString(row.getCell(5)));
        request.setAlamat(getCellValueAsString(row.getCell(6)));
        request.setNamaAyah(getCellValueAsString(row.getCell(7)));
        request.setNamaIbu(getCellValueAsString(row.getCell(8)));
        request.setPekerjaanAyah(getCellValueAsString(row.getCell(9)));
        request.setPekerjaanIbu(getCellValueAsString(row.getCell(10)));
        request.setNoHpOrtu(getCellValueAsString(row.getCell(11)));
        request.setAlamatOrtu(getCellValueAsString(row.getCell(12)));

        // Parse year
        String yearStr = getCellValueAsString(row.getCell(13));
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                request.setTahunMasuk(Integer.parseInt(yearStr));
            } catch (NumberFormatException e) {
                logger.warn("Invalid year format at row {}: {}", rowNumber, yearStr);
            }
        }

        request.setAsalSekolah(getCellValueAsString(row.getCell(14)));

        // Parse status
        String statusStr = getCellValueAsString(row.getCell(15));
        if (statusStr != null) {
            try {
                request.setStatus(StudentStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status at row {}: {}", rowNumber, statusStr);
                request.setStatus(StudentStatus.ACTIVE); // Default
            }
        } else {
            request.setStatus(StudentStatus.ACTIVE); // Default
        }

        return request;
    }

    private void validateStudentRequest(CreateStudentRequest request, int rowNumber, List<ImportError> errors) {
        // Validate required fields
        if (request.getNis() == null || request.getNis().trim().isEmpty()) {
            errors.add(new ImportError(rowNumber, "NIS", request.getNis(), "NIS is required"));
        }

        if (request.getNamaLengkap() == null || request.getNamaLengkap().trim().isEmpty()) {
            errors.add(new ImportError(rowNumber, "Nama Lengkap", request.getNamaLengkap(), "Name is required"));
        }

        if (request.getTahunMasuk() == null) {
            errors.add(new ImportError(rowNumber, "Tahun Masuk", "", "Year of entry is required"));
        }

        // Check if NIS already exists
        if (request.getNis() != null && studentService.existsByNis(request.getNis())) {
            errors.add(new ImportError(rowNumber, "NIS", request.getNis(), "NIS already exists"));
        }

        // Validate year range
        if (request.getTahunMasuk() != null && 
            (request.getTahunMasuk() < 2000 || request.getTahunMasuk() > LocalDate.now().getYear() + 1)) {
            errors.add(new ImportError(rowNumber, "Tahun Masuk", request.getTahunMasuk().toString(), 
                "Year must be between 2000 and " + (LocalDate.now().getYear() + 1)));
        }
    }

    private void populateStudentRow(Row row, StudentResponse student) {
        int cellIndex = 0;

        row.createCell(cellIndex++).setCellValue(student.getNis());
        row.createCell(cellIndex++).setCellValue(student.getNamaLengkap());
        row.createCell(cellIndex++).setCellValue(student.getTempatLahir());
        
        if (student.getTanggalLahir() != null) {
            row.createCell(cellIndex++).setCellValue(student.getTanggalLahir().format(DATE_FORMATTER));
        } else {
            row.createCell(cellIndex++).setCellValue("");
        }

        row.createCell(cellIndex++).setCellValue(student.getJenisKelamin() != null ? 
            student.getJenisKelamin().toString() : "");
        row.createCell(cellIndex++).setCellValue(student.getAgama());
        row.createCell(cellIndex++).setCellValue(student.getAlamat());
        row.createCell(cellIndex++).setCellValue(student.getNamaAyah());
        row.createCell(cellIndex++).setCellValue(student.getNamaIbu());
        row.createCell(cellIndex++).setCellValue(student.getPekerjaanAyah());
        row.createCell(cellIndex++).setCellValue(student.getPekerjaanIbu());
        row.createCell(cellIndex++).setCellValue(student.getNoHpOrtu());
        row.createCell(cellIndex++).setCellValue(student.getAlamatOrtu());
        row.createCell(cellIndex++).setCellValue(student.getTahunMasuk() != null ? 
            student.getTahunMasuk().toString() : "");
        row.createCell(cellIndex++).setCellValue(student.getAsalSekolah());
        row.createCell(cellIndex++).setCellValue(student.getStatus() != null ? 
            student.getStatus().toString() : "");
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private void createInstructionsSheet(Workbook workbook) {
        Sheet instructionsSheet = workbook.createSheet("Instructions");
        
        String[] instructions = {
            "STUDENT IMPORT INSTRUCTIONS",
            "",
            "1. Fill in the student data in the 'Student Template' sheet",
            "2. Required fields: NIS, Nama Lengkap, Tahun Masuk",
            "3. Date format: DD/MM/YYYY (e.g., 01/01/2005)",
            "4. Gender: MALE or FEMALE",
            "5. Status: ACTIVE, INACTIVE, or GRADUATED",
            "6. NIS must be unique",
            "7. Year of entry must be between 2000 and current year + 1",
            "",
            "FIELD DESCRIPTIONS:",
            "- NIS: Student identification number",
            "- Nama Lengkap: Full name of the student",
            "- Tempat Lahir: Place of birth",
            "- Tanggal Lahir: Date of birth (DD/MM/YYYY)",
            "- Jenis Kelamin: Gender (MALE/FEMALE)",
            "- Agama: Religion",
            "- Alamat: Student address",
            "- Nama Ayah: Father's name",
            "- Nama Ibu: Mother's name",
            "- Pekerjaan Ayah: Father's occupation",
            "- Pekerjaan Ibu: Mother's occupation",
            "- No HP Orang Tua: Parent's phone number",
            "- Alamat Orang Tua: Parent's address",
            "- Tahun Masuk: Year of entry",
            "- Asal Sekolah: Previous school",
            "- Status: Student status (ACTIVE/INACTIVE/GRADUATED)"
        };

        for (int i = 0; i < instructions.length; i++) {
            Row row = instructionsSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(instructions[i]);
            
            if (i == 0) {
                CellStyle headerStyle = createHeaderStyle(workbook);
                cell.setCellStyle(headerStyle);
            }
        }

        instructionsSheet.autoSizeColumn(0);
    }

    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
            contentType.equals("application/vnd.ms-excel")
        );
    }

    private void validateHeaders(Row headerRow, List<String> errors) {
        for (int i = 0; i < STUDENT_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String expectedHeader = STUDENT_HEADERS[i];
            String actualHeader = getCellValueAsString(cell);
            
            if (!expectedHeader.equals(actualHeader)) {
                errors.add(String.format("Invalid header at column %d. Expected: '%s', Found: '%s'", 
                    i + 1, expectedHeader, actualHeader));
            }
        }
    }
}
