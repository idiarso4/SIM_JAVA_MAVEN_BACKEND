package com.school.sim.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Entity representing an extracurricular activity
 * Manages non-academic activities like sports, clubs, events, etc.
 */
@Entity
@Table(name = "extracurricular_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants")
    private Integer currentParticipants;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    @Column(name = "requires_permission")
    private Boolean requiresPermission;

    @Column(name = "academic_year", length = 9)
    private String academicYear;

    @Column(name = "semester")
    private Integer semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(mappedBy = "extracurricularActivities", fetch = FetchType.LAZY)
    private List<Student> participants;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Enum for activity types
     */
    public enum ActivityType {
        SPORTS,
        CLUB,
        COMPETITION,
        WORKSHOP,
        SEMINAR,
        CULTURAL_EVENT,
        COMMUNITY_SERVICE,
        FIELD_TRIP,
        CONFERENCE,
        OTHER
    }

    /**
     * Enum for activity status
     */
    public enum ActivityStatus {
        PLANNED,
        OPEN_FOR_REGISTRATION,
        REGISTRATION_CLOSED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        POSTPONED
    }

    /**
     * Helper method to check if registration is open
     */
    public boolean isRegistrationOpen() {
        return status == ActivityStatus.OPEN_FOR_REGISTRATION &&
               (registrationDeadline == null || !LocalDate.now().isAfter(registrationDeadline)) &&
               (maxParticipants == null || currentParticipants < maxParticipants);
    }

    /**
     * Helper method to check if activity is full
     */
    public boolean isFull() {
        return maxParticipants != null && currentParticipants >= maxParticipants;
    }

    /**
     * Helper method to get available spots
     */
    public Integer getAvailableSpots() {
        if (maxParticipants == null) {
            return null; // Unlimited
        }
        return Math.max(0, maxParticipants - (currentParticipants != null ? currentParticipants : 0));
    }

    /**
     * Helper method to check if activity is upcoming
     */
    public boolean isUpcoming() {
        return activityDate.isAfter(LocalDate.now()) || 
               (activityDate.equals(LocalDate.now()) && 
                startTime != null && LocalTime.now().isBefore(startTime));
    }

    /**
     * Helper method to check if activity is today
     */
    public boolean isToday() {
        return activityDate.equals(LocalDate.now());
    }

    /**
     * Helper method to get duration in minutes
     */
    public Integer getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (currentParticipants == null) {
            currentParticipants = 0;
        }
        if (isMandatory == null) {
            isMandatory = false;
        }
        if (requiresPermission == null) {
            requiresPermission = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Any pre-update logic can be added here
    }
}