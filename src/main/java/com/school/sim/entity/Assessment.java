package com.school.sim.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing academic assessments/evaluations
 * Defines assessment criteria and scoring methods
 */
@Entity
@Table(name = "assessments")
public class Assessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AssessmentType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;
    
    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight; // Weight in percentage (0.00 to 1.00)
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear; // Format: 2024/2025
    
    @Column(name = "semester", nullable = false)
    private Integer semester; // 1 or 2
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;
    
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentAssessment> studentAssessments;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Assessment() {}
    
    public Assessment(String title, String description, AssessmentType type, Subject subject,
                     ClassRoom classRoom, User teacher, BigDecimal maxScore, BigDecimal weight,
                     String academicYear, Integer semester) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.subject = subject;
        this.classRoom = classRoom;
        this.teacher = teacher;
        this.maxScore = maxScore;
        this.weight = weight;
        this.academicYear = academicYear;
        this.semester = semester;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AssessmentType getType() {
        return type;
    }
    
    public void setType(AssessmentType type) {
        this.type = type;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    public ClassRoom getClassRoom() {
        return classRoom;
    }
    
    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public Integer getSemester() {
        return semester;
    }
    
    public void setSemester(Integer semester) {
        this.semester = semester;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public List<StudentAssessment> getStudentAssessments() {
        return studentAssessments;
    }
    
    public void setStudentAssessments(List<StudentAssessment> studentAssessments) {
        this.studentAssessments = studentAssessments;
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
