package com.school.sim.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis caching configuration for the School Information Management System
 * Provides comprehensive caching strategy with different TTL policies for various data types
 */
@Configuration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    @Value("${spring.cache.redis.time-to-live:3600}")
    private long defaultTtl;

    /**
     * Redis connection factory configuration
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("Configuring Redis connection to {}:{}", redisHost, redisPort);
        
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        factory.setValidateConnection(true);
        
        logger.info("Redis connection factory configured successfully");
        return factory;
    }

    /**
     * Redis template for general operations
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring Redis template");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        
        logger.info("Redis template configured successfully");
        return template;
    }

    /**
     * Cache manager with different TTL configurations for different cache types
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring Redis cache manager");
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(defaultTtl))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer()))
            .disableCachingNullValues();

        // Specific cache configurations with different TTL
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User data - 30 minutes
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userProfiles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userRoles", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        
        // Student data - 1 hour
        cacheConfigurations.put("students", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("studentProfiles", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("studentsByClass", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Academic data - 2 hours
        cacheConfigurations.put("assessments", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("grades", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("transcripts", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Attendance data - 30 minutes (more dynamic)
        cacheConfigurations.put("attendance", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("attendanceReports", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("dailyAttendance", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Reports - 1 hour
        cacheConfigurations.put("academicReports", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("performanceReports", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("statisticsReports", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Reference data - 4 hours (rarely changes)
        cacheConfigurations.put("classRooms", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("subjects", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("majors", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Session data - 8 hours
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofHours(8)));
        cacheConfigurations.put("authTokens", defaultConfig.entryTtl(Duration.ofHours(8)));
        
        // Extracurricular data - 1 hour
        cacheConfigurations.put("extracurricularActivities", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("activityParticipants", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Dashboard data - 15 minutes (frequently updated)
        cacheConfigurations.put("dashboardData", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("kpiData", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();

        logger.info("Redis cache manager configured with {} specific cache configurations", cacheConfigurations.size());
        return cacheManager;
    }

    /**
     * Custom key generator for cache keys
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            
            for (Object param : params) {
                if (param != null) {
                    sb.append(param.toString()).append(":");
                }
            }
            
            // Remove trailing colon
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ':') {
                sb.setLength(sb.length() - 1);
            }
            
            return sb.toString();
        };
    }

    /**
     * Custom cache error handler to prevent cache failures from breaking the application
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache GET error for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                logger.warn("Cache PUT error for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache EVICT error for cache '{}' and key '{}': {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                logger.warn("Cache CLEAR error for cache '{}': {}", cache.getName(), exception.getMessage());
            }
        };
    }

    /**
     * Create JSON serializer with proper configuration
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}