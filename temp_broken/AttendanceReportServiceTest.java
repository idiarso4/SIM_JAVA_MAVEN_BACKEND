package com.school.sim.service;

import com.school.sim.dto.request.AttendanceReportRequest;
import com.school.sim.dto.response.AttendanceReportResponse;
import com.school.sim.entity.*;
import com.school.sim.repository.*;
import com.school.sim.service.impl.AttendanceReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AttendanceReportService
 */
@ExtendWith(MockitoExtension.class)
class AttendanceReportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassRoomRepository classRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ExcelService excelService;

    @InjectMocks
    private AttendanceReportServiceImpl attendanceReportService;

    private Student testStudent;
    private ClassRoom testClassRoom;
    private User testTeacher;
    private Subject testSubject;
    private List<Attendance> testAttendances;

    @BeforeEach
    void setUp() {
        // Set up test data
        testClassRoom = ClassRoom.builder()
                .id(1L)
                .name("Class A")
                .code("CLA")
                .build();

        testStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentNumber("STU001")
                .classRoom(testClassRoom)
                .build();

        testTeacher = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .username("teacher1")
                .build();

        testSubject = Subject.builder()
                .id(1L)
                .name("Mathematics")
                .code("MATH")
                .build();

        // Create test attendance records
        testAttendances = Arrays.asList(
                createAttendance(1L, testStudent, AttendanceStatus.PRESENT, LocalDate.now().minusDays(2)),
                createAttendance(2L, testStudent, AttendanceStatus.ABSENT, LocalDate.now().minusDays(1)),
                createAttendance(3L, testStudent, AttendanceStatus.PRESENT, LocalDate.now())
        );
    }

    @Test
    void generateAttendanceReport_ShouldReturnValidReport() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .reportType("GENERAL")
                .build();

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        AttendanceReportResponse response = attendanceReportService.generateAttendanceReport(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("GENERAL");
        assertThat(response.getStartDate()).isEqualTo(startDate);
        assertThat(response.getEndDate()).isEqualTo(endDate);
        assertThat(response.getStatistics()).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getTotalRecords()).isEqualTo(1); // One student
    }

    @Test
    void generateStudentAttendanceReport_ShouldReturnStudentSpecificReport() {
        // Given
        Long studentId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        AttendanceReportResponse response = attendanceReportService.generateStudentAttendanceReport(
                studentId, startDate, endDate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("STUDENT");
        assertThat(response.getItems()).hasSize(1);
        
        AttendanceReportResponse.AttendanceReportItem item = response.getItems().get(0);
        assertThat(item.getType()).isEqualTo("STUDENT");
        assertThat(item.getEntityName()).isEqualTo("John Doe");
        assertThat(item.getTotalSessions()).isEqualTo(3);
        assertThat(item.getPresentSessions()).isEqualTo(2);
        assertThat(item.getAbsentSessions()).isEqualTo(1);
    }

    @Test
    void generateClassAttendanceReport_ShouldReturnClassSpecificReport() {
        // Given
        Long classRoomId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        AttendanceReportResponse response = attendanceReportService.generateClassAttendanceReport(
                classRoomId, startDate, endDate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("CLASS");
        assertThat(response.getItems()).hasSize(1);
        
        AttendanceReportResponse.AttendanceReportItem item = response.getItems().get(0);
        assertThat(item.getType()).isEqualTo("CLASS");
        assertThat(item.getEntityName()).isEqualTo("Class A");
        assertThat(item.getTotalSessions()).isEqualTo(3);
    }

    @Test
    void generateDailyAttendanceSummary_ShouldReturnDailySummary() {
        // Given
        LocalDate date = LocalDate.now();
        List<Attendance> dailyAttendances = Arrays.asList(
                createAttendance(1L, testStudent, AttendanceStatus.PRESENT, date)
        );

        when(attendanceRepository.findByAttendanceDateBetween(date, date))
                .thenReturn(dailyAttendances);

        // When
        List<Map<String, Object>> summary = attendanceReportService.generateDailyAttendanceSummary(date);

        // Then
        assertThat(summary).isNotEmpty();
        assertThat(summary.get(0)).containsKeys("className", "date", "statusBreakdown", "totalStudents");
    }

    @Test
    void generateWeeklyAttendanceSummary_ShouldReturnWeeklySummary() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        List<Map<String, Object>> summary = attendanceReportService.generateWeeklyAttendanceSummary(startDate);

        // Then
        assertThat(summary).isNotNull();
    }

    @Test
    void generateMonthlyAttendanceSummary_ShouldReturnMonthlySummary() {
        // Given
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        List<Map<String, Object>> summary = attendanceReportService.generateMonthlyAttendanceSummary(year, month);

        // Then
        assertThat(summary).isNotNull();
    }

    @Test
    void generateAttendanceStatisticsDashboard_ShouldReturnStatistics() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(testAttendances);

        // When
        Map<String, Object> dashboard = attendanceReportService.generateAttendanceStatisticsDashboard(startDate, endDate);

        // Then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard).containsKeys("totalRecords", "dateRange", "statusBreakdown", "dailyTrends", "classStatistics");
        assertThat(dashboard.get("totalRecords")).isEqualTo(3);
    }

    @Test
    void generateAbsenteeismReport_ShouldReturnAbsenteeismReport() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        int minAbsences = 1;

        List<Attendance> absences = Arrays.asList(
                createAttendance(2L, testStudent, AttendanceStatus.ABSENT, LocalDate.now().minusDays(1))
        );

        when(attendanceRepository.findByAttendanceDateBetweenAndStatus(startDate, endDate, AttendanceStatus.ABSENT))
                .thenReturn(absences);

        // When
        AttendanceReportResponse response = attendanceReportService.generateAbsenteeismReport(
                startDate, endDate, minAbsences);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("ABSENTEEISM");
        assertThat(response.getItems()).hasSize(1);
        
        AttendanceReportResponse.AttendanceReportItem item = response.getItems().get(0);
        assertThat(item.getAbsentSessions()).isEqualTo(1);
    }

    @Test
    void generatePerfectAttendanceReport_ShouldReturnPerfectAttendanceReport() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        // Create a student with perfect attendance
        Student perfectStudent = Student.builder()
                .id(2L)
                .firstName("Perfect")
                .lastName("Student")
                .studentNumber("STU002")
                .classRoom(testClassRoom)
                .build();

        List<Attendance> perfectAttendances = Arrays.asList(
                createAttendance(4L, perfectStudent, AttendanceStatus.PRESENT, LocalDate.now().minusDays(2)),
                createAttendance(5L, perfectStudent, AttendanceStatus.PRESENT, LocalDate.now().minusDays(1)),
                createAttendance(6L, perfectStudent, AttendanceStatus.PRESENT, LocalDate.now())
        );

        when(attendanceRepository.findByAttendanceDateBetween(startDate, endDate))
                .thenReturn(perfectAttendances);
        when(attendanceRepository.findByStudentAndAttendanceDateBetween(eq(perfectStudent), eq(startDate), eq(endDate)))
                .thenReturn(perfectAttendances);

        // When
        AttendanceReportResponse response = attendanceReportService.generatePerfectAttendanceReport(startDate, endDate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("PERFECT_ATTENDANCE");
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void calculateStatistics_WithEmptyList_ShouldReturnZeroStatistics() {
        // Given
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .build();

        when(attendanceRepository.findByAttendanceDateBetween(any(), any()))
                .thenReturn(Arrays.asList());

        // When
        AttendanceReportResponse response = attendanceReportService.generateAttendanceReport(request);

        // Then
        assertThat(response.getStatistics().getTotalAttendanceRecords()).isEqualTo(0);
        assertThat(response.getStatistics().getOverallAttendanceRate()).isEqualTo(0.0);
    }

    @Test
    void calculateStatistics_WithValidData_ShouldReturnCorrectStatistics() {
        // Given
        AttendanceReportRequest request = AttendanceReportRequest.builder()
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .build();

        when(attendanceRepository.findByAttendanceDateBetween(any(), any()))
                .thenReturn(testAttendances);

        // When
        AttendanceReportResponse response = attendanceReportService.generateAttendanceReport(request);

        // Then
        AttendanceReportResponse.AttendanceStatistics stats = response.getStatistics();
        assertThat(stats.getTotalAttendanceRecords()).isEqualTo(3);
        assertThat(stats.getPresentCount()).isEqualTo(2);
        assertThat(stats.getAbsentCount()).isEqualTo(1);
        assertThat(stats.getOverallAttendanceRate()).isEqualTo(66.66666666666666); // 2/3 * 100
        assertThat(stats.getAbsenteeismRate()).isEqualTo(33.33333333333333); // 1/3 * 100
    }

    // Helper method to create test attendance records
    private Attendance createAttendance(Long id, Student student, AttendanceStatus status, LocalDate date) {
        return Attendance.builder()
                .id(id)
                .student(student)
                .status(status)
                .attendanceDate(date)
                .recordedAt(LocalDateTime.now())
                .build();
    }
}
