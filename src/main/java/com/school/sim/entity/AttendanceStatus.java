package com.school.sim.entity;

/**
 * Enum representing different attendance status values
 * Based on Indonesian school system attendance tracking
 */
public enum AttendanceStatus {
    PRESENT("Hadir"),
    LATE("Terlambat"), 
    ABSENT("Tidak Hadir"),
    SICK("Sakit"),
    PERMIT("Izin");
    
    private final String description;
    
    AttendanceStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
