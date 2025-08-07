package com.school.sim.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom security annotations for common authorization patterns
 * These annotations provide reusable security expressions for method-level security
 */
public class SecurityAnnotations {

    /**
     * Allows access only to administrators
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {
    }

    /**
     * Allows access to administrators and teachers
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public @interface AdminOrTeacher {
    }

    /**
     * Allows access to administrators, teachers, and students
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public @interface AllRoles {
    }

    /**
     * Allows access to any authenticated user
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("isAuthenticated()")
    public @interface AuthenticatedUser {
    }

    /**
     * Allows access to administrators or the owner of the resource
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    public @interface AdminOrOwner {
    }

    /**
     * Allows access to users who can access a specific student
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.canAccessStudent(#studentId))")
    public @interface CanAccessStudent {
    }

    /**
     * Allows access to users who can access a specific class
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.canAccessClass(#classId))")
    public @interface CanAccessClass {
    }

    /**
     * Allows access to users who can modify attendance
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canModifyAttendance()")
    public @interface CanModifyAttendance {
    }

    /**
     * Allows access to users who can view attendance reports
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canViewAttendanceReports()")
    public @interface CanViewAttendanceReports {
    }

    /**
     * Allows access to users who can manage assessments
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canManageAssessments()")
    public @interface CanManageAssessments {
    }

    /**
     * Allows access to users who can manage schedules
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canManageSchedules()")
    public @interface CanManageSchedules {
    }

    /**
     * Allows access to users who can manage extracurricular activities
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canManageExtracurricular()")
    public @interface CanManageExtracurricular {
    }
}
