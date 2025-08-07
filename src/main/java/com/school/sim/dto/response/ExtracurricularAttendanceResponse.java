package com.school.sim.dto.response;

import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for extracurricular attendance data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularAttendanceResponse {

    private Long id;
    private ActivityInfo activity;
    private StudentInfo student;
    private LocalDate attendanceDate;
    private String attendanceDateFormatted;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String timeSlot;
    private Integer participationTimeMinutes;
    private AttendanceStatus status;
    private String statusName;
    private Integer participationScore;
    private Integer performanceRating;
    private String notes;
    private String recordedBy;
    private Boolean isExcused;
    private String excuseReason;
    private Integer lateArrivalMinutes;
    private Integer earlyDepartureMinutes;
    private Integer achievementPoints;
    private Integer attendanceQualityScore;
    private Boolean isQualityAttendance;
    private Boolean isPresent;
    private Boolean isOnTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Nested class for activity information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityInfo {
        private Long id;
        private String name;
        private String type;
        private String location;
        private LocalDate activityDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String supervisorName;
        private String departmentName;
    }

    /**
     * Nested class for student information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String studentNumber;
        private String email;
        private String phone;
        private String className;
    }

    /**
     * Nested class for attendance statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceStatistics {
        private Long totalRecords;
        private Long presentCount;
        private Long absentCount;
        private Long lateCount;
        private Long excusedCount;
        private Double attendanceRate;
        private Double averageParticipationScore;
        private Long totalAchievementPoints;
        private Double averagePerformanceRating;
        private Long qualityAttendanceCount;
        private Double qualityAttendanceRate;
    }

    /**
     * Nested class for student progress tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProgress {
        private StudentInfo student;
        private Long totalActivities;
        private Long attendedActivities;
        private Double attendanceRate;
        private Double averageParticipationScore;
        private Long totalAchievementPoints;
        private Double averagePerformanceRating;
        private Long qualityAttendanceCount;
        private String progressLevel; // EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT
        private List<String> achievements;
        private List<String> recommendations;
        private Map<String, Object> monthlyTrends;
    }

    /**
     * Nested class for activity participation report
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityParticipationReport {
        private ActivityInfo activity;
        private Long totalSessions;
        private Long totalParticipants;
        private Double averageAttendanceRate;
        private Double averageParticipationScore;
        private Long totalAchievementPoints;
        private List<StudentProgress> topPerformers;
        private List<StudentProgress> needsAttention;
        private Map<String, Long> attendanceByStatus;
        private Map<String, Object> trends;
    }

    /**
     * Nested class for attendance trends
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceTrend {
        private String period; // YYYY-MM format
        private Long totalRecords;
        private Long presentCount;
        private Double attendanceRate;
        private Double averageParticipationScore;
        private Long totalAchievementPoints;
    }

    /**
     * Helper method to get formatted attendance status
     */
    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case PRESENT:
                return "Present";
            case ABSENT:
                return "Absent";
            case LATE:
                return "Late";
            case EXCUSED:
                return "Excused";
            case PARTIAL:
                return "Partial Attendance";
            case NO_SHOW:
                return "No Show";
            case CANCELLED:
                return "Cancelled";
            default:
                return status.name();
        }
    }

    /**
     * Helper method to get formatted time slot
     */
    public String getFormattedTimeSlot() {
        if (checkInTime == null && checkOutTime == null) {
            return "Not recorded";
        }
        if (checkInTime == null) {
            return "Until " + checkOutTime;
        }
        if (checkOutTime == null) {
            return "From " + checkInTime;
        }
        return checkInTime + " - " + checkOutTime;
    }

    /**
     * Helper method to get performance level
     */
    public String getPerformanceLevel() {
        if (participationScore == null) {
            return "Not Rated";
        }
        if (participationScore >= 90) {
            return "Excellent";
        } else if (participationScore >= 80) {
            return "Good";
        } else if (participationScore >= 70) {
            return "Average";
        } else if (participationScore >= 60) {
            return "Below Average";
        } else {
            return "Needs Improvement";
        }
    }

    /**
     * Helper method to get attendance quality level
     */
    public String getQualityLevel() {
        if (attendanceQualityScore == null) {
            return "Not Calculated";
        }
        if (attendanceQualityScore >= 95) {
            return "Excellent";
        } else if (attendanceQualityScore >= 85) {
            return "Good";
        } else if (attendanceQualityScore >= 75) {
            return "Average";
        } else {
            return "Needs Improvement";
        }
    }

    /**
     * Helper method to check if attendance has issues
     */
    public Boolean hasAttendanceIssues() {
        return (lateArrivalMinutes != null && lateArrivalMinutes > 0) ||
               (earlyDepartureMinutes != null && earlyDepartureMinutes > 0) ||
               status == AttendanceStatus.ABSENT ||
               status == AttendanceStatus.NO_SHOW;
    }

    /**
     * Helper method to get participation duration in hours
     */
    public Double getParticipationHours() {
        if (participationTimeMinutes == null) {
            return null;
        }
        return participationTimeMinutes / 60.0;
    }

    /**
     * Helper method to get achievement level based on points
     */
    public String getAchievementLevel() {
        if (achievementPoints == null || achievementPoints == 0) {
            return "None";
        } else if (achievementPoints >= 50) {
            return "Outstanding";
        } else if (achievementPoints >= 30) {
            return "Excellent";
        } else if (achievementPoints >= 20) {
            return "Good";
        } else if (achievementPoints >= 10) {
            return "Fair";
        } else {
            return "Basic";
        }
    }

    /**
     * Static factory method to create response from entity
     */
    public static ExtracurricularAttendanceResponse from(com.school.sim.entity.ExtracurricularAttendance attendance) {
        if (attendance == null) {
            return null;
        }

        ExtracurricularAttendanceResponseBuilder builder = ExtracurricularAttendanceResponse.builder()
                .id(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .participationScore(attendance.getParticipationScore())
                .performanceRating(attendance.getPerformanceRating())
                .notes(attendance.getNotes())
                .recordedBy(attendance.getRecordedBy())
                .isExcused(attendance.getIsExcused())
                .excuseReason(attendance.getExcuseReason())
                .lateArrivalMinutes(attendance.getLateArrivalMinutes())
                .earlyDepartureMinutes(attendance.getEarlyDepartureMinutes())
                .achievementPoints(attendance.getAchievementPoints())
                .isActive(attendance.getIsActive())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .createdBy(attendance.getCreatedBy())
                .updatedBy(attendance.getUpdatedBy());

        // Set calculated fields
        builder.participationTimeMinutes(attendance.getParticipationTimeMinutes())
               .attendanceQualityScore(attendance.getAttendanceQualityScore())
               .isQualityAttendance(attendance.isQualityAttendance())
               .isPresent(attendance.isPresent())
               .isOnTime(attendance.isOnTime());

        // Set activity info if available
        if (attendance.getActivity() != null) {
            com.school.sim.entity.ExtracurricularActivity activity = attendance.getActivity();
            ActivityInfo activityInfo = ActivityInfo.builder()
                    .id(activity.getId())
                    .name(activity.getName())
                    .type(activity.getType() != null ? activity.getType().toString() : null)
                    .location(activity.getLocation())
                    .build();
            builder.activity(activityInfo);
        }

        // Set student info if available
        if (attendance.getStudent() != null) {
            com.school.sim.entity.Student student = attendance.getStudent();
            StudentInfo studentInfo = StudentInfo.builder()
                    .id(student.getId())
                    .studentNumber(student.getNis())
                    .fullName(student.getNamaLengkap())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .build();
            builder.student(studentInfo);
        }

        return builder.build();
    }
}