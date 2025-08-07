package com.school.sim.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Spring Security configuration
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicEndpointsAccessible() throws Exception {
        // Health endpoint should be accessible without authentication
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 401/403

        // Auth endpoints should be accessible without authentication
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 401/403
    }

    @Test
    void testProtectedEndpointsRequireAuthentication() throws Exception {
        // Student endpoints should require authentication
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());

        // Attendance endpoints should require authentication
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isUnauthorized());

        // Assessment endpoints should require authentication
        mockMvc.perform(get("/api/v1/assessments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanAccessAllEndpoints() throws Exception {
        // Admin should be able to access admin endpoints
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403

        // Admin should be able to access student endpoints
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403

        // Admin should be able to access attendance endpoints
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void testTeacherCanAccessTeacherEndpoints() throws Exception {
        // Teacher should be able to access student endpoints
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403

        // Teacher should be able to access attendance endpoints
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403

        // Teacher should NOT be able to access admin endpoints
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void testStudentCannotAccessRestrictedEndpoints() throws Exception {
        // Student should NOT be able to access admin endpoints
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());

        // Student should NOT be able to access student management endpoints
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isForbidden());

        // Student should NOT be able to access attendance management endpoints
        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCanAccessProfileEndpoints() throws Exception {
        // Authenticated user should be able to access profile endpoints
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 403
    }
}
