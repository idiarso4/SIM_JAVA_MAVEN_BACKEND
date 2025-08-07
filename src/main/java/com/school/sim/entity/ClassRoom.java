package com.school.sim.entity;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassRoom entity representing classes in the school
 */
@Entity
@Table(name = "class_rooms", indexes = {
        @Index(name = "idx_classroom_name", columnList = "name"),
        @Index(name = "idx_classroom_grade", columnList = "grade"),
        @Index(name = "idx_classroom_major", columnList = "major_id"),
        @Index(name = "idx_classroom_teacher", columnList = "homeroom_teacher_id")
})
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Class name is required")
    @Size(max = 50, message = "Class name must not exceed 50 characters")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull(message = "Grade is required")
    @Min(value = 1, message = "Grade must be at least 1")
    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Size(max = 10, message = "Class code must not exceed 10 characters")
    @Column(name = "class_code", length = 10)
    private String classCode;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_enrollment", nullable = false)
    private Integer currentEnrollment = 0;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_teacher_id")
    private User homeroomTeacher;

    @OneToMany(mappedBy = "classRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    // Constructors
    public ClassRoom() {
    }

    public ClassRoom(String name, Integer grade, Major major) {
        this.name = name;
        this.grade = grade;
        this.major = major;
    }

    public ClassRoom(String name, Integer grade, String classCode, Integer capacity, Major major) {
        this.name = name;
        this.grade = grade;
        this.classCode = classCode;
        this.capacity = capacity;
        this.major = major;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    // Alias methods for compatibility
    public String getCode() {
        return classCode;
    }

    public void setCode(String code) {
        this.classCode = code;
    }

    public String getLocation() {
        return name; // Using name as location for now
    }

    public void setLocation(String location) {
        this.name = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentEnrollment() {
        return currentEnrollment;
    }

    public void setCurrentEnrollment(Integer currentEnrollment) {
        this.currentEnrollment = currentEnrollment;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    public User getHomeroomTeacher() {
        return homeroomTeacher;
    }

    public void setHomeroomTeacher(User homeroomTeacher) {
        this.homeroomTeacher = homeroomTeacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    // Helper methods
    public void addStudent(Student student) {
        if (capacity != null && currentEnrollment >= capacity) {
            throw new IllegalStateException("Class is at full capacity");
        }
        students.add(student);
        student.setClassRoom(this);
        currentEnrollment = students.size();
    }

    public void removeStudent(Student student) {
        students.remove(student);
        student.setClassRoom(null);
        currentEnrollment = students.size();
    }

    public boolean isFull() {
        return capacity != null && currentEnrollment >= capacity;
    }

    public boolean hasAvailableSpace() {
        return capacity == null || currentEnrollment < capacity;
    }

    public int getAvailableSpace() {
        if (capacity == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, capacity - currentEnrollment);
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassRoom classRoom = (ClassRoom) o;
        return Objects.equals(name, classRoom.name) && 
               Objects.equals(grade, classRoom.grade) && 
               Objects.equals(major, classRoom.major);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, grade, major);
    }

    @Override
    public String toString() {
        return "ClassRoom{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", grade=" + grade +
                ", classCode='" + classCode + '\'' +
                ", capacity=" + capacity +
                ", currentEnrollment=" + currentEnrollment +
                ", isActive=" + isActive +
                '}';
    }
}
