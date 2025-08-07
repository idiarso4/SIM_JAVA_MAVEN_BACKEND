package com.school.sim.controller;

import com.school.sim.dto.request.LoginRequest;
import com.school.sim.dto.request.PasswordResetConfirmRequest;
import com.school.sim.dto.request.PasswordResetRequest;
import com.school.sim.dto.request.RefreshTokenRequest;
import com.school.sim.dto.response.AuthenticationResponse;
import com.school.sim.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations
 * Provides endpoints for login, logout, token refresh, and password reset
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication management endpoints")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getIdentifier());
        
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            logger.info("Login successful for user: {}", request.getIdentifier());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getIdentifier(), e);
            throw e;
        }
    }

    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        logger.info("Logout request received");
        
        try {
            authenticationService.logout(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logout successful");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Logout failed", e);
            throw e;
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        
        try {
            AuthenticationResponse response = authenticationService.refreshToken(request);
            logger.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            throw e;
        }
    }

    /**
     * Password reset request endpoint
     */
    @PostMapping("/password-reset")
    @Operation(summary = "Request password reset", description = "Send password reset email to user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        logger.info("Password reset request for email: {}", request.getEmail());
        
        try {
            authenticationService.initiatePasswordReset(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset email sent successfully");
            response.put("email", request.getEmail());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Password reset email sent for: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Password reset request failed for: {}", request.getEmail(), e);
            throw e;
        }
    }

    /**
     * Password reset confirmation endpoint
     */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Reset password using reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired reset token"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Map<String, Object>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        logger.info("Password reset confirmation request received");
        
        try {
            authenticationService.confirmPasswordReset(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset successful");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Password reset confirmation successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Password reset confirmation failed", e);
            throw e;
        }
    }

    /**
     * Validate token endpoint
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        logger.debug("Token validation request received");
        
        try {
            Map<String, Object> tokenInfo = authenticationService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userInfo", tokenInfo);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Token validation successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get current user info endpoint
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        logger.debug("Current user info request received");
        
        try {
            Map<String, Object> userInfo = authenticationService.getCurrentUserInfo(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", userInfo);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Current user info retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get current user info", e);
            throw e;
        }
    }

    /**
     * Change password endpoint
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid current password or request data"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        logger.info("Password change request received");
        
        try {
            authenticationService.changePassword(token, currentPassword, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Password change successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Password change failed", e);
            throw e;
        }
    }

    /**
     * Check authentication status endpoint
     */
    @GetMapping("/status")
    @Operation(summary = "Check auth status", description = "Check if user is authenticated")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication status retrieved")
    })
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        logger.debug("Authentication status check requested");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (token != null && authenticationService.isTokenValid(token)) {
                Map<String, Object> userInfo = authenticationService.getCurrentUserInfo(token);
                response.put("authenticated", true);
                response.put("user", userInfo);
            } else {
                response.put("authenticated", false);
            }
        } catch (Exception e) {
            logger.debug("Authentication status check failed", e);
            response.put("authenticated", false);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}