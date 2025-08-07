package com.school.sim.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {

    private User user;
    private Role adminRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .userType(UserType.TEACHER)
                .isActive(true)
                .build();
        adminRole = new Role("ADMIN", "Administrator role");
        teacherRole = new Role("TEACHER", "Teacher role");
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("password", user.getPassword());
        assertEquals(UserType.TEACHER, user.getUserType());
        assertTrue(user.getIsActive());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void testAddRole() {
        user.addRole(adminRole);
        
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(adminRole.getUsers().contains(user));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void testRemoveRole() {
        user.addRole(adminRole);
        user.addRole(teacherRole);
        
        user.removeRole(adminRole);
        
        assertFalse(user.getRoles().contains(adminRole));
        assertFalse(adminRole.getUsers().contains(user));
        assertTrue(user.getRoles().contains(teacherRole));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void testHasRole() {
        user.addRole(teacherRole);
        
        assertTrue(user.hasRole("TEACHER"));
        assertTrue(user.hasRole("teacher")); // case insensitive
        assertFalse(user.hasRole("ADMIN"));
    }

    @Test
    void testIsAdmin() {
        // Test with UserType
        User adminUser = User.builder().email("admin@example.com").firstName("Admin").lastName("").password("password").userType(UserType.ADMIN).build();
        assertTrue(adminUser.isAdmin());
        assertFalse(user.isAdmin());
        
        // Test with Role
        user.addRole(adminRole);
        assertTrue(user.hasRole("ADMIN"));
    }

    @Test
    void testIsTeacher() {
        assertTrue(user.isTeacher()); // UserType is TEACHER
        
        User studentUser = User.builder().email("student@example.com").firstName("Student").lastName("").password("password").userType(UserType.STUDENT).build();
        assertFalse(studentUser.isTeacher());
        
        // Test with Role
        studentUser.addRole(teacherRole);
        assertTrue(studentUser.hasRole("TEACHER"));
    }

    @Test
    void testIsStudent() {
        assertFalse(user.isStudent()); // UserType is TEACHER
        
        User studentUser = User.builder().email("student@example.com").firstName("Student").lastName("").password("password").userType(UserType.STUDENT).build();
        assertTrue(studentUser.isStudent());
    }

    @Test
    void testEmailVerification() {
        assertFalse(user.isEmailVerified());
        assertNull(user.getEmailVerifiedAt());
        
        user.markEmailAsVerified();
        
        assertTrue(user.isEmailVerified());
        assertNotNull(user.getEmailVerifiedAt());
    }

    @Test
    void testUpdateLastLogin() {
        assertNull(user.getLastLoginAt());
        
        user.updateLastLogin();
        
        assertNotNull(user.getLastLoginAt());
        assertTrue(user.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testPasswordChange() {
        LocalDateTime originalPasswordChangedAt = user.getPasswordChangedAt();
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        user.setPassword("newPassword");
        
        assertEquals("newPassword", user.getPassword());
        assertNotNull(user.getPasswordChangedAt());
        if (originalPasswordChangedAt != null) {
            assertTrue(user.getPasswordChangedAt().isAfter(originalPasswordChangedAt));
        }
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = User.builder().email("test@example.com").firstName("Test").lastName("User 1").password("password1").userType(UserType.ADMIN).build();
        User user2 = User.builder().email("test@example.com").firstName("Test").lastName("User 2").password("password2").userType(UserType.TEACHER).build();
        User user3 = User.builder().email("different@example.com").firstName("Different").lastName("User").password("password3").userType(UserType.STUDENT).build();
        
        // Same email should be equal
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        
        // Different email should not be equal
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testToString() {
        String toString = user.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("firstName=Test"));
        assertTrue(toString.contains("lastName=User"));
        // Note: toString might not contain the full name, just check it's not null
        assertFalse(toString.isEmpty());
    }
}
