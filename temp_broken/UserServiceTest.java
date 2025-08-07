package com.school.sim.service;

import com.school.sim.dto.request.CreateUserRequest;
import com.school.sim.dto.request.UpdateUserRequest;
import com.school.sim.dto.response.UserResponse;
import com.school.sim.entity.Role;
import com.school.sim.entity.User;
import com.school.sim.entity.UserType;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.RoleRepository;
import com.school.sim.repository.UserRepository;
import com.school.sim.service.impl.UserServiceImpl;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setNip("12345");
        testUser.setPhone("1234567890");
        testUser.setAddress("Test Address");
        testUser.setUserType(UserType.TEACHER);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setRoles(new ArrayList<>());

        // Set up test role
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("TEACHER");
        testRole.setDescription("Teacher role");

        // Set up create request
        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("New User");
        createUserRequest.setEmail("new@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setUserType(UserType.TEACHER);
        createUserRequest.setActive(true);

        // Set up update request
        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("Updated User");
        updateUserRequest.setPhone("9876543210");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.findByEmail(createUserRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.createUser(createUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(createUserRequest.getPassword());
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsValidationException() {
        // Arrange
        when(userRepository.findByEmail(createUserRequest.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateUser(1L, updateUserRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updateUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserResponse> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getName(), result.get().getName());
    }

    @Test
    void getUserById_NotFound_ReturnsEmpty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponse> result = userService.getUserById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserResponse> result = userService.getUserByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchUsers_Success() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "test";
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchTerm, searchTerm, pageable)).thenReturn(userPage);

        // Act
        Page<UserResponse> result = userService.searchUsers(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void activateUser_Success() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.activateUser(1L);

        // Assert
        assertTrue(testUser.isActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void deactivateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertFalse(testUser.isActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void assignRole_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.assignRole(1L, 1L);

        // Assert
        assertTrue(testUser.getRoles().contains(testRole));
        verify(userRepository).save(testUser);
    }

    @Test
    void removeRole_Success() {
        // Arrange
        testUser.getRoles().add(testRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.removeRole(1L, 1L);

        // Assert
        assertFalse(testUser.getRoles().contains(testRole));
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_Success() {
        // Arrange
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.changePassword(1L, currentPassword, newPassword);

        // Assert
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsValidationException() {
        // Arrange
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            userService.changePassword(1L, currentPassword, newPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByNip_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByNip("12345")).thenReturn(true);

        // Act
        boolean result = userService.existsByNip("12345");

        // Assert
        assertTrue(result);
    }

    @Test
    void getUsersByRole_Success() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userRepository.findByRoles_Name("TEACHER")).thenReturn(users);

        // Act
        List<UserResponse> result = userService.getUsersByRole("TEACHER");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
    }

    @Test
    void getActiveUsersCount_ReturnsCount() {
        // Arrange
        when(userRepository.countByActiveTrue()).thenReturn(5L);

        // Act
        long result = userService.getActiveUsersCount();

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void getTotalUsersCount_ReturnsCount() {
        // Arrange
        when(userRepository.count()).thenReturn(10L);

        // Act
        long result = userService.getTotalUsersCount();

        // Assert
        assertEquals(10L, result);
    }

    @Test
    void bulkActivateUsers_Success() {
        // Arrange
        List<Long> userIds = List.of(1L, 2L);
        List<User> users = List.of(testUser);
        when(userRepository.findAllById(userIds)).thenReturn(users);
        when(userRepository.saveAll(users)).thenReturn(users);

        // Act
        userService.bulkActivateUsers(userIds);

        // Assert
        verify(userRepository).saveAll(users);
        assertTrue(testUser.isActive());
    }

    @Test
    void bulkDeactivateUsers_Success() {
        // Arrange
        List<Long> userIds = List.of(1L, 2L);
        List<User> users = List.of(testUser);
        when(userRepository.findAllById(userIds)).thenReturn(users);
        when(userRepository.saveAll(users)).thenReturn(users);

        // Act
        userService.bulkDeactivateUsers(userIds);

        // Assert
        verify(userRepository).saveAll(users);
        assertFalse(testUser.isActive());
    }
}
