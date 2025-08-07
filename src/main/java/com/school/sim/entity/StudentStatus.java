package com.school.sim.entity;

/**
 * Enumeration for student status in the School Information Management System
 */
public enum StudentStatus {
    /**
     * Student is currently active and enrolled
     */
    ACTIVE("Active"),
    
    /**
     * Student is temporarily inactive
     */
    INACTIVE("Inactive"),
    
    /**
     * Student has graduated
     */
    GRADUATED("Graduated"),
    
    /**
     * Student has been transferred to another school
     */
    TRANSFERRED("Transferred");

    private final String displayName;

    StudentStatus(String displayName) {
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
