package com.school.sim.service;

import com.school.sim.dto.response.StudentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;


/**
 * Service interface for Excel import/export operations
 * Provides methods for importing and exporting student data using Apache POI
 */
public interface ExcelService {

    /**
     * Import students from Excel file
     */
    ImportResult importStudentsFromExcel(MultipartFile file);

    /**
     * Export students to Excel file
     */
    ByteArrayOutputStream exportStudentsToExcel(List<StudentResponse> students);

    /**
     * Export students by class room to Excel
     */
    ByteArrayOutputStream exportStudentsByClassRoomToExcel(Long classRoomId);

    /**
     * Export all students to Excel
     */
    ByteArrayOutputStream exportAllStudentsToExcel();

    /**
     * Generate student import template
     */
    ByteArrayOutputStream generateStudentImportTemplate();

    /**
     * Validate Excel file format
     */
    ValidationResult validateExcelFile(MultipartFile file);

    /**
     * Import result container
     */
    class ImportResult {
        private int totalRows;
        private int successfulImports;
        private int failedImports;
        private List<ImportError> errors;
        private List<StudentResponse> importedStudents;

        // Constructors
        public ImportResult() {}

        public ImportResult(int totalRows, int successfulImports, int failedImports, 
                           List<ImportError> errors, List<StudentResponse> importedStudents) {
            this.totalRows = totalRows;
            this.successfulImports = successfulImports;
            this.failedImports = failedImports;
            this.errors = errors;
            this.importedStudents = importedStudents;
        }

        // Getters and setters
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        public int getSuccessfulImports() { return successfulImports; }
        public void setSuccessfulImports(int successfulImports) { this.successfulImports = successfulImports; }
        public int getFailedImports() { return failedImports; }
        public void setFailedImports(int failedImports) { this.failedImports = failedImports; }
        public List<ImportError> getErrors() { return errors; }
        public void setErrors(List<ImportError> errors) { this.errors = errors; }
        public List<StudentResponse> getImportedStudents() { return importedStudents; }
        public void setImportedStudents(List<StudentResponse> importedStudents) { this.importedStudents = importedStudents; }
    }

    /**
     * Import error container
     */
    class ImportError {
        private int rowNumber;
        private String field;
        private String value;
        private String errorMessage;

        // Constructors
        public ImportError() {}

        public ImportError(int rowNumber, String field, String value, String errorMessage) {
            this.rowNumber = rowNumber;
            this.field = field;
            this.value = value;
            this.errorMessage = errorMessage;
        }

        // Getters and setters
        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Validation result container
     */
    class ValidationResult {
        private boolean valid;
        private List<String> errors;

        // Constructors
        public ValidationResult() {}

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
