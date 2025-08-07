package com.school.sim.service;

import com.school.sim.dto.request.CreateExtracurricularAttendanceRequest;
import com.school.sim.dto.request.UpdateExtracurricularAttendanceRequest;
import com.school.sim.dto.response.ExtracurricularAttendanceResponse;
import com.school.sim.entity.*;
import com.school.sim.entity.ExtracurricularAttendance.AttendanceStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.impl.ExtracurricularAttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExtracurricularAttendanceService
 */
@ExtendWith(MockitoExtension.class)
class ExtracurricularAttendanceServiceTest {

    @Mock
    private ExtracurricularAttendanceRepository attendanceRepository;

    @Mock
    private ExtracurricularActivityRepository activityRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ExtracurricularAttendanceServiceImpl attendanceService;

    private ExtracurricularActivity testActivity;
    private Student testStudent;
    private ExtracurricularAttendance testAttendance;
    private CreateExtracurricularAttendanceRequest createRequest;
    private UpdateExtracurricularAttendanceRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        User supervisor = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .email("john.doe@school.com")
                .build();

        testActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Programming Club")
                .type(ActivityType.CLUB)
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .location("Room 101")
                .supervisor(supervisor)
                .isActive(true)
                .build();

        testStudent = Student.builder()
                .id(1L)
                .nis("STU001")
                .namaLengkap("Jane Smith")
                .build();

