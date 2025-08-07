package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for schedule data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    private DayOfWeek dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot;
    private Integer duration; // in minutes
    
    // Related entities
    private ClassRoomInfo classRoom;
    private SubjectInfo subject;
    private TeacherInfo teacher;
    
    private String academicYear;
    private Integer semester;
    private Boolean isActive;
    private String notes;
    
    // Conflict information
    private List<ConflictInfo> conflicts;
    private Boolean hasConflicts;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassRoomInfo {
        private Long id;
        private String name;
        private String code;
        private Integer capacity;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectInfo {
        private Long id;
        private String name;
        private String code;
        private Integer credits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String username;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private String type; // TEACHER_CONFLICT, CLASSROOM_CONFLICT, TIME_OVERLAP
        private String description;
        private Long conflictingScheduleId;
        private String conflictingEntity;
        private LocalTime conflictStartTime;
        private LocalTime conflictEndTime;
        private String severity; // HIGH, MEDIUM, LOW
    }
}
