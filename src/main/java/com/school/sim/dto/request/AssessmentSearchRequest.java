package com.school.sim.dto.request;

import com.school.sim.entity.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for searching and filtering assessments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSearchRequest {

    private String title;
    private AssessmentType type;
    private Long subjectId;
    private Long classRoomId;
    private Long teacherId;
    private String academicYear;
    private Integer semester;
    private Boolean isActive;

    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private LocalDate createdFrom;
    private LocalDate createdTo;

    private List<AssessmentType> types;
    private List<Long> subjectIds;
    private List<Long> classRoomIds;
    private List<Long> teacherIds;

    private String sortBy; // title, dueDate, createdAt, type
    private String sortDirection; // ASC, DESC

    // Pagination
    private Integer page;
    private Integer size;
}
