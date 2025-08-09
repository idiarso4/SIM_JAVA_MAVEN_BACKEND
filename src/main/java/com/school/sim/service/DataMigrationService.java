package com.school.sim.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for data migration from Laravel to Spring Boot
 * Provides comprehensive migration functionality with validation and rollback capabilities
 */
public interface DataMigrationService {

    // Migration Execution
    
    /**
     * Execute complete migration from Laravel database
     */
    Map<String, Object> executeCompleteMigration(Map<String, Object> migrationConfig);
    
    /**
     * Execute partial migration for specific tables
     */
    Map<String, Object> executePartialMigration(List<String> tableNames, Map<String, Object> migrationConfig);
    
    /**
     * Execute incremental migration (only new/updated records)
     */
    Map<String, Object> executeIncrementalMigration(Map<String, Object> migrationConfig);
    
    /**
     * Resume failed migration from checkpoint
     */
    Map<String, Object> resumeMigration(String migrationId);
    
    /**
     * Cancel running migration
     */
    void cancelMigration(String migrationId);
    
    // Data Extraction
    
    /**
     * Extract data from Laravel database
     */
    Map<String, List<Map<String, Object>>> extractLaravelData(Map<String, Object> extractionConfig);
    
    /**
     * Extract specific table data
     */
    List<Map<String, Object>> extractTableData(String tableName, Map<String, Object> config);
    
    /**
     * Initialize test data for development and testing
     */
    Map<String, Object> initializeTestData();
    
    /**
     * Extract data with pagination
     */
    Map<String, Object> extractDataWithPagination(String tableName, int page, int size, Map<String, Object> config);
    
    /**
     * Extract data by date range
     */
    List<Map<String, Object>> extractDataByDateRange(String tableName, String dateColumn, 
                                                    String startDate, String endDate, Map<String, Object> config);
    
    // Data Transformation
    
    /**
     * Transform Laravel data to Spring Boot format
     */
    Map<String, List<Map<String, Object>>> transformData(Map<String, List<Map<String, Object>>> laravelData);
    
    /**
     * Transform specific table data
     */
    List<Map<String, Object>> transformTableData(String tableName, List<Map<String, Object>> data);
    
    /**
     * Apply data mapping rules
     */
    Map<String, Object> applyDataMapping(String tableName, Map<String, Object> record);
    
    /**
     * Convert data types
     */
    Object convertDataType(Object value, String sourceType, String targetType);
    
    /**
     * Handle null values and defaults
     */
    Object handleNullValues(Object value, String columnName, Map<String, Object> defaultValues);
    
    // Data Loading
    
    /**
     * Load transformed data into Spring Boot database
     */
    Map<String, Object> loadData(Map<String, List<Map<String, Object>>> transformedData);
    
    /**
     * Load specific table data
     */
    Map<String, Object> loadTableData(String tableName, List<Map<String, Object>> data);
    
    /**
     * Bulk insert data
     */
    Map<String, Object> bulkInsertData(String tableName, List<Map<String, Object>> data);
    
    /**
     * Update existing records
     */
    Map<String, Object> updateExistingRecords(String tableName, List<Map<String, Object>> data);
    
    /**
     * Handle foreign key relationships
     */
    void handleForeignKeyRelationships(Map<String, List<Map<String, Object>>> allData);
    
    // Validation and Integrity
    
    /**
     * Validate migrated data integrity
     */
    Map<String, Object> validateDataIntegrity(Map<String, Object> validationConfig);
    
    /**
     * Compare source and target data
     */
    Map<String, Object> compareSourceAndTargetData(String tableName);
    
    /**
     * Validate foreign key constraints
     */
    List<Map<String, Object>> validateForeignKeyConstraints();
    
    /**
     * Check for duplicate records
     */
    List<Map<String, Object>> checkForDuplicateRecords(String tableName);
    
    /**
     * Validate data types and formats
     */
    List<Map<String, Object>> validateDataTypesAndFormats(String tableName);
    
    /**
     * Check required fields
     */
    List<Map<String, Object>> checkRequiredFields(String tableName);
    
