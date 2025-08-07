package com.school.sim.service.impl;

import com.school.sim.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of CacheService for comprehensive cache management
 * Provides cache invalidation strategies, monitoring, and maintenance operations
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Cache Invalidation Implementation

    @Override
    public void invalidateAllCaches() {
        logger.info("Invalidating all caches");
        
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    logger.debug("Cleared cache: {}", cacheName);
                }
            }
            logger.info("Successfully invalidated {} caches", cacheNames.size());
        } catch (Exception e) {
            logger.error("Failed to invalidate all caches", e);
            throw new RuntimeException("Cache invalidation failed", e);
        }
    }

    @Override
    public void invalidateCache(String cacheName) {
        logger.info("Invalidating cache: {}", cacheName);
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("Successfully invalidated cache: {}", cacheName);
            } else {
                logger.warn("Cache not found: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate cache: {}", cacheName, e);
            throw new RuntimeException("Cache invalidation failed for: " + cacheName, e);
        }
    }

    @Override
    public void invalidateCacheEntry(String cacheName, String key) {
        logger.debug("Invalidating cache entry - cache: {}, key: {}", cacheName, key);
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                logger.debug("Successfully invalidated cache entry - cache: {}, key: {}", cacheName, key);
            } else {
                logger.warn("Cache not found: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate cache entry - cache: {}, key: {}", cacheName, key, e);
        }
    }

    @Override
    public void invalidateCacheEntries(String cacheName, List<String> keys) {
        logger.info("Invalidating {} cache entries from cache: {}", keys.size(), cacheName);
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                for (String key : keys) {
                    cache.evict(key);
                }
                logger.info("Successfully invalidated {} cache entries from cache: {}", keys.size(), cacheName);
            } else {
                logger.warn("Cache not found: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate cache entries from cache: {}", cacheName, e);
        }
    }

    @Override
    public void invalidateCacheEntriesByPattern(String cacheName, String pattern) {
        logger.info("Invalidating cache entries by pattern - cache: {}, pattern: {}", cacheName, pattern);
        
        try {
            Set<String> keys = redisTemplate.keys(cacheName + "::" + pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Successfully invalidated {} cache entries by pattern", keys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate cache entries by pattern - cache: {}, pattern: {}", cacheName, pattern, e);
        }
    }

    // User-related cache invalidation

    @Override
    public void invalidateUserCaches(Long userId) {
        logger.info("Invalidating user caches for user: {}", userId);
        
        try {
            // Invalidate specific user caches
            invalidateCacheEntry("users", "user:" + userId);
            invalidateCacheEntry("userProfiles", "profile:" + userId);
            invalidateCacheEntry("userRoles", "roles:" + userId);
            
            // Invalidate related caches that might contain user data
            invalidateCache("dashboardData");
            invalidateCache("statisticsReports");
            
            logger.info("Successfully invalidated user caches for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to invalidate user caches for user: {}", userId, e);
        }
    }

    @Override
    public void invalidateAllUserCaches() {
        logger.info("Invalidating all user caches");
        
        try {
            invalidateCache("users");
            invalidateCache("userProfiles");
            invalidateCache("userRoles");
            
            logger.info("Successfully invalidated all user caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate all user caches", e);
        }
    }

    // Student-related cache invalidation

    @Override
    public void invalidateStudentCaches(Long studentId) {
        logger.info("Invalidating student caches for student: {}", studentId);
        
        try {
            // Invalidate specific student caches
            invalidateCacheEntry("students", "student:" + studentId);
            invalidateCacheEntry("studentProfiles", "profile:" + studentId);
            invalidateCacheEntry("transcripts", "transcript:" + studentId);
            invalidateCacheEntry("grades", "grades:" + studentId);
            
            // Invalidate attendance caches
            invalidateAttendanceCaches(studentId);
            
            // Invalidate related report caches
            invalidateAcademicReportCaches();
            invalidatePerformanceReportCaches();
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated student caches for student: {}", studentId);
        } catch (Exception e) {
            logger.error("Failed to invalidate student caches for student: {}", studentId, e);
        }
    }

    @Override
    public void invalidateClassCaches(Long classRoomId) {
        logger.info("Invalidating class caches for class: {}", classRoomId);
        
        try {
            // Invalidate class-specific caches
            invalidateCacheEntry("classRooms", "class:" + classRoomId);
            invalidateCacheEntry("studentsByClass", "class:" + classRoomId);
            
            // Invalidate related report caches
            invalidatePerformanceReportCaches();
            invalidateAttendanceReportCaches();
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated class caches for class: {}", classRoomId);
        } catch (Exception e) {
            logger.error("Failed to invalidate class caches for class: {}", classRoomId, e);
        }
    }

    @Override
    public void invalidateAllStudentCaches() {
        logger.info("Invalidating all student caches");
        
        try {
            invalidateCache("students");
            invalidateCache("studentProfiles");
            invalidateCache("studentsByClass");
            invalidateCache("transcripts");
            invalidateCache("grades");
            
            logger.info("Successfully invalidated all student caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate all student caches", e);
        }
    }

    // Academic-related cache invalidation

    @Override
    public void invalidateAssessmentCaches(Long assessmentId) {
        logger.info("Invalidating assessment caches for assessment: {}", assessmentId);
        
        try {
            invalidateCacheEntry("assessments", "assessment:" + assessmentId);
            invalidateCache("grades");
            invalidateCache("transcripts");
            invalidateAcademicReportCaches();
            
            logger.info("Successfully invalidated assessment caches for assessment: {}", assessmentId);
        } catch (Exception e) {
            logger.error("Failed to invalidate assessment caches for assessment: {}", assessmentId, e);
        }
    }

    @Override
    public void invalidateGradeCaches(Long studentId) {
        logger.info("Invalidating grade caches for student: {}", studentId);
        
        try {
            invalidateCacheEntriesByPattern("grades", "*student:" + studentId + "*");
            invalidateCacheEntry("transcripts", "transcript:" + studentId);
            invalidateAcademicReportCaches();
            
            logger.info("Successfully invalidated grade caches for student: {}", studentId);
        } catch (Exception e) {
            logger.error("Failed to invalidate grade caches for student: {}", studentId, e);
        }
    }

    @Override
    public void invalidateTranscriptCaches(Long studentId) {
        logger.info("Invalidating transcript caches for student: {}", studentId);
        
        try {
            invalidateCacheEntry("transcripts", "transcript:" + studentId);
            invalidateAcademicReportCaches();
            
            logger.info("Successfully invalidated transcript caches for student: {}", studentId);
        } catch (Exception e) {
            logger.error("Failed to invalidate transcript caches for student: {}", studentId, e);
        }
    }

    // Attendance-related cache invalidation

    @Override
    public void invalidateAttendanceCaches(Long studentId) {
        logger.info("Invalidating attendance caches for student: {}", studentId);
        
        try {
            invalidateCacheEntriesByPattern("attendance", "*student:" + studentId + "*");
            invalidateAttendanceReportCaches();
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated attendance caches for student: {}", studentId);
        } catch (Exception e) {
            logger.error("Failed to invalidate attendance caches for student: {}", studentId, e);
        }
    }

    @Override
    public void invalidateDailyAttendanceCaches(String date) {
        logger.info("Invalidating daily attendance caches for date: {}", date);
        
        try {
            invalidateCacheEntry("dailyAttendance", "daily:" + date);
            invalidateAttendanceReportCaches();
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated daily attendance caches for date: {}", date);
        } catch (Exception e) {
            logger.error("Failed to invalidate daily attendance caches for date: {}", date, e);
        }
    }

    @Override
    public void invalidateAttendanceReportCaches() {
        logger.info("Invalidating attendance report caches");
        
        try {
            invalidateCache("attendanceReports");
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated attendance report caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate attendance report caches", e);
        }
    }

    // Report-related cache invalidation

    @Override
    public void invalidateAcademicReportCaches() {
        logger.info("Invalidating academic report caches");
        
        try {
            invalidateCache("academicReports");
            invalidateCache("performanceReports");
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated academic report caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate academic report caches", e);
        }
    }

    @Override
    public void invalidatePerformanceReportCaches() {
        logger.info("Invalidating performance report caches");
        
        try {
            invalidateCache("performanceReports");
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated performance report caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate performance report caches", e);
        }
    }

    @Override
    public void invalidateStatisticsReportCaches() {
        logger.info("Invalidating statistics report caches");
        
        try {
            invalidateCache("statisticsReports");
            invalidateDashboardCaches();
            
            logger.info("Successfully invalidated statistics report caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate statistics report caches", e);
        }
    }

    @Override
    public void invalidateDashboardCaches() {
        logger.info("Invalidating dashboard caches");
        
        try {
            invalidateCache("dashboardData");
            invalidateCache("kpiData");
            
            logger.info("Successfully invalidated dashboard caches");
        } catch (Exception e) {
            logger.error("Failed to invalidate dashboard caches", e);
        }
    }

    // Cache Monitoring Implementation

    @Override
    public Map<String, Object> getCacheStatistics() {
        logger.debug("Getting cache statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            statistics.put("totalCaches", cacheNames.size());
            statistics.put("cacheNames", cacheNames);
            
            Map<String, Object> cacheDetails = new HashMap<>();
            for (String cacheName : cacheNames) {
                Map<String, Object> cacheStats = getCacheStatistics(cacheName);
                cacheDetails.put(cacheName, cacheStats);
            }
            statistics.put("cacheDetails", cacheDetails);
            
            // Redis-specific statistics
            try {
                Map<String, Object> redisInfo = getRedisInfo();
                statistics.put("redisInfo", redisInfo);
            } catch (Exception e) {
                logger.warn("Failed to get Redis info", e);
            }
            
            statistics.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Failed to get cache statistics", e);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getCacheStatistics(String cacheName) {
        logger.debug("Getting cache statistics for cache: {}", cacheName);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                statistics.put("cacheName", cacheName);
                statistics.put("cacheType", cache.getClass().getSimpleName());
                
                // Get cache size (approximate)
                Long size = getCacheSize(cacheName);
                statistics.put("size", size);
                
                // Get cache keys
                Set<String> keys = getCacheKeys(cacheName);
                statistics.put("keyCount", keys.size());
                statistics.put("sampleKeys", keys.stream().limit(10).collect(Collectors.toList()));
                
            } else {
                statistics.put("error", "Cache not found");
            }
            
        } catch (Exception e) {
            logger.error("Failed to get cache statistics for cache: {}", cacheName, e);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }

    @Override
    public Long getCacheSize(String cacheName) {
        try {
            Set<String> keys = redisTemplate.keys(cacheName + "::*");
            return keys != null ? (long) keys.size() : 0L;
        } catch (Exception e) {
            logger.error("Failed to get cache size for cache: {}", cacheName, e);
            return 0L;
        }
    }

    @Override
    public Set<String> getAllCacheNames() {
        return new HashSet<>(cacheManager.getCacheNames());
    }

    @Override
    public Set<String> getCacheKeys(String cacheName) {
        try {
            Set<String> keys = redisTemplate.keys(cacheName + "::*");
            return keys != null ? keys.stream()
                .map(key -> key.substring((cacheName + "::").length()))
                .collect(Collectors.toSet()) : new HashSet<>();
        } catch (Exception e) {
            logger.error("Failed to get cache keys for cache: {}", cacheName, e);
            return new HashSet<>();
        }
    }

    @Override
    public Boolean isCacheEntryExists(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to check cache entry existence - cache: {}, key: {}", cacheName, key, e);
            return false;
        }
    }

    @Override
    public Long getCacheEntryTTL(String cacheName, String key) {
        try {
            String redisKey = cacheName + "::" + key;
            return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to get cache entry TTL - cache: {}, key: {}", cacheName, key, e);
            return -1L;
        }
    }

    // Cache Health Check Implementation

    @Override
    public Map<String, Object> checkCacheHealth() {
        logger.debug("Checking cache health");
        
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test basic connectivity
            Boolean connectivity = testCacheConnectivity();
            health.put("connectivity", connectivity);
            
            // Get memory usage
            Map<String, Object> memoryUsage = getCacheMemoryUsage();
            health.put("memoryUsage", memoryUsage);
            
            // Get cache statistics
            Map<String, Object> statistics = getCacheStatistics();
            health.put("statistics", statistics);
            
            health.put("status", connectivity ? "UP" : "DOWN");
            health.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Failed to check cache health", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    @Override
    public Boolean testCacheConnectivity() {
        try {
            // Test Redis connectivity
            redisTemplate.opsForValue().set("health:test", "test", 10, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get("health:test");
            redisTemplate.delete("health:test");
            
            return "test".equals(result);
        } catch (Exception e) {
            logger.error("Cache connectivity test failed", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getCacheMemoryUsage() {
        Map<String, Object> memoryUsage = new HashMap<>();
        
        try {
            // Get Redis memory info
            Map<String, Object> redisInfo = getRedisInfo();
            memoryUsage.put("redisMemory", redisInfo);
            
        } catch (Exception e) {
            logger.error("Failed to get cache memory usage", e);
            memoryUsage.put("error", e.getMessage());
        }
        
        return memoryUsage;
    }

    // Utility Methods

    @Override
    public String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(":").append(param != null ? param.toString() : "null");
        }
        return sanitizeCacheKey(keyBuilder.toString());
    }

    @Override
    public Boolean isValidCacheKey(String key) {
        return key != null && !key.trim().isEmpty() && key.length() <= 250;
    }

    @Override
    public String sanitizeCacheKey(String key) {
        if (key == null) return "null";
        
        // Replace problematic characters
        return key.replaceAll("[\\s\\r\\n\\t]", "_")
                 .replaceAll("[^a-zA-Z0-9:_-]", "")
                 .substring(0, Math.min(key.length(), 250));
    }

    @Override
    public String getCacheKeyPattern(String entityType, Long entityId) {
        return generateCacheKey(entityType, entityId);
    }

    // Helper Methods

    private Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // This would typically use Redis INFO command
            // For now, return basic information
            info.put("connected", testCacheConnectivity());
            info.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Failed to get Redis info", e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    // Placeholder implementations for remaining interface methods
    @Override public void warmUpCaches() { logger.info("Cache warm-up not implemented yet"); }
    @Override public void warmUpUserCaches() { logger.info("User cache warm-up not implemented yet"); }
    @Override public void warmUpStudentCaches() { logger.info("Student cache warm-up not implemented yet"); }
    @Override public void warmUpReferenceDataCaches() { logger.info("Reference data cache warm-up not implemented yet"); }
    @Override public void warmUpDashboardCaches() { logger.info("Dashboard cache warm-up not implemented yet"); }
    @Override public Double getCacheHitRatio(String cacheName) { return 0.0; }
    @Override public void cleanupExpiredEntries() { logger.info("Cache cleanup not implemented yet"); }
    @Override public void optimizeCacheMemory() { logger.info("Cache memory optimization not implemented yet"); }
    @Override public Map<String, Object> backupCacheData(List<String> cacheNames) { return new HashMap<>(); }
    @Override public void restoreCacheData(Map<String, Object> backupData) { logger.info("Cache restore not implemented yet"); }
    @Override public void updateCacheTTL(String cacheName, Long ttlSeconds) { logger.info("Cache TTL update not implemented yet"); }
    @Override public Map<String, Object> getCacheConfiguration(String cacheName) { return new HashMap<>(); }
    @Override public void updateCacheConfiguration(String cacheName, Map<String, Object> config) { logger.info("Cache config update not implemented yet"); }
    @Override public void publishCacheInvalidationEvent(String cacheName, String key) { logger.info("Cache invalidation event publishing not implemented yet"); }
    @Override public void subscribeToCacheInvalidationEvents() { logger.info("Cache invalidation event subscription not implemented yet"); }
    @Override public void synchronizeCache(String cacheName) { logger.info("Cache synchronization not implemented yet"); }
    @Override public Map<String, Object> getCachePerformanceMetrics() { return new HashMap<>(); }
    @Override public List<Map<String, Object>> getMostAccessedCacheEntries(String cacheName, Integer limit) { return new ArrayList<>(); }
    @Override public Map<String, Object> getCacheAccessPatterns(String cacheName) { return new HashMap<>(); }
    @Override public Map<String, Object> generateCacheUsageReport() { return new HashMap<>(); }
}