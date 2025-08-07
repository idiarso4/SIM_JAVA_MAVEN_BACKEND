package com.school.sim.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing student attendance records
 * Tracks attendance for teaching activities with status and notes
 */
@Entity
@Table(name = "attendances", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"teaching_activity_id", "student_id"}))
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_activity_id", nullable = false)
    private TeachingActivity teachingActivity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
    
    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan; // Notes/remarks about the attendance
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy; // Teacher who recorded the attendance
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Attendance() {}
    
    public Attendance(TeachingActivity teachingActivity, Student student, AttendanceStatus status, 
                     String keterangan, User recordedBy) {
        this.teachingActivity = teachingActivity;
        this.student = student;
        this.status = status;
        this.keterangan = keterangan;
        this.recordedBy = recordedBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TeachingActivity getTeachingActivity() {
        return teachingActivity;
    }
    
    public void setTeachingActivity(TeachingActivity teachingActivity) {
        this.teachingActivity = teachingActivity;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public AttendanceStatus getStatus() {
        return status;
    }
    
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
    
    public String getKeterangan() {
        return keterangan;
    }
    
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    public User getRecordedBy() {
        return recordedBy;
    }
    
    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
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
}
