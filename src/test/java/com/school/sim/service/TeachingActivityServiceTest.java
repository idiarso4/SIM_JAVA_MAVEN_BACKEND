package com.school.sim.service;

import com.school.sim.dto.request.CreateTeachingActivityRequest;
import com.school.sim.dto.response.TeachingActivityResponse;
import com.school.sim.entity.*;
import com.school.sim.repository.*;
import com.school.sim.service.impl.TeachingActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeachingActivityService
 */
@ExtendWith(MockitoExtension.class)
class TeachingActivityServiceTest {

    @Mock
    private TeachingActivityRepository teachingActivityRepository;
    
    @Mock
    private ScheduleRepository scheduleRepository;
    
    @Mock
    private SubjectRepository subjectRepository;
    
    @Mock
    private ClassRoomRepository classRoomRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private TeachingActivityServiceImpl teachingActivityService;

    private Schedule testSchedule;
    private Subject testSubject;
    private ClassRoom testClassRoom;
    private User testTeacher;
    private TeachingActivity testActivity;

    @BeforeEach
    void setUp() {
        // Setup test data
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("Mathematics");
        testSubject.setCode("MATH101");
        testSubject.setCredits(3);

        testClassRoom = new ClassRoom();
        testClassRoom.setId(1L);
        testClassRoom.setName("Class 10A");
        testClassRoom.setCode("10A");
        testClassRoom.setCapacity(30);
        testClassRoom.setLocation("Building A, Floor 2");

        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setUsername("john.doe");
        testTeacher.setEmail("john.doe@school.com");

        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setClassRoom(testClassRoom);
        testSchedule.setSubject(testSubject);
        testSchedule.setTeacher(testTeacher);
        testSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        testSchedule.setStartTime(LocalTime.of(8, 0));
        testSchedule.setEndTime(LocalTime.of(9, 30));
        testSchedule.setAcademicYear("2024/2025");
        testSchedule.setSemester(1);
        testSchedule.setIsActive(true);

        testActivity = new TeachingActivity();
        testActivity.setId(1L);
        testActivity.setSchedule(testSchedule);
        testActivity.setSubject(testSubject);
        testActivity.setClassRoom(testClassRoom);
        testActivity.setTeacher(testTeacher);
        testActivity.setDate(LocalDate.of(2024, 1, 15));
        testActivity.setStartTime(LocalTime.of(8, 0));
        testActivity.setEndTime(LocalTime.of(9, 30));
        testActivity.setTopic("Introduction to Algebra");
        testActivity.setDescription("Basic algebraic concepts");
        testActivity.setIsCompleted(false);
        testActivity.setCreatedAt(LocalDateTime.now());
        testActivity.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateTeachingActivity_Success() {
        // Given
        CreateTeachingActivityRequest request = CreateTeachingActivityRequest.builder()
                .scheduleId(1L)
                .date(LocalDate.of(2024, 1, 15))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 30))
                .topic("Introduction to Algebra")
                .description("Basic algebraic concepts")
                .isCompleted(false)
                .build();

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(teachingActivityRepository.save(any(TeachingActivity.class))).thenReturn(testActivity);

        // When
        TeachingActivityResponse response = teachingActivityService.createTeachingActivity(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(LocalDate.of(2024, 1, 15), response.getDate());
        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(9, 30), response.getEndTime());
        assertEquals("Introduction to Algebra", response.getTopic());
        assertEquals("Basic algebraic concepts", response.getDescription());
        assertFalse(response.getIsCompleted());

        verify(teachingActivityRepository).save(any(TeachingActivity.class));
    }

    @Test
    void testGetTeachingActivityById_Success() {
        // Given
        when(teachingActivityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // When
        TeachingActivityResponse response = teachingActivityService.getTeachingActivityById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Introduction to Algebra", response.getTopic());
        assertEquals("Mathematics", response.getSubject().getName());
        assertEquals("John Doe", response.getTeacher().getFullName());
        assertEquals("Class 10A", response.getClassRoom().getName());
    }

    @Test
    void testMarkActivityAsCompleted_Success() {
        // Given
        when(teachingActivityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        
        TeachingActivity completedActivity = new TeachingActivity();
        completedActivity.setId(1L);
        completedActivity.setSchedule(testSchedule);
        completedActivity.setSubject(testSubject);
        completedActivity.setClassRoom(testClassRoom);
        completedActivity.setTeacher(testTeacher);
        completedActivity.setDate(LocalDate.of(2024, 1, 15));
        completedActivity.setStartTime(LocalTime.of(8, 0));
        completedActivity.setEndTime(LocalTime.of(9, 30));
        completedActivity.setTopic("Introduction to Algebra");
        completedActivity.setIsCompleted(true);
        completedActivity.setNotes("Class completed successfully");
        completedActivity.setCreatedAt(LocalDateTime.now());
        completedActivity.setUpdatedAt(LocalDateTime.now());
        
        when(teachingActivityRepository.save(any(TeachingActivity.class))).thenReturn(completedActivity);

        // When
        TeachingActivityResponse response = teachingActivityService.markActivityAsCompleted(1L, "Class completed successfully");

        // Then
        assertNotNull(response);
        assertTrue(response.getIsCompleted());
        assertEquals("Class completed successfully", response.getNotes());

        verify(teachingActivityRepository).save(any(TeachingActivity.class));
    }

    @Test
    void testGenerateActivityFromSchedule_Success() {
        // Given
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(teachingActivityRepository.findByScheduleIdAndDate(1L, LocalDate.of(2024, 1, 15)))
                .thenReturn(Optional.empty());
        when(teachingActivityRepository.save(any(TeachingActivity.class))).thenReturn(testActivity);

        // When
        TeachingActivityResponse response = teachingActivityService.generateActivityFromSchedule(
                1L, LocalDate.of(2024, 1, 15), "Introduction to Algebra");

        // Then
        assertNotNull(response);
        assertEquals(LocalDate.of(2024, 1, 15), response.getDate());
        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(9, 30), response.getEndTime());
        assertEquals("Introduction to Algebra", response.getTopic());

        verify(teachingActivityRepository).save(any(TeachingActivity.class));
    }

    @Test
    void testGetTodaysActivitiesForTeacher_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teachingActivityRepository.findByTeacherAndDate(testTeacher, LocalDate.now()))
                .thenReturn(List.of(testActivity));

        // When
        List<TeachingActivityResponse> responses = teachingActivityService.getTodaysActivitiesForTeacher(1L);

        // Then
        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("Introduction to Algebra", responses.get(0).getTopic());
    }

    @Test
    void testIsAttendanceRecorded_NoAttendance() {
        // Given
        testActivity.setAttendances(null);
        when(teachingActivityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // When
        Boolean result = teachingActivityService.isAttendanceRecorded(1L);

        // Then
        assertFalse(result);
    }
}