package com.school.sim.service;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.TranscriptResponse;
import com.school.sim.entity.*;
import com.school.sim.repository.*;
import com.school.sim.service.impl.AcademicReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AcademicReportService
 */
@ExtendWith(MockitoExtension.class)
class AcademicReportServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private StudentAssessmentRepository studentAssessmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassRoomRepository classRoomRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AcademicReportServiceImpl academicReportService;

    private Student testStudent;
    private ClassRoom testClassRoom;
    private Subject testSubject;
    private User testTeacher;
    private Assessment testAssessment;
    private StudentAssessment testStudentAssessment;
    private Major testMajor;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Set up test data
        testDepartment = Department.builder()
                .id(1L)
                .name("Computer Science")
                .code("CS")
                .build();

        testMajor = Major.builder()
                .id(1L)
                .name("Software Engineering")
                .code("SE")
                .department(testDepartment)
                .build();

        testClassRoom = ClassRoom.builder()
                .id(1L)
                .name("Class A")
                .code("CLA")
                .major(testMajor)
                .build();

        testStudent = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .studentNumber("STU001")
                .classRoom(testClassRoom)
                .enrollmentDate(LocalDate.now().minusYears(1))
                .status(StudentStatus.ACTIVE)
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

        testAssessment = Assessment.builder()
                .id(1L)
                .title("Mid-term Exam")
                .type(AssessmentType.UTS)
                .subject(testSubject)
                .classRoom(testClassRoom)
                .teacher(testTeacher)
                .maxScore(new BigDecimal("100"))
                .weight(new BigDecimal("0.3"))
                .academicYear("2024/2025")
                .semester(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        testStudentAssessment = StudentAssessment.builder()
                .id(1L)
                .assessment(testAssessment)
                .student(testStudent)
                .score(new BigDecimal("85"))
                .grade("B")
                .isSubmitted(true)
                .gradedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateAcademicReport_ShouldReturnValidReport() {
        // Given
        AcademicReportRequest request = AcademicReportRequest.builder()
                .academicYear("2024/2025")
                .semester(1)
                .reportType("GENERAL")
                .build();

        when(assessmentRepository.findByAcademicYearAndSemester("2024/2025", 1))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessment(testAssessment))
                .thenReturn(Arrays.asList(testStudentAssessment));
        when(studentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testStudent));

        // When
        AcademicReportResponse response = academicReportService.generateAcademicReport(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("GENERAL");
        assertThat(response.getAcademicYear()).isEqualTo("2024/2025");
        assertThat(response.getSemester()).isEqualTo(1);
        assertThat(response.getStatistics()).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
    }

    @Test
    void generateStudentTranscript_ShouldReturnValidTranscript() {
        // Given
        Long studentId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        TranscriptResponse response = academicReportService.generateStudentTranscript(studentId, academicYear, semester);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStudent()).isNotNull();
        assertThat(response.getStudent().getStudentNumber()).isEqualTo("STU001");
        assertThat(response.getAcademicPeriods()).isNotEmpty();
        assertThat(response.getSummary()).isNotNull();
        assertThat(response.getTranscriptId()).isNotNull();
    }

    @Test
    void generateCompleteStudentTranscript_ShouldReturnCompleteTranscript() {
        // Given
        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(assessmentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        TranscriptResponse response = academicReportService.generateCompleteStudentTranscript(studentId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStudent()).isNotNull();
        assertThat(response.getAcademicPeriods()).isNotEmpty();
        assertThat(response.getSummary()).isNotNull();
        assertThat(response.getTranscriptId()).contains("COMPLETE");
    }

    @Test
    void calculateStudentGPA_ShouldReturnCorrectGPA() {
        // Given
        Long studentId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        BigDecimal gpa = academicReportService.calculateStudentGPA(studentId, academicYear, semester);

        // Then
        assertThat(gpa).isNotNull();
        assertThat(gpa).isGreaterThan(BigDecimal.ZERO);
        assertThat(gpa).isEqualTo(new BigDecimal("85.00"));
    }

    @Test
    void calculateCumulativeGPA_ShouldReturnCorrectCumulativeGPA() {
        // Given
        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(assessmentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        BigDecimal cumulativeGPA = academicReportService.calculateCumulativeGPA(studentId);

        // Then
        assertThat(cumulativeGPA).isNotNull();
        assertThat(cumulativeGPA).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void calculateClassAverage_ShouldReturnCorrectAverage() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(classRoomRepository.findById(classRoomId)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.findByClassRoom(testClassRoom)).thenReturn(Arrays.asList(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        BigDecimal average = academicReportService.calculateClassAverage(classRoomId, academicYear, semester);

        // Then
        assertThat(average).isNotNull();
        assertThat(average).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void generateStudentRankings_ShouldReturnRankings() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(classRoomRepository.findById(classRoomId)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.findByClassRoom(testClassRoom)).thenReturn(Arrays.asList(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        List<AcademicReportResponse.StudentRanking> rankings = academicReportService.generateStudentRankings(
                classRoomId, academicYear, semester);

        // Then
        assertThat(rankings).isNotEmpty();
        assertThat(rankings.get(0).getStudentName()).isEqualTo("John Doe");
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        assertThat(rankings.get(0).getLetterGrade()).isEqualTo("B");
    }

    @Test
    void generateGradeDistributionAnalysis_ShouldReturnDistribution() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(classRoomRepository.findById(classRoomId)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.findByClassRoom(testClassRoom)).thenReturn(Arrays.asList(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        Map<String, Object> distribution = academicReportService.generateGradeDistributionAnalysis(
                classRoomId, academicYear, semester);

        // Then
        assertThat(distribution).isNotEmpty();
        assertThat(distribution).containsKeys("totalStudents", "gradeDistribution", "gradePercentages", "averageGPA");
        assertThat(distribution.get("totalStudents")).isEqualTo(1L);
    }

    @Test
    void calculatePassFailRates_ShouldReturnRates() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(classRoomRepository.findById(classRoomId)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.findByClassRoom(testClassRoom)).thenReturn(Arrays.asList(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));

        // When
        Map<String, Object> rates = academicReportService.calculatePassFailRates(
                classRoomId, academicYear, semester);

        // Then
        assertThat(rates).isNotEmpty();
        assertThat(rates).containsKeys("totalStudents", "passedStudents", "failedStudents", "passRate", "failRate");
        assertThat(rates.get("totalStudents")).isEqualTo(1L);
        assertThat(rates.get("passedStudents")).isEqualTo(1L);
        assertThat(rates.get("passRate")).isEqualTo(100.0);
    }

    @Test
    void generateClassAcademicReport_ShouldReturnClassReport() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(assessmentRepository.findByAcademicYearAndSemester(academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessment(testAssessment))
                .thenReturn(Arrays.asList(testStudentAssessment));
        when(studentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testStudent));

        // When
        AcademicReportResponse response = academicReportService.generateClassAcademicReport(
                classRoomId, academicYear, semester);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("CLASS");
        assertThat(response.getAcademicYear()).isEqualTo(academicYear);
        assertThat(response.getSemester()).isEqualTo(semester);
    }

    @Test
    void generateSubjectPerformanceReport_ShouldReturnSubjectReport() {
        // Given
        Long subjectId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(assessmentRepository.findByAcademicYearAndSemester(academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessment(testAssessment))
                .thenReturn(Arrays.asList(testStudentAssessment));
        when(studentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testStudent));

        // When
        AcademicReportResponse response = academicReportService.generateSubjectPerformanceReport(
                subjectId, academicYear, semester);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("SUBJECT");
        assertThat(response.getAcademicYear()).isEqualTo(academicYear);
        assertThat(response.getSemester()).isEqualTo(semester);
    }

    @Test
    void generateTeacherPerformanceReport_ShouldReturnTeacherReport() {
        // Given
        Long teacherId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(assessmentRepository.findByAcademicYearAndSemester(academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessment(testAssessment))
                .thenReturn(Arrays.asList(testStudentAssessment));
        when(studentRepository.findByClassRoom(testClassRoom))
                .thenReturn(Arrays.asList(testStudent));

        // When
        AcademicReportResponse response = academicReportService.generateTeacherPerformanceReport(
                teacherId, academicYear, semester);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportType()).isEqualTo("TEACHER");
        assertThat(response.getAcademicYear()).isEqualTo(academicYear);
        assertThat(response.getSemester()).isEqualTo(semester);
    }

    @Test
    void calculateStudentGPA_WithNoAssessments_ShouldReturnZero() {
        // Given
        Long studentId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(testStudent));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList());

        // When
        BigDecimal gpa = academicReportService.calculateStudentGPA(studentId, academicYear, semester);

        // Then
        assertThat(gpa).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void generateStudentRankings_WithMultipleStudents_ShouldReturnCorrectRanking() {
        // Given
        Long classRoomId = 1L;
        String academicYear = "2024/2025";
        Integer semester = 1;

        Student student2 = Student.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .studentNumber("STU002")
                .classRoom(testClassRoom)
                .build();

        StudentAssessment studentAssessment2 = StudentAssessment.builder()
                .id(2L)
                .assessment(testAssessment)
                .student(student2)
                .score(new BigDecimal("95"))
                .grade("A")
                .isSubmitted(true)
                .build();

        when(classRoomRepository.findById(classRoomId)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.findByClassRoom(testClassRoom)).thenReturn(Arrays.asList(testStudent, student2));
        when(assessmentRepository.findByClassRoomAndAcademicYearAndSemester(testClassRoom, academicYear, semester))
                .thenReturn(Arrays.asList(testAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, testStudent))
                .thenReturn(Optional.of(testStudentAssessment));
        when(studentAssessmentRepository.findByAssessmentAndStudent(testAssessment, student2))
                .thenReturn(Optional.of(studentAssessment2));

        // When
        List<AcademicReportResponse.StudentRanking> rankings = academicReportService.generateStudentRankings(
                classRoomId, academicYear, semester);

        // Then
        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getStudentName()).isEqualTo("Jane Smith");
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        assertThat(rankings.get(1).getStudentName()).isEqualTo("John Doe");
        assertThat(rankings.get(1).getRank()).isEqualTo(2);
    }
}
