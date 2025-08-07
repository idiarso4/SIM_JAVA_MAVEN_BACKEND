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

/**
 * Entity representing attendance for extracurricular activities
 * Tracks student participation in extracurricular activities
 */
@Entity
@Table(name = "extracurricular_attendance", indexes = {
        @Index(name = "idx_extracurricular_attendance_activity", columnList = "activity_id"),
        @Index(name = "idx_extracurricular_attendance_student", columnList = "student_id"),
        @Index(name = "idx_extracurricular_attendance_date", columnList = "attendance_date"),
        @Index(name = "idx_extracurricular_attendance_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ExtracurricularActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "participation_score")
    private Integer participationScore; // 0-100

    @Column(name = "performance_rating")
    private Integer performanceRating; // 1-5 stars

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "recorded_by")
    private String recordedBy;

    @Column(name = "is_excused")
    private Boolean isExcused;

    @Column(name = "excuse_reason", length = 500)
    private String excuseReason;

    @Column(name = "late_arrival_minutes")
    private Integer lateArrivalMinutes;

    @Column(name = "early_departure_minutes")
    private Integer earlyDepartureMinutes;

    @Column(name = "achievement_points")
    private Integer achievementPoints;

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
     * Enum for attendance status
     */
    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LATE,
        EXCUSED,
        PARTIAL, // Left early or arrived late but participated
        NO_SHOW, // Registered but didn't attend
        CANCELLED // Activity was cancelled
    }

    /**
     * Helper method to check if student was present
     */
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT || 
               status == AttendanceStatus.LATE || 
               status == AttendanceStatus.PARTIAL;
    }

    /**
     * Helper method to check if attendance was on time
     */
    public boolean isOnTime() {
        return status == AttendanceStatus.PRESENT && 
               (lateArrivalMinutes == null || lateArrivalMinutes == 0);
    }

    /**
     * Helper method to calculate total participation time in minutes
     */
    public Integer getParticipationTimeMinutes() {
        if (checkInTime == null || checkOutTime == null) {
            return null;
        }
        return (int) java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    /**
     * Helper method to get attendance quality score (0-100)
     */
    public Integer getAttendanceQualityScore() {
        if (!isPresent()) {
            return 0;
        }
        
        int score = 100;
        
        // Deduct points for late arrival
        if (lateArrivalMinutes != null && lateArrivalMinutes > 0) {
            score -= Math.min(30, lateArrivalMinutes); // Max 30 points deduction
        }
        
        // Deduct points for early departure
        if (earlyDepartureMinutes != null && earlyDepartureMinutes > 0) {
            score -= Math.min(20, earlyDepartureMinutes); // Max 20 points deduction
        }
        
        return Math.max(0, score);
    }

    /**
     * Helper method to check if this is a quality attendance
     */
    public boolean isQualityAttendance() {
        return isPresent() && 
               (lateArrivalMinutes == null || lateArrivalMinutes <= 5) &&
               (earlyDepartureMinutes == null || earlyDepartureMinutes <= 5) &&
               (participationScore == null || participationScore >= 70);
    }

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (isExcused == null) {
            isExcused = false;
        }
        if (lateArrivalMinutes == null) {
            lateArrivalMinutes = 0;
        }
        if (earlyDepartureMinutes == null) {
            earlyDepartureMinutes = 0;
        }
        if (achievementPoints == null) {
            achievementPoints = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Any pre-update logic can be added here
    }
}