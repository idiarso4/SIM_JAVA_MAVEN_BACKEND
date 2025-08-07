package com.school.sim.entity;

/**
 * Enumeration for different types of users in the School Information Management System
 */
public enum UserType {
    /**
     * System administrator with full access
     */
    ADMIN("Administrator"),
    
    /**
     * Teacher with access to student management, attendance, and assessments
     */
    TEACHER("Teacher"),
    
    /**
     * Student with limited access to their own data
     */
    STUDENT("Student");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
