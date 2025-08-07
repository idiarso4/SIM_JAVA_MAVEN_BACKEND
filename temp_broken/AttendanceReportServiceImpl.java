package com.school.sim.service.impl;

import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AttendanceReportResponse;
import com.school.sim.entity.*;
import com.school.sim.repository.*;
import com.school.sim.service.AttendanceReportService;
import com.school.sim.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AttendanceReportService
 * Provides comprehensive attendance reporting and analytics functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ExcelService excelService;

    @Override
    @Cacheable(value = "attendanceReports", key = "#request.hashCode()")
    public AttendanceReportResponse generateAttendanceReport(AttendanceReportRequest request) {
        log.info("Generating attendance report for period: {} to {}", request.getStartDate(), request.getEndDate());
        
        List<Attendance> attendances = getFilteredAttendances(request);
        AttendanceReportResponse.AttendanceStatistics statistics = calculateStatistics(attendances);
        List<AttendanceReportResponse.AttendanceReportItem> items = generateReportItems(attendances, request);
        
        return AttendanceReportResponse.builder()
                .reportId(generateReportId())
                .reportType(request.getReportType() != null ? request.getReportType() : "GENERAL")
                .title("Attendance Report")
                .description(generateReportDescription(request))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .generatedAt(LocalDateTime.now())
                .generatedBy("System")
                .statistics(statistics)
                .items(items)
                .totalRecords(items.size())
                .filters(buildFiltersMap(request))
                .build();
    }

    @Override
    public AttendanceReportResponse generateStudentAttendanceReport(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating student attendance report for student: {}", studentId);
        
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .studentId(studentId)
                .startDate(startDate)
                .endDate(endDate)
                .reportType("STUDENT")
                .build();
        
        return generateAttendanceReport(request);
    }

    @Override
    public AttendanceReportResponse generateClassAttendanceReport(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating class attendance report for class: {}", classRoomId);
        
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .classRoomId(classRoomId)
                .startDate(startDate)
                .endDate(endDate)
                .reportType("CLASS")
                .build();
        
        return generateAttendanceReport(request);
    }

    @Override
    public AttendanceReportResponse generateTeacherAttendanceReport(Long teacherId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating teacher attendance report for teacher: {}", teacherId);
        
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .teacherId(teacherId)
                .startDate(startDate)
                .endDate(endDate)
                .reportType("TEACHER")
                .build();
        
        return generateAttendanceReport(request);
    }

    @Override
    public AttendanceReportResponse generateSubjectAttendanceReport(Long subjectId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating subject attendance report for subject: {}", subjectId);
        
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .subjectId(subjectId)
                .startDate(startDate)
                .endDate(endDate)
                .reportType("SUBJECT")
                .build();
        
        return generateAttendanceReport(request);
    }

    @Override
    @Cacheable(value = "dailyAttendanceSummary", key = "#date")
    public List<Map<String, Object>> generateDailyAttendanceSummary(LocalDate date) {
        log.info("Generating daily attendance summary for date: {}", date);
        
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(date, date);
        
        return attendances.stream()
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getStudent().getClassRoom().getName(),
                        Collectors.groupingBy(
                                attendance -> attendance.getStatus(),
                                Collectors.counting()
                        )
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("className", entry.getKey());
                    summary.put("date", date);
                    summary.put("statusBreakdown", entry.getValue());
                    summary.put("totalStudents", entry.getValue().values().stream().mapToLong(Long::longValue).sum());
                    return summary;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "weeklyAttendanceSummary", key = "#startDate")
    public List<Map<String, Object>> generateWeeklyAttendanceSummary(LocalDate startDate) {
        log.info("Generating weekly attendance summary starting from: {}", startDate);
        
        LocalDate endDate = startDate.plusDays(6);
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
        
        return generatePeriodSummary(attendances, startDate, endDate, "WEEK");
    }

    @Override
    @Cacheable(value = "monthlyAttendanceSummary", key = "#year + '-' + #month")
    public List<Map<String, Object>> generateMonthlyAttendanceSummary(int year, int month) {
        log.info("Generating monthly attendance summary for {}/{}", year, month);
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
        
        return generatePeriodSummary(attendances, startDate, endDate, "MONTH");
    }

    @Override
    @Cacheable(value = "attendanceStatistics", key = "#startDate + '-' + #endDate")
    public Map<String, Object> generateAttendanceStatisticsDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Generating attendance statistics dashboard for period: {} to {}", startDate, endDate);
        
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalRecords", attendances.size());
        dashboard.put("dateRange", Map.of("start", startDate, "end", endDate));
        
        // Status breakdown
        Map<AttendanceStatus, Long> statusBreakdown = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
        dashboard.put("statusBreakdown", statusBreakdown);
        
        // Daily trends
        Map<LocalDate, Long> dailyTrends = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate, Collectors.counting()));
        dashboard.put("dailyTrends", dailyTrends);
        
        // Class-wise statistics
        Map<String, Long> classStats = attendances.stream()
                .collect(Collectors.groupingBy(
                        attendance -> attendance.getStudent().getClassRoom().getName(),
                        Collectors.counting()
                ));
        dashboard.put("classStatistics", classStats);
        
        return dashboard;
    }

    @Override
    public List<Map<String, Object>> generateAttendanceTrendAnalysis(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("Generating attendance trend analysis grouped by: {}", groupBy);
        
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
        
        switch (groupBy.toUpperCase()) {
            case "DAY":
                return generateDailyTrends(attendances);
            case "WEEK":
                return generateWeeklyTrends(attendances, startDate);
            case "MONTH":
                return generateMonthlyTrends(attendances);
            case "STUDENT":
                return generateStudentTrends(attendances);
            case "CLASS":
                return generateClassTrends(attendances);
            default:
                return generateDailyTrends(attendances);
        }
    }

    @Override
    public AttendanceReportResponse generateAbsenteeismReport(LocalDate startDate, LocalDate endDate, int minAbsences) {
        log.info("Generating absenteeism report with minimum {} absences", minAbsences);
        
        List<Attendance> absences = attendanceRepository.findByAttendanceDateBetweenAndStatus(
                startDate, endDate, AttendanceStatus.ABSENT);
        
        Map<Student, Long> studentAbsences = absences.stream()
                .collect(Collectors.groupingBy(Attendance::getStudent, Collectors.counting()));
        
        List<AttendanceReportResponse.AttendanceReportItem> items = studentAbsences.entrySet().stream()
                .filter(entry -> entry.getValue() >= minAbsences)
                .map(entry -> buildStudentReportItem(entry.getKey(), entry.getValue(), startDate, endDate))
                .collect(Collectors.toList());
        
        return AttendanceReportResponse.builder()
                .reportId(generateReportId())
                .reportType("ABSENTEEISM")
                .title("Absenteeism Report")
                .description(String.format("Students with %d or more absences", minAbsences))
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .items(items)
                .totalRecords(items.size())
                .build();
    }

    @Override
    public AttendanceReportResponse generatePerfectAttendanceReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating perfect attendance report");
        
        // Get all students who have attendance records in the period
        List<Student> studentsWithRecords = attendanceRepository.findByAttendanceDateBetween(startDate, endDate)
                .stream()
                .map(Attendance::getStudent)
                .distinct()
                .collect(Collectors.toList());
        
        List<AttendanceReportResponse.AttendanceReportItem> perfectAttendanceItems = studentsWithRecords.stream()
                .filter(student -> hasPerfectAttendance(student, startDate, endDate))
                .map(student -> buildStudentReportItem(student, 0L, startDate, endDate))
                .collect(Collectors.toList());
        
        return AttendanceReportResponse.builder()
                .reportId(generateReportId())
                .reportType("PERFECT_ATTENDANCE")
                .title("Perfect Attendance Report")
                .description("Students with perfect attendance")
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .items(perfectAttendanceItems)
                .totalRecords(perfectAttendanceItems.size())
                .build();
    }

    // Helper methods
    private List<Attendance> getFilteredAttendances(AttendanceReportRequest request) {
        // Implementation would use Specification pattern for complex filtering
        List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(
                request.getStartDate(), request.getEndDate());
        
        if (request.getStudentId() != null) {
            attendances = attendances.stream()
                    .filter(a -> a.getStudent().getId().equals(request.getStudentId()))
                    .collect(Collectors.toList());
        }
        
        if (request.getClassRoomId() != null) {
            attendances = attendances.stream()
                    .filter(a -> a.getStudent().getClassRoom().getId().equals(request.getClassRoomId()))
                    .collect(Collectors.toList());
        }
        
        if (request.getStatusFilter() != null && !request.getStatusFilter().isEmpty()) {
            attendances = attendances.stream()
                    .filter(a -> request.getStatusFilter().contains(a.getStatus()))
                    .collect(Collectors.toList());
        }
        
        return attendances;
    }

    private AttendanceReportResponse.AttendanceStatistics calculateStatistics(List<Attendance> attendances) {
        if (attendances.isEmpty()) {
            return AttendanceReportResponse.AttendanceStatistics.builder()
                    .totalAttendanceRecords(0L)
                    .overallAttendanceRate(0.0)
                    .build();
        }
        
        Map<AttendanceStatus, Long> statusCounts = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
        
        long totalRecords = attendances.size();
        long presentCount = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
        long absentCount = statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long lateCount = statusCounts.getOrDefault(AttendanceStatus.LATE, 0L);
        long excusedCount = statusCounts.getOrDefault(AttendanceStatus.EXCUSED, 0L);
        
        double attendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;
        double absenteeismRate = totalRecords > 0 ? (double) absentCount / totalRecords * 100 : 0.0;
        double lateArrivalRate = totalRecords > 0 ? (double) lateCount / totalRecords * 100 : 0.0;
        
        return AttendanceReportResponse.AttendanceStatistics.builder()
                .totalAttendanceRecords((long) totalRecords)
                .totalStudents((long) attendances.stream().map(Attendance::getStudent).distinct().count())
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .excusedCount(excusedCount)
                .overallAttendanceRate(attendanceRate)
                .absenteeismRate(absenteeismRate)
                .lateArrivalRate(lateArrivalRate)
                .statusBreakdown(statusCounts.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                Map.Entry::getValue
                        )))
                .build();
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateReportItems(
            List<Attendance> attendances, AttendanceReportRequest request) {
        
        String reportType = request.getReportType() != null ? request.getReportType() : "GENERAL";
        
        switch (reportType.toUpperCase()) {
            case "STUDENT":
                return generateStudentItems(attendances);
            case "CLASS":
                return generateClassItems(attendances);
            case "SUBJECT":
                return generateSubjectItems(attendances);
            case "TEACHER":
                return generateTeacherItems(attendances);
            default:
                return generateGeneralItems(attendances);
        }
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateStudentItems(List<Attendance> attendances) {
        return attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStudent))
                .entrySet().stream()
                .map(entry -> {
                    Student student = entry.getKey();
                    List<Attendance> studentAttendances = entry.getValue();
                    
                    Map<AttendanceStatus, Long> statusCounts = studentAttendances.stream()
                            .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
                    
                    long totalSessions = studentAttendances.size();
                    long presentSessions = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
                    double attendanceRate = totalSessions > 0 ? (double) presentSessions / totalSessions * 100 : 0.0;
                    
                    return AttendanceReportResponse.AttendanceReportItem.builder()
                            .type("STUDENT")
                            .entityId(student.getId())
                            .entityName(student.getFirstName() + " " + student.getLastName())
                            .entityCode(student.getStudentNumber())
                            .totalSessions(totalSessions)
                            .presentSessions(presentSessions)
                            .absentSessions(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L))
                            .lateSessions(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L))
                            .excusedSessions(statusCounts.getOrDefault(AttendanceStatus.EXCUSED, 0L))
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateClassItems(List<Attendance> attendances) {
        return attendances.stream()
                .collect(Collectors.groupingBy(attendance -> attendance.getStudent().getClassRoom()))
                .entrySet().stream()
                .map(entry -> {
                    ClassRoom classRoom = entry.getKey();
                    List<Attendance> classAttendances = entry.getValue();
                    
                    Map<AttendanceStatus, Long> statusCounts = classAttendances.stream()
                            .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
                    
                    long totalSessions = classAttendances.size();
                    long presentSessions = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
                    double attendanceRate = totalSessions > 0 ? (double) presentSessions / totalSessions * 100 : 0.0;
                    
                    return AttendanceReportResponse.AttendanceReportItem.builder()
                            .type("CLASS")
                            .entityId(classRoom.getId())
                            .entityName(classRoom.getName())
                            .entityCode(classRoom.getCode())
                            .totalSessions(totalSessions)
                            .presentSessions(presentSessions)
                            .absentSessions(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L))
                            .lateSessions(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L))
                            .excusedSessions(statusCounts.getOrDefault(AttendanceStatus.EXCUSED, 0L))
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateSubjectItems(List<Attendance> attendances) {
        return attendances.stream()
                .filter(attendance -> attendance.getTeachingActivity() != null)
                .collect(Collectors.groupingBy(attendance -> attendance.getTeachingActivity().getSubject()))
                .entrySet().stream()
                .map(entry -> {
                    Subject subject = entry.getKey();
                    List<Attendance> subjectAttendances = entry.getValue();
                    
                    Map<AttendanceStatus, Long> statusCounts = subjectAttendances.stream()
                            .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
                    
                    long totalSessions = subjectAttendances.size();
                    long presentSessions = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
                    double attendanceRate = totalSessions > 0 ? (double) presentSessions / totalSessions * 100 : 0.0;
                    
                    return AttendanceReportResponse.AttendanceReportItem.builder()
                            .type("SUBJECT")
                            .entityId(subject.getId())
                            .entityName(subject.getName())
                            .entityCode(subject.getCode())
                            .totalSessions(totalSessions)
                            .presentSessions(presentSessions)
                            .absentSessions(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L))
                            .lateSessions(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L))
                            .excusedSessions(statusCounts.getOrDefault(AttendanceStatus.EXCUSED, 0L))
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateTeacherItems(List<Attendance> attendances) {
        return attendances.stream()
                .filter(attendance -> attendance.getTeachingActivity() != null)
                .collect(Collectors.groupingBy(attendance -> attendance.getTeachingActivity().getTeacher()))
                .entrySet().stream()
                .map(entry -> {
                    User teacher = entry.getKey();
                    List<Attendance> teacherAttendances = entry.getValue();
                    
                    Map<AttendanceStatus, Long> statusCounts = teacherAttendances.stream()
                            .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
                    
                    long totalSessions = teacherAttendances.size();
                    long presentSessions = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
                    double attendanceRate = totalSessions > 0 ? (double) presentSessions / totalSessions * 100 : 0.0;
                    
                    return AttendanceReportResponse.AttendanceReportItem.builder()
                            .type("TEACHER")
                            .entityId(teacher.getId())
                            .entityName(teacher.getFirstName() + " " + teacher.getLastName())
                            .entityCode(teacher.getUsername())
                            .totalSessions(totalSessions)
                            .presentSessions(presentSessions)
                            .absentSessions(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L))
                            .lateSessions(statusCounts.getOrDefault(AttendanceStatus.LATE, 0L))
                            .excusedSessions(statusCounts.getOrDefault(AttendanceStatus.EXCUSED, 0L))
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AttendanceReportResponse.AttendanceReportItem> generateGeneralItems(List<Attendance> attendances) {
        // For general reports, group by student
        return generateStudentItems(attendances);
    }

    // Additional helper methods would be implemented here...
    
    private String generateReportId() {
        return "RPT-" + System.currentTimeMillis();
    }
    
    private String generateReportDescription(AttendanceReportRequest request) {
        return String.format("Attendance report from %s to %s", 
                request.getStartDate(), request.getEndDate());
    }
    
    private Map<String, Object> buildFiltersMap(AttendanceReportRequest request) {
        Map<String, Object> filters = new HashMap<>();
        if (request.getStudentId() != null) filters.put("studentId", request.getStudentId());
        if (request.getClassRoomId() != null) filters.put("classRoomId", request.getClassRoomId());
        if (request.getTeacherId() != null) filters.put("teacherId", request.getTeacherId());
        if (request.getSubjectId() != null) filters.put("subjectId", request.getSubjectId());
        return filters;
    }

    // Placeholder implementations for remaining methods
    @Override
    public List<Map<String, Object>> generateAttendanceRateComparison(LocalDate startDate, LocalDate endDate, String compareBy) {
        return new ArrayList<>();
    }

    @Override
    public AttendanceReportResponse generateLateArrivalReport(LocalDate startDate, LocalDate endDate) {
        return AttendanceReportResponse.builder().build();
    }

    @Override
    public AttendanceReportResponse generateAttendanceFollowUpReport(LocalDate fromDate) {
        return AttendanceReportResponse.builder().build();
    }

    @Override
    public ByteArrayOutputStream exportAttendanceReportToExcel(AttendanceReportRequest request) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportDailyAttendanceToExcel(LocalDate date) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportMonthlyAttendanceToExcel(int year, int month) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportStudentAttendanceSummaryToExcel(Long studentId, LocalDate startDate, LocalDate endDate) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportClassAttendanceSummaryToExcel(Long classRoomId, LocalDate startDate, LocalDate endDate) {
        return new ByteArrayOutputStream();
    }

    @Override
    public Map<String, Object> generateAttendanceAnalytics(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public Page<Map<String, Object>> getAttendanceReports(AttendanceReportRequest request, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>());
    }

    @Override
    public AttendanceReportResponse generateCustomAttendanceReport(Map<String, Object> filters, LocalDate startDate, LocalDate endDate) {
        return AttendanceReportResponse.builder().build();
    }

    @Override
    public Map<String, Double> calculateAttendanceMetrics(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateAttendanceHeatmapData(LocalDate startDate, LocalDate endDate, String granularity) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAttendancePatternAnalysis(Long studentId, LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateClassComparisonReport(List<Long> classRoomIds, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAttendanceForecast(LocalDate startDate, LocalDate endDate, int forecastDays) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateAttendanceAlertReport(LocalDate startDate, LocalDate endDate, Map<String, Object> thresholds) {
        return new ArrayList<>();
    }

    // Helper method implementations
    private List<Map<String, Object>> generatePeriodSummary(List<Attendance> attendances, LocalDate startDate, LocalDate endDate, String period) {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> generateDailyTrends(List<Attendance> attendances) {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> generateWeeklyTrends(List<Attendance> attendances, LocalDate startDate) {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> generateMonthlyTrends(List<Attendance> attendances) {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> generateStudentTrends(List<Attendance> attendances) {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> generateClassTrends(List<Attendance> attendances) {
        return new ArrayList<>();
    }

    private AttendanceReportResponse.AttendanceReportItem buildStudentReportItem(Student student, Long absenceCount, LocalDate startDate, LocalDate endDate) {
        return AttendanceReportResponse.AttendanceReportItem.builder()
                .type("STUDENT")
                .entityId(student.getId())
                .entityName(student.getFirstName() + " " + student.getLastName())
                .entityCode(student.getStudentNumber())
                .absentSessions(absenceCount)
                .build();
    }

    private boolean hasPerfectAttendance(Student student, LocalDate startDate, LocalDate endDate) {
        List<Attendance> studentAttendances = attendanceRepository.findByStudentAndAttendanceDateBetween(student, startDate, endDate);
        return studentAttendances.stream().allMatch(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT);
    }
}
