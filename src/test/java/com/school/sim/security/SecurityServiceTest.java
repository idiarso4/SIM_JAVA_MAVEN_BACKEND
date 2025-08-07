package com.school.sim.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;



import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityService
 */
class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUsername_WithAuthenticatedUser() {
        // Setup authenticated user
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        String username = securityService.getCurrentUsername();
        assertEquals("test@example.com", username);
    }

    @Test
    void testGetCurrentUsername_WithoutAuthentication() {
        // Test without authentication
        String username = securityService.getCurrentUsername();
        assertNull(username);
    }

    @Test
    void testIsAuthenticated_WithAuthenticatedUser() {
        // Setup authenticated user
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertTrue(securityService.isAuthenticated());
    }

    @Test
    void testIsAuthenticated_WithoutAuthentication() {
        // Test without authentication
        assertFalse(securityService.isAuthenticated());
    }

    @Test
    void testHasRole_WithMatchingRole() {
        // Setup user with ADMIN role
        UserDetails userDetails = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertTrue(securityService.hasRole("ADMIN"));
        assertTrue(securityService.hasRole("ROLE_ADMIN"));
        assertTrue(securityService.hasRole("USER"));
        assertFalse(securityService.hasRole("TEACHER"));
    }

    @Test
    void testHasAnyRole_WithMatchingRoles() {
        // Setup user with TEACHER role
        UserDetails userDetails = User.builder()
                .username("teacher@example.com")
                .password("password")
                .authorities("ROLE_TEACHER", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertTrue(securityService.hasAnyRole("ADMIN", "TEACHER"));
        assertTrue(securityService.hasAnyRole("TEACHER", "STUDENT"));
        assertFalse(securityService.hasAnyRole("ADMIN", "STUDENT"));
    }

    @Test
    void testIsAdmin_WithAdminUser() {
        // Setup admin user
        UserDetails userDetails = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertTrue(securityService.isAdmin());
        assertFalse(securityService.isTeacher());
        assertFalse(securityService.isStudent());
    }

    @Test
    void testIsTeacher_WithTeacherUser() {
        // Setup teacher user
        UserDetails userDetails = User.builder()
                .username("teacher@example.com")
                .password("password")
                .authorities("ROLE_TEACHER", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertFalse(securityService.isAdmin());
        assertTrue(securityService.isTeacher());
        assertFalse(securityService.isStudent());
    }

    @Test
    void testIsStudent_WithStudentUser() {
        // Setup student user
        UserDetails userDetails = User.builder()
                .username("student@example.com")
                .password("password")
                .authorities("ROLE_STUDENT", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test
        assertFalse(securityService.isAdmin());
        assertFalse(securityService.isTeacher());
        assertTrue(securityService.isStudent());
    }

    @Test
    void testCanAccessStudent_WithAdminUser() {
        // Setup admin user
        UserDetails userDetails = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test - admin can access any student
        assertTrue(securityService.canAccessStudent(1L));
        assertTrue(securityService.canAccessStudent(999L));
    }

    @Test
    void testCanAccessStudent_WithTeacherUser() {
        // Setup teacher user
        UserDetails userDetails = User.builder()
                .username("teacher@example.com")
                .password("password")
                .authorities("ROLE_TEACHER", "ROLE_USER")
                .build();
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test - teacher can access students (placeholder implementation)
        assertTrue(securityService.canAccessStudent(1L));
    }

    @Test
    void testCanModifyAttendance_WithAuthorizedRoles() {
        // Test with admin
        UserDetails adminUser = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();
        
        UsernamePasswordAuthenticationToken adminAuth = 
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
        
        assertTrue(securityService.canModifyAttendance());

        // Test with teacher
        UserDetails teacherUser = User.builder()
                .username("teacher@example.com")
                .password("password")
                .authorities("ROLE_TEACHER")
                .build();
        
        UsernamePasswordAuthenticationToken teacherAuth = 
                new UsernamePasswordAuthenticationToken(teacherUser, null, teacherUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(teacherAuth);
        
        assertTrue(securityService.canModifyAttendance());

        // Test with student
        UserDetails studentUser = User.builder()
                .username("student@example.com")
                .password("password")
                .authorities("ROLE_STUDENT")
                .build();
        
        UsernamePasswordAuthenticationToken studentAuth = 
                new UsernamePasswordAuthenticationToken(studentUser, null, studentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(studentAuth);
        
        assertFalse(securityService.canModifyAttendance());
    }

    @Test
    void testCanAccessSystemAdmin_OnlyAdmin() {
        // Test with admin
        UserDetails adminUser = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();
        
        UsernamePasswordAuthenticationToken adminAuth = 
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
        
        assertTrue(securityService.canAccessSystemAdmin());

        // Test with teacher
        UserDetails teacherUser = User.builder()
                .username("teacher@example.com")
                .password("password")
                .authorities("ROLE_TEACHER")
                .build();
        
        UsernamePasswordAuthenticationToken teacherAuth = 
                new UsernamePasswordAuthenticationToken(teacherUser, null, teacherUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(teacherAuth);
        
        assertFalse(securityService.canAccessSystemAdmin());
    }
}