        testAttendance = ExtracurricularAttendance.builder()
                .id(1L)
                .activity(testActivity)
                .student(testStudent)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(16, 0))
                .status(AttendanceStatus.PRESENT)
                .participationScore(85)
                .performanceRating(4)
                .notes("Good participation")
                .isExcused(false)
                .lateArrivalMinutes(0)
                .earlyDepartureMinutes(0)
                .achievementPoints(10)
                .isActive(true)
                .build();

        createRequest = CreateExtracurricularAttendanceRequest.builder()
                .activityId(1L)
                .studentId(1L)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(16, 0))
                .status(AttendanceStatus.PRESENT)
                .participationScore(85)
                .performanceRating(4)
                .notes("Good participation")
                .isExcused(false)
                .lateArrivalMinutes(0)
                .earlyDepartureMinutes(0)
                .achievementPoints(10)
                .build();

        updateRequest = UpdateExtracurricularAttendanceRequest.builder()
                .participationScore(90)
                .performanceRating(5)
                .notes("Excellent participation")
                .achievementPoints(15)
                .build();
    }

    @Test
    void recordAttendance_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);
        when(attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
                testActivity, testStudent, LocalDate.now())).thenReturn(false);
        when(attendanceRepository.save(any(ExtracurricularAttendance.class))).thenReturn(testAttendance);

        // When
        ExtracurricularAttendanceResponse response = attendanceService.recordAttendance(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertEquals(85, response.getParticipationScore());
        assertEquals(4, response.getPerformanceRating());
        assertEquals("Good participation", response.getNotes());
        assertTrue(response.getIsPresent());
        assertTrue(response.getIsOnTime());

        verify(attendanceRepository).save(any(ExtracurricularAttendance.class));
    }

    @Test
    void recordAttendance_ActivityNotFound_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.recordAttendance(createRequest);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void recordAttendance_StudentNotFound_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.recordAttendance(createRequest);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void recordAttendance_StudentNotRegistered_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(false);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            attendanceService.recordAttendance(createRequest);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void recordAttendance_AttendanceAlreadyExists_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);
        when(attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
                testActivity, testStudent, LocalDate.now())).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            attendanceService.recordAttendance(createRequest);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void recordAttendance_LateArrival_Success() {
        // Given
        CreateExtracurricularAttendanceRequest lateRequest = CreateExtracurricularAttendanceRequest.builder()
                .activityId(1L)
                .studentId(1L)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(14, 15)) // 15 minutes late
                .checkOutTime(LocalTime.of(16, 0))
                .status(AttendanceStatus.LATE)
                .participationScore(75)
                .performanceRating(3)
                .lateArrivalMinutes(15)
                .build();

        ExtracurricularAttendance lateAttendance = ExtracurricularAttendance.builder()
                .id(2L)
                .activity(testActivity)
                .student(testStudent)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(14, 15))
                .checkOutTime(LocalTime.of(16, 0))
                .status(AttendanceStatus.LATE)
                .participationScore(75)
                .performanceRating(3)
                .lateArrivalMinutes(15)
                .isActive(true)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);
        when(attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
                testActivity, testStudent, LocalDate.now())).thenReturn(false);
        when(attendanceRepository.save(any(ExtracurricularAttendance.class))).thenReturn(lateAttendance);

        // When
        ExtracurricularAttendanceResponse response = attendanceService.recordAttendance(lateRequest);

        // Then
        assertNotNull(response);
        assertEquals(AttendanceStatus.LATE, response.getStatus());
        assertEquals(15, response.getLateArrivalMinutes());
        assertTrue(response.getIsPresent()); // Late is still considered present
        assertFalse(response.getIsOnTime());

        verify(attendanceRepository).save(any(ExtracurricularAttendance.class));
    }

    @Test
    void recordAttendance_ExcusedAbsence_Success() {
        // Given
        CreateExtracurricularAttendanceRequest excusedRequest = CreateExtracurricularAttendanceRequest.builder()
                .activityId(1L)
                .studentId(1L)
                .attendanceDate(LocalDate.now())
                .status(AttendanceStatus.EXCUSED)
                .isExcused(true)
                .excuseReason("Medical appointment")
                .build();

        ExtracurricularAttendance excusedAttendance = ExtracurricularAttendance.builder()
                .id(3L)
                .activity(testActivity)
                .student(testStudent)
                .attendanceDate(LocalDate.now())
                .status(AttendanceStatus.EXCUSED)
                .isExcused(true)
                .excuseReason("Medical appointment")
                .isActive(true)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);
        when(attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
                testActivity, testStudent, LocalDate.now())).thenReturn(false);
        when(attendanceRepository.save(any(ExtracurricularAttendance.class))).thenReturn(excusedAttendance);

        // When
        ExtracurricularAttendanceResponse response = attendanceService.recordAttendance(excusedRequest);

        // Then
        assertNotNull(response);
        assertEquals(AttendanceStatus.EXCUSED, response.getStatus());
        assertTrue(response.getIsExcused());
        assertEquals("Medical appointment", response.getExcuseReason());
        assertFalse(response.getIsPresent()); // Excused is not considered present

        verify(attendanceRepository).save(any(ExtracurricularAttendance.class));
    }

    @Test
    void updateAttendance_Success() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        
        ExtracurricularAttendance updatedAttendance = ExtracurricularAttendance.builder()
                .id(1L)
                .activity(testActivity)
                .student(testStudent)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(16, 0))
                .status(AttendanceStatus.PRESENT)
                .participationScore(90)
                .performanceRating(5)
                .notes("Excellent participation")
                .achievementPoints(15)
                .isActive(true)
                .build();
        
        when(attendanceRepository.save(any(ExtracurricularAttendance.class))).thenReturn(updatedAttendance);

        // When
        ExtracurricularAttendanceResponse response = attendanceService.updateAttendance(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(90, response.getParticipationScore());
        assertEquals(5, response.getPerformanceRating());
        assertEquals("Excellent participation", response.getNotes());
        assertEquals(15, response.getAchievementPoints());

        verify(attendanceRepository).save(any(ExtracurricularAttendance.class));
    }

    @Test
    void updateAttendance_NotFound_ThrowsException() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.updateAttendance(1L, updateRequest);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void getAttendanceById_Success() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // When
        ExtracurricularAttendanceResponse response = attendanceService.getAttendanceById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertEquals("Programming Club", response.getActivity().getName());
        assertEquals("Jane Smith", response.getStudent().getFullName());
    }

    @Test
    void getAttendanceById_NotFound_ThrowsException() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.getAttendanceById(1L);
        });
    }

    @Test
    void deleteAttendance_Success() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(ExtracurricularAttendance.class))).thenReturn(testAttendance);

        // When
        attendanceService.deleteAttendance(1L);

        // Then
        verify(attendanceRepository).save(argThat(attendance -> !attendance.getIsActive()));
    }

    @Test
    void deleteAttendance_NotFound_ThrowsException() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.deleteAttendance(1L);
        });

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void getAttendanceByActivity_Success() {
        // Given
        List<ExtracurricularAttendance> attendanceList = Arrays.asList(testAttendance);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByActivityAndIsActiveTrueOrderByAttendanceDateDesc(testActivity))
                .thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = attendanceService.getAttendanceByActivity(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Programming Club", responses.get(0).getActivity().getName());
    }

    @Test
    void getAttendanceByStudent_Success() {
        // Given
        List<ExtracurricularAttendance> attendanceList = Arrays.asList(testAttendance);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(attendanceRepository.findByStudentAndIsActiveTrueOrderByAttendanceDateDesc(testStudent))
                .thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = attendanceService.getAttendanceByStudent(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Jane Smith", responses.get(0).getStudent().getFullName());
    }

    @Test
    void getAttendanceByActivityAndDate_Success() {
        // Given
        LocalDate testDate = LocalDate.now();
        List<ExtracurricularAttendance> attendanceList = Arrays.asList(testAttendance);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByActivityAndAttendanceDateAndIsActiveTrueOrderByStudentNamaLengkapAsc(
                testActivity, testDate)).thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = 
                attendanceService.getAttendanceByActivityAndDate(1L, testDate);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testDate, responses.get(0).getAttendanceDate());
    }

    @Test
    void getAttendanceByStatus_Success() {
        // Given
        List<ExtracurricularAttendance> attendanceList = Arrays.asList(testAttendance);
        when(attendanceRepository.findByStatusAndIsActiveTrueOrderByAttendanceDateDesc(AttendanceStatus.PRESENT))
                .thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = 
                attendanceService.getAttendanceByStatus(AttendanceStatus.PRESENT);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(AttendanceStatus.PRESENT, responses.get(0).getStatus());
    }

    @Test
    void getRecentAttendance_Success() {
        // Given
        List<ExtracurricularAttendance> attendanceList = Arrays.asList(testAttendance);
        when(attendanceRepository.findRecentAttendance(any(LocalDate.class))).thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = attendanceService.getRecentAttendance(7);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAttendanceNeedingReview_Success() {
        // Given
        ExtracurricularAttendance attendanceNeedingReview = ExtracurricularAttendance.builder()
                .id(2L)
                .activity(testActivity)
                .student(testStudent)
                .attendanceDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .participationScore(null) // No participation score - needs review
                .isActive(true)
                .build();

        List<ExtracurricularAttendance> attendanceList = Arrays.asList(attendanceNeedingReview);
        when(attendanceRepository.findAttendanceNeedingReview()).thenReturn(attendanceList);

        // When
        List<ExtracurricularAttendanceResponse> responses = attendanceService.getAttendanceNeedingReview();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertNull(responses.get(0).getParticipationScore());
    }

    @Test
    void getAttendanceStatisticsByActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalRecords", 10L);
        statsMap.put("presentCount", 8L);
        statsMap.put("absentCount", 2L);
        statsMap.put("lateCount", 1L);
        statsMap.put("excusedCount", 0L);
        statsMap.put("avgParticipationScore", 82.5);
        statsMap.put("totalAchievementPoints", 150L);
        
        when(attendanceRepository.getAttendanceStatisticsByActivity(testActivity))
                .thenReturn(Arrays.asList(statsMap));

        // When
        ExtracurricularAttendanceResponse.AttendanceStatistics stats = 
                attendanceService.getAttendanceStatisticsByActivity(1L);

        // Then
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalRecords());
        assertEquals(8L, stats.getPresentCount());
        assertEquals(2L, stats.getAbsentCount());
        assertEquals(1L, stats.getLateCount());
        assertEquals(0L, stats.getExcusedCount());
        assertEquals(80.0, stats.getAttendanceRate()); // 8/10 * 100
        assertEquals(82.5, stats.getAverageParticipationScore());
        assertEquals(150L, stats.getTotalAchievementPoints());
    }

    @Test
    void getAttendanceStatisticsByStudent_Success() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalRecords", 5L);
        statsMap.put("presentCount", 4L);
        statsMap.put("absentCount", 1L);
        statsMap.put("lateCount", 0L);
        statsMap.put("excusedCount", 0L);
        statsMap.put("avgParticipationScore", 85.0);
        statsMap.put("totalAchievementPoints", 50L);
        
        when(attendanceRepository.getAttendanceStatisticsByStudent(testStudent))
                .thenReturn(Arrays.asList(statsMap));

        // When
        ExtracurricularAttendanceResponse.AttendanceStatistics stats = 
                attendanceService.getAttendanceStatisticsByStudent(1L);

        // Then
        assertNotNull(stats);
        assertEquals(5L, stats.getTotalRecords());
        assertEquals(4L, stats.getPresentCount());
        assertEquals(1L, stats.getAbsentCount());
        assertEquals(80.0, stats.getAttendanceRate()); // 4/5 * 100
        assertEquals(85.0, stats.getAverageParticipationScore());
        assertEquals(50L, stats.getTotalAchievementPoints());
    }

    @Test
    void calculateAttendanceRateByActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(attendanceRepository.calculateAttendanceRateByActivity(testActivity)).thenReturn(85.5);

        // When
        Double attendanceRate = attendanceService.calculateAttendanceRateByActivity(1L);

        // Then
        assertNotNull(attendanceRate);
        assertEquals(85.5, attendanceRate);
    }

    @Test
    void calculateAttendanceRateByStudent_Success() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(attendanceRepository.calculateAttendanceRateByStudent(testStudent)).thenReturn(90.0);

        // When
        Double attendanceRate = attendanceService.calculateAttendanceRateByStudent(1L);

        // Then
        assertNotNull(attendanceRate);
        assertEquals(90.0, attendanceRate);
    }

    @Test
    void validateAttendanceRecording_Success() {
        // Given
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.existsById(1L)).thenReturn(true);

        // When
        Map<String, Object> result = attendanceService.validateAttendanceRecording(1L, 1L, LocalDate.now());

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("canRecord"));
        assertTrue(((List<String>) result.get("errors")).isEmpty());
    }

    @Test
    void validateAttendanceRecording_ActivityNotFound() {
        // Given
        when(activityRepository.existsById(1L)).thenReturn(false);
        when(studentRepository.existsById(1L)).thenReturn(true);

        // When
        Map<String, Object> result = attendanceService.validateAttendanceRecording(1L, 1L, LocalDate.now());

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("canRecord"));
        assertTrue(((List<String>) result.get("errors")).contains("Activity not found"));
    }

    @Test
    void validateAttendanceRecording_FutureDate() {
        // Given
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.existsById(1L)).thenReturn(true);

        // When
        Map<String, Object> result = attendanceService.validateAttendanceRecording(
                1L, 1L, LocalDate.now().plusDays(2));

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("canRecord"));
        assertTrue(((List<String>) result.get("errors")).contains("Cannot record attendance for future dates"));
    }

    @Test
    void isStudentRegisteredForActivity_Success() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);

        // When
        Boolean isRegistered = attendanceService.isStudentRegisteredForActivity(1L, 1L);

        // Then
        assertTrue(isRegistered);
    }

    @Test
    void attendanceExists_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(attendanceRepository.existsByActivityAndStudentAndAttendanceDateAndIsActiveTrue(
                testActivity, testStudent, LocalDate.now())).thenReturn(true);

        // When
        Boolean exists = attendanceService.attendanceExists(1L, 1L, LocalDate.now());

        // Then
        assertTrue(exists);
    }
}