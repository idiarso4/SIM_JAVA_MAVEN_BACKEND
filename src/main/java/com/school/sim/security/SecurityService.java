package com.school.sim.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Security service for method-level security checks and user context operations
 */
@Service("securityService")
public class SecurityService {

    /**
     * Get the currently authenticated user's username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * Check if the current user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Check if the current user has a specific role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is a teacher
     */
    public boolean isTeacher() {
        return hasRole("TEACHER");
    }

    /**
     * Check if the current user is a student
     */
    public boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * Check if the current user can access a specific student's data
     * This method will be enhanced when User and Student entities are implemented
     */
    public boolean canAccessStudent(Long studentId) {
        // Admin can access all students
        if (isAdmin()) {
            return true;
        }
        
        // Teachers can access students in their classes (simplified access control)
        if (isTeacher()) {
            // Allow all teachers to access all students for now
            return true; // Teacher-student relationship check not implemented yet
        }

        // Students can only access their own data
        if (isStudent()) {
            // Student self-access check not implemented yet
            return false; // Restricted access until proper implementation
        }
        
        return false;
    }

    /**
     * Check if the current user can access a specific class
     */
    public boolean canAccessClass(Long classId) {
        // Admin can access all classes
        if (isAdmin()) {
            return true;
        }
        
        // Teachers can access their assigned classes (simplified access control)
        if (isTeacher()) {
            // Allow all teachers to access all classes for now
            return true; // Teacher-class relationship check not implemented yet
        }

        // Students can access their own class
        if (isStudent()) {
            // Allow students to access classes for now
            return true; // Student-class relationship check not implemented yet
        }
        
        return false;
    }

    /**
     * Check if the current user can modify attendance records
     */
    public boolean canModifyAttendance() {
        return hasAnyRole("ADMIN", "TEACHER");
    }

    /**
     * Check if the current user can view attendance reports
     */
    public boolean canViewAttendanceReports() {
        return hasAnyRole("ADMIN", "TEACHER");
    }

    /**
     * Check if the current user can manage assessments
     */
    public boolean canManageAssessments() {
        return hasAnyRole("ADMIN", "TEACHER");
    }

    /**
     * Check if the current user can manage schedules
     */
    public boolean canManageSchedules() {
        return hasAnyRole("ADMIN", "TEACHER");
    }

    /**
     * Check if the current user can manage extracurricular activities
     */
    public boolean canManageExtracurricular() {
        return hasAnyRole("ADMIN", "TEACHER");
    }

    /**
     * Check if the current user can access system administration features
     */
    public boolean canAccessSystemAdmin() {
        return hasRole("ADMIN");
    }
}
