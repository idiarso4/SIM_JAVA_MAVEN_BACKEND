package com.school.sim.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for export functionality with multiple formats
 * Provides methods for exporting data to Excel, PDF, CSV, and other formats
 */
public interface ExportService {

    // Excel Export
    
    /**
     * Export data to Excel format
     */
    byte[] exportToExcel(String reportType, Map<String, Object> data, Map<String, Object> options);
    
    /**
     * Export list data to Excel with custom headers
     */
    byte[] exportListToExcel(List<Map<String, Object>> data, List<String> headers, String sheetName);
    
    /**
     * Export multiple sheets to Excel
     */
    byte[] exportMultipleSheetsToExcel(Map<String, List<Map<String, Object>>> sheetsData, Map<String, List<String>> headersMap);
    
    /**
     * Export with Excel template
     */
    byte[] exportWithExcelTemplate(String templatePath, Map<String, Object> data);
    
    // PDF Export
    
    /**
     * Export data to PDF format
     */
    byte[] exportToPDF(String reportType, Map<String, Object> data, Map<String, Object> options);
    
    /**
     * Export HTML content to PDF
     */
    byte[] exportHTMLToPDF(String htmlContent, Map<String, Object> options);
    
    /**
     * Export with PDF template
     */
    byte[] exportWithPDFTemplate(String templatePath, Map<String, Object> data);
    
    /**
     * Generate PDF report with charts
     */
    byte[] exportPDFWithCharts(Map<String, Object> data, List<Map<String, Object>> chartConfigs);
    
    // CSV Export
    
    /**
     * Export data to CSV format
     */
    byte[] exportToCSV(List<Map<String, Object>> data, List<String> headers);
    
    /**
     * Export data to CSV with custom delimiter
     */
    byte[] exportToCSV(List<Map<String, Object>> data, List<String> headers, String delimiter);
    
    /**
     * Export multiple tables to CSV (ZIP format)
     */
    byte[] exportMultipleCSV(Map<String, List<Map<String, Object>>> tablesData, Map<String, List<String>> headersMap);
    
    // JSON Export
    
    /**
     * Export data to JSON format
     */
    byte[] exportToJSON(Map<String, Object> data);
    
    /**
     * Export data to formatted JSON
     */
    byte[] exportToFormattedJSON(Map<String, Object> data, boolean prettyPrint);
    
    // XML Export
    
    /**
     * Export data to XML format
     */
    byte[] exportToXML(Map<String, Object> data, String rootElementName);
    
    /**
     * Export with XML schema validation
     */
    byte[] exportToXMLWithSchema(Map<String, Object> data, String schemaPath);
    
    // Progress Tracking for Large Exports
    
    /**
     * Start async export job
     */
    String startAsyncExport(String exportType, String format, Map<String, Object> data, Map<String, Object> options);
    
    /**
     * Get export job status
     */
    Map<String, Object> getExportJobStatus(String jobId);
    
    /**
     * Get export job result
     */
    byte[] getExportJobResult(String jobId);
    
    /**
     * Cancel export job
     */
    void cancelExportJob(String jobId);
    
    /**
     * Clean up completed export jobs
     */
    void cleanupCompletedJobs(int hoursOld);
    
    // Template Management
    
    /**
     * Create export template
     */
    Map<String, Object> createExportTemplate(String templateName, String format, byte[] templateData, Map<String, Object> metadata);
    
    /**
     * Update export template
     */
    Map<String, Object> updateExportTemplate(Long templateId, byte[] templateData, Map<String, Object> metadata);
    
    /**
     * Delete export template
     */
    void deleteExportTemplate(Long templateId);
    
    /**
     * Get export template
     */
    Map<String, Object> getExportTemplate(Long templateId);
    
    /**
     * List all export templates
     */
    List<Map<String, Object>> listExportTemplates(String format);
    
    // Format Conversion
    
    /**
     * Convert between formats
     */
    byte[] convertFormat(byte[] sourceData, String sourceFormat, String targetFormat);
    
    /**
     * Get supported export formats
     */
    List<String> getSupportedFormats();
    
    /**
     * Validate export format
     */
    boolean isFormatSupported(String format);
    
    // Compression and Archiving
    
    /**
     * Compress export data
     */
    byte[] compressExportData(byte[] data, String compressionType);
    
    /**
     * Create ZIP archive with multiple files
     */
    byte[] createZipArchive(Map<String, byte[]> files);
    
    /**
     * Extract files from ZIP archive
     */
    Map<String, byte[]> extractZipArchive(byte[] zipData);
    
    // Export Validation and Quality
    
    /**
     * Validate export data
     */
    Map<String, Object> validateExportData(Map<String, Object> data, String format);
    
    /**
     * Check export data quality
     */
    Map<String, Object> checkExportDataQuality(Map<String, Object> data);
    
    /**
     * Estimate export file size
     */
    Map<String, Object> estimateExportSize(Map<String, Object> data, String format);
    
    // Security and Access Control
    
    /**
     * Apply password protection to export
     */
    byte[] applyPasswordProtection(byte[] data, String format, String password);
    
    /**
     * Apply digital signature to export
     */
    byte[] applyDigitalSignature(byte[] data, String format, Map<String, Object> signatureConfig);
    
    /**
     * Encrypt export data
     */
    byte[] encryptExportData(byte[] data, String encryptionKey);
    
    /**
     * Decrypt export data
     */
    byte[] decryptExportData(byte[] encryptedData, String decryptionKey);
    
    // Batch Export Operations
    
    /**
     * Export multiple reports in batch
     */
    Map<String, byte[]> batchExport(List<Map<String, Object>> exportRequests);
    
    /**
     * Schedule batch export
     */
    String scheduleBatchExport(List<Map<String, Object>> exportRequests, String cronExpression);
    
    /**
     * Get batch export status
     */
    Map<String, Object> getBatchExportStatus(String batchId);
    
    // Export History and Audit
    
    /**
     * Log export activity
     */
    void logExportActivity(String userId, String exportType, String format, Map<String, Object> metadata);
    
    /**
     * Get export history
     */
    List<Map<String, Object>> getExportHistory(String userId, String exportType, int days);
    
    /**
     * Get export statistics
     */
    Map<String, Object> getExportStatistics(String period);
    
    // Utility Methods
    
    /**
     * Generate export filename
     */
    String generateExportFilename(String reportType, String format, Map<String, Object> options);
    
    /**
     * Get MIME type for format
     */
    String getMimeType(String format);
    
    /**
     * Get file extension for format
     */
    String getFileExtension(String format);
    
    /**
     * Sanitize data for export
     */
    Map<String, Object> sanitizeExportData(Map<String, Object> data);
    
    /**
     * Apply export filters
     */
    Map<String, Object> applyExportFilters(Map<String, Object> data, List<Map<String, Object>> filters);
    
    /**
     * Transform data for export format
     */
    Map<String, Object> transformDataForFormat(Map<String, Object> data, String format);
}