    // Rollback and Recovery
    
    /**
     * Create migration backup
     */
    String createMigrationBackup();
    
    /**
     * Rollback migration
     */
    Map<String, Object> rollbackMigration(String migrationId);
    
    /**
     * Restore from backup
     */
    Map<String, Object> restoreFromBackup(String backupId);
    
    /**
     * Clean up failed migration data
     */
    void cleanupFailedMigration(String migrationId);
    
    // Progress Tracking
    
    /**
     * Get migration progress
     */
    Map<String, Object> getMigrationProgress(String migrationId);
    
    /**
     * Get migration history
     */
    List<Map<String, Object>> getMigrationHistory();
    
    /**
     * Get migration statistics
     */
    Map<String, Object> getMigrationStatistics(String migrationId);
    
    /**
     * Create migration checkpoint
     */
    String createMigrationCheckpoint(String migrationId, Map<String, Object> state);
    
    // Configuration Management
    
    /**
     * Load migration configuration
     */
    Map<String, Object> loadMigrationConfiguration(String configPath);
    
    /**
     * Validate migration configuration
     */
    Map<String, Object> validateMigrationConfiguration(Map<String, Object> config);
    
    /**
     * Get default migration configuration
     */
    Map<String, Object> getDefaultMigrationConfiguration();
    
    /**
     * Update migration configuration
     */
    void updateMigrationConfiguration(Map<String, Object> config);
    
    // Schema Migration
    
    /**
     * Migrate database schema
     */
    Map<String, Object> migrateDatabaseSchema(Map<String, Object> schemaConfig);
    
    /**
     * Compare database schemas
     */
    Map<String, Object> compareDatabaseSchemas();
    
    /**
     * Generate schema migration scripts
     */
    List<String> generateSchemaMigrationScripts();
    
    /**
     * Execute schema changes
     */
    Map<String, Object> executeSchemaChanges(List<String> migrationScripts);
    
    // Data Quality and Cleansing
    
    /**
     * Analyze data quality
     */
    Map<String, Object> analyzeDataQuality(String tableName);
    
    /**
     * Clean and normalize data
     */
    List<Map<String, Object>> cleanAndNormalizeData(String tableName, List<Map<String, Object>> data);
    
    /**
     * Handle data inconsistencies
     */
    List<Map<String, Object>> handleDataInconsistencies(String tableName, List<Map<String, Object>> data);
    
    /**
     * Apply data validation rules
     */
    List<Map<String, Object>> applyDataValidationRules(String tableName, List<Map<String, Object>> data);
    
    // Reporting and Documentation
    
    /**
     * Generate migration report
     */
    Map<String, Object> generateMigrationReport(String migrationId);
    
    /**
     * Generate data mapping documentation
     */
    String generateDataMappingDocumentation();
    
    /**
     * Generate migration summary
     */
    Map<String, Object> generateMigrationSummary(String migrationId);
    
    /**
     * Export migration logs
     */
    byte[] exportMigrationLogs(String migrationId);
    
    // Testing and Verification
    
    /**
     * Run migration tests
     */
    Map<String, Object> runMigrationTests(String migrationId);
    
    /**
     * Verify data consistency
     */
    Map<String, Object> verifyDataConsistency();
    
    /**
     * Test rollback functionality
     */
    Map<String, Object> testRollbackFunctionality(String migrationId);
    
    /**
     * Validate migration completeness
     */
    Map<String, Object> validateMigrationCompleteness(String migrationId);
    
    // Utility Methods
    
    /**
     * Get supported table mappings
     */
    Map<String, String> getSupportedTableMappings();
    
    /**
     * Get data type mappings
     */
    Map<String, String> getDataTypeMappings();
    
    /**
     * Estimate migration time
     */
    Map<String, Object> estimateMigrationTime(Map<String, Object> config);
    
    /**
     * Check migration prerequisites
     */
    Map<String, Object> checkMigrationPrerequisites();
    
    /**
     * Generate migration ID
     */
    String generateMigrationId();
}