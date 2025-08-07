package com.school.sim.service;

import com.school.sim.dto.request.CreateScheduleRequest;
import com.school.sim.dto.response.ScheduleResponse;
import com.school.sim.entity.*;
import com.school.sim.repository.*;
import com.school.sim.service.ScheduleService;
import com.school.sim.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleService
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    
    @Mock
    private ClassRoomRepository classRoomRepository;
    
    @Mock
    private SubjectRepository subjectRepository;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private ClassRoom testClassRoom;
    private Subject testSubject;
    private User testTeacher;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        // Setup test data
        testClassRoom = new ClassRoom();
        testClassRoom.setId(1L);
        testClassRoom.setName("Class 10A");
        testClassRoom.setCode("10A");
        testClassRoom.setCapacity(30);
        testClassRoom.setLocation("Building A, Floor 2");

        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("Mathematics");
        testSubject.setCode("MATH101");
        testSubject.setCredits(3);

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
        testSchedule.setCreatedAt(LocalDateTime.now());
        testSchedule.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateSchedule_Success() {
        // Given
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .classRoomId(1L)
                .subjectId(1L)
                .teacherId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 30))
                .academicYear("2024/2025")
                .semester(1)
                .skipConflictCheck(true)
                .build();

        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // When
        ScheduleResponse response = scheduleService.createSchedule(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(DayOfWeek.MONDAY, response.getDayOfWeek());
        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(9, 30), response.getEndTime());
        assertEquals("2024/2025", response.getAcademicYear());
        assertEquals(1, response.getSemester());
        assertTrue(response.getIsActive());

        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testGetScheduleById_Success() {
        // Given
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));

        // When
        ScheduleResponse response = scheduleService.getScheduleById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mathematics", response.getSubject().getName());
        assertEquals("John Doe", response.getTeacher().getFullName());
        assertEquals("Class 10A", response.getClassRoom().getName());
    }

    @Test
    void testValidateTimeSlot_Valid() {
        // Given
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(9, 30);

        // When
        Boolean result = scheduleService.validateTimeSlot(startTime, endTime);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidateTimeSlot_Invalid() {
        // Given
        LocalTime startTime = LocalTime.of(9, 30);
        LocalTime endTime = LocalTime.of(8, 0);

        // When
        Boolean result = scheduleService.validateTimeSlot(startTime, endTime);

        // Then
        assertFalse(result);
    }

    @Test
    void testCalculateScheduleDuration() {
        // Given
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(9, 30);

        // When
        Integer duration = scheduleService.calculateScheduleDuration(startTime, endTime);

        // Then
        assertEquals(90, duration); // 1 hour 30 minutes = 90 minutes
    }

    @Test
    void testValidateAcademicPeriod_Valid() {
        // When
        Boolean result = scheduleService.validateAcademicPeriod("2024/2025", 1);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidateAcademicPeriod_InvalidYear() {
        // When
        Boolean result = scheduleService.validateAcademicPeriod("2024", 1);

        // Then
        assertFalse(result);
    }

    @Test
    void testValidateAcademicPeriod_InvalidSemester() {
        // When
        Boolean result = scheduleService.validateAcademicPeriod("2024/2025", 3);

        // Then
        assertFalse(result);
    }
}