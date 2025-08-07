package com.school.sim.entity;

/**
 * Enumeration for gender in the School Information Management System
 */
public enum Gender {
    /**
     * Male gender
     */
    LAKI_LAKI("Laki-laki"),
    
    /**
     * Female gender
     */
    PEREMPUAN("Perempuan");

    private final String displayName;

    Gender(String displayName) {
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
