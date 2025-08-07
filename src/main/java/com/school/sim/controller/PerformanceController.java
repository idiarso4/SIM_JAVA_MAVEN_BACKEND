package com.school.sim.controller;

import com.school.sim.service.CacheService;
import com.school.sim.service.DatabaseOptimizationService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for performance optimization operations
 * Provides endpoints for cache management and database optimization
 */
@RestController
@RequestMapping("/api/v1/performance")
@Tag(name = "Performance Management", description = "Performance optimization endpoints")
@Validated
public class PerformanceController {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceController.class);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private DatabaseOptimizationService databaseOptimizationService;

    // Cache Management Endpoints

    /**
     * Get cache statistics
     */
    @GetMapping("/cache/statistics")
    @Operation(summary = "Get cache statistics", description = "Get comprehensive cache statistics and metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        logger.info("Getting cache statistics");
        
        try {
            Map<String, Object> statistics = cacheService.getCacheStatistics();
            logger.info("Successfully retrieved cache statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get cache statistics", e);
            throw e;
        }
    }

    /**
     * Get cache statistics for specific cache
     */
    @GetMapping("/cache/statistics/{cacheName}")
    @Operation(summary = "Get cache statistics by name", description = "Get statistics for a specific cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Cache not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStatistics(@PathVariable("cacheName") @NotNull String cacheName) {
        logger.info("Getting cache statistics for cache: {}", cacheName);
        
        try {
            Map<String, Object> statistics = cacheService.getCacheStatistics(cacheName);
            logger.info("Successfully retrieved statistics for cache: {}", cacheName);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get statistics for cache: {}", cacheName, e);
            throw e;
        }
    }

    /**
     * Invalidate all caches
     */
    @DeleteMapping("/cache/all")
    @Operation(summary = "Invalidate all caches", description = "Clear all cache entries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All caches invalidated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> invalidateAllCaches() {
        logger.info("Invalidating all caches");
        
        try {
            cacheService.invalidateAllCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All caches invalidated successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully invalidated all caches");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to invalidate all caches", e);
            throw e;
        }
    }

    /**
     * Invalidate specific cache
     */
    @DeleteMapping("/cache/{cacheName}")
    @Operation(summary = "Invalidate specific cache", description = "Clear all entries from a specific cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache invalidated successfully"),
        @ApiResponse(responseCode = "404", description = "Cache not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> invalidateCache(@PathVariable("cacheName") @NotNull String cacheName) {
        logger.info("Invalidating cache: {}", cacheName);
        
        try {
            cacheService.invalidateCache(cacheName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache invalidated successfully");
            response.put("cacheName", cacheName);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully invalidated cache: {}", cacheName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to invalidate cache: {}", cacheName, e);
            throw e;
        }
    }

    /**
     * Invalidate cache entry
     */
    @DeleteMapping("/cache/{cacheName}/entry/{key}")
    @Operation(summary = "Invalidate cache entry", description = "Remove specific entry from cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache entry invalidated successfully"),
        @ApiResponse(responseCode = "404", description = "Cache or entry not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> invalidateCacheEntry(
            @PathVariable("cacheName") @NotNull String cacheName,
            @PathVariable("key") @NotNull String key) {
        
        logger.info("Invalidating cache entry - cache: {}, key: {}", cacheName, key);
        
        try {
            cacheService.invalidateCacheEntry(cacheName, key);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache entry invalidated successfully");
            response.put("cacheName", cacheName);
            response.put("key", key);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully invalidated cache entry - cache: {}, key: {}", cacheName, key);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to invalidate cache entry - cache: {}, key: {}", cacheName, key, e);
            throw e;
        }
    }

    /**
     * Check cache health
     */
    @GetMapping("/cache/health")
    @Operation(summary = "Check cache health", description = "Check cache system health and connectivity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache health checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkCacheHealth() {
        logger.info("Checking cache health");
        
        try {
            Map<String, Object> health = cacheService.checkCacheHealth();
            logger.info("Successfully checked cache health");
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Failed to check cache health", e);
            throw e;
        }
    }

    /**
     * Warm up caches
     */
    @PostMapping("/cache/warmup")
    @Operation(summary = "Warm up caches", description = "Pre-load frequently accessed data into cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache warm-up completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> warmUpCaches() {
        logger.info("Starting cache warm-up");
        
        try {
            cacheService.warmUpCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache warm-up completed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully completed cache warm-up");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to warm up caches", e);
            throw e;
        }
    }

    // Database Optimization Endpoints

    /**
     * Get database performance metrics
     */
    @GetMapping("/database/metrics")
    @Operation(summary = "Get database performance metrics", description = "Get comprehensive database performance metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDatabasePerformanceMetrics() {
        logger.info("Getting database performance metrics");
        
        try {
            Map<String, Object> metrics = databaseOptimizationService.getDatabasePerformanceMetrics();
            logger.info("Successfully retrieved database performance metrics");
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to get database performance metrics", e);
            throw e;
        }
    }

    /**
     * Analyze slow queries
     */
    @GetMapping("/database/slow-queries")
    @Operation(summary = "Analyze slow queries", description = "Get analysis of slow-running database queries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slow queries analyzed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> analyzeSlowQueries() {
        logger.info("Analyzing slow queries");
        
        try {
            List<Map<String, Object>> slowQueries = databaseOptimizationService.analyzeSlowQueries();
            logger.info("Successfully analyzed {} slow queries", slowQueries.size());
            return ResponseEntity.ok(slowQueries);
        } catch (Exception e) {
            logger.error("Failed to analyze slow queries", e);
            throw e;
        }
    }

    /**
     * Get query execution plan
     */
    @PostMapping("/database/explain")
    @Operation(summary = "Get query execution plan", description = "Get execution plan for a specific query")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution plan retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getQueryExecutionPlan(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        logger.info("Getting execution plan for query");
        
        try {
            Map<String, Object> executionPlan = databaseOptimizationService.getQueryExecutionPlan(query);
            logger.info("Successfully retrieved execution plan");
            return ResponseEntity.ok(executionPlan);
        } catch (Exception e) {
            logger.error("Failed to get execution plan", e);
            throw e;
        }
    }

    /**
     * Identify missing indexes
     */
    @GetMapping("/database/missing-indexes")
    @Operation(summary = "Identify missing indexes", description = "Identify database indexes that could improve performance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Missing indexes identified successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> identifyMissingIndexes() {
        logger.info("Identifying missing indexes");
        
        try {
            List<Map<String, Object>> missingIndexes = databaseOptimizationService.identifyMissingIndexes();
            logger.info("Successfully identified {} missing indexes", missingIndexes.size());
            return ResponseEntity.ok(missingIndexes);
        } catch (Exception e) {
            logger.error("Failed to identify missing indexes", e);
            throw e;
        }
    }

    /**
     * Create performance indexes
     */
    @PostMapping("/database/indexes/create")
    @Operation(summary = "Create performance indexes", description = "Create performance-critical database indexes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Indexes created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> createPerformanceIndexes() {
        logger.info("Creating performance indexes");
        
        try {
            databaseOptimizationService.createPerformanceIndexes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Performance indexes created successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully created performance indexes");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create performance indexes", e);
            throw e;
        }
    }

    /**
     * Analyze index usage
     */
    @GetMapping("/database/indexes/usage")
    @Operation(summary = "Analyze index usage", description = "Get statistics on database index usage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Index usage analyzed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> analyzeIndexUsage() {
        logger.info("Analyzing index usage");
        
        try {
            List<Map<String, Object>> indexUsage = databaseOptimizationService.analyzeIndexUsage();
            logger.info("Successfully analyzed {} indexes", indexUsage.size());
            return ResponseEntity.ok(indexUsage);
        } catch (Exception e) {
            logger.error("Failed to analyze index usage", e);
            throw e;
        }
    }

    /**
     * Get table size statistics
     */
    @GetMapping("/database/tables/statistics")
    @Operation(summary = "Get table size statistics", description = "Get size and row count statistics for database tables")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTableSizeStatistics() {
        logger.info("Getting table size statistics");
        
        try {
            List<Map<String, Object>> tableStats = databaseOptimizationService.getTableSizeStatistics();
            logger.info("Successfully retrieved statistics for {} tables", tableStats.size());
            return ResponseEntity.ok(tableStats);
        } catch (Exception e) {
            logger.error("Failed to get table size statistics", e);
            throw e;
        }
    }

    /**
     * Check database health
     */
    @GetMapping("/database/health")
    @Operation(summary = "Check database health", description = "Check database system health and performance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database health checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        logger.info("Checking database health");
        
        try {
            Map<String, Object> health = databaseOptimizationService.checkDatabaseHealth();
            logger.info("Successfully checked database health");
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Failed to check database health", e);
            throw e;
        }
    }

    /**
     * Monitor long-running queries
     */
    @GetMapping("/database/long-running-queries")
    @Operation(summary = "Monitor long-running queries", description = "Get list of currently running long queries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Long-running queries retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> monitorLongRunningQueries() {
        logger.info("Monitoring long-running queries");
        
        try {
            List<Map<String, Object>> longRunningQueries = databaseOptimizationService.monitorLongRunningQueries();
            logger.info("Successfully retrieved {} long-running queries", longRunningQueries.size());
            return ResponseEntity.ok(longRunningQueries);
        } catch (Exception e) {
            logger.error("Failed to monitor long-running queries", e);
            throw e;
        }
    }

    /**
     * Perform database maintenance
     */
    @PostMapping("/database/maintenance")
    @Operation(summary = "Perform database maintenance", description = "Execute database maintenance operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database maintenance completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> performDatabaseMaintenance() {
        logger.info("Performing database maintenance");
        
        try {
            databaseOptimizationService.performDatabaseMaintenance();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Database maintenance completed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully completed database maintenance");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to perform database maintenance", e);
            throw e;
        }
    }

    /**
     * Get connection pool statistics
     */
    @GetMapping("/database/connection-pool")
    @Operation(summary = "Get connection pool statistics", description = "Get database connection pool statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connection pool statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getConnectionPoolStatistics() {
        logger.info("Getting connection pool statistics");
        
        try {
            Map<String, Object> poolStats = databaseOptimizationService.getConnectionPoolStatistics();
            logger.info("Successfully retrieved connection pool statistics");
            return ResponseEntity.ok(poolStats);
        } catch (Exception e) {
            logger.error("Failed to get connection pool statistics", e);
            throw e;
        }
    }

    /**
     * Get comprehensive performance overview
     */
    @GetMapping("/overview")
    @Operation(summary = "Get performance overview", description = "Get comprehensive performance overview including cache and database metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Performance overview retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceOverview() {
        logger.info("Getting comprehensive performance overview");
        
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // Cache metrics
            overview.put("cache", cacheService.getCacheStatistics());
            
            // Database metrics
            overview.put("database", databaseOptimizationService.getDatabasePerformanceMetrics());
            
            // Health checks
            Map<String, Object> health = new HashMap<>();
            health.put("cache", cacheService.checkCacheHealth());
            health.put("database", databaseOptimizationService.checkDatabaseHealth());
            overview.put("health", health);
            
            overview.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully retrieved comprehensive performance overview");
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            logger.error("Failed to get performance overview", e);
            throw e;
        }
    }
}