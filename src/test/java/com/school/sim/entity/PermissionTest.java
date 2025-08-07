package com.school.sim.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Permission entity
 */
class PermissionTest {

    private Permission permission;
    private Role role;

    @BeforeEach
    void setUp() {
        permission = new Permission("USER_CREATE", "Create user permission");
        role = new Role("ADMIN", "Administrator role");
    }

    @Test
    void testPermissionCreation() {
        assertNotNull(permission);
        assertEquals("USER_CREATE", permission.getName());
        assertEquals("Create user permission", permission.getDescription());
        assertNull(permission.getResource());
        assertNull(permission.getAction());
        assertNotNull(permission.getRoles());
        assertTrue(permission.getRoles().isEmpty());
    }

    @Test
    void testPermissionCreationWithResourceAndAction() {
        Permission fullPermission = new Permission("USER_CREATE", "Create user permission", "users", "create");
        
        assertEquals("USER_CREATE", fullPermission.getName());
        assertEquals("Create user permission", fullPermission.getDescription());
        assertEquals("users", fullPermission.getResource());
        assertEquals("create", fullPermission.getAction());
    }

    @Test
    void testAddRole() {
        permission.addRole(role);
        
        assertTrue(permission.getRoles().contains(role));
        assertTrue(role.getPermissions().contains(permission));
        assertEquals(1, permission.getRoles().size());
    }

    @Test
    void testRemoveRole() {
        permission.addRole(role);
        permission.removeRole(role);
        
        assertFalse(permission.getRoles().contains(role));
        assertFalse(role.getPermissions().contains(permission));
        assertEquals(0, permission.getRoles().size());
    }

    @Test
    void testEqualsAndHashCode() {
        Permission permission1 = new Permission("USER_CREATE", "Create user permission 1");
        Permission permission2 = new Permission("USER_CREATE", "Create user permission 2");
        Permission permission3 = new Permission("USER_DELETE", "Delete user permission");
        
        // Same name should be equal
        assertEquals(permission1, permission2);
        assertEquals(permission1.hashCode(), permission2.hashCode());
        
        // Different name should not be equal
        assertNotEquals(permission1, permission3);
        assertNotEquals(permission1.hashCode(), permission3.hashCode());
    }

    @Test
    void testToString() {
        Permission fullPermission = new Permission("USER_CREATE", "Create user permission", "users", "create");
        String toString = fullPermission.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("USER_CREATE"));
        assertTrue(toString.contains("Create user permission"));
        assertTrue(toString.contains("users"));
        assertTrue(toString.contains("create"));
    }
}
