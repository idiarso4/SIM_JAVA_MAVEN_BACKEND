package com.school.sim.controller;

import com.school.sim.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for export functionality
 * Provides endpoints for exporting data in various formats (Excel, CSV, JSON, PDF)
 */
@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export Management", description = "Data export endpoints")
@Validated
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private ExportService exportService;

    /**
     * Export data to Excel format
     */
    @PostMapping("/excel")
    @Operation(summary = "Export to Excel", description = "Export data to Excel format")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportToExcel(
            @Parameter(description = "Report type") @RequestParam String reportType,
            @Valid @RequestBody Map<String, Object> data,
            @Parameter(description = "Export options") @RequestParam(required = false) Map<String, Object> options) {
        
        logger.info("Exporting {} to Excel format", reportType);
        
        try {
            if (options == null) {
                options = new HashMap<>();
            }
            
            byte[] excelData = exportService.exportToExcel(reportType, data, options);
            String filename = exportService.generateExportFilename(reportType, "excel", options);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(exportService.getMimeType("excel")));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            logger.info("Successfully exported {} to Excel ({} bytes)", reportType, excelData.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            logger.error("Failed to export {} to Excel", reportType, e);
            throw e;
        }
    }

    /**
     * Export list data to Excel
     */
    @PostMapping("/excel/list")
    @Operation(summary = "Export list to Excel", description = "Export list data to Excel format")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportListToExcel(
            @Valid @RequestBody List<Map<String, Object>> data,
            @Parameter(description = "Column headers") @RequestParam(required = false) List<String> headers,
            @Parameter(description = "Sheet name") @RequestParam(defaultValue = "Data") String sheetName) {
        
        logger.info("Exporting list data to Excel ({} rows)", data.size());
        
        try {
            byte[] excelData = exportService.exportListToExcel(data, headers, sheetName);
            String filename = exportService.generateExportFilename("list_data", "excel", Map.of("sheet", sheetName));
            
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(exportService.getMimeType("excel")));
            httpHeaders.setContentDispositionFormData("attachment", filename);
            httpHeaders.setContentLength(excelData.length);
            
            logger.info("Successfully exported list data to Excel ({} bytes)", excelData.length);
            return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(excelData);
                
        } catch (Exception e) {
            logger.error("Failed to export list data to Excel", e);
            throw e;
        }
    }

    /**
     * Export multiple sheets to Excel
     */
    @PostMapping("/excel/multiple-sheets")
    @Operation(summary = "Export multiple sheets to Excel", description = "Export multiple data sets as separate Excel sheets")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportMultipleSheetsToExcel(
            @Valid @RequestBody Map<String, List<Map<String, Object>>> sheetsData,
            @Parameter(description = "Headers for each sheet") @RequestParam(required = false) Map<String, List<String>> headersMap) {
        
        logger.info("Exporting multiple sheets to Excel ({} sheets)", sheetsData.size());
        
        try {
            if (headersMap == null) {
                headersMap = new HashMap<>();
            }
            
            byte[] excelData = exportService.exportMultipleSheetsToExcel(sheetsData, headersMap);
            String filename = exportService.generateExportFilename("multi_sheet_data", "excel", 
                Map.of("sheets", sheetsData.size()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(exportService.getMimeType("excel")));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            logger.info("Successfully exported multiple sheets to Excel ({} bytes)", excelData.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            logger.error("Failed to export multiple sheets to Excel", e);
            throw e;
        }
    }

    /**
     * Export data to CSV format
     */
    @PostMapping("/csv")
    @Operation(summary = "Export to CSV", description = "Export data to CSV format")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportToCSV(
            @Valid @RequestBody List<Map<String, Object>> data,
            @Parameter(description = "Column headers") @RequestParam(required = false) List<String> headers,
            @Parameter(description = "Delimiter") @RequestParam(defaultValue = ",") String delimiter) {
        
        logger.info("Exporting data to CSV ({} rows)", data.size());
        
        try {
            byte[] csvData = exportService.exportToCSV(data, headers, delimiter);
            String filename = exportService.generateExportFilename("data", "csv", Map.of("delimiter", delimiter));
            
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(exportService.getMimeType("csv")));
            httpHeaders.setContentDispositionFormData("attachment", filename);
            httpHeaders.setContentLength(csvData.length);
            
            logger.info("Successfully exported data to CSV ({} bytes)", csvData.length);
            return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(csvData);
                
        } catch (Exception e) {
            logger.error("Failed to export data to CSV", e);
            throw e;
        }
    }

    /**
     * Export multiple CSV files as ZIP
     */
    @PostMapping("/csv/multiple")
    @Operation(summary = "Export multiple CSV files", description = "Export multiple data sets as CSV files in ZIP archive")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportMultipleCSV(
            @Valid @RequestBody Map<String, List<Map<String, Object>>> tablesData,
            @Parameter(description = "Headers for each table") @RequestParam(required = false) Map<String, List<String>> headersMap) {
        
        logger.info("Exporting multiple CSV files as ZIP ({} files)", tablesData.size());
        
        try {
            if (headersMap == null) {
                headersMap = new HashMap<>();
            }
            
            byte[] zipData = exportService.exportMultipleCSV(tablesData, headersMap);
            String filename = exportService.generateExportFilename("multi_csv_data", "zip", 
                Map.of("files", tablesData.size()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);
            
            logger.info("Successfully exported multiple CSV files as ZIP ({} bytes)", zipData.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(zipData);
                
        } catch (Exception e) {
            logger.error("Failed to export multiple CSV files", e);
            throw e;
        }
    }

    /**
     * Export data to JSON format
     */
    @PostMapping("/json")
    @Operation(summary = "Export to JSON", description = "Export data to JSON format")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportToJSON(
            @Valid @RequestBody Map<String, Object> data,
            @Parameter(description = "Pretty print") @RequestParam(defaultValue = "false") boolean prettyPrint) {
        
        logger.info("Exporting data to JSON (prettyPrint: {})", prettyPrint);
        
        try {
            byte[] jsonData = exportService.exportToFormattedJSON(data, prettyPrint);
            String filename = exportService.generateExportFilename("data", "json", 
                Map.of("prettyPrint", prettyPrint));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(exportService.getMimeType("json")));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(jsonData.length);
            
            logger.info("Successfully exported data to JSON ({} bytes)", jsonData.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(jsonData);
                
        } catch (Exception e) {
            logger.error("Failed to export data to JSON", e);
            throw e;
        }
    }

    /**
     * Start async export job
     */
    @PostMapping("/async")
    @Operation(summary = "Start async export", description = "Start asynchronous export job for large datasets")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Export job started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> startAsyncExport(
            @Parameter(description = "Export type") @RequestParam String exportType,
            @Parameter(description = "Export format") @RequestParam String format,
            @Valid @RequestBody Map<String, Object> data,
            @Parameter(description = "Export options") @RequestParam(required = false) Map<String, Object> options) {
        
        logger.info("Starting async export job (type: {}, format: {})", exportType, format);
        
        try {
            if (options == null) {
                options = new HashMap<>();
            }
            
            String jobId = exportService.startAsyncExport(exportType, format, data, options);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("message", "Export job started successfully");
            response.put("statusUrl", "/api/v1/export/async/" + jobId + "/status");
            response.put("resultUrl", "/api/v1/export/async/" + jobId + "/result");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully started async export job: {}", jobId);
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Failed to start async export job", e);
            throw e;
        }
    }

    /**
     * Get async export job status
     */
    @GetMapping("/async/{jobId}/status")
    @Operation(summary = "Get export job status", description = "Get status of asynchronous export job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getExportJobStatus(@PathVariable("jobId") @NotNull String jobId) {
        logger.debug("Getting status for export job: {}", jobId);
        
        try {
            Map<String, Object> status = exportService.getExportJobStatus(jobId);
            logger.debug("Retrieved status for export job: {}", jobId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get status for export job: {}", jobId, e);
            throw e;
        }
    }

    /**
     * Get async export job result
     */
    @GetMapping("/async/{jobId}/result")
    @Operation(summary = "Get export job result", description = "Download result of asynchronous export job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "202", description = "Job still processing"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<byte[]> getExportJobResult(@PathVariable("jobId") @NotNull String jobId) {
        logger.info("Getting result for export job: {}", jobId);
        
        try {
            // Check job status first
            Map<String, Object> status = exportService.getExportJobStatus(jobId);
            String jobStatus = (String) status.get("status");
            
            if (!"COMPLETED".equals(jobStatus)) {
                if ("PROCESSING".equals(jobStatus) || "STARTED".equals(jobStatus)) {
                    return ResponseEntity.accepted().build();
                } else {
                    throw new RuntimeException("Export job failed or was cancelled");
                }
            }
            
            byte[] result = exportService.getExportJobResult(jobId);
            String exportType = (String) status.get("exportType");
            String format = (String) status.get("format");
            
            String filename = exportService.generateExportFilename(exportType, format, 
                Map.of("jobId", jobId));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(exportService.getMimeType(format)));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(result.length);
            
            logger.info("Successfully retrieved result for export job: {} ({} bytes)", jobId, result.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(result);
                
        } catch (Exception e) {
            logger.error("Failed to get result for export job: {}", jobId, e);
            throw e;
        }
    }

    /**
     * Cancel async export job
     */
    @DeleteMapping("/async/{jobId}")
    @Operation(summary = "Cancel export job", description = "Cancel asynchronous export job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> cancelExportJob(@PathVariable("jobId") @NotNull String jobId) {
        logger.info("Cancelling export job: {}", jobId);
        
        try {
            exportService.cancelExportJob(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Export job cancelled successfully");
            response.put("jobId", jobId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully cancelled export job: {}", jobId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to cancel export job: {}", jobId, e);
            throw e;
        }
    }

    /**
     * Get supported export formats
     */
    @GetMapping("/formats")
    @Operation(summary = "Get supported formats", description = "Get list of supported export formats")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Formats retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        logger.debug("Getting supported export formats");
        
        try {
            List<String> formats = exportService.getSupportedFormats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("formats", formats);
            response.put("count", formats.size());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved {} supported export formats", formats.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get supported export formats", e);
            throw e;
        }
    }
}