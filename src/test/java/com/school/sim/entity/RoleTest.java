package com.school.sim.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role entity
 */
class RoleTest {

    private Role role;
    private User user;
    private Permission permission;

    @BeforeEach
    void setUp() {
        role = new Role("ADMIN", "Administrator role");
        user = User.builder()
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .password("password")
                .userType(UserType.ADMIN)
                .build();
        permission = new Permission("USER_CREATE", "Create user permission");
    }

    @Test
    void testRoleCreation() {
        assertNotNull(role);
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertFalse(role.getIsSystemRole());
        assertNotNull(role.getUsers());
        assertNotNull(role.getPermissions());
        assertTrue(role.getUsers().isEmpty());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void testRoleCreationWithSystemFlag() {
        Role systemRole = new Role("SYSTEM_ADMIN", "System administrator", true);
        
        assertEquals("SYSTEM_ADMIN", systemRole.getName());
        assertEquals("System administrator", systemRole.getDescription());
        assertTrue(systemRole.getIsSystemRole());
    }

    @Test
    void testAddUser() {
        role.addUser(user);
        
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRoles().contains(role));
        assertEquals(1, role.getUsers().size());
    }

    @Test
    void testRemoveUser() {
        role.addUser(user);
        role.removeUser(user);
        
        assertFalse(role.getUsers().contains(user));
        assertFalse(user.getRoles().contains(role));
        assertEquals(0, role.getUsers().size());
    }

    @Test
    void testAddPermission() {
        role.addPermission(permission);
        
        assertTrue(role.getPermissions().contains(permission));
        assertTrue(permission.getRoles().contains(role));
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    void testRemovePermission() {
        role.addPermission(permission);
        role.removePermission(permission);
        
        assertFalse(role.getPermissions().contains(permission));
        assertFalse(permission.getRoles().contains(role));
        assertEquals(0, role.getPermissions().size());
    }

    @Test
    void testHasPermission() {
        assertFalse(role.hasPermission("USER_CREATE"));
        
        role.addPermission(permission);
        
        assertTrue(role.hasPermission("USER_CREATE"));
        assertFalse(role.hasPermission("USER_DELETE"));
    }

    @Test
    void testEqualsAndHashCode() {
        Role role1 = new Role("ADMIN", "Administrator role 1");
        Role role2 = new Role("ADMIN", "Administrator role 2");
        Role role3 = new Role("TEACHER", "Teacher role");
        
        // Same name should be equal
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
        
        // Different name should not be equal
        assertNotEquals(role1, role3);
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void testToString() {
        String toString = role.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ADMIN"));
        assertTrue(toString.contains("Administrator role"));
        assertTrue(toString.contains("false")); // isSystemRole
    }
}
