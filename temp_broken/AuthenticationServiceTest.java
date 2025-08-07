package com.school.sim.service;

import com.school.sim.dto.request.LoginRequest;
import com.school.sim.dto.request.RefreshTokenRequest;
import com.school.sim.dto.request.PasswordResetRequest;
import com.school.sim.dto.request.PasswordResetConfirmRequest;
import com.school.sim.dto.response.AuthenticationResponse;
import com.school.sim.entity.User;
import com.school.sim.entity.UserType;
import com.school.sim.exception.AuthenticationException;
import com.school.sim.exception.InvalidTokenException;
import com.school.sim.exception.UserNotFoundException;
import com.school.sim.repository.UserRepository;
import com.school.sim.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private UserDetails testUserDetails;
    private Authentication testAuthentication;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setUserType(UserType.TEACHER);
        testUser.setIsActive(true);
        testUser.setRoles(new HashSet<>());

        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .build();

        testAuthentication = mock(Authentication.class);
        when(testAuthentication.getPrincipal()).thenReturn(testUserDetails);
    }

    @Test
    void authenticate_ValidCredentials_ShouldReturnAuthenticationResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(testAuthentication);
        when(userRepository.findByEmailOrNipForAuthentication("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(testAuthentication))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(testUserDetails))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getTokenRemainingTime("access-token"))
                .thenReturn(86400000L);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).save(testUser);
        assertNotNull(testUser.getLastLoginAt());
    }

    @Test
    void authenticate_InvalidCredentials_ShouldThrowAuthenticationException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrong-password");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
    }

    @Test
    void authenticate_InactiveUser_ShouldThrowAuthenticationException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        testUser.setIsActive(false);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(testAuthentication);
        when(userRepository.findByEmailOrNipForAuthentication("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
    }

    @Test
    void refreshToken_ValidRefreshToken_ShouldReturnNewTokens() {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("valid-refresh-token");
        
        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(userRepository.findByEmailOrNipForAuthentication("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateTokenFromUserDetails(testUserDetails))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(testUserDetails))
                .thenReturn("new-refresh-token");
        when(jwtTokenProvider.getTokenRemainingTime("new-access-token"))
                .thenReturn(86400000L);

        // Act
        AuthenticationResponse response = authenticationService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshToken_InvalidToken_ShouldThrowInvalidTokenException() {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("invalid-token");
        
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authenticationService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void refreshToken_NotRefreshToken_ShouldThrowInvalidTokenException() {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("access-token");
        
        when(jwtTokenProvider.validateToken("access-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("access-token")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> {
            authenticationService.refreshToken(refreshTokenRequest);
        });
    }

    @Test
    void initiatePasswordReset_ValidEmail_ShouldSendResetEmail() {
        // Arrange
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest("test@example.com");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generatePasswordResetToken("test@example.com"))
                .thenReturn("reset-token");

        // Act
        authenticationService.initiatePasswordReset(passwordResetRequest);

        // Assert
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordResetEmail(testUser, "reset-token");
        assertEquals("reset-token", testUser.getPasswordResetToken());
        assertNotNull(testUser.getPasswordResetExpires());
    }

    @Test
    void initiatePasswordReset_NonExistentEmail_ShouldNotThrowException() {
        // Arrange
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest("nonexistent@example.com");
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> {
            authenticationService.initiatePasswordReset(passwordResetRequest);
        });

        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void confirmPasswordReset_ValidToken_ShouldUpdatePassword() {
        // Arrange
        String resetToken = "valid-reset-token";
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest(
                resetToken, "newPassword", "newPassword");
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        when(jwtTokenProvider.validateToken(resetToken)).thenReturn(true);
        when(jwtTokenProvider.isPasswordResetToken(resetToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromPasswordResetToken(resetToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-password");

        // Act
        authenticationService.confirmPasswordReset(confirmRequest);

        // Assert
        verify(userRepository).save(testUser);
        assertEquals("encoded-password", testUser.getPassword());
        assertNull(testUser.getPasswordResetToken());
        assertNull(testUser.getPasswordResetExpires());
    }

    @Test
    void confirmPasswordReset_InvalidToken_ShouldThrowInvalidTokenException() {
        // Arrange
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest(
                "invalid-token", "newPassword", "newPassword");
        
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.confirmPasswordReset(confirmRequest);
        });
    }

    @Test
    void confirmPasswordReset_ExpiredToken_ShouldThrowInvalidTokenException() {
        // Arrange
        String resetToken = "expired-token";
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest(
                resetToken, "newPassword", "newPassword");
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setPasswordResetExpires(LocalDateTime.now().minusHours(1)); // Expired
        
        when(jwtTokenProvider.validateToken(resetToken)).thenReturn(true);
        when(jwtTokenProvider.isPasswordResetToken(resetToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromPasswordResetToken(resetToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.confirmPasswordReset(confirmRequest);
        });
    }
}
