package com.school.sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for timetable data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {

    private String title;
    private String type; // CLASS, TEACHER, SUBJECT
    private String academicYear;
    private Integer semester;
    
    // Entity information
    private Long entityId;
    private String entityName;
    private String entityCode;
    
    // Timetable data organized by day
    private Map<DayOfWeek, List<TimeSlot>> weeklySchedule;
    
    // Summary statistics
    private TimetableStatistics statistics;
    
    // Metadata
    private String generatedAt;
    private Integer totalHours;
    private Integer totalSessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private Long scheduleId;
        private LocalTime startTime;
        private LocalTime endTime;
        private String timeRange;
        private Integer duration;
        
        // Subject information
        private String subjectName;
        private String subjectCode;
        
        // Teacher information (for class timetables)
        private String teacherName;
        private String teacherCode;
        
        // Classroom information (for teacher timetables)
        private String classRoomName;
        private String classRoomCode;
        
        // Additional info
        private String notes;
        private Boolean isActive;
        private List<String> conflicts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimetableStatistics {
        private Integer totalSessions;
        private Integer totalHours;
        private Integer totalMinutes;
        private Map<DayOfWeek, Integer> sessionsByDay;
        private Map<DayOfWeek, Integer> hoursByDay;
        private Map<String, Integer> subjectHours;
        private Map<String, Integer> teacherHours;
        private Double averageSessionsPerDay;
        private String busiestDay;
        private String lightestDay;
        private Integer conflictCount;
        private List<String> conflictSummary;
    }
}
