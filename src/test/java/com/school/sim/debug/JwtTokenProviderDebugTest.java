package com.school.sim.debug;

import com.school.sim.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test for JwtTokenProvider to verify JWT functionality works
 */
public class JwtTokenProviderDebugTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeyThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 604800000L);
        
        testUser = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .build();
    }

    @Test
    void testGenerateToken() {
        try {
            String token = jwtTokenProvider.generateTokenFromUserDetails(testUser);
            assertNotNull(token);
            assertFalse(token.isEmpty());
            System.out.println("Generated token: " + token.substring(0, 20) + "...");
        } catch (Exception e) {
            System.err.println("Error generating token: " + e.getMessage());
            e.printStackTrace();
            fail("Token generation failed: " + e.getMessage());
        }
    }

    @Test
    void testValidateToken() {
        try {
            String token = jwtTokenProvider.generateTokenFromUserDetails(testUser);
            boolean isValid = jwtTokenProvider.validateToken(token);
            assertTrue(isValid);
            System.out.println("Token validation successful");
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            e.printStackTrace();
            fail("Token validation failed: " + e.getMessage());
        }
    }

    @Test
    void testGetUsernameFromToken() {
        try {
            String token = jwtTokenProvider.generateTokenFromUserDetails(testUser);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            assertEquals("test@example.com", username);
            System.out.println("Username extraction successful: " + username);
        } catch (Exception e) {
            System.err.println("Error extracting username: " + e.getMessage());
            e.printStackTrace();
            fail("Username extraction failed: " + e.getMessage());
        }
    }

    @Test
    void testGenerateRefreshToken() {
        try {
            String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);
            assertNotNull(refreshToken);
            assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
            System.out.println("Refresh token generation successful");
        } catch (Exception e) {
            System.err.println("Error generating refresh token: " + e.getMessage());
            e.printStackTrace();
            fail("Refresh token generation failed: " + e.getMessage());
        }
    }

    @Test
    void testGeneratePasswordResetToken() {
        try {
            String resetToken = jwtTokenProvider.generatePasswordResetToken("test@example.com");
            assertNotNull(resetToken);
            assertTrue(jwtTokenProvider.isPasswordResetToken(resetToken));
            assertEquals("test@example.com", jwtTokenProvider.getEmailFromPasswordResetToken(resetToken));
            System.out.println("Password reset token generation successful");
        } catch (Exception e) {
            System.err.println("Error generating password reset token: " + e.getMessage());
            e.printStackTrace();
            fail("Password reset token generation failed: " + e.getMessage());
        }
    }
}
