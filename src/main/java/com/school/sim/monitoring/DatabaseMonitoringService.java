package com.school.sim.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for monitoring database performance and connection pool health
 * Provides real-time metrics and health checks for database operations
 */
@Service
public class DatabaseMonitoringService implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMonitoringService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile DatabaseHealthMetrics lastHealthMetrics;

    /**
     * Health check implementation for Spring Boot Actuator
     */
    @Override
    public Health health() {
        try {
            DatabaseHealthMetrics metrics = collectHealthMetrics();
            this.lastHealthMetrics = metrics;

            Health.Builder healthBuilder = Health.up();
            
            // Add connection pool metrics
            if (metrics.getConnectionPoolMetrics() != null) {
                healthBuilder.withDetails(metrics.getConnectionPoolMetrics());
            }

            // Check for warning conditions
            if (metrics.getActiveConnections() > metrics.getMaxConnections() * 0.8) {
                healthBuilder.status("WARNING")
                    .withDetail("warning", "High connection pool usage: " + 
                               metrics.getActiveConnections() + "/" + metrics.getMaxConnections());
            }

            // Check for critical conditions
            if (metrics.getActiveConnections() >= metrics.getMaxConnections()) {
                healthBuilder.down()
                    .withDetail("error", "Connection pool exhausted");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    /**
     * Collect comprehensive database health metrics
     */
    public DatabaseHealthMetrics collectHealthMetrics() {
        DatabaseHealthMetrics metrics = new DatabaseHealthMetrics();

        try {
            // Test database connectivity
            long startTime = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;
            metrics.setResponseTimeMs(responseTime);
            metrics.setDatabaseConnected(true);

            // Collect connection pool metrics
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                if (poolBean != null) {
                    Map<String, Object> poolMetrics = new HashMap<>();
                    poolMetrics.put("activeConnections", poolBean.getActiveConnections());
                    poolMetrics.put("idleConnections", poolBean.getIdleConnections());
                    poolMetrics.put("totalConnections", poolBean.getTotalConnections());
                    poolMetrics.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                    
                    metrics.setActiveConnections(poolBean.getActiveConnections());
                    metrics.setIdleConnections(poolBean.getIdleConnections());
                    metrics.setTotalConnections(poolBean.getTotalConnections());
                    metrics.setMaxConnections(hikariDataSource.getMaximumPoolSize());
                    metrics.setConnectionPoolMetrics(poolMetrics);
                }
            }

            // Collect database statistics
            collectDatabaseStatistics(metrics);

        } catch (Exception e) {
            logger.error("Error collecting database health metrics", e);
            metrics.setDatabaseConnected(false);
            metrics.setErrorMessage(e.getMessage());
        }

        return metrics;
    }

    /**
     * Collect database-specific statistics
     */
    private void collectDatabaseStatistics(DatabaseHealthMetrics metrics) {
        try {
            // Get table sizes
            String tableSizeSql = "SELECT " +
                "SUM(ROUND(((data_length + index_length) / 1024 / 1024), 2)) AS total_size_mb, " +
                "COUNT(*) AS table_count " +
                "FROM information_schema.tables " +
                "WHERE table_schema = DATABASE()";
            
            Map<String, Object> sizeInfo = jdbcTemplate.queryForMap(tableSizeSql);
            metrics.setTotalDatabaseSizeMb(((Number) sizeInfo.get("total_size_mb")).doubleValue());
            metrics.setTableCount(((Number) sizeInfo.get("table_count")).intValue());

            // Get slow query information
            String slowQuerySql = "SHOW STATUS LIKE 'Slow_queries'";
            try {
                Map<String, Object> slowQueryInfo = jdbcTemplate.queryForMap(slowQuerySql);
                metrics.setSlowQueryCount(Long.parseLong(slowQueryInfo.get("Value").toString()));
            } catch (Exception e) {
                logger.debug("Could not retrieve slow query count: {}", e.getMessage());
            }

            // Get connection statistics
            String connectionSql = "SHOW STATUS WHERE Variable_name IN ('Threads_connected', 'Threads_running', 'Max_used_connections')";
            try {
                Map<String, Object> connectionStats = new HashMap<>();
                jdbcTemplate.queryForList(connectionSql).forEach(row -> {
                    connectionStats.put(row.get("Variable_name").toString(), row.get("Value"));
                });
                metrics.setDatabaseConnectionStats(connectionStats);
            } catch (Exception e) {
                logger.debug("Could not retrieve connection statistics: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.warn("Error collecting database statistics: {}", e.getMessage());
        }
    }

    /**
     * Scheduled health monitoring - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void monitorDatabaseHealth() {
        try {
            DatabaseHealthMetrics metrics = collectHealthMetrics();
            
            // Log warnings for high resource usage
            if (metrics.getActiveConnections() > metrics.getMaxConnections() * 0.8) {
                logger.warn("High database connection pool usage: {}/{} ({}%)", 
                           metrics.getActiveConnections(), 
                           metrics.getMaxConnections(),
                           (metrics.getActiveConnections() * 100 / metrics.getMaxConnections()));
            }

            if (metrics.getResponseTimeMs() > 1000) {
                logger.warn("Slow database response time: {}ms", metrics.getResponseTimeMs());
            }

            // Log info every hour
            if (System.currentTimeMillis() % 3600000 < 300000) { // Within 5 minutes of the hour
                logger.info("Database Health - Connections: {}/{}, Response Time: {}ms, DB Size: {}MB", 
                           metrics.getActiveConnections(), 
                           metrics.getMaxConnections(),
                           metrics.getResponseTimeMs(),
                           metrics.getTotalDatabaseSizeMb());
            }

        } catch (Exception e) {
            logger.error("Error during scheduled database health monitoring", e);
        }
    }

    /**
     * Get the last collected health metrics
     */
    public DatabaseHealthMetrics getLastHealthMetrics() {
        return lastHealthMetrics;
    }

    /**
     * Force a connection pool cleanup (use with caution)
     */
    public void cleanupConnectionPool() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            try {
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();
                logger.info("Connection pool cleanup completed");
            } catch (Exception e) {
                logger.error("Error during connection pool cleanup", e);
            }
        }
    }

    /**
     * Container class for database health metrics
     */
    public static class DatabaseHealthMetrics {
        private boolean databaseConnected;
        private long responseTimeMs;
        private int activeConnections;
        private int idleConnections;
        private int totalConnections;
        private int maxConnections;
        private double totalDatabaseSizeMb;
        private int tableCount;
        private long slowQueryCount;
        private String errorMessage;
        private Map<String, Object> connectionPoolMetrics;
        private Map<String, Object> databaseConnectionStats;

        // Getters and setters
        public boolean isDatabaseConnected() {
            return databaseConnected;
        }

        public void setDatabaseConnected(boolean databaseConnected) {
            this.databaseConnected = databaseConnected;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public void setResponseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        public void setActiveConnections(int activeConnections) {
            this.activeConnections = activeConnections;
        }

        public int getIdleConnections() {
            return idleConnections;
        }

        public void setIdleConnections(int idleConnections) {
            this.idleConnections = idleConnections;
        }

        public int getTotalConnections() {
            return totalConnections;
        }

        public void setTotalConnections(int totalConnections) {
            this.totalConnections = totalConnections;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public double getTotalDatabaseSizeMb() {
            return totalDatabaseSizeMb;
        }

        public void setTotalDatabaseSizeMb(double totalDatabaseSizeMb) {
            this.totalDatabaseSizeMb = totalDatabaseSizeMb;
        }

        public int getTableCount() {
            return tableCount;
        }

        public void setTableCount(int tableCount) {
            this.tableCount = tableCount;
        }

        public long getSlowQueryCount() {
            return slowQueryCount;
        }

        public void setSlowQueryCount(long slowQueryCount) {
            this.slowQueryCount = slowQueryCount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Map<String, Object> getConnectionPoolMetrics() {
            return connectionPoolMetrics;
        }

        public void setConnectionPoolMetrics(Map<String, Object> connectionPoolMetrics) {
            this.connectionPoolMetrics = connectionPoolMetrics;
        }

        public Map<String, Object> getDatabaseConnectionStats() {
            return databaseConnectionStats;
        }

        public void setDatabaseConnectionStats(Map<String, Object> databaseConnectionStats) {
            this.databaseConnectionStats = databaseConnectionStats;
        }
    }
}
