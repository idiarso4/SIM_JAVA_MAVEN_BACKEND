package com.school.sim.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for managing JWT token blacklist
 * Provides functionality to blacklist tokens on logout and validate token status
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "blacklisted_token:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            long remainingTime = jwtTokenProvider.getTokenRemainingTime(token);
            
            if (remainingTime > 0) {
                // Store token in Redis with expiration time matching token expiration
                redisTemplate.opsForValue().set(key, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
                logger.debug("Token blacklisted successfully");
            }
        } catch (Exception e) {
            logger.error("Error blacklisting token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Error checking token blacklist status", e);
            return false; // Fail open - don't block valid tokens due to Redis issues
        }
    }

    /**
     * Remove token from blacklist (rarely used, mainly for testing)
     */
    public void removeTokenFromBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            logger.debug("Token removed from blacklist");
        } catch (Exception e) {
            logger.error("Error removing token from blacklist", e);
        }
    }

    /**
     * Clear all blacklisted tokens (admin function)
     */
    public void clearAllBlacklistedTokens() {
        try {
            String pattern = BLACKLIST_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            logger.info("All blacklisted tokens cleared");
        } catch (Exception e) {
            logger.error("Error clearing blacklisted tokens", e);
        }
    }

    /**
     * Get count of blacklisted tokens
     */
    public long getBlacklistedTokenCount() {
        try {
            String pattern = BLACKLIST_PREFIX + "*";
            return redisTemplate.keys(pattern).size();
        } catch (Exception e) {
            logger.error("Error getting blacklisted token count", e);
            return 0;
        }
    }
}
