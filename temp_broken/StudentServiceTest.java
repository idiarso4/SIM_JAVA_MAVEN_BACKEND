package com.school.sim.service;

import com.school.sim.dto.request.CreateStudentRequest;
import com.school.sim.dto.request.UpdateStudentRequest;
import com.school.sim.dto.request.StudentSearchRequest;
import com.school.sim.dto.response.StudentResponse;
import com.school.sim.entity.*;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.*;
import com.school.sim.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassRoomRepository classRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentAssessmentRepository studentAssessmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student testStudent;
    private ClassRoom testClassRoom;
    private CreateStudentRequest createStudentRequest;
    private UpdateStudentRequest updateStudentRequest;

    @BeforeEach
    void setUp() {
        // Set up test student
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setNis("12345");
        testStudent.setNamaLengkap("Test Student");
        testStudent.setTanggalLahir(LocalDate.of(2005, 1, 1));
        testStudent.setJenisKelamin(Gender.MALE);
        testStudent.setAgama("Islam");
        testStudent.setTahunMasuk(2023);
        testStudent.setStatus(StudentStatus.ACTIVE);
        testStudent.setCreatedAt(LocalDateTime.now());
        testStudent.setUpdatedAt(LocalDateTime.now());
        testStudent.setAttendances(new ArrayList<>());
        testStudent.setAssessments(new ArrayList<>());

        // Set up test class room
        testClassRoom = new ClassRoom();
        testClassRoom.setId(1L);
        testClassRoom.setName("X-1");
        testClassRoom.setGrade(10);
        testClassRoom.setCapacity(40);
        testClassRoom.setCurrentEnrollment(20);

        // Set up create request
        createStudentRequest = new CreateStudentRequest();
        createStudentRequest.setNis("54321");
        createStudentRequest.setNamaLengkap("New Student");
        createStudentRequest.setTanggalLahir(LocalDate.of(2005, 6, 15));
        createStudentRequest.setJenisKelamin(Gender.FEMALE);
        createStudentRequest.setAgama("Christian");
        createStudentRequest.setTahunMasuk(2023);
        createStudentRequest.setStatus(StudentStatus.ACTIVE);

        // Set up update request
        updateStudentRequest = new UpdateStudentRequest();
        updateStudentRequest.setNamaLengkap("Updated Student");
        updateStudentRequest.setAgama("Buddhist");
    }

    @Test
    void createStudent_ValidRequest_ShouldReturnStudentResponse() {
        // Arrange
        when(studentRepository.existsByNis(createStudentRequest.getNis())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentResponse result = studentService.createStudent(createStudentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testStudent.getId(), result.getId());
        assertEquals(testStudent.getNis(), result.getNis());
        assertEquals(testStudent.getNamaLengkap(), result.getNamaLengkap());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_DuplicateNis_ShouldThrowValidationException() {
        // Arrange
        when(studentRepository.existsByNis(createStudentRequest.getNis())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            studentService.createStudent(createStudentRequest);
        });

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudent_WithClassRoomAssignment_ShouldAssignToClassRoom() {
        // Arrange
        createStudentRequest.setClassRoomId(1L);
        when(studentRepository.existsByNis(createStudentRequest.getNis())).thenReturn(false);
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(20L);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentResponse result = studentService.createStudent(createStudentRequest);

        // Assert
        assertNotNull(result);
        verify(classRoomRepository).findById(1L);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void updateStudent_ValidRequest_ShouldReturnUpdatedStudentResponse() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentResponse result = studentService.updateStudent(1L, updateStudentRequest);

        // Assert
        assertNotNull(result);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void updateStudent_StudentNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            studentService.updateStudent(1L, updateStudentRequest);
        });

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getStudentById_ExistingStudent_ShouldReturnStudentResponse() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act
        Optional<StudentResponse> result = studentService.getStudentById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testStudent.getId(), result.get().getId());
        assertEquals(testStudent.getNis(), result.get().getNis());
    }

    @Test
    void getStudentById_NonExistentStudent_ShouldReturnEmpty() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<StudentResponse> result = studentService.getStudentById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getStudentByNis_ExistingStudent_ShouldReturnStudentResponse() {
        // Arrange
        when(studentRepository.findByNis("12345")).thenReturn(Optional.of(testStudent));

        // Act
        Optional<StudentResponse> result = studentService.getStudentByNis("12345");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testStudent.getNis(), result.get().getNis());
    }

    @Test
    void getAllStudents_ShouldReturnPagedStudentResponses() {
        // Arrange
        List<Student> students = List.of(testStudent);
        Page<Student> studentPage = new PageImpl<>(students);
        Pageable pageable = PageRequest.of(0, 10);
        when(studentRepository.findAll(pageable)).thenReturn(studentPage);

        // Act
        Page<StudentResponse> result = studentService.getAllStudents(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testStudent.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchStudents_WithCriteria_ShouldReturnFilteredResults() {
        // Arrange
        StudentSearchRequest searchRequest = new StudentSearchRequest();
        searchRequest.setName("Test");
        searchRequest.setStatus(StudentStatus.ACTIVE);

        List<Student> students = List.of(testStudent);
        Page<Student> studentPage = new PageImpl<>(students);
        Pageable pageable = PageRequest.of(0, 10);

        when(studentRepository.advancedSearchStudents(
            eq("Test"), isNull(), isNull(), eq(StudentStatus.ACTIVE), 
            isNull(), isNull(), isNull(), isNull(), eq(pageable)))
            .thenReturn(studentPage);

        // Act
        Page<StudentResponse> result = studentService.searchStudents(searchRequest, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteStudent_StudentWithoutRecords_ShouldDeleteSuccessfully() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act
        studentService.deleteStudent(1L);

        // Assert
        verify(studentRepository).delete(testStudent);
    }

    @Test
    void deleteStudent_StudentWithAttendanceRecords_ShouldThrowValidationException() {
        // Arrange
        testStudent.setAttendances(List.of(new Attendance()));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            studentService.deleteStudent(1L);
        });

        verify(studentRepository, never()).delete(any(Student.class));
    }

    @Test
    void activateStudent_ShouldUpdateStatusToActive() {
        // Arrange
        testStudent.setStatus(StudentStatus.INACTIVE);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.activateStudent(1L);

        // Assert
        verify(studentRepository).save(argThat(student -> 
            student.getStatus() == StudentStatus.ACTIVE));
    }

    @Test
    void deactivateStudent_ShouldUpdateStatusToInactive() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.deactivateStudent(1L);

        // Assert
        verify(studentRepository).save(argThat(student -> 
            student.getStatus() == StudentStatus.INACTIVE));
    }

    @Test
    void graduateStudent_ShouldUpdateStatusToGraduated() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.graduateStudent(1L);

        // Assert
        verify(studentRepository).save(argThat(student -> 
            student.getStatus() == StudentStatus.GRADUATED));
    }

    @Test
    void assignToClassRoom_ValidAssignment_ShouldAssignSuccessfully() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(20L);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.assignToClassRoom(1L, 1L);

        // Assert
        verify(studentRepository).save(argThat(student -> 
            student.getClassRoom() != null && student.getClassRoom().getId().equals(1L)));
    }

    @Test
    void assignToClassRoom_ClassRoomAtCapacity_ShouldThrowValidationException() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(40L);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            studentService.assignToClassRoom(1L, 1L);
        });

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void bulkAssignToClassRoom_ValidAssignment_ShouldAssignAllStudents() {
        // Arrange
        List<Long> studentIds = List.of(1L, 2L);
        List<Student> students = List.of(testStudent, new Student());
        
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(20L);
        when(studentRepository.findAllById(studentIds)).thenReturn(students);
        when(studentRepository.saveAll(students)).thenReturn(students);

        // Act
        studentService.bulkAssignToClassRoom(studentIds, 1L);

        // Assert
        verify(studentRepository).saveAll(students);
    }

    @Test
    void existsByNis_ExistingNis_ShouldReturnTrue() {
        // Arrange
        when(studentRepository.existsByNis("12345")).thenReturn(true);

        // Act
        boolean result = studentService.existsByNis("12345");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByNis_NonExistentNis_ShouldReturnFalse() {
        // Arrange
        when(studentRepository.existsByNis("99999")).thenReturn(false);

        // Act
        boolean result = studentService.existsByNis("99999");

        // Assert
        assertFalse(result);
    }

    @Test
    void getStudentStatistics_ShouldReturnComprehensiveStatistics() {
        // Arrange
        when(studentRepository.count()).thenReturn(100L);
        when(studentRepository.getStudentStatisticsByStatus()).thenReturn(
            List.of(new Object[]{"ACTIVE", 80L}, new Object[]{"INACTIVE", 20L}));
        when(studentRepository.getStudentStatisticsByGrade()).thenReturn(
            List.of(new Object[]{10, 30L}, new Object[]{11, 35L}, new Object[]{12, 35L}));
        when(studentRepository.getStudentStatisticsByMajor()).thenReturn(
            List.of(new Object[]{"Science", 50L}, new Object[]{"Social", 50L}));
        when(studentRepository.findByClassRoomIsNull()).thenReturn(List.of());
        when(studentRepository.findStudentsWithoutUserAccount()).thenReturn(List.of());

        // Act
        Map<String, Object> result = studentService.getStudentStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.get("totalStudents"));
        assertNotNull(result.get("studentsByStatus"));
        assertNotNull(result.get("studentsByGrade"));
        assertNotNull(result.get("studentsByMajor"));
    }

    @Test
    void createUserAccountForStudent_ValidStudent_ShouldCreateUserAccount() {
        // Arrange
        testStudent.setUser(null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.createUserAccountForStudent(1L, "password123");

        // Assert
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createUserAccountForStudent_StudentAlreadyHasAccount_ShouldThrowValidationException() {
        // Arrange
        testStudent.setUser(new User());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            studentService.createUserAccountForStudent(1L, "password123");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getStudentCountByClassRoom_ShouldReturnCorrectCount() {
        // Arrange
        when(studentRepository.countByClassRoomId(1L)).thenReturn(25L);

        // Act
        long result = studentService.getStudentCountByClassRoom(1L);

        // Assert
        assertEquals(25L, result);
    }

    @Test
    void getActiveStudentCountByClassRoom_ShouldReturnCorrectCount() {
        // Arrange
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(20L);

        // Act
        long result = studentService.getActiveStudentCountByClassRoom(1L);

        // Assert
        assertEquals(20L, result);
    }

    @Test
    void canAssignToClassRoom_WithinCapacity_ShouldReturnTrue() {
        // Arrange
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(20L);

        // Act
        boolean result = studentService.canAssignToClassRoom(1L, 5);

        // Assert
        assertTrue(result);
    }

    @Test
    void canAssignToClassRoom_ExceedsCapacity_ShouldReturnFalse() {
        // Arrange
        when(classRoomRepository.findById(1L)).thenReturn(Optional.of(testClassRoom));
        when(studentRepository.countByClassRoomIdAndStatus(1L, StudentStatus.ACTIVE)).thenReturn(35L);

        // Act
        boolean result = studentService.canAssignToClassRoom(1L, 10);

        // Assert
        assertFalse(result);
    }
}
