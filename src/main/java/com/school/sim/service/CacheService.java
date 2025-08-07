package com.school.sim.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service interface for cache management operations
 * Provides methods for cache invalidation, warming, and monitoring
 */
public interface CacheService {

    // Cache Invalidation

    /**
     * Invalidate all caches
     */
    void invalidateAllCaches();

    /**
     * Invalidate specific cache by name
     */
    void invalidateCache(String cacheName);

    /**
     * Invalidate cache entry by key
     */
    void invalidateCacheEntry(String cacheName, String key);

    /**
     * Invalidate multiple cache entries
     */
    void invalidateCacheEntries(String cacheName, List<String> keys);

    /**
     * Invalidate cache entries by pattern
     */
    void invalidateCacheEntriesByPattern(String cacheName, String pattern);

    // User-related cache invalidation

    /**
     * Invalidate user-related caches when user data changes
     */
    void invalidateUserCaches(Long userId);

    /**
     * Invalidate all user caches
     */
    void invalidateAllUserCaches();

    // Student-related cache invalidation

    /**
     * Invalidate student-related caches when student data changes
     */
    void invalidateStudentCaches(Long studentId);

    /**
     * Invalidate class-related caches when class data changes
     */
    void invalidateClassCaches(Long classRoomId);

    /**
     * Invalidate all student caches
     */
    void invalidateAllStudentCaches();

    // Academic-related cache invalidation

    /**
     * Invalidate assessment-related caches
     */
    void invalidateAssessmentCaches(Long assessmentId);

    /**
     * Invalidate grade-related caches for a student
     */
    void invalidateGradeCaches(Long studentId);

    /**
     * Invalidate transcript caches for a student
     */
    void invalidateTranscriptCaches(Long studentId);

    // Attendance-related cache invalidation

    /**
     * Invalidate attendance caches for a student
     */
    void invalidateAttendanceCaches(Long studentId);

    /**
     * Invalidate daily attendance caches for a date
     */
    void invalidateDailyAttendanceCaches(String date);

    /**
     * Invalidate attendance report caches
     */
    void invalidateAttendanceReportCaches();

    // Report-related cache invalidation

    /**
     * Invalidate academic report caches
     */
    void invalidateAcademicReportCaches();

    /**
     * Invalidate performance report caches
     */
    void invalidatePerformanceReportCaches();

    /**
     * Invalidate statistics report caches
     */
    void invalidateStatisticsReportCaches();

    /**
     * Invalidate dashboard caches
     */
    void invalidateDashboardCaches();

    // Cache Warming

    /**
     * Warm up frequently accessed caches
     */
    void warmUpCaches();

    /**
     * Warm up user caches
     */
    void warmUpUserCaches();

    /**
     * Warm up student caches
     */
    void warmUpStudentCaches();

    /**
     * Warm up reference data caches
     */
    void warmUpReferenceDataCaches();

    /**
     * Warm up dashboard caches
     */
    void warmUpDashboardCaches();

    // Cache Monitoring

    /**
     * Get cache statistics
     */
    Map<String, Object> getCacheStatistics();

    /**
     * Get cache statistics for specific cache
     */
    Map<String, Object> getCacheStatistics(String cacheName);

    /**
     * Get cache hit ratio
     */
    Double getCacheHitRatio(String cacheName);

    /**
     * Get cache size
     */
    Long getCacheSize(String cacheName);

    /**
     * Get all cache names
     */
    Set<String> getAllCacheNames();

    /**
     * Get cache keys for specific cache
     */
    Set<String> getCacheKeys(String cacheName);

    /**
     * Check if cache entry exists
     */
    Boolean isCacheEntryExists(String cacheName, String key);

    /**
     * Get cache entry TTL
     */
    Long getCacheEntryTTL(String cacheName, String key);

    // Cache Health Check

    /**
     * Check cache health
     */
    Map<String, Object> checkCacheHealth();

    /**
     * Test cache connectivity
     */
    Boolean testCacheConnectivity();

    /**
     * Get cache memory usage
     */
    Map<String, Object> getCacheMemoryUsage();

    // Cache Maintenance

    /**
     * Clean up expired cache entries
     */
    void cleanupExpiredEntries();

    /**
     * Optimize cache memory usage
     */
    void optimizeCacheMemory();

    /**
     * Backup cache data
     */
    Map<String, Object> backupCacheData(List<String> cacheNames);

    /**
     * Restore cache data
     */
    void restoreCacheData(Map<String, Object> backupData);

    // Cache Configuration

    /**
     * Update cache TTL
     */
    void updateCacheTTL(String cacheName, Long ttlSeconds);

    /**
     * Get cache configuration
     */
    Map<String, Object> getCacheConfiguration(String cacheName);

    /**
     * Update cache configuration
     */
    void updateCacheConfiguration(String cacheName, Map<String, Object> config);

    // Distributed Cache Operations

    /**
     * Publish cache invalidation event
     */
    void publishCacheInvalidationEvent(String cacheName, String key);

    /**
     * Subscribe to cache invalidation events
     */
    void subscribeToCacheInvalidationEvents();

    /**
     * Synchronize cache across instances
     */
    void synchronizeCache(String cacheName);

    // Cache Analytics

    /**
     * Get cache performance metrics
     */
    Map<String, Object> getCachePerformanceMetrics();

    /**
     * Get most accessed cache entries
     */
    List<Map<String, Object>> getMostAccessedCacheEntries(String cacheName, Integer limit);

    /**
     * Get cache access patterns
     */
    Map<String, Object> getCacheAccessPatterns(String cacheName);

    /**
     * Generate cache usage report
     */
    Map<String, Object> generateCacheUsageReport();

    // Utility Methods

    /**
     * Generate cache key
     */
    String generateCacheKey(String prefix, Object... params);

    /**
     * Validate cache key
     */
    Boolean isValidCacheKey(String key);

    /**
     * Sanitize cache key
     */
    String sanitizeCacheKey(String key);

    /**
     * Get cache key pattern for entity
     */
    String getCacheKeyPattern(String entityType, Long entityId);
}