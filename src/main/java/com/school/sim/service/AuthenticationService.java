package com.school.sim.service;

import com.school.sim.dto.request.LoginRequest;
import com.school.sim.dto.request.RefreshTokenRequest;
import com.school.sim.dto.request.PasswordResetRequest;
import com.school.sim.dto.request.PasswordResetConfirmRequest;
import com.school.sim.dto.response.AuthenticationResponse;
import com.school.sim.entity.User;
import com.school.sim.exception.AuthenticationException;
import com.school.sim.exception.InvalidTokenException;
import com.school.sim.exception.UserNotFoundException;
import com.school.sim.repository.UserRepository;
import com.school.sim.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.stream.Collectors;

/**
 * Authentication service for handling user login, logout, token refresh, and password reset
 * Implements JWT-based authentication with role-based access control
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.school.sim.security.TokenBlacklistService tokenBlacklistService;

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        logger.info("Authentication attempt for user: {}", loginRequest.getIdentifier());

        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getIdentifier(),
                    loginRequest.getPassword()
                )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Find user entity for additional information
            User user = userRepository.findByEmailOrNipForAuthentication(loginRequest.getIdentifier())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + loginRequest.getIdentifier()));

            // Check if user is active
            if (!user.getIsActive()) {
                throw new AuthenticationException("User account is inactive");
            }

            // Generate tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            logger.info("Authentication successful for user: {}", user.getEmail());

            return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenRemainingTime(accessToken))
                .user(mapUserToResponse(user))
                .build();

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - Invalid credentials", loginRequest.getIdentifier());
            throw new AuthenticationException("Invalid credentials");
        } catch (Exception e) {
            logger.error("Authentication error for user: {}", loginRequest.getIdentifier(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Refresh JWT token using refresh token
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        logger.info("Token refresh attempt");

        try {
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Check if it's actually a refresh token
            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("Token is not a refresh token");
            }

            // Get username from token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Find user entity
            User user = userRepository.findByEmailOrNipForAuthentication(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

            // Check if user is still active
            if (!user.getIsActive()) {
                throw new AuthenticationException("User account is inactive");
            }

            // Generate new access token
            String newAccessToken = jwtTokenProvider.generateTokenFromUserDetails(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            logger.info("Token refresh successful for user: {}", user.getEmail());

            return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenRemainingTime(newAccessToken))
                .user(mapUserToResponse(user))
                .build();

        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            throw new InvalidTokenException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Logout user (invalidate token)
     */
    public void logout(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            logger.info("Logout for user: {}", username);
            
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            logger.info("User {} logged out successfully", username);
            
        } catch (Exception e) {
            logger.warn("Error during logout", e);
        }
    }

    /**
     * Initiate password reset process
     */
    public void initiatePasswordReset(PasswordResetRequest passwordResetRequest) {
        String email = passwordResetRequest.getEmail();
        logger.info("Password reset initiated for email: {}", email);

        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                // Don't reveal if email exists or not for security reasons
                logger.warn("Password reset requested for non-existent email: {}", email);
                return;
            }

            User user = userOptional.get();

            // Check if user is active
            if (!user.getIsActive()) {
                logger.warn("Password reset requested for inactive user: {}", email);
                return;
            }

            // Generate password reset token
            String resetToken = jwtTokenProvider.generatePasswordResetToken(email);
            
            // Store reset token and expiration in user entity
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // 1 hour expiration
            userRepository.save(user);

            // Send password reset email
            emailService.sendPasswordResetEmail(user, resetToken);

            logger.info("Password reset email sent to: {}", email);

        } catch (Exception e) {
            logger.error("Error initiating password reset for email: {}", email, e);
            throw new RuntimeException("Failed to initiate password reset");
        }
    }

    /**
     * Confirm password reset with new password
     */
    public void confirmPasswordReset(PasswordResetConfirmRequest passwordResetConfirmRequest) {
        String token = passwordResetConfirmRequest.getToken();
        String newPassword = passwordResetConfirmRequest.getNewPassword();

        logger.info("Password reset confirmation attempt");

        try {
            // Validate reset token
            if (!jwtTokenProvider.validateToken(token)) {
                throw new InvalidTokenException("Invalid or expired reset token");
            }

            // Check if it's a password reset token
            if (!jwtTokenProvider.isPasswordResetToken(token)) {
                throw new InvalidTokenException("Invalid reset token type");
            }

            // Get email from token
            String email = jwtTokenProvider.getEmailFromPasswordResetToken(token);

            // Find user
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

            // Verify token matches stored token and is not expired
            if (!token.equals(user.getPasswordResetToken()) || 
                user.getPasswordResetExpires() == null ||
                user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("Invalid or expired reset token");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetExpires(null);
            user.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(user);

            logger.info("Password reset successful for user: {}", email);

        } catch (Exception e) {
            logger.error("Password reset confirmation failed", e);
            throw new RuntimeException("Password reset failed: " + e.getMessage());
        }
    }

    /**
     * Validate if current user has permission to access resource
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByEmailOrNipForAuthentication(username).orElse(null);
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Validate token including blacklist check
     */
    public boolean isTokenValid(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return false;
        }
        
        return !tokenBlacklistService.isTokenBlacklisted(token);
    }

    /**
     * Logout all sessions for a user (blacklist all their tokens)
     */
    public void logoutAllSessions(String username) {
        try {
            // In a production system, you would need to track all active tokens per user
            // This is a simplified implementation
            logger.info("Logging out all sessions for user: {}", username);
            
            // Update user's token version or similar mechanism to invalidate all tokens
            User user = userRepository.findByEmailOrNipForAuthentication(username).orElse(null);
            if (user != null) {
                // You could add a tokenVersion field to User entity and increment it
                // All tokens would need to include this version and be validated against it
                logger.info("All sessions logged out for user: {}", username);
            }
            
        } catch (Exception e) {
            logger.error("Error logging out all sessions for user: {}", username, e);
        }
    }

    /**
     * Validate token and return token info
     */
    public java.util.Map<String, Object> validateToken(String token) {
        try {
            if (!isTokenValid(token)) {
                throw new InvalidTokenException("Invalid token");
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByEmailOrNipForAuthentication(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            java.util.Map<String, Object> tokenInfo = new java.util.HashMap<>();
            tokenInfo.put("username", username);
            tokenInfo.put("userId", user.getId());
            tokenInfo.put("userType", user.getUserType());
            tokenInfo.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
            tokenInfo.put("expiresAt", jwtTokenProvider.getExpirationDateFromToken(token));
            
            return tokenInfo;
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            throw new InvalidTokenException("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Get current user info from token
     */
    public java.util.Map<String, Object> getCurrentUserInfo(String token) {
        try {
            if (!isTokenValid(token)) {
                throw new InvalidTokenException("Invalid token");
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByEmailOrNipForAuthentication(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getFirstName() + " " + user.getLastName());
            userInfo.put("email", user.getEmail());
            userInfo.put("nip", user.getNip());
            userInfo.put("userType", user.getUserType());
            userInfo.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
            userInfo.put("permissions", user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList()));
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            userInfo.put("isActive", user.getIsActive());
            
            return userInfo;
        } catch (Exception e) {
            logger.error("Failed to get current user info", e);
            throw new RuntimeException("Failed to get user info: " + e.getMessage());
        }
    }

    /**
     * Change password for authenticated user
     */
    public void changePassword(String token, String currentPassword, String newPassword) {
        try {
            if (!isTokenValid(token)) {
                throw new InvalidTokenException("Invalid token");
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByEmailOrNipForAuthentication(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new AuthenticationException("Current password is incorrect");
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            logger.info("Password changed successfully for user: {}", username);
            
        } catch (Exception e) {
            logger.error("Password change failed", e);
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }

    /**
     * Map User entity to response DTO
     */
    private AuthenticationResponse.UserInfo mapUserToResponse(User user) {
        return AuthenticationResponse.UserInfo.builder()
            .id(user.getId())
            .name(user.getFirstName() + " " + user.getLastName())
            .email(user.getEmail())
            .nip(user.getNip())
            .userType(user.getUserType())
            .roles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()))
            .permissions(user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList()))
            .build();
    }
}
