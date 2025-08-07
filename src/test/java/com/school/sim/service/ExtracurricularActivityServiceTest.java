package com.school.sim.service;

import com.school.sim.dto.request.CreateExtracurricularActivityRequest;
import com.school.sim.dto.request.UpdateExtracurricularActivityRequest;
import com.school.sim.dto.response.ExtracurricularActivityResponse;
import com.school.sim.entity.*;
import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.impl.ExtracurricularActivityServiceImpl;
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
 * Unit tests for ExtracurricularActivityService
 */
@ExtendWith(MockitoExtension.class)
class ExtracurricularActivityServiceTest {

    @Mock
    private ExtracurricularActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ExtracurricularActivityServiceImpl activityService;

    private ExtracurricularActivity testActivity;
    private User testSupervisor;
    private Department testDepartment;
    private Student testStudent;
    private CreateExtracurricularActivityRequest createRequest;
    private UpdateExtracurricularActivityRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        testSupervisor = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .email("john.doe@school.com")
                .phone("123-456-7890")
                .build();

        testDepartment = Department.builder()
                .id(1L)
                .name("Computer Science")
                .code("CS")
                .description("Computer Science Department")
                .build();

        testStudent = Student.builder()
                .id(1L)
                .nis("STU001")
                .namaLengkap("Jane Smith")
                .build();

        testActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Programming Club")
                .description("Weekly programming club meeting")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.OPEN_FOR_REGISTRATION)
                .activityDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .location("Room 101")
                .maxParticipants(20)
                .currentParticipants(5)
                .registrationDeadline(LocalDate.now().plusDays(5))
                .isMandatory(false)
                .requiresPermission(false)
                .academicYear("2024/2025")
                .semester(1)
                .supervisor(testSupervisor)
                .department(testDepartment)
                .participants(new ArrayList<>())
                .notes("Bring your laptop")
                .isActive(true)
                .build();

        createRequest = CreateExtracurricularActivityRequest.builder()
                .name("Programming Club")
                .description("Weekly programming club meeting")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.OPEN_FOR_REGISTRATION)
                .activityDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .location("Room 101")
                .maxParticipants(20)
                .registrationDeadline(LocalDate.now().plusDays(5))
                .isMandatory(false)
                .requiresPermission(false)
                .academicYear("2024/2025")
                .semester(1)
                .supervisorId(1L)
                .departmentId(1L)
                .notes("Bring your laptop")
                .isActive(true)
                .build();

        updateRequest = UpdateExtracurricularActivityRequest.builder()
                .name("Advanced Programming Club")
                .description("Advanced programming club meeting")
                .maxParticipants(25)
                .build();
    }

    @Test
    void createActivity_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSupervisor));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(testActivity);
        when(activityRepository.findConflictingActivities(any(), any(), any(), any())).thenReturn(new ArrayList<>());

        // When
        ExtracurricularActivityResponse response = activityService.createActivity(createRequest);

        // Then
        assertNotNull(response);
        assertEquals("Programming Club", response.getName());
        assertEquals(ActivityType.CLUB, response.getType());
        assertEquals(ActivityStatus.OPEN_FOR_REGISTRATION, response.getStatus());
        assertEquals(testSupervisor.getFirstName() + " " + testSupervisor.getLastName(), 
                     response.getSupervisor().getFullName());

        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void createActivity_SupervisorNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            activityService.createActivity(createRequest);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void createActivity_DepartmentNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSupervisor));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            activityService.createActivity(createRequest);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void createActivity_InvalidConstraints_ThrowsException() {
        // Given
        CreateExtracurricularActivityRequest invalidRequest = CreateExtracurricularActivityRequest.builder()
                .name("Test Activity")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.OPEN_FOR_REGISTRATION)
                .activityDate(LocalDate.now().minusDays(1)) // Past date
                .startTime(LocalTime.of(16, 0))
                .endTime(LocalTime.of(14, 0)) // End before start
                .academicYear("2024/2025")
                .semester(1)
                .supervisorId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testSupervisor));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            activityService.createActivity(invalidRequest);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void updateActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        ExtracurricularActivity updatedActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Advanced Programming Club")
                .description("Advanced programming club meeting")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.OPEN_FOR_REGISTRATION)
                .activityDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .location("Room 101")
                .maxParticipants(25)
                .currentParticipants(5)
                .supervisor(testSupervisor)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(updatedActivity);

        // When
        ExtracurricularActivityResponse response = activityService.updateActivity(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Advanced Programming Club", response.getName());
        assertEquals("Advanced programming club meeting", response.getDescription());
        assertEquals(25, response.getMaxParticipants());

        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void updateActivity_NotFound_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            activityService.updateActivity(1L, updateRequest);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void getActivityById_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // When
        ExtracurricularActivityResponse response = activityService.getActivityById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Programming Club", response.getName());
        assertEquals(ActivityType.CLUB, response.getType());
    }

    @Test
    void getActivityById_NotFound_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            activityService.getActivityById(1L);
        });
    }

    @Test
    void deleteActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(testActivity);

        // When
        activityService.deleteActivity(1L);

        // Then
        verify(activityRepository).save(argThat(activity -> !activity.getIsActive()));
    }

    @Test
    void deleteActivity_NotFound_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            activityService.deleteActivity(1L);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void getAllActiveActivities_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(activityRepository.findByIsActiveTrue()).thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getAllActiveActivities();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Programming Club", responses.get(0).getName());
    }

    @Test
    void getActivitiesByType_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(activityRepository.findByTypeAndIsActiveTrue(ActivityType.CLUB)).thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getActivitiesByType(ActivityType.CLUB);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ActivityType.CLUB, responses.get(0).getType());
    }

    @Test
    void getActivitiesByStatus_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(activityRepository.findByStatusAndIsActiveTrue(ActivityStatus.OPEN_FOR_REGISTRATION))
                .thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = 
                activityService.getActivitiesByStatus(ActivityStatus.OPEN_FOR_REGISTRATION);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ActivityStatus.OPEN_FOR_REGISTRATION, responses.get(0).getStatus());
    }

    @Test
    void registerStudentForActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(false);
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(testActivity);

        // When
        ExtracurricularActivityResponse response = activityService.registerStudentForActivity(1L, 1L);

        // Then
        assertNotNull(response);
        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void registerStudentForActivity_AlreadyRegistered_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            activityService.registerStudentForActivity(1L, 1L);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void registerStudentForActivity_ActivityFull_ThrowsException() {
        // Given
        ExtracurricularActivity fullActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Full Activity")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.OPEN_FOR_REGISTRATION)
                .activityDate(LocalDate.now().plusDays(7))
                .maxParticipants(5)
                .currentParticipants(5)
                .registrationDeadline(LocalDate.now().plusDays(5))
                .isActive(true)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(fullActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(false);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            activityService.registerStudentForActivity(1L, 1L);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void unregisterStudentFromActivity_Success() {
        // Given
        testActivity.getParticipants().add(testStudent);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(testActivity);

        // When
        ExtracurricularActivityResponse response = activityService.unregisterStudentFromActivity(1L, 1L);

        // Then
        assertNotNull(response);
        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void unregisterStudentFromActivity_NotRegistered_ThrowsException() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(false);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            activityService.unregisterStudentFromActivity(1L, 1L);
        });

        verify(activityRepository, never()).save(any());
    }

    @Test
    void getStudentActivities_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.findByParticipant(testStudent)).thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getStudentActivities(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Programming Club", responses.get(0).getName());
    }

    @Test
    void isStudentRegistered_Success() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(true);

        // When
        Boolean isRegistered = activityService.isStudentRegistered(1L, 1L);

        // Then
        assertTrue(isRegistered);
    }

    @Test
    void updateActivityStatus_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        ExtracurricularActivity updatedActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Programming Club")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.IN_PROGRESS)
                .activityDate(LocalDate.now().plusDays(7))
                .supervisor(testSupervisor)
                .isActive(true)
                .build();
        
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(updatedActivity);

        // When
        ExtracurricularActivityResponse response = activityService.updateActivityStatus(1L, ActivityStatus.IN_PROGRESS);

        // Then
        assertNotNull(response);
        assertEquals(ActivityStatus.IN_PROGRESS, response.getStatus());
        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void getUpcomingActivities_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(activityRepository.findUpcomingActivities(any(LocalDate.class))).thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getUpcomingActivities();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getIsUpcoming());
    }

    @Test
    void getTodaysActivities_Success() {
        // Given
        ExtracurricularActivity todayActivity = ExtracurricularActivity.builder()
                .id(2L)
                .name("Today's Activity")
                .type(ActivityType.SEMINAR)
                .status(ActivityStatus.IN_PROGRESS)
                .activityDate(LocalDate.now())
                .supervisor(testSupervisor)
                .isActive(true)
                .build();
        
        List<ExtracurricularActivity> activities = Arrays.asList(todayActivity);
        when(activityRepository.findByActivityDateAndIsActiveTrueOrderByStartTimeAsc(LocalDate.now()))
                .thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getTodaysActivities();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getIsToday());
    }

    @Test
    void getActivitiesOpenForRegistration_Success() {
        // Given
        List<ExtracurricularActivity> activities = Arrays.asList(testActivity);
        when(activityRepository.findActivitiesOpenForRegistration(any(LocalDate.class))).thenReturn(activities);

        // When
        List<ExtracurricularActivityResponse> responses = activityService.getActivitiesOpenForRegistration();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(ActivityStatus.OPEN_FOR_REGISTRATION, responses.get(0).getStatus());
    }

    @Test
    void getRegistrationInfo_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(activityRepository.isStudentRegistered(1L, testStudent)).thenReturn(false);

        // When
        ExtracurricularActivityResponse.RegistrationInfo registrationInfo = 
                activityService.getRegistrationInfo(1L, 1L);

        // Then
        assertNotNull(registrationInfo);
        assertTrue(registrationInfo.getCanRegister());
        assertEquals("CAN_REGISTER", registrationInfo.getRegistrationStatus());
        assertEquals(testActivity.getRegistrationDeadline(), registrationInfo.getRegistrationDeadline());
        assertEquals(testActivity.getAvailableSpots(), registrationInfo.getAvailableSpots());
    }

    @Test
    void cancelActivity_Success() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        ExtracurricularActivity cancelledActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Programming Club")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.CANCELLED)
                .activityDate(LocalDate.now().plusDays(7))
                .supervisor(testSupervisor)
                .notes("Bring your laptop\nCancellation reason: Weather conditions")
                .isActive(true)
                .build();
        
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(cancelledActivity);

        // When
        ExtracurricularActivityResponse response = activityService.cancelActivity(1L, "Weather conditions");

        // Then
        assertNotNull(response);
        assertEquals(ActivityStatus.CANCELLED, response.getStatus());
        assertTrue(response.getNotes().contains("Cancellation reason: Weather conditions"));
        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }

    @Test
    void postponeActivity_Success() {
        // Given
        LocalDate newDate = LocalDate.now().plusDays(14);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        ExtracurricularActivity postponedActivity = ExtracurricularActivity.builder()
                .id(1L)
                .name("Programming Club")
                .type(ActivityType.CLUB)
                .status(ActivityStatus.POSTPONED)
                .activityDate(newDate)
                .supervisor(testSupervisor)
                .notes("Bring your laptop\nPostponed reason: Room unavailable")
                .isActive(true)
                .build();
        
        when(activityRepository.save(any(ExtracurricularActivity.class))).thenReturn(postponedActivity);

        // When
        ExtracurricularActivityResponse response = activityService.postponeActivity(1L, newDate, "Room unavailable");

        // Then
        assertNotNull(response);
        assertEquals(ActivityStatus.POSTPONED, response.getStatus());
        assertEquals(newDate, response.getActivityDate());
        assertTrue(response.getNotes().contains("Postponed reason: Room unavailable"));
        verify(activityRepository).save(any(ExtracurricularActivity.class));
    }
}