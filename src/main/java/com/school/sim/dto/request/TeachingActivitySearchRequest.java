package com.school.sim.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for searching and filtering teaching activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingActivitySearchRequest {

    private Long scheduleId;
    private Long subjectId;
    private Long classRoomId;
    private Long teacherId;
    
    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    private String topic;
    private Boolean isCompleted;
    
    // Multiple filters
    private List<Long> scheduleIds;
    private List<Long> subjectIds;
    private List<Long> classRoomIds;
    private List<Long> teacherIds;
    
    // Sorting
    private String sortBy; // date, startTime, topic, teacher, subject
    private String sortDirection; // ASC, DESC
    
    // Pagination
    private Integer page;
    private Integer size;
}