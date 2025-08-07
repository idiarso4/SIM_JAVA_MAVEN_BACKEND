package com.school.sim.dto.response;

import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for extracurricular activity data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularActivityResponse {

    private Long id;
    private String name;
    private String description;
    private ActivityType type;
    private String typeName;
    private ActivityStatus status;
    private String statusName;
    private LocalDate activityDate;
    private String activityDateFormatted;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot;
    private Integer durationMinutes;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer availableSpots;
    private LocalDate registrationDeadline;
    private String registrationDeadlineFormatted;
    private Boolean isMandatory;
    private Boolean requiresPermission;
    private String academicYear;
    private Integer semester;
    private SupervisorInfo supervisor;
    private DepartmentInfo department;
    private List<ParticipantInfo> participants;
    private String notes;
    private Boolean isActive;
    private Boolean isRegistrationOpen;
    private Boolean isFull;
    private Boolean isUpcoming;
    private Boolean isToday;
    private Boolean isPast;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Nested class for supervisor information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupervisorInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String username;
        private String email;
        private String phone;
        private String title;
    }

    /**
     * Nested class for department information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfo {
        private Long id;
        private String name;
        private String code;
        private String description;
        private String headName;
    }

    /**
     * Nested class for participant information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String studentNumber;
        private String email;
        private String phone;
        private String className;
        private LocalDateTime registrationDate;
        private Boolean hasPermission;
        private String notes;
    }

    /**
     * Nested class for activity statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityStatistics {
        private Integer totalActivities;
        private Integer upcomingActivities;
        private Integer completedActivities;
        private Integer cancelledActivities;
        private Integer totalParticipants;
        private Double averageParticipants;
        private Integer activitiesOpenForRegistration;
        private Integer mandatoryActivities;
        private Integer activitiesRequiringPermission;
    }

    /**
     * Nested class for registration information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationInfo {
        private Boolean canRegister;
        private String registrationStatus;
        private List<String> registrationRequirements;
        private List<String> registrationRestrictions;
        private LocalDate registrationDeadline;
        private Integer availableSpots;
        private Boolean requiresPermission;
        private Boolean isMandatory;
    }

    /**
     * Helper method to get formatted time slot
     */
    public String getFormattedTimeSlot() {
        if (startTime == null && endTime == null) {
            return "All Day";
        }
        if (startTime == null) {
            return "Until " + endTime;
        }
        if (endTime == null) {
            return "From " + startTime;
        }
        return startTime + " - " + endTime;
    }

    /**
     * Helper method to get activity status display name
     */
    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case PLANNED:
                return "Planned";
            case OPEN_FOR_REGISTRATION:
                return "Open for Registration";
            case REGISTRATION_CLOSED:
                return "Registration Closed";
            case IN_PROGRESS:
                return "In Progress";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            case POSTPONED:
                return "Postponed";
            default:
                return status.name();
        }
    }

    /**
     * Helper method to get activity type display name
     */
    public String getTypeDisplayName() {
        if (type == null) {
            return "Unknown";
        }
        switch (type) {
            case SPORTS:
                return "Sports";
            case CLUB:
                return "Club";
            case COMPETITION:
                return "Competition";
            case WORKSHOP:
                return "Workshop";
            case SEMINAR:
                return "Seminar";
            case CULTURAL_EVENT:
                return "Cultural Event";
            case COMMUNITY_SERVICE:
                return "Community Service";
            case FIELD_TRIP:
                return "Field Trip";
            case CONFERENCE:
                return "Conference";
            case OTHER:
                return "Other";
            default:
                return type.name();
        }
    }

    /**
     * Helper method to get participation rate
     */
    public Double getParticipationRate() {
        if (maxParticipants == null || maxParticipants == 0) {
            return null;
        }
        return (double) (currentParticipants != null ? currentParticipants : 0) / maxParticipants * 100;
    }

    /**
     * Helper method to check if activity is nearly full
     */
    public Boolean isNearlyFull() {
        if (maxParticipants == null) {
            return false;
        }
        double rate = getParticipationRate() != null ? getParticipationRate() : 0;
        return rate >= 80.0; // Consider 80% or more as nearly full
    }

    /**
     * Helper method to get days until activity
     */
    public Long getDaysUntilActivity() {
        if (activityDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), activityDate);
    }

    /**
     * Helper method to get days until registration deadline
     */
    public Long getDaysUntilRegistrationDeadline() {
        if (registrationDeadline == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), registrationDeadline);
    }
}