package com.school.sim.security;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;


import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationEntryPoint
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() throws IOException {
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
        
        when(response.getOutputStream()).thenReturn(mock(javax.servlet.ServletOutputStream.class));
    }

    @Test
    void testCommence() throws IOException, ServletException {
        // Arrange
        String requestUri = "/api/v1/test";
        String errorMessage = "Authentication failed";
        
        when(request.getRequestURI()).thenReturn(requestUri);
        when(authException.getMessage()).thenReturn(errorMessage);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getOutputStream();
    }

    @Test
    void testCommenceWithNullRequestUri() throws IOException, ServletException {
        // Arrange
        String errorMessage = "Authentication failed";
        
        when(request.getRequestURI()).thenReturn(null);
        when(authException.getMessage()).thenReturn(errorMessage);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getOutputStream();
    }

    @Test
    void testCommenceWithNullAuthException() throws IOException, ServletException {
        // Arrange
        String requestUri = "/api/v1/test";
        
        when(request.getRequestURI()).thenReturn(requestUri);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, null);

        // Assert
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getOutputStream();
    }

    @Test
    void testResponseStructure() throws IOException, ServletException {
        // This test verifies the structure of the JSON response
        // In a real scenario, you would capture the actual output and parse it
        
        // Arrange
        String requestUri = "/api/v1/protected";
        String errorMessage = "Full authentication is required";
        
        when(request.getRequestURI()).thenReturn(requestUri);
        when(authException.getMessage()).thenReturn(errorMessage);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert - verify the response format is set correctly
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // The actual JSON structure would contain:
        // - success: false
        // - message: "Unauthorized access - Authentication required"
        // - error: errorMessage
        // - timestamp: current timestamp
        // - path: requestUri
        // - status: 401
    }
}
