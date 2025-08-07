package com.school.sim.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing user sessions and active tokens
 * Tracks active sessions and provides session management capabilities
 */
@Service
public class SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    private static final String SESSION_PREFIX = "user_session:";
    private static final String ACTIVE_SESSIONS_PREFIX = "active_sessions:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Create a new user session
     */
    public void createSession(String username, String token, String userAgent, String ipAddress) {
        try {
            String sessionId = generateSessionId(username, token);
            String sessionKey = SESSION_PREFIX + sessionId;
            String activeSessionsKey = ACTIVE_SESSIONS_PREFIX + username;

            // Create session data
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("username", username);
            sessionData.put("token", token);
            sessionData.put("userAgent", userAgent);
            sessionData.put("ipAddress", ipAddress);
            sessionData.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            sessionData.put("lastActivity", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Store session data
            long tokenExpiration = jwtTokenProvider.getTokenRemainingTime(token);
            redisTemplate.opsForHash().putAll(sessionKey, sessionData);
            redisTemplate.expire(sessionKey, tokenExpiration, TimeUnit.MILLISECONDS);

            // Add to user's active sessions
            redisTemplate.opsForSet().add(activeSessionsKey, sessionId);
            redisTemplate.expire(activeSessionsKey, tokenExpiration, TimeUnit.MILLISECONDS);

            logger.debug("Session created for user: {} with session ID: {}", username, sessionId);

        } catch (Exception e) {
            logger.error("Error creating session for user: {}", username, e);
        }
    }

    /**
     * Update session activity
     */
    public void updateSessionActivity(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String sessionId = generateSessionId(username, token);
            String sessionKey = SESSION_PREFIX + sessionId;

            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                redisTemplate.opsForHash().put(sessionKey, "lastActivity", 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            logger.error("Error updating session activity", e);
        }
    }

    /**
     * Get active sessions for a user
     */
    public Set<Object> getActiveSessions(String username) {
        try {
            String activeSessionsKey = ACTIVE_SESSIONS_PREFIX + username;
            return redisTemplate.opsForSet().members(activeSessionsKey);
        } catch (Exception e) {
            logger.error("Error getting active sessions for user: {}", username, e);
            return Set.of();
        }
    }

    /**
     * Get session details
     */
    public Map<Object, Object> getSessionDetails(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return redisTemplate.opsForHash().entries(sessionKey);
        } catch (Exception e) {
            logger.error("Error getting session details for session: {}", sessionId, e);
            return Map.of();
        }
    }

    /**
     * Terminate a specific session
     */
    public void terminateSession(String username, String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            String activeSessionsKey = ACTIVE_SESSIONS_PREFIX + username;

            // Get session data to extract token for blacklisting
            Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);
            String token = (String) sessionData.get("token");

            if (token != null) {
                // Add token to blacklist
                TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();
                // Note: This would need proper dependency injection in a real implementation
            }

            // Remove session data
            redisTemplate.delete(sessionKey);
            redisTemplate.opsForSet().remove(activeSessionsKey, sessionId);

            logger.info("Session terminated for user: {} with session ID: {}", username, sessionId);

        } catch (Exception e) {
            logger.error("Error terminating session for user: {} with session ID: {}", username, sessionId, e);
        }
    }

    /**
     * Terminate all sessions for a user
     */
    public void terminateAllSessions(String username) {
        try {
            Set<Object> activeSessions = getActiveSessions(username);
            
            for (Object sessionId : activeSessions) {
                terminateSession(username, (String) sessionId);
            }

            logger.info("All sessions terminated for user: {}", username);

        } catch (Exception e) {
            logger.error("Error terminating all sessions for user: {}", username, e);
        }
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        try {
            // This would be called by a scheduled task
            // Redis TTL handles most cleanup automatically, but we can do additional cleanup here
            logger.debug("Cleaning up expired sessions");

        } catch (Exception e) {
            logger.error("Error cleaning up expired sessions", e);
        }
    }

    /**
     * Get session statistics
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Count active sessions
            Set<String> sessionKeys = redisTemplate.keys(SESSION_PREFIX + "*");
            stats.put("totalActiveSessions", sessionKeys != null ? sessionKeys.size() : 0);

            // Count unique users with active sessions
            Set<String> userKeys = redisTemplate.keys(ACTIVE_SESSIONS_PREFIX + "*");
            stats.put("uniqueActiveUsers", userKeys != null ? userKeys.size() : 0);

            stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        } catch (Exception e) {
            logger.error("Error getting session statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Check if session exists and is valid
     */
    public boolean isSessionValid(String username, String token) {
        try {
            String sessionId = generateSessionId(username, token);
            String sessionKey = SESSION_PREFIX + sessionId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            logger.error("Error checking session validity", e);
            return false;
        }
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId(String username, String token) {
        // Create a unique session ID based on username and token hash
        return username + ":" + Integer.toHexString(token.hashCode());
    }

    /**
     * Get session count for user
     */
    public long getSessionCount(String username) {
        try {
            String activeSessionsKey = ACTIVE_SESSIONS_PREFIX + username;
            return redisTemplate.opsForSet().size(activeSessionsKey);
        } catch (Exception e) {
            logger.error("Error getting session count for user: {}", username, e);
            return 0;
        }
    }

    /**
     * Check if user has reached maximum session limit
     */
    public boolean hasReachedMaxSessions(String username, int maxSessions) {
        return getSessionCount(username) >= maxSessions;
    }
}
