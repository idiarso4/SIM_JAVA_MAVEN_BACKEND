package com.school.sim.service.impl;

import com.school.sim.dto.request.AcademicReportRequest;
import com.school.sim.dto.response.AcademicReportResponse;
import com.school.sim.dto.response.TranscriptResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.repository.*;
import com.school.sim.service.AcademicReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AcademicReportService
 * Provides comprehensive academic reporting and analytics functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicReportServiceImpl implements AcademicReportService {

    private final AssessmentRepository assessmentRepository;
    private final StudentAssessmentRepository studentAssessmentRepository;
    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    // Grade scale constants
    private static final BigDecimal GRADE_A_MIN = new BigDecimal("90");
    private static final BigDecimal GRADE_B_MIN = new BigDecimal("80");
    private static final BigDecimal GRADE_C_MIN = new BigDecimal("70");
    private static final BigDecimal GRADE_D_MIN = new BigDecimal("60");
    private static final BigDecimal PASSING_GRADE = new BigDecimal("60");

    @Override
    @Cacheable(value = "academicReports", key = "#request.hashCode()")
    public AcademicReportResponse generateAcademicReport(AcademicReportRequest request) {
        log.info("Generating academic report for academic year: {}, semester: {}", 
                request.getAcademicYear(), request.getSemester());

        List<Assessment> assessments = getFilteredAssessments(request);
        AcademicReportResponse.AcademicStatistics statistics = calculateAcademicStatistics(assessments, request);
        List<AcademicReportResponse.AcademicReportItem> items = generateAcademicReportItems(assessments, request);
        List<AcademicReportResponse.StudentRanking> rankings = generateRankingsFromAssessments(assessments, request);

        return AcademicReportResponse.builder()
                .reportId(generateReportId())
                .reportType(request.getReportType() != null ? request.getReportType() : "GENERAL")
                .title("Academic Report")
                .description(generateReportDescription(request))
                .academicYear(request.getAcademicYear())
                .semester(request.getSemester())
                .generatedAt(LocalDateTime.now())
                .generatedBy("System")
                .statistics(statistics)
                .items(items)
                .rankings(rankings)
                .totalRecords(items.size())
                .filters(buildFiltersMap(request))
                .build();
    }

    @Override
    @Cacheable(value = "transcripts", key = "#studentId + '-' + #academicYear + '-' + #semester")
    public TranscriptResponse generateStudentTranscript(Long studentId, String academicYear, Integer semester) {
        log.info("Generating transcript for student: {}, period: {}/{}", studentId, academicYear, semester);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Assessment> assessments = assessmentRepository.findByClassRoomAndAcademicYearAndSemester(
                student.getClassRoom(), academicYear, semester);

        TranscriptResponse.StudentInfo studentInfo = buildStudentInfo(student);
        List<TranscriptResponse.AcademicPeriod> academicPeriods = buildAcademicPeriods(student, academicYear, semester);
        TranscriptResponse.TranscriptSummary summary = buildTranscriptSummary(student, academicYear, semester);

        return TranscriptResponse.builder()
                .student(studentInfo)
                .academicPeriods(academicPeriods)
                .summary(summary)
                .generatedAt(LocalDateTime.now())
                .generatedBy("System")
                .transcriptId(generateTranscriptId(studentId, academicYear, semester))
                .build();
    }

    @Override
    public TranscriptResponse generateCompleteStudentTranscript(Long studentId) {
        log.info("Generating complete transcript for student: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Get all assessments for the student
        List<Assessment> allAssessments = assessmentRepository.findByClassRoom(student.getClassRoom());
        
        // Group by academic year and semester
        Map<String, Map<Integer, List<Assessment>>> assessmentsByPeriod = allAssessments.stream()
                .collect(Collectors.groupingBy(
                        Assessment::getAcademicYear,
                        Collectors.groupingBy(Assessment::getSemester)
                ));

        TranscriptResponse.StudentInfo studentInfo = buildStudentInfo(student);
        List<TranscriptResponse.AcademicPeriod> academicPeriods = buildCompleteAcademicPeriods(student, assessmentsByPeriod);
        TranscriptResponse.TranscriptSummary summary = buildCompleteTranscriptSummary(student);

        return TranscriptResponse.builder()
                .student(studentInfo)
                .academicPeriods(academicPeriods)
                .summary(summary)
                .generatedAt(LocalDateTime.now())
                .generatedBy("System")
                .transcriptId(generateTranscriptId(studentId, "COMPLETE", 0))
                .build();
    }

    @Override
    public AcademicReportResponse generateClassAcademicReport(Long classRoomId, String academicYear, Integer semester) {
        log.info("Generating class academic report for class: {}", classRoomId);

        AcademicReportRequest request = AcademicReportRequest.builder()
                .classRoomId(classRoomId)
                .academicYear(academicYear)
                .semester(semester)
                .reportType("CLASS")
                .build();

        return generateAcademicReport(request);
    }

    @Override
    public AcademicReportResponse generateSubjectPerformanceReport(Long subjectId, String academicYear, Integer semester) {
        log.info("Generating subject performance report for subject: {}", subjectId);

        AcademicReportRequest request = AcademicReportRequest.builder()
                .subjectId(subjectId)
                .academicYear(academicYear)
                .semester(semester)
                .reportType("SUBJECT")
                .build();

        return generateAcademicReport(request);
    }

    @Override
    public AcademicReportResponse generateTeacherPerformanceReport(Long teacherId, String academicYear, Integer semester) {
        log.info("Generating teacher performance report for teacher: {}", teacherId);

        AcademicReportRequest request = AcademicReportRequest.builder()
                .teacherId(teacherId)
                .academicYear(academicYear)
                .semester(semester)
                .reportType("TEACHER")
                .build();

        return generateAcademicReport(request);
    }

    @Override
    @Cacheable(value = "studentGPA", key = "#studentId + '-' + #academicYear + '-' + #semester")
    public BigDecimal calculateStudentGPA(Long studentId, String academicYear, Integer semester) {
        log.info("Calculating GPA for student: {}, period: {}/{}", studentId, academicYear, semester);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Assessment> assessments = assessmentRepository.findByClassRoomAndAcademicYearAndSemester(
                student.getClassRoom(), academicYear, semester);

        Map<Subject, List<StudentAssessment>> subjectGrades = assessments.stream()
                .collect(Collectors.groupingBy(
                        Assessment::getSubject,
                        Collectors.flatMapping(
                                assessment -> studentAssessmentRepository.findByAssessmentAndStudent(assessment, student).stream(),
                                Collectors.toList()
                        )
                ));

        BigDecimal totalWeightedGrade = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (Map.Entry<Subject, List<StudentAssessment>> entry : subjectGrades.entrySet()) {
            BigDecimal subjectGrade = calculateSubjectFinalGrade(entry.getValue());
            if (subjectGrade != null) {
                // Assuming each subject has equal weight for GPA calculation
                totalWeightedGrade = totalWeightedGrade.add(subjectGrade);
                totalWeight = totalWeight.add(BigDecimal.ONE);
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalWeightedGrade.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Cacheable(value = "cumulativeGPA", key = "#studentId")
    public BigDecimal calculateCumulativeGPA(Long studentId) {
        log.info("Calculating cumulative GPA for student: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Assessment> allAssessments = assessmentRepository.findByClassRoom(student.getClassRoom());
        
        Map<String, Map<Integer, List<Assessment>>> assessmentsByPeriod = allAssessments.stream()
                .collect(Collectors.groupingBy(
                        Assessment::getAcademicYear,
                        Collectors.groupingBy(Assessment::getSemester)
                ));

        BigDecimal totalGPA = BigDecimal.ZERO;
        int periodCount = 0;

        for (Map.Entry<String, Map<Integer, List<Assessment>>> yearEntry : assessmentsByPeriod.entrySet()) {
            for (Map.Entry<Integer, List<Assessment>> semesterEntry : yearEntry.getValue().entrySet()) {
                BigDecimal periodGPA = calculatePeriodGPA(student, semesterEntry.getValue());
                if (periodGPA.compareTo(BigDecimal.ZERO) > 0) {
                    totalGPA = totalGPA.add(periodGPA);
                    periodCount++;
                }
            }
        }

        if (periodCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalGPA.divide(BigDecimal.valueOf(periodCount), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Cacheable(value = "classAverage", key = "#classRoomId + '-' + #academicYear + '-' + #semester")
    public BigDecimal calculateClassAverage(Long classRoomId, String academicYear, Integer semester) {
        log.info("Calculating class average for class: {}, period: {}/{}", classRoomId, academicYear, semester);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<Student> students = studentRepository.findByClassRoom(classRoom);
        List<BigDecimal> studentGPAs = students.stream()
                .map(student -> calculateStudentGPA(student.getId(), academicYear, semester))
                .filter(gpa -> gpa.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (studentGPAs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = studentGPAs.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(studentGPAs.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<AcademicReportResponse.StudentRanking> generateStudentRankings(Long classRoomId, String academicYear, Integer semester) {
        log.info("Generating student rankings for class: {}, period: {}/{}", classRoomId, academicYear, semester);

        ClassRoom classRoom = classRoomRepository.findById(classRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom not found with id: " + classRoomId));

        List<Student> students = studentRepository.findByClassRoom(classRoom);

        List<AcademicReportResponse.StudentRanking> rankings = students.stream()
                .map(student -> {
                    BigDecimal gpa = calculateStudentGPA(student.getId(), academicYear, semester);
                    return AcademicReportResponse.StudentRanking.builder()
                            .studentId(student.getId())
                            .studentName(student.getFirstName() + " " + student.getLastName())
                            .studentNumber(student.getStudentNumber())
                            .className(student.getClassRoom().getName())
                            .overallGrade(gpa)
                            .gpa(gpa)
                            .letterGrade(convertToLetterGrade(gpa))
                            .build();
                })
                .filter(ranking -> ranking.getGpa().compareTo(BigDecimal.ZERO) > 0)
                .sorted((r1, r2) -> r2.getGpa().compareTo(r1.getGpa()))
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    @Override
    @Cacheable(value = "gradeDistribution", key = "#classRoomId + '-' + #academicYear + '-' + #semester")
    public Map<String, Object> generateGradeDistributionAnalysis(Long classRoomId, String academicYear, Integer semester) {
        log.info("Generating grade distribution analysis for class: {}", classRoomId);

        List<AcademicReportResponse.StudentRanking> rankings = generateStudentRankings(classRoomId, academicYear, semester);

        Map<String, Long> gradeDistribution = rankings.stream()
                .collect(Collectors.groupingBy(
                        AcademicReportResponse.StudentRanking::getLetterGrade,
                        Collectors.counting()
                ));

        long totalStudents = rankings.size();
        Map<String, Double> gradePercentages = gradeDistribution.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> totalStudents > 0 ? (double) entry.getValue() / totalStudents * 100 : 0.0
                ));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalStudents", totalStudents);
        analysis.put("gradeDistribution", gradeDistribution);
        analysis.put("gradePercentages", gradePercentages);
        analysis.put("averageGPA", rankings.stream()
                .map(AcademicReportResponse.StudentRanking::getGpa)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(totalStudents, 1)), 2, RoundingMode.HALF_UP));

        return analysis;
    }

    @Override
    public Map<String, Object> calculatePassFailRates(Long classRoomId, String academicYear, Integer semester) {
        log.info("Calculating pass/fail rates for class: {}", classRoomId);

        List<AcademicReportResponse.StudentRanking> rankings = generateStudentRankings(classRoomId, academicYear, semester);

        long totalStudents = rankings.size();
        long passedStudents = rankings.stream()
                .filter(ranking -> ranking.getGpa().compareTo(PASSING_GRADE) >= 0)
                .count();
        long failedStudents = totalStudents - passedStudents;

        double passRate = totalStudents > 0 ? (double) passedStudents / totalStudents * 100 : 0.0;
        double failRate = totalStudents > 0 ? (double) failedStudents / totalStudents * 100 : 0.0;

        Map<String, Object> rates = new HashMap<>();
        rates.put("totalStudents", totalStudents);
        rates.put("passedStudents", passedStudents);
        rates.put("failedStudents", failedStudents);
        rates.put("passRate", passRate);
        rates.put("failRate", failRate);

        return rates;
    }

    // Helper methods
    private List<Assessment> getFilteredAssessments(AcademicReportRequest request) {
        List<Assessment> assessments = assessmentRepository.findByAcademicYearAndSemester(
                request.getAcademicYear(), request.getSemester());

        return assessments.stream()
                .filter(assessment -> {
                    if (request.getClassRoomId() != null && 
                        !assessment.getClassRoom().getId().equals(request.getClassRoomId())) {
                        return false;
                    }
                    if (request.getSubjectId() != null && 
                        !assessment.getSubject().getId().equals(request.getSubjectId())) {
                        return false;
                    }
                    if (request.getTeacherId() != null && 
                        !assessment.getTeacher().getId().equals(request.getTeacherId())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private AcademicReportResponse.AcademicStatistics calculateAcademicStatistics(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        if (assessments.isEmpty()) {
            return AcademicReportResponse.AcademicStatistics.builder()
                    .totalAssessments(0L)
                    .overallAverage(BigDecimal.ZERO)
                    .build();
        }

        // Get all student assessments for these assessments
        List<StudentAssessment> studentAssessments = assessments.stream()
                .flatMap(assessment -> studentAssessmentRepository.findByAssessment(assessment).stream())
                .filter(sa -> sa.getScore() != null)
                .collect(Collectors.toList());

        if (studentAssessments.isEmpty()) {
            return AcademicReportResponse.AcademicStatistics.builder()
                    .totalAssessments((long) assessments.size())
                    .overallAverage(BigDecimal.ZERO)
                    .build();
        }

        // Calculate statistics
        List<BigDecimal> scores = studentAssessments.stream()
                .map(StudentAssessment::getScore)
                .collect(Collectors.toList());

        BigDecimal totalScore = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageScore = totalScore.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        BigDecimal highestScore = scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowestScore = scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        // Calculate pass/fail rates
        long passedCount = scores.stream().filter(score -> score.compareTo(PASSING_GRADE) >= 0).count();
        long failedCount = scores.size() - passedCount;
        long excellentCount = scores.stream().filter(score -> score.compareTo(GRADE_A_MIN) >= 0).count();

        double passRate = scores.size() > 0 ? (double) passedCount / scores.size() * 100 : 0.0;
        double failureRate = scores.size() > 0 ? (double) failedCount / scores.size() * 100 : 0.0;
        double excellenceRate = scores.size() > 0 ? (double) excellentCount / scores.size() * 100 : 0.0;

        // Grade breakdown
        Map<String, Long> gradeBreakdown = scores.stream()
                .collect(Collectors.groupingBy(
                        this::convertToLetterGrade,
                        Collectors.counting()
                ));

        return AcademicReportResponse.AcademicStatistics.builder()
                .totalStudents((long) studentAssessments.stream()
                        .map(StudentAssessment::getStudent)
                        .distinct()
                        .count())
                .totalAssessments((long) assessments.size())
                .overallAverage(averageScore)
                .highestGrade(highestScore)
                .lowestGrade(lowestScore)
                .passRate(passRate)
                .failureRate(failureRate)
                .excellenceRate(excellenceRate)
                .gradeBreakdown(gradeBreakdown)
                .build();
    }

    private List<AcademicReportResponse.AcademicReportItem> generateAcademicReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        String reportType = request.getReportType() != null ? request.getReportType() : "GENERAL";
        
        switch (reportType.toUpperCase()) {
            case "STUDENT":
                return generateStudentReportItems(assessments, request);
            case "CLASS":
                return generateClassReportItems(assessments, request);
            case "SUBJECT":
                return generateSubjectReportItems(assessments, request);
            case "TEACHER":
                return generateTeacherReportItems(assessments, request);
            default:
                return generateGeneralReportItems(assessments, request);
        }
    }

    private List<AcademicReportResponse.AcademicReportItem> generateStudentReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        // Group assessments by student
        Map<Student, List<Assessment>> assessmentsByStudent = new HashMap<>();
        
        for (Assessment assessment : assessments) {
            List<Student> students = studentRepository.findByClassRoom(assessment.getClassRoom());
            for (Student student : students) {
                assessmentsByStudent.computeIfAbsent(student, k -> new ArrayList<>()).add(assessment);
            }
        }

        return assessmentsByStudent.entrySet().stream()
                .map(entry -> {
                    Student student = entry.getKey();
                    List<Assessment> studentAssessments = entry.getValue();
                    
                    BigDecimal overallGrade = calculateStudentOverallGrade(student, studentAssessments);
                    
                    return AcademicReportResponse.AcademicReportItem.builder()
                            .type("STUDENT")
                            .entityId(student.getId())
                            .entityName(student.getFirstName() + " " + student.getLastName())
                            .entityCode(student.getStudentNumber())
                            .overallGrade(overallGrade)
                            .letterGrade(convertToLetterGrade(overallGrade))
                            .gpa(overallGrade)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AcademicReportResponse.AcademicReportItem> generateClassReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        return assessments.stream()
                .collect(Collectors.groupingBy(Assessment::getClassRoom))
                .entrySet().stream()
                .map(entry -> {
                    ClassRoom classRoom = entry.getKey();
                    List<Assessment> classAssessments = entry.getValue();
                    
                    BigDecimal classAverage = calculateClassAverageFromAssessments(classRoom, classAssessments);
                    
                    return AcademicReportResponse.AcademicReportItem.builder()
                            .type("CLASS")
                            .entityId(classRoom.getId())
                            .entityName(classRoom.getName())
                            .entityCode(classRoom.getCode())
                            .overallGrade(classAverage)
                            .letterGrade(convertToLetterGrade(classAverage))
                            .gpa(classAverage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AcademicReportResponse.AcademicReportItem> generateSubjectReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        return assessments.stream()
                .collect(Collectors.groupingBy(Assessment::getSubject))
                .entrySet().stream()
                .map(entry -> {
                    Subject subject = entry.getKey();
                    List<Assessment> subjectAssessments = entry.getValue();
                    
                    BigDecimal subjectAverage = calculateSubjectAverageFromAssessments(subjectAssessments);
                    
                    return AcademicReportResponse.AcademicReportItem.builder()
                            .type("SUBJECT")
                            .entityId(subject.getId())
                            .entityName(subject.getName())
                            .entityCode(subject.getCode())
                            .overallGrade(subjectAverage)
                            .letterGrade(convertToLetterGrade(subjectAverage))
                            .gpa(subjectAverage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AcademicReportResponse.AcademicReportItem> generateTeacherReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        return assessments.stream()
                .collect(Collectors.groupingBy(Assessment::getTeacher))
                .entrySet().stream()
                .map(entry -> {
                    User teacher = entry.getKey();
                    List<Assessment> teacherAssessments = entry.getValue();
                    
                    BigDecimal teacherAverage = calculateTeacherAverageFromAssessments(teacherAssessments);
                    
                    return AcademicReportResponse.AcademicReportItem.builder()
                            .type("TEACHER")
                            .entityId(teacher.getId())
                            .entityName(teacher.getFirstName() + " " + teacher.getLastName())
                            .entityCode(teacher.getUsername())
                            .overallGrade(teacherAverage)
                            .letterGrade(convertToLetterGrade(teacherAverage))
                            .gpa(teacherAverage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AcademicReportResponse.AcademicReportItem> generateGeneralReportItems(
            List<Assessment> assessments, AcademicReportRequest request) {
        // For general reports, group by student
        return generateStudentReportItems(assessments, request);
    }

    private List<AcademicReportResponse.StudentRanking> generateRankingsFromAssessments(
            List<Assessment> assessments, AcademicReportRequest request) {
        
        if (request.getClassRoomId() != null) {
            return generateStudentRankings(request.getClassRoomId(), request.getAcademicYear(), request.getSemester());
        }
        
        return new ArrayList<>();
    }

    private String convertToLetterGrade(BigDecimal score) {
        if (score.compareTo(GRADE_A_MIN) >= 0) return "A";
        if (score.compareTo(GRADE_B_MIN) >= 0) return "B";
        if (score.compareTo(GRADE_C_MIN) >= 0) return "C";
        if (score.compareTo(GRADE_D_MIN) >= 0) return "D";
        return "F";
    }

    private BigDecimal calculateSubjectFinalGrade(List<StudentAssessment> studentAssessments) {
        if (studentAssessments.isEmpty()) {
            return null;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (StudentAssessment sa : studentAssessments) {
            if (sa.getScore() != null) {
                BigDecimal weight = sa.getAssessment().getWeight();
                BigDecimal weightedScore = sa.getScore().multiply(weight);
                totalWeightedScore = totalWeightedScore.add(weightedScore);
                totalWeight = totalWeight.add(weight);
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePeriodGPA(Student student, List<Assessment> assessments) {
        Map<Subject, List<StudentAssessment>> subjectGrades = assessments.stream()
                .collect(Collectors.groupingBy(
                        Assessment::getSubject,
                        Collectors.flatMapping(
                                assessment -> studentAssessmentRepository.findByAssessmentAndStudent(assessment, student).stream(),
                                Collectors.toList()
                        )
                ));

        BigDecimal totalGrade = BigDecimal.ZERO;
        int subjectCount = 0;

        for (List<StudentAssessment> subjectAssessments : subjectGrades.values()) {
            BigDecimal subjectGrade = calculateSubjectFinalGrade(subjectAssessments);
            if (subjectGrade != null) {
                totalGrade = totalGrade.add(subjectGrade);
                subjectCount++;
            }
        }

        if (subjectCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalGrade.divide(BigDecimal.valueOf(subjectCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStudentOverallGrade(Student student, List<Assessment> assessments) {
        return calculatePeriodGPA(student, assessments);
    }

    private BigDecimal calculateClassAverageFromAssessments(ClassRoom classRoom, List<Assessment> assessments) {
        List<Student> students = studentRepository.findByClassRoom(classRoom);
        
        List<BigDecimal> studentGrades = students.stream()
                .map(student -> calculateStudentOverallGrade(student, assessments))
                .filter(grade -> grade.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (studentGrades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = studentGrades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(studentGrades.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSubjectAverageFromAssessments(List<Assessment> assessments) {
        List<StudentAssessment> allStudentAssessments = assessments.stream()
                .flatMap(assessment -> studentAssessmentRepository.findByAssessment(assessment).stream())
                .filter(sa -> sa.getScore() != null)
                .collect(Collectors.toList());

        if (allStudentAssessments.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = allStudentAssessments.stream()
                .map(StudentAssessment::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(allStudentAssessments.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTeacherAverageFromAssessments(List<Assessment> assessments) {
        return calculateSubjectAverageFromAssessments(assessments);
    }

    private TranscriptResponse.StudentInfo buildStudentInfo(Student student) {
        return TranscriptResponse.StudentInfo.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .studentNumber(student.getStudentNumber())
                .className(student.getClassRoom().getName())
                .majorName(student.getClassRoom().getMajor().getName())
                .departmentName(student.getClassRoom().getMajor().getDepartment().getName())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus().toString())
                .build();
    }

    private List<TranscriptResponse.AcademicPeriod> buildAcademicPeriods(Student student, String academicYear, Integer semester) {
        List<Assessment> assessments = assessmentRepository.findByClassRoomAndAcademicYearAndSemester(
                student.getClassRoom(), academicYear, semester);

        TranscriptResponse.AcademicPeriod period = buildAcademicPeriod(student, academicYear, semester, assessments);
        return Arrays.asList(period);
    }

    private List<TranscriptResponse.AcademicPeriod> buildCompleteAcademicPeriods(
            Student student, Map<String, Map<Integer, List<Assessment>>> assessmentsByPeriod) {
        
        List<TranscriptResponse.AcademicPeriod> periods = new ArrayList<>();
        
        for (Map.Entry<String, Map<Integer, List<Assessment>>> yearEntry : assessmentsByPeriod.entrySet()) {
            for (Map.Entry<Integer, List<Assessment>> semesterEntry : yearEntry.getValue().entrySet()) {
                TranscriptResponse.AcademicPeriod period = buildAcademicPeriod(
                        student, yearEntry.getKey(), semesterEntry.getKey(), semesterEntry.getValue());
                periods.add(period);
            }
        }
        
        return periods;
    }

    private TranscriptResponse.AcademicPeriod buildAcademicPeriod(
            Student student, String academicYear, Integer semester, List<Assessment> assessments) {
        
        Map<Subject, List<Assessment>> assessmentsBySubject = assessments.stream()
                .collect(Collectors.groupingBy(Assessment::getSubject));

        List<TranscriptResponse.SubjectRecord> subjectRecords = assessmentsBySubject.entrySet().stream()
                .map(entry -> buildSubjectRecord(student, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        BigDecimal semesterGPA = calculatePeriodGPA(student, assessments);

        return TranscriptResponse.AcademicPeriod.builder()
                .academicYear(academicYear)
                .semester(semester)
                .subjects(subjectRecords)
                .semesterGPA(semesterGPA)
                .cumulativeGPA(calculateCumulativeGPA(student.getId()))
                .totalCredits(subjectRecords.size() * 3) // Assuming 3 credits per subject
                .earnedCredits(subjectRecords.size() * 3) // Simplified calculation
                .status("COMPLETED")
                .build();
    }

    private TranscriptResponse.SubjectRecord buildSubjectRecord(Student student, Subject subject, List<Assessment> assessments) {
        List<StudentAssessment> studentAssessments = assessments.stream()
                .flatMap(assessment -> studentAssessmentRepository.findByAssessmentAndStudent(assessment, student).stream())
                .collect(Collectors.toList());

        BigDecimal finalGrade = calculateSubjectFinalGrade(studentAssessments);
        String letterGrade = finalGrade != null ? convertToLetterGrade(finalGrade) : "I";
        String status = finalGrade != null && finalGrade.compareTo(PASSING_GRADE) >= 0 ? "PASSED" : "FAILED";

        List<TranscriptResponse.AssessmentRecord> assessmentRecords = studentAssessments.stream()
                .map(this::buildAssessmentRecord)
                .collect(Collectors.toList());

        return TranscriptResponse.SubjectRecord.builder()
                .subjectCode(subject.getCode())
                .subjectName(subject.getName())
                .credits(3) // Assuming 3 credits per subject
                .finalGrade(finalGrade != null ? finalGrade : BigDecimal.ZERO)
                .letterGrade(letterGrade)
                .status(status)
                .assessments(assessmentRecords)
                .build();
    }

    private TranscriptResponse.AssessmentRecord buildAssessmentRecord(StudentAssessment studentAssessment) {
        Assessment assessment = studentAssessment.getAssessment();
        BigDecimal percentage = studentAssessment.getScore() != null && assessment.getMaxScore().compareTo(BigDecimal.ZERO) > 0 ?
                studentAssessment.getScore().divide(assessment.getMaxScore(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

        return TranscriptResponse.AssessmentRecord.builder()
                .title(assessment.getTitle())
                .type(assessment.getType().toString())
                .score(studentAssessment.getScore())
                .maxScore(assessment.getMaxScore())
                .percentage(percentage)
                .grade(studentAssessment.getGrade())
                .weight(assessment.getWeight())
                .assessmentDate(assessment.getDueDate())
                .build();
    }

    private TranscriptResponse.TranscriptSummary buildTranscriptSummary(Student student, String academicYear, Integer semester) {
        BigDecimal gpa = calculateStudentGPA(student.getId(), academicYear, semester);
        
        return TranscriptResponse.TranscriptSummary.builder()
                .overallGPA(gpa)
                .totalCreditsAttempted(15) // Simplified calculation
                .totalCreditsEarned(15) // Simplified calculation
                .completionRate(new BigDecimal("100"))
                .academicStanding(determineAcademicStanding(gpa))
                .honors(new ArrayList<>())
                .remarks(new ArrayList<>())
                .build();
    }

    private TranscriptResponse.TranscriptSummary buildCompleteTranscriptSummary(Student student) {
        BigDecimal cumulativeGPA = calculateCumulativeGPA(student.getId());
        
        return TranscriptResponse.TranscriptSummary.builder()
                .overallGPA(cumulativeGPA)
                .totalCreditsAttempted(60) // Simplified calculation
                .totalCreditsEarned(60) // Simplified calculation
                .completionRate(new BigDecimal("100"))
                .academicStanding(determineAcademicStanding(cumulativeGPA))
                .honors(new ArrayList<>())
                .remarks(new ArrayList<>())
                .build();
    }

    private String determineAcademicStanding(BigDecimal gpa) {
        if (gpa.compareTo(new BigDecimal("3.5")) >= 0) return "EXCELLENT";
        if (gpa.compareTo(new BigDecimal("3.0")) >= 0) return "GOOD";
        if (gpa.compareTo(new BigDecimal("2.0")) >= 0) return "SATISFACTORY";
        return "PROBATION";
    }

    private String generateReportId() {
        return "AR-" + System.currentTimeMillis();
    }

    private String generateTranscriptId(Long studentId, String academicYear, Integer semester) {
        return String.format("TR-%d-%s-%d-%d", studentId, academicYear, semester, System.currentTimeMillis());
    }

    private String generateReportDescription(AcademicReportRequest request) {
        return String.format("Academic report for %s semester %d", request.getAcademicYear(), request.getSemester());
    }

    private Map<String, Object> buildFiltersMap(AcademicReportRequest request) {
        Map<String, Object> filters = new HashMap<>();
        if (request.getStudentId() != null) filters.put("studentId", request.getStudentId());
        if (request.getClassRoomId() != null) filters.put("classRoomId", request.getClassRoomId());
        if (request.getSubjectId() != null) filters.put("subjectId", request.getSubjectId());
        if (request.getTeacherId() != null) filters.put("teacherId", request.getTeacherId());
        return filters;
    }

    // Placeholder implementations for remaining methods
    @Override
    public BigDecimal calculateSubjectAverage(Long subjectId, String academicYear, Integer semester) {
        return BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> generateSubjectRankings(String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> generateClassRankings(String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, BigDecimal> calculateWeightedFinalGrades(Long studentId, String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateProgressTrackingReport(Long studentId, String academicYear) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generatePerformanceTrends(Long classRoomId, String academicYear) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAcademicAnalyticsDashboard(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateHonorRollList(String academicYear, Integer semester, BigDecimal minimumGPA) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> generateDeansList(String academicYear, Integer semester, BigDecimal minimumGPA) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> generateAcademicProbationList(String academicYear, Integer semester, BigDecimal maximumGPA) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> calculateCreditCompletionRates(Long studentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateGraduationEligibilityReport(Long studentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateAcademicStandingReport(Long studentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateSemesterStatistics(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateComparativeAnalysis(List<Long> classRoomIds, String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateImprovementRecommendations(Long studentId, String academicYear, Integer semester) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> calculateGPAByPeriod(Long studentId) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> generateAcademicCalendarPerformance(String academicYear) {
        return new HashMap<>();
    }

    @Override
    public ByteArrayOutputStream exportAcademicReportToExcel(AcademicReportRequest request) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportTranscriptToPDF(Long studentId, String academicYear, Integer semester) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportClassReportToExcel(Long classRoomId, String academicYear, Integer semester) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportRankingsToExcel(Long classRoomId, String academicYear, Integer semester) {
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream exportGradeDistributionToExcel(String academicYear, Integer semester) {
        return new ByteArrayOutputStream();
    }

    @Override
    public Page<Map<String, Object>> getAcademicReports(AcademicReportRequest request, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>());
    }

    @Override
    public AcademicReportResponse generateCustomAcademicReport(Map<String, Object> criteria) {
        return AcademicReportResponse.builder().build();
    }

    @Override
    public Map<String, Object> calculateAcademicMetrics(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generatePerformanceBenchmarks(Long classRoomId, String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateAcademicQualityIndicators(String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateRetentionRates(String academicYear) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateAcademicSuccessPredictors(Long studentId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateCurriculumEffectivenessReport(Long subjectId, String academicYear) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> calculateLearningOutcomesAssessment(Long classRoomId, String academicYear, Integer semester) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateAcademicInterventionRecommendations(String academicYear, Integer semester) {
        return new ArrayList<>();
    }
}
