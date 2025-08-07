package com.school.sim.dto.request;

import com.school.sim.entity.AttendanceStatus;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for attendance report generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long studentId;
    private Long classRoomId;
    private Long teacherId;
    private Long subjectId;
    private Long departmentId;
    private Long majorId;

    private List<AttendanceStatus> statusFilter;
    private List<Long> studentIds;
    private List<Long> classRoomIds;
    private List<Long> teacherIds;
    private List<Long> subjectIds;

    private String reportType; // SUMMARY, DETAILED, ANALYTICS, COMPARISON
    private String groupBy; // DAY, WEEK, MONTH, STUDENT, CLASS, SUBJECT
    private String sortBy;
    private String sortDirection; // ASC, DESC

    private Boolean includeStatistics;
    private Boolean includeCharts;
    private Boolean includeAbsenteeism;
    private Boolean includeLateArrivals;

    private Integer minAbsences;
    private Double minAttendanceRate;
    private Double maxAttendanceRate;

    private String exportFormat; // EXCEL, PDF, CSV
    private Boolean includeHeaders;
    private String templateName;

    // Pagination
    private Integer page;
    private Integer size;
}
