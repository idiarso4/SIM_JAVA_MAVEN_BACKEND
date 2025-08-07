package com.school.sim.dto.request;

import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for searching extracurricular attendance records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularAttendanceSearchRequest {

    private Long activityId;
    
    private String activityName;
    
    private Long studentId;
    
    private String studentName;
    
    private String studentNumber;
    
    private AttendanceStatus status;
    
    private List<AttendanceStatus> statuses;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate attendanceDate;
    
    private Boolean isExcused;
    
    private Integer minParticipationScore;
    
    private Integer maxParticipationScore;
    
    private Integer minPerformanceRating;
    
    private Integer maxPerformanceRating;
    
    private Integer minAchievementPoints;
    
    private Integer maxAchievementPoints;
    
    private Boolean hasParticipationScore;
    
    private Boolean hasPerformanceRating;
    
    private Boolean hasAchievementPoints;
    
    private Boolean isLate; // Has late arrival minutes > 0
    
    private Boolean isEarlyDeparture; // Has early departure minutes > 0
    
    private String recordedBy;
    
    private Boolean isActive;
    
    // Sorting options
    private String sortBy; // attendanceDate, participationScore, achievementPoints, etc.
    
    @Builder.Default
    private String sortDirection = "DESC"; // ASC or DESC
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    // Search options
    @Builder.Default
    private Boolean includeInactive = false;
    
    /**
     * Helper method to check if date range is valid
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }
    
    /**
     * Helper method to check if participation score range is valid
     */
    public boolean isValidParticipationScoreRange() {
        if (minParticipationScore == null || maxParticipationScore == null) {
            return true;
        }
        return minParticipationScore <= maxParticipationScore;
    }
    
    /**
     * Helper method to check if performance rating range is valid
     */
    public boolean isValidPerformanceRatingRange() {
        if (minPerformanceRating == null || maxPerformanceRating == null) {
            return true;
        }
        return minPerformanceRating <= maxPerformanceRating;
    }
    
    /**
     * Helper method to check if achievement points range is valid
     */
    public boolean isValidAchievementPointsRange() {
        if (minAchievementPoints == null || maxAchievementPoints == null) {
            return true;
        }
        return minAchievementPoints <= maxAchievementPoints;
    }
    
    /**
     * Helper method to check if search has any criteria
     */
    public boolean hasSearchCriteria() {
        return activityId != null || activityName != null || studentId != null ||
               studentName != null || studentNumber != null || status != null ||
               statuses != null || startDate != null || endDate != null ||
               attendanceDate != null || isExcused != null || minParticipationScore != null ||
               maxParticipationScore != null || minPerformanceRating != null ||
               maxPerformanceRating != null || minAchievementPoints != null ||
               maxAchievementPoints != null || hasParticipationScore != null ||
               hasPerformanceRating != null || hasAchievementPoints != null ||
               isLate != null || isEarlyDeparture != null || recordedBy != null;
    }
    
    /**
     * Helper method to get valid sort fields
     */
    public static List<String> getValidSortFields() {
        return List.of("attendanceDate", "participationScore", "performanceRating", 
                      "achievementPoints", "createdAt", "status", "checkInTime", "checkOutTime");
    }
    
    /**
     * Helper method to validate sort field
     */
    public boolean isValidSortField() {
        if (sortBy == null) {
            return true;
        }
        return getValidSortFields().contains(sortBy);
    }
}