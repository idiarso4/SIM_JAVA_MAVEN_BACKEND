package com.school.sim.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configuration for role hierarchy in the School Information Management System
 * Defines the hierarchical relationships between different user roles
 */
@Configuration
public class RoleHierarchyConfig {

    /**
     * Define role hierarchy where higher roles inherit permissions from lower roles
     * 
     * Hierarchy:
     * ADMIN > TEACHER > STUDENT > USER
     * 
     * This means:
     * - ADMIN has all permissions of TEACHER, STUDENT, and USER
     * - TEACHER has all permissions of STUDENT and USER
     * - STUDENT has all permissions of USER
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        
        String hierarchy = "ROLE_ADMIN > ROLE_TEACHER\n" +
                "ROLE_TEACHER > ROLE_STUDENT\n" +
                "ROLE_STUDENT > ROLE_USER";
        
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }
}
