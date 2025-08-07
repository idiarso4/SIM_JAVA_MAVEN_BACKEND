package com.school.sim.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing academic subjects taught in the school
 */
@Entity
@Table(name = "subjects")
public class Subject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "kode_mapel", unique = true, nullable = false, length = 10)
    private String kodeMapel;
    
    @Column(name = "nama_mapel", nullable = false, length = 100)
    private String namaMapel;
    
    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
    
    @Column(name = "sks")
    private Integer sks; // Credit hours
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeachingActivity> teachingActivities;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assessment> assessments;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Subject() {}
    
    public Subject(String kodeMapel, String namaMapel, String deskripsi, Integer sks) {
        this.kodeMapel = kodeMapel;
        this.namaMapel = namaMapel;
        this.deskripsi = deskripsi;
        this.sks = sks;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getKodeMapel() {
        return kodeMapel;
    }
    
    public void setKodeMapel(String kodeMapel) {
        this.kodeMapel = kodeMapel;
    }
    
    public String getNamaMapel() {
        return namaMapel;
    }
    
    public void setNamaMapel(String namaMapel) {
        this.namaMapel = namaMapel;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public Integer getSks() {
        return sks;
    }
    
    public void setSks(Integer sks) {
        this.sks = sks;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<TeachingActivity> getTeachingActivities() {
        return teachingActivities;
    }
    
    public void setTeachingActivities(List<TeachingActivity> teachingActivities) {
        this.teachingActivities = teachingActivities;
    }
    
    public List<Assessment> getAssessments() {
        return assessments;
    }
    
    public void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Alias methods for compatibility
    public String getName() {
        return namaMapel;
    }
    
    public void setName(String name) {
        this.namaMapel = name;
    }
    
    public String getCode() {
        return kodeMapel;
    }
    
    public void setCode(String code) {
        this.kodeMapel = code;
    }
    
    public Integer getCredits() {
        return sks;
    }
    
    public void setCredits(Integer credits) {
        this.sks = credits;
    }
}
