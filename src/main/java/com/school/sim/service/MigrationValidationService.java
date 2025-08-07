package com.school.sim.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for migration validation and testing
 * Provides comprehensive validation tools and testing capabilities for data migration
 */
public interface MigrationValidationService {

    // Data Validation
    
    /**
     * Validate migrated data completeness
     */
    Map<String, Object> validateDataCompleteness(String migrationId);
    
    /**
     * Validate data accuracy between source and target
     */
    Map<String, Object> validateDataAccuracy(String tableName);
    
    /**
     * Validate referential integrity
     */
    Map<String, Object> validateReferentialIntegrity();
    
    /**
     * Validate data consistency across tables
     */
    Map<String, Object> validateDataConsistency();
    
    /**
     * Validate business rules compliance
     */
    Map<String, Object> validateBusinessRules(String tableName);
    
    // Data Comparison
    
    /**
     * Compare record counts between source and target
     */
    Map<String, Object> compareRecordCounts();
    
    /**
     * Compare data checksums
     */
    Map<String, Object> compareDataChecksums(String tableName);
    
    /**
     * Compare sample data records
     */
    Map<String, Object> compareSampleData(String tableName, int sampleSize);
    
    /**
     * Identify data discrepancies
     */
    List<Map<String, Object>> identifyDataDiscrepancies(String tableName);
    
    // Migration Testing
    
    /**
     * Run comprehensive migration test suite
     */
    Map<String, Object> runMigrationTestSuite(String migrationId);
    
    /**
     * Test data extraction functionality
     */
    Map<String, Object> testDataExtraction();
    
    /**
     * Test data transformation functionality
     */
    Map<String, Object> testDataTransformation();
    
    /**
     * Test data loading functionality
     */
    Map<String, Object> testDataLoading();
    
    /**
     * Test rollback functionality
     */
    Map<String, Object> testRollbackFunctionality(String migrationId);
    
    // Performance Testing
    
    /**
     * Test migration performance
     */
    Map<String, Object> testMigrationPerformance(Map<String, Object> testConfig);
    
    /**
     * Benchmark migration speed
     */
    Map<String, Object> benchmarkMigrationSpeed(String tableName, int recordCount);
    
    /**
     * Test concurrent migration scenarios
     */
    Map<String, Object> testConcurrentMigration();
    
    /**
     * Test large dataset migration
     */
    Map<String, Object> testLargeDatasetMigration(String tableName);
    
    // Progress Tracking and Reporting
    
    /**
     * Track migration validation progress
     */
    Map<String, Object> trackValidationProgress(String validationId);
    
    /**
     * Generate validation report
     */
    Map<String, Object> generateValidationReport(String migrationId);
    
    /**
     * Generate test execution report
     */
    Map<String, Object> generateTestExecutionReport(String testSuiteId);
    
    /**
     * Generate data quality report
     */
    Map<String, Object> generateDataQualityReport();
    
    // Error Detection and Analysis
    
    /**
     * Detect migration errors
     */
    List<Map<String, Object>> detectMigrationErrors(String migrationId);
    
    /**
     * Analyze error patterns
     */
    Map<String, Object> analyzeErrorPatterns(List<Map<String, Object>> errors);
    
    /**
     * Suggest error resolutions
     */
    List<Map<String, Object>> suggestErrorResolutions(List<Map<String, Object>> errors);
    
    /**
     * Validate error fixes
     */
    Map<String, Object> validateErrorFixes(String migrationId);
    
    // Automated Testing
    
    /**
     * Create automated test cases
     */
    List<Map<String, Object>> createAutomatedTestCases(String tableName);
    
    /**
     * Execute automated tests
     */
    Map<String, Object> executeAutomatedTests(List<Map<String, Object>> testCases);
    
    /**
     * Schedule recurring validation tests
     */
    String scheduleRecurringValidation(Map<String, Object> scheduleConfig);
    
    /**
     * Generate test data for validation
     */
    List<Map<String, Object>> generateTestData(String tableName, int recordCount);
    
    // Documentation and Compliance
    
    /**
     * Generate migration documentation
     */
    String generateMigrationDocumentation(String migrationId);
    
    /**
     * Create validation checklist
     */
    List<Map<String, Object>> createValidationChecklist();
    
    /**
     * Generate compliance report
     */
    Map<String, Object> generateComplianceReport(String migrationId);
    
    /**
     * Document validation procedures
     */
    String documentValidationProcedures();
    
    // Utility Methods
    
    /**
     * Get validation configuration
     */
    Map<String, Object> getValidationConfiguration();
    
    /**
     * Update validation rules
     */
    void updateValidationRules(Map<String, Object> rules);
    
    /**
     * Get supported validation types
     */
    List<String> getSupportedValidationTypes();
    
    /**
     * Estimate validation time
     */
    Map<String, Object> estimateValidationTime(String validationType);
}