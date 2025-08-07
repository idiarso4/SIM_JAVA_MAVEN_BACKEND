package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for teaching activity data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingActivityResponse {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot;
    private Integer duration; // in minutes
    
    private String topic;
    private String description;
    private String notes;
    private Boolean isCompleted;
    
    // Related entities
    private ScheduleInfo schedule;
    private SubjectInfo subject;
    private ClassRoomInfo classRoom;
    private TeacherInfo teacher;
    
    // Attendance summary
    private AttendanceSummary attendanceSummary;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        private Long id;
        private String dayOfWeek;
        private String academicYear;
        private Integer semester;
        private Boolean isActive;
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
    public static class AttendanceSummary {
        private Integer totalStudents;
        private Integer presentCount;
        private Integer absentCount;
        private Integer lateCount;
        private Integer sickCount;
        private Integer permitCount;
        private Double attendanceRate;
        private Boolean isAttendanceRecorded;
    }
}