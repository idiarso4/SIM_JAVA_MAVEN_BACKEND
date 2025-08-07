package com.school.sim.dto.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for academic report generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportRequest {

    @NotNull(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Semester is required")
    private Integer semester;

    private Long studentId;
    private Long classRoomId;
    private Long subjectId;
    private Long teacherId;
    private Long departmentId;
    private Long majorId;

    private List<Long> studentIds;
    private List<Long> classRoomIds;
    private List<Long> subjectIds;
    private List<Long> teacherIds;

    private String reportType; // TRANSCRIPT, PROGRESS, RANKING, SUMMARY, DETAILED
    private String groupBy; // STUDENT, CLASS, SUBJECT, TEACHER
    private String sortBy; // GRADE, NAME, RANK, AVERAGE
    private String sortDirection; // ASC, DESC

    private Boolean includeStatistics;
    private Boolean includeRanking;
    private Boolean includeGradeDistribution;
    private Boolean includeProgressTracking;
    private Boolean includeAttendanceData;

    private String exportFormat; // PDF, EXCEL, CSV
    private String templateName;

    // Grade calculation options
    private Boolean useWeightedGrades;
    private String gradingScale; // LETTER, NUMERIC, PERCENTAGE
    private Double passingGrade;

    // Pagination
    private Integer page;
    private Integer size;
}
