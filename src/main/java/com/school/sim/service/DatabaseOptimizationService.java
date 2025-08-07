package com.school.sim.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for database optimization operations
 * Provides methods for query optimization, indexing, and performance monitoring
 */
public interface DatabaseOptimizationService {

    // Query Optimization
    
    /**
     * Analyze slow queries and provide optimization recommendations
     */
    List<Map<String, Object>> analyzeSlowQueries();
    
    /**
     * Get query execution plan
     */
    Map<String, Object> getQueryExecutionPlan(String query);
    
    /**
     * Optimize specific query
     */
    Map<String, Object> optimizeQuery(String query);
    
    /**
     * Get query performance statistics
     */
    Map<String, Object> getQueryPerformanceStats(String query);
    
    /**
     * Identify missing indexes for queries
     */
    List<Map<String, Object>> identifyMissingIndexes();
    
    // Index Management
    
    /**
     * Create performance-critical indexes
     */
    void createPerformanceIndexes();
    
    /**
     * Create index for specific table and columns
     */
    void createIndex(String tableName, List<String> columns, String indexName);
    
    /**
     * Drop unused indexes
     */
    void dropUnusedIndexes();
    
    /**
     * Analyze index usage statistics
     */
    List<Map<String, Object>> analyzeIndexUsage();
    
    /**
     * Get index recommendations
     */
    List<Map<String, Object>> getIndexRecommendations();
    
    /**
     * Rebuild fragmented indexes
     */
    void rebuildFragmentedIndexes();
    
    // Database Statistics
    
    /**
     * Update table statistics
     */
    void updateTableStatistics();
    
    /**
     * Get database performance metrics
     */
    Map<String, Object> getDatabasePerformanceMetrics();
    
    /**
     * Get table size statistics
     */
    List<Map<String, Object>> getTableSizeStatistics();
    
    /**
     * Get connection pool statistics
     */
    Map<String, Object> getConnectionPoolStatistics();
    
    // Query Monitoring
    
    /**
     * Monitor long-running queries
     */
    List<Map<String, Object>> monitorLongRunningQueries();
    
    /**
     * Get active connections
     */
    List<Map<String, Object>> getActiveConnections();
    
    /**
     * Kill long-running query
     */
    void killLongRunningQuery(Long processId);
    
    /**
     * Get query cache statistics
     */
    Map<String, Object> getQueryCacheStatistics();
    
    // Performance Tuning
    
    /**
     * Optimize database configuration
     */
    Map<String, Object> optimizeDatabaseConfiguration();
    
    /**
     * Tune memory settings
     */
    Map<String, Object> tuneMemorySettings();
    
    /**
     * Optimize buffer pool
     */
    Map<String, Object> optimizeBufferPool();
    
    /**
     * Configure query cache
     */
    Map<String, Object> configureQueryCache();
    
    // Maintenance Operations
    
    /**
     * Perform database maintenance
     */
    void performDatabaseMaintenance();
    
    /**
     * Optimize tables
     */
    void optimizeTables();
    
    /**
     * Analyze tables
     */
    void analyzeTables();
    
    /**
     * Clean up temporary tables
     */
    void cleanupTemporaryTables();
    
    /**
     * Purge old logs
     */
    void purgeOldLogs(int daysToKeep);
    
    // Backup and Recovery
    
    /**
     * Create database backup
     */
    Map<String, Object> createDatabaseBackup();
    
    /**
     * Verify backup integrity
     */
    Boolean verifyBackupIntegrity(String backupPath);
    
    /**
     * Get backup history
     */
    List<Map<String, Object>> getBackupHistory();
    
    // Health Monitoring
    
    /**
     * Check database health
     */
    Map<String, Object> checkDatabaseHealth();
    
    /**
     * Monitor disk space usage
     */
    Map<String, Object> monitorDiskSpaceUsage();
    
    /**
     * Check replication status
     */
    Map<String, Object> checkReplicationStatus();
    
    /**
     * Monitor database locks
     */
    List<Map<String, Object>> monitorDatabaseLocks();
    
    // Reporting
    
    /**
     * Generate performance report
     */
    Map<String, Object> generatePerformanceReport();
    
    /**
     * Generate index usage report
     */
    Map<String, Object> generateIndexUsageReport();
    
    /**
     * Generate query analysis report
     */
    Map<String, Object> generateQueryAnalysisReport();
    
    /**
     * Generate database health report
     */
    Map<String, Object> generateDatabaseHealthReport();
    
    // Pagination Optimization
    
    /**
     * Optimize pagination queries
     */
    Map<String, Object> optimizePaginationQuery(String baseQuery, int page, int size);
    
    /**
     * Create cursor-based pagination
     */
    Map<String, Object> createCursorBasedPagination(String query, String cursorColumn, Object cursorValue, int limit);
    
    /**
     * Analyze pagination performance
     */
    Map<String, Object> analyzePaginationPerformance(String query);
    
    // Connection Pool Management
    
    /**
     * Configure connection pool
     */
    void configureConnectionPool(Map<String, Object> config);
    
    /**
     * Monitor connection pool health
     */
    Map<String, Object> monitorConnectionPoolHealth();
    
    /**
     * Optimize connection pool settings
     */
    Map<String, Object> optimizeConnectionPoolSettings();
    
    /**
     * Reset connection pool
     */
    void resetConnectionPool();
    
    // Utility Methods
    
    /**
     * Execute custom optimization query
     */
    List<Map<String, Object>> executeOptimizationQuery(String query);
    
    /**
     * Get database version and configuration
     */
    Map<String, Object> getDatabaseInfo();
    
    /**
     * Validate database schema
     */
    Map<String, Object> validateDatabaseSchema();
    
    /**
     * Get table constraints
     */
    List<Map<String, Object>> getTableConstraints(String tableName);
}