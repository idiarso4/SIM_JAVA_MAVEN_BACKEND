package com.school.sim.service;

import com.school.sim.dto.request.CreateUserRequest;
import com.school.sim.dto.request.UpdateUserRequest;
import com.school.sim.dto.response.UserResponse;
import com.school.sim.entity.Role;
import com.school.sim.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations
 * Provides methods for user CRUD operations, role management, and profile management
 */
public interface UserService {

    /**
     * Create a new user
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Update an existing user
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    /**
     * Get user by ID
     */
    Optional<UserResponse> getUserById(Long userId);

    /**
     * Get user by email
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * Get user by NIP (for teachers/staff)
     */
    Optional<UserResponse> getUserByNip(String nip);

    /**
     * Get all users with pagination
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Search users by criteria
     */
    Page<UserResponse> searchUsers(String searchTerm, Pageable pageable);

    /**
     * Delete user by ID
     */
    void deleteUser(Long userId);

    /**
     * Activate user account
     */
    void activateUser(Long userId);

    /**
     * Deactivate user account
     */
    void deactivateUser(Long userId);

    /**
     * Assign role to user
     */
    void assignRole(Long userId, Long roleId);

    /**
     * Remove role from user
     */
    void removeRole(Long userId, Long roleId);

    /**
     * Get user's roles
     */
    List<Role> getUserRoles(Long userId);

    /**
     * Update user profile
     */
    UserResponse updateProfile(Long userId, UpdateUserRequest request);

    /**
     * Change user password
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Reset user password (admin function)
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by NIP
     */
    boolean existsByNip(String nip);

    /**
     * Get users by role
     */
    List<UserResponse> getUsersByRole(String roleName);

    /**
     * Get active users count
     */
    long getActiveUsersCount();

    /**
     * Get total users count
     */
    long getTotalUsersCount();

    /**
     * Bulk activate users
     */
    void bulkActivateUsers(List<Long> userIds);

    /**
     * Bulk deactivate users
     */
    void bulkDeactivateUsers(List<Long> userIds);
}
