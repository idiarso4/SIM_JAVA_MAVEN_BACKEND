package com.school.sim.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for searching and filtering schedules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSearchRequest {

    private Long classRoomId;
    private Long subjectId;
    private Long teacherId;
    private String academicYear;
    private Integer semester;
    private DayOfWeek dayOfWeek;
    private Boolean isActive;

    private LocalTime startTimeFrom;
    private LocalTime startTimeTo;
    private LocalTime endTimeFrom;
    private LocalTime endTimeTo;

    private List<Long> classRoomIds;
    private List<Long> subjectIds;
    private List<Long> teacherIds;
    private List<DayOfWeek> daysOfWeek;

    private String sortBy; // dayOfWeek, startTime, endTime, subject, teacher
    private String sortDirection; // ASC, DESC

    // Pagination
    private Integer page;
    private Integer size;
}
