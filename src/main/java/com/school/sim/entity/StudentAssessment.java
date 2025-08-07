package com.school.sim.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing individual student assessment scores and evaluations
 * Links students to their specific assessment results
 */
@Entity
@Table(name = "student_assessments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "student_id"}))
public class StudentAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "grade", length = 2)
    private String grade; // A, B, C, D, E or numeric grade
    
    @Column(name = "is_submitted", nullable = false)
    private Boolean isSubmitted = false;
    
    @Column(name = "submission_date")
    private LocalDateTime submissionDate;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy; // Teacher who graded the assessment
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public StudentAssessment() {}
    
    public StudentAssessment(Assessment assessment, Student student, BigDecimal score, 
                           String grade, User gradedBy) {
        this.assessment = assessment;
        this.student = student;
        this.score = score;
        this.grade = grade;
        this.gradedBy = gradedBy;
        this.gradedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Assessment getAssessment() {
        return assessment;
    }
    
    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public Boolean getIsSubmitted() {
        return isSubmitted;
    }
    
    public void setIsSubmitted(Boolean isSubmitted) {
        this.isSubmitted = isSubmitted;
    }
    
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public User getGradedBy() {
        return gradedBy;
    }
    
    public void setGradedBy(User gradedBy) {
        this.gradedBy = gradedBy;
    }
    
    public LocalDateTime getGradedAt() {
        return gradedAt;
    }
    
    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
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
