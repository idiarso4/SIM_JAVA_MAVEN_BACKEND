package com.school.sim.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing database migrations from Laravel to Spring Boot
 * Provides utilities for data validation, integrity checking, and migration monitoring
 */
@Service
public class DatabaseMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Value("${app.migration.batch-size:1000}")
    private int batchSize;

    @Value("${app.migration.validation-enabled:true}")
    private boolean validationEnabled;

    /**
     * Validate database schema and indexes
     */
    public MigrationValidationResult validateDatabaseSchema() {
        logger.info("Starting database schema validation");
        
        MigrationValidationResult result = new MigrationValidationResult();
        
        try {
            // Validate required tables exist
            validateRequiredTables(result);
            
            // Validate indexes exist and are optimal
            validateIndexes(result);
            
            // Validate foreign key constraints
            validateForeignKeys(result);
            
            // Validate data integrity
            if (validationEnabled) {
                validateDataIntegrity(result);
            }
            
            logger.info("Database schema validation completed. Errors: {}, Warnings: {}", 
                       result.getErrors().size(), result.getWarnings().size());
            
        } catch (Exception e) {
            logger.error("Error during database schema validation", e);
            result.addError("Schema validation failed: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Validate that all required tables exist
     */
    private void validateRequiredTables(MigrationValidationResult result) throws SQLException {
        String[] requiredTables = {
            "users", "roles", "permissions", "user_roles", "role_permissions",
            "students", "class_rooms", "majors", "departments",
            "subjects", "schedules", "teaching_activities", "attendances",
            "assessments", "student_assessments"
        };

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (String tableName : requiredTables) {
                try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        result.addError("Required table missing: " + tableName);
                    } else {
                        result.addInfo("Table exists: " + tableName);
                    }
                }
            }
        }
    }

    /**
     * Validate that performance indexes exist
     */
    private void validateIndexes(MigrationValidationResult result) {
        Map<String, String[]> requiredIndexes = new HashMap<>();
        requiredIndexes.put("users", new String[]{"idx_user_email_type", "idx_user_nip"});
        requiredIndexes.put("students", new String[]{"idx_student_nis", "idx_student_class_status"});
        requiredIndexes.put("attendances", new String[]{"idx_attendance_student_date", "idx_attendance_teaching_activity"});
        requiredIndexes.put("teaching_activities", new String[]{"idx_teaching_activity_teacher_date", "idx_teaching_activity_classroom_date"});

        for (Map.Entry<String, String[]> entry : requiredIndexes.entrySet()) {
            String tableName = entry.getKey();
            String[] indexes = entry.getValue();
            
            for (String indexName : indexes) {
                if (!indexExists(tableName, indexName)) {
                    result.addWarning("Missing recommended index: " + indexName + " on table " + tableName);
                } else {
                    result.addInfo("Index exists: " + indexName);
                }
            }
        }
    }

    /**
     * Check if an index exists on a table
     */
    private boolean indexExists(String tableName, String indexName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, indexName);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.warn("Error checking index existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate foreign key constraints
     */
    private void validateForeignKeys(MigrationValidationResult result) {
        String sql = "SELECT COUNT(*) as orphaned_records, " +
                    "'students' as table_name, 'class_room_id' as column_name " +
                    "FROM students s LEFT JOIN class_rooms c ON s.class_room_id = c.id " +
                    "WHERE s.class_room_id IS NOT NULL AND c.id IS NULL " +
                    "UNION ALL " +
                    "SELECT COUNT(*) as orphaned_records, " +
                    "'students' as table_name, 'user_id' as column_name " +
                    "FROM students s LEFT JOIN users u ON s.user_id = u.id " +
                    "WHERE s.user_id IS NOT NULL AND u.id IS NULL " +
                    "UNION ALL " +
                    "SELECT COUNT(*) as orphaned_records, " +
                    "'attendances' as table_name, 'student_id' as column_name " +
                    "FROM attendances a LEFT JOIN students s ON a.student_id = s.id " +
                    "WHERE a.student_id IS NOT NULL AND s.id IS NULL";

        try {
            List<Map<String, Object>> orphanedRecords = jdbcTemplate.queryForList(sql);
            
            for (Map<String, Object> record : orphanedRecords) {
                Integer count = (Integer) record.get("orphaned_records");
                String tableName = (String) record.get("table_name");
                String columnName = (String) record.get("column_name");
                
                if (count > 0) {
                    result.addError("Found " + count + " orphaned records in " + tableName + "." + columnName);
                }
            }
        } catch (Exception e) {
            logger.error("Error validating foreign keys", e);
            result.addWarning("Could not validate foreign key constraints: " + e.getMessage());
        }
    }

    /**
     * Validate data integrity
     */
    private void validateDataIntegrity(MigrationValidationResult result) {
        // Check for duplicate NIS in students
        String duplicateNisSql = "SELECT nis, COUNT(*) as count FROM students GROUP BY nis HAVING COUNT(*) > 1";
        List<Map<String, Object>> duplicateNis = jdbcTemplate.queryForList(duplicateNisSql);
        
        if (!duplicateNis.isEmpty()) {
            result.addError("Found " + duplicateNis.size() + " duplicate NIS values in students table");
        }

        // Check for duplicate email in users
        String duplicateEmailSql = "SELECT email, COUNT(*) as count FROM users GROUP BY email HAVING COUNT(*) > 1";
        List<Map<String, Object>> duplicateEmails = jdbcTemplate.queryForList(duplicateEmailSql);
        
        if (!duplicateEmails.isEmpty()) {
            result.addError("Found " + duplicateEmails.size() + " duplicate email values in users table");
        }

        // Check for students without valid class assignments
        String invalidClassSql = "SELECT COUNT(*) FROM students WHERE class_room_id IS NULL AND status = 'ACTIVE'";
        Integer activeStudentsWithoutClass = jdbcTemplate.queryForObject(invalidClassSql, Integer.class);
        
        if (activeStudentsWithoutClass > 0) {
            result.addWarning("Found " + activeStudentsWithoutClass + " active students without class assignment");
        }
    }

    /**
     * Get database performance statistics
     */
    public DatabasePerformanceStats getDatabasePerformanceStats() {
        DatabasePerformanceStats stats = new DatabasePerformanceStats();
        
        try {
            // Get table sizes
            String tableSizeSql = "SELECT table_name, " +
                                 "ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb, " +
                                 "table_rows " +
                                 "FROM information_schema.tables " +
                                 "WHERE table_schema = DATABASE() " +
                                 "ORDER BY (data_length + index_length) DESC";
            
            List<Map<String, Object>> tableSizes = jdbcTemplate.queryForList(tableSizeSql);
            stats.setTableSizes(tableSizes);

            // Get index usage statistics
            String indexUsageSql = "SELECT table_name, index_name, " +
                                  "cardinality, " +
                                  "CASE WHEN non_unique = 0 THEN 'UNIQUE' ELSE 'NON-UNIQUE' END as uniqueness " +
                                  "FROM information_schema.statistics " +
                                  "WHERE table_schema = DATABASE() " +
                                  "ORDER BY table_name, seq_in_index";
            
            List<Map<String, Object>> indexStats = jdbcTemplate.queryForList(indexUsageSql);
            stats.setIndexStatistics(indexStats);

            // Get connection pool statistics (if available)
            stats.setConnectionPoolStats(getConnectionPoolStats());

        } catch (Exception e) {
            logger.error("Error collecting database performance statistics", e);
        }
        
        return stats;
    }

    /**
     * Get connection pool statistics
     */
    private Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> poolStats = new HashMap<>();
        
        try {
            // These would be specific to HikariCP
            String processListSql = "SHOW PROCESSLIST";
            List<Map<String, Object>> processes = jdbcTemplate.queryForList(processListSql);
            poolStats.put("active_connections", processes.size());
            
            String statusSql = "SHOW STATUS LIKE 'Threads_%'";
            List<Map<String, Object>> threadStats = jdbcTemplate.queryForList(statusSql);
            poolStats.put("thread_statistics", threadStats);
            
        } catch (Exception e) {
            logger.warn("Could not retrieve connection pool statistics: {}", e.getMessage());
        }
        
        return poolStats;
    }

    /**
     * Optimize database tables
     */
    @Transactional
    public void optimizeDatabaseTables() {
        logger.info("Starting database table optimization");
        
        String[] tablesToOptimize = {
            "users", "students", "attendances", "teaching_activities", 
            "assessments", "student_assessments", "class_rooms"
        };
        
        for (String tableName : tablesToOptimize) {
            try {
                logger.info("Optimizing table: {}", tableName);
                jdbcTemplate.execute("OPTIMIZE TABLE " + tableName);
                logger.info("Successfully optimized table: {}", tableName);
            } catch (Exception e) {
                logger.error("Error optimizing table {}: {}", tableName, e.getMessage());
            }
        }
        
        logger.info("Database table optimization completed");
    }

    /**
     * Update table statistics for query optimizer
     */
    public void updateTableStatistics() {
        logger.info("Updating table statistics");
        
        String[] tables = {
            "users", "students", "class_rooms", "majors", "departments",
            "subjects", "schedules", "teaching_activities", "attendances",
            "assessments", "student_assessments", "roles", "permissions"
        };
        
        for (String table : tables) {
            try {
                jdbcTemplate.execute("ANALYZE TABLE " + table);
                logger.debug("Updated statistics for table: {}", table);
            } catch (Exception e) {
                logger.warn("Could not update statistics for table {}: {}", table, e.getMessage());
            }
        }
        
        logger.info("Table statistics update completed");
    }

    /**
     * Migration validation result container
     */
    public static class MigrationValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> info = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void addInfo(String info) {
            this.info.add(info);
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public List<String> getInfo() {
            return info;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    /**
     * Database performance statistics container
     */
    public static class DatabasePerformanceStats {
        private List<Map<String, Object>> tableSizes;
        private List<Map<String, Object>> indexStatistics;
        private Map<String, Object> connectionPoolStats;

        // Getters and setters
        public List<Map<String, Object>> getTableSizes() {
            return tableSizes;
        }

        public void setTableSizes(List<Map<String, Object>> tableSizes) {
            this.tableSizes = tableSizes;
        }

        public List<Map<String, Object>> getIndexStatistics() {
            return indexStatistics;
        }

        public void setIndexStatistics(List<Map<String, Object>> indexStatistics) {
            this.indexStatistics = indexStatistics;
        }

        public Map<String, Object> getConnectionPoolStats() {
            return connectionPoolStats;
        }

        public void setConnectionPoolStats(Map<String, Object> connectionPoolStats) {
            this.connectionPoolStats = connectionPoolStats;
        }
    }
}
