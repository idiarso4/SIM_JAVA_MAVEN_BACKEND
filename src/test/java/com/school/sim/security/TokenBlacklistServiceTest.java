package com.school.sim.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenBlacklistService
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = "test.jwt.token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void blacklistToken_ValidToken_ShouldAddToBlacklist() {
        // Arrange
        long remainingTime = 3600000L; // 1 hour
        when(jwtTokenProvider.getTokenRemainingTime(testToken)).thenReturn(remainingTime);

        // Act
        tokenBlacklistService.blacklistToken(testToken);

        // Assert
        verify(valueOperations).set(
            eq("blacklisted_token:" + testToken),
            eq("blacklisted"),
            eq(remainingTime),
            eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void blacklistToken_ExpiredToken_ShouldNotAddToBlacklist() {
        // Arrange
        when(jwtTokenProvider.getTokenRemainingTime(testToken)).thenReturn(0L);

        // Act
        tokenBlacklistService.blacklistToken(testToken);

        // Assert
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void isTokenBlacklisted_BlacklistedToken_ShouldReturnTrue() {
        // Arrange
        when(redisTemplate.hasKey("blacklisted_token:" + testToken)).thenReturn(true);

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(testToken);

        // Assert
        assertTrue(result);
    }

    @Test
    void isTokenBlacklisted_NonBlacklistedToken_ShouldReturnFalse() {
        // Arrange
        when(redisTemplate.hasKey("blacklisted_token:" + testToken)).thenReturn(false);

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(testToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isTokenBlacklisted_RedisException_ShouldReturnFalse() {
        // Arrange
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(testToken);

        // Assert
        assertFalse(result); // Fail open
    }

    @Test
    void removeTokenFromBlacklist_ValidToken_ShouldRemoveFromBlacklist() {
        // Act
        tokenBlacklistService.removeTokenFromBlacklist(testToken);

        // Assert
        verify(redisTemplate).delete("blacklisted_token:" + testToken);
    }

    @Test
    void clearAllBlacklistedTokens_ShouldClearAllTokens() {
        // Arrange
        Set<String> keys = Set.of("key1", "key2");
        when(redisTemplate.keys("blacklisted_token:*")).thenReturn(keys);

        // Act
        tokenBlacklistService.clearAllBlacklistedTokens();

        // Assert
        verify(redisTemplate).delete(keys);
    }
}
