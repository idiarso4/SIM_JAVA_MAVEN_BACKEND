package com.school.sim.dto.request;

import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for searching extracurricular activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtracurricularActivitySearchRequest {

    private String name;
    
    private String description;
    
    private ActivityType type;
    
    private List<ActivityType> types;
    
    private ActivityStatus status;
    
    private List<ActivityStatus> statuses;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate activityDate;
    
    private String location;
    
    private Long supervisorId;
    
    private String supervisorName;
    
    private Long departmentId;
    
    private String departmentName;
    
    @Pattern(regexp = "\\d{4}/\\d{4}", message = "Academic year must be in format YYYY/YYYY")
    private String academicYear;
    
    private Integer semester;
    
    private Boolean isMandatory;
    
    private Boolean requiresPermission;
    
    private Boolean hasAvailableSpots;
    
    private Boolean isUpcoming;
    
    private Boolean isToday;
    
    private Boolean openForRegistration;
    
    private Integer minParticipants;
    
    private Integer maxParticipants;
    
    private Long participantId; // To find activities a specific student is registered for
    
    private Boolean isActive;
    
    // Sorting options
    private String sortBy; // name, activityDate, createdAt, currentParticipants
    
    @Builder.Default
    private String sortDirection = "ASC"; // ASC or DESC
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    // Search options
    @Builder.Default
    private Boolean includeInactive = false;
    
    @Builder.Default
    private Boolean exactMatch = false; // For name and location searches
    
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
     * Helper method to get effective start date
     */
    public LocalDate getEffectiveStartDate() {
        if (isUpcoming != null && isUpcoming) {
            return LocalDate.now();
        }
        if (isToday != null && isToday) {
            return LocalDate.now();
        }
        return startDate;
    }
    
    /**
     * Helper method to get effective end date
     */
    public LocalDate getEffectiveEndDate() {
        if (isToday != null && isToday) {
            return LocalDate.now();
        }
        return endDate;
    }
    
    /**
     * Helper method to check if search has any criteria
     */
    public boolean hasSearchCriteria() {
        return name != null || description != null || type != null || types != null ||
               status != null || statuses != null || startDate != null || endDate != null ||
               activityDate != null || location != null || supervisorId != null ||
               supervisorName != null || departmentId != null || departmentName != null ||
               academicYear != null || semester != null || isMandatory != null ||
               requiresPermission != null || hasAvailableSpots != null || isUpcoming != null ||
               isToday != null || openForRegistration != null || minParticipants != null ||
               maxParticipants != null || participantId != null;
    }
    
    /**
     * Helper method to get valid sort fields
     */
    public static List<String> getValidSortFields() {
        return List.of("name", "activityDate", "createdAt", "currentParticipants", 
                      "type", "status", "startTime", "endTime");
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