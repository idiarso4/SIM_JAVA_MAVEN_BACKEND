package com.school.sim.entity;

/**
 * Enum representing different types of academic assessments
 * Based on Indonesian school assessment system
 */
public enum AssessmentType {
    TUGAS("Tugas"),           // Assignment
    KUIS("Kuis"),             // Quiz
    UTS("Ujian Tengah Semester"),  // Mid-term exam
    UAS("Ujian Akhir Semester"),   // Final exam
    PRAKTIKUM("Praktikum"),   // Practical/Lab work
    PROYEK("Proyek"),         // Project
    PRESENTASI("Presentasi"), // Presentation
    UJIAN_HARIAN("Ujian Harian"); // Daily test
    
    private final String description;
    
    AssessmentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
