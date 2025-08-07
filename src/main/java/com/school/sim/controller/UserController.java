package com.school.sim.controller;

import com.school.sim.dto.request.CreateUserRequest;
import com.school.sim.dto.request.UpdateUserRequest;
import com.school.sim.dto.response.UserResponse;
import com.school.sim.entity.Role;
import com.school.sim.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for user management operations
 * Provides endpoints for user CRUD operations, role management, and profile management
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User management endpoints")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Create a new user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());
        
        try {
            UserResponse response = userService.createUser(request);
            logger.info("Successfully created user with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to create user with email: {}", request.getEmail(), e);
            throw e;
        }
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Fetching all users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserResponse> users = userService.getAllUsers(pageable);
        logger.debug("Retrieved {} users", users.getTotalElements());
        
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") @NotNull Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        
        Optional<UserResponse> user = userService.getUserById(userId);
        if (user.isPresent()) {
            logger.debug("User found with ID: {}", userId);
            return ResponseEntity.ok(user.get());
        } else {
            logger.debug("User not found with ID: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") @NotNull Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        
        logger.info("Updating user with ID: {}", userId);
        
        try {
            UserResponse response = userService.updateUser(userId, request);
            logger.info("Successfully updated user with ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update user with ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") @NotNull Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        
        try {
            userService.deleteUser(userId);
            logger.info("Successfully deleted user with ID: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete user with ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Searching users with term: {}", searchTerm);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserResponse> users = userService.searchUsers(searchTerm, pageable);
        logger.debug("Found {} users matching search term: {}", users.getTotalElements(), searchTerm);
        
        return ResponseEntity.ok(users);
    }

    /**
     * Activate user
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activate user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User activated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable("id") @NotNull Long userId) {
        logger.info("Activating user with ID: {}", userId);
        
        try {
            userService.activateUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User activated successfully");
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully activated user with ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to activate user with ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * Deactivate user
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable("id") @NotNull Long userId) {
        logger.info("Deactivating user with ID: {}", userId);
        
        try {
            userService.deactivateUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deactivated successfully");
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully deactivated user with ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to deactivate user with ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * Assign role to user
     */
    @PostMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Assign role", description = "Assign role to user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> assignRole(
            @PathVariable("id") @NotNull Long userId,
            @PathVariable("roleId") @NotNull Long roleId) {
        
        logger.info("Assigning role {} to user {}", roleId, userId);
        
        try {
            userService.assignRole(userId, roleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Role assigned successfully");
            response.put("userId", userId);
            response.put("roleId", roleId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully assigned role {} to user {}", roleId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to assign role {} to user {}", roleId, userId, e);
            throw e;
        }
    }

    /**
     * Remove role from user
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Remove role", description = "Remove role from user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role removed successfully"),
        @ApiResponse(responseCode = "404", description = "User or role not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> removeRole(
            @PathVariable("id") @NotNull Long userId,
            @PathVariable("roleId") @NotNull Long roleId) {
        
        logger.info("Removing role {} from user {}", roleId, userId);
        
        try {
            userService.removeRole(userId, roleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Role removed successfully");
            response.put("userId", userId);
            response.put("roleId", roleId);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully removed role {} from user {}", roleId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to remove role {} from user {}", roleId, userId, e);
            throw e;
        }
    }

    /**
     * Get user roles
     */
    @GetMapping("/{id}/roles")
    @Operation(summary = "Get user roles", description = "Get all roles assigned to user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable("id") @NotNull Long userId) {
        logger.debug("Fetching roles for user: {}", userId);
        
        try {
            List<Role> roles = userService.getUserRoles(userId);
            logger.debug("Retrieved {} roles for user: {}", roles.size(), userId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Failed to get roles for user: {}", userId, e);
            throw e;
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/by-role/{roleName}")
    @Operation(summary = "Get users by role", description = "Get all users with specific role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable("roleName") String roleName) {
        logger.debug("Fetching users by role: {}", roleName);
        
        try {
            List<UserResponse> users = userService.getUsersByRole(roleName);
            logger.debug("Retrieved {} users with role: {}", users.size(), roleName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Failed to get users by role: {}", roleName, e);
            throw e;
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get user count statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        logger.debug("Fetching user statistics");
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", userService.getTotalUsersCount());
            statistics.put("activeUsers", userService.getActiveUsersCount());
            statistics.put("inactiveUsers", userService.getTotalUsersCount() - userService.getActiveUsersCount());
            statistics.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Retrieved user statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Failed to get user statistics", e);
            throw e;
        }
    }

    /**
     * Bulk activate users
     */
    @PostMapping("/bulk/activate")
    @Operation(summary = "Bulk activate users", description = "Activate multiple users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users activated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkActivateUsers(@RequestBody List<Long> userIds) {
        logger.info("Bulk activating {} users", userIds.size());
        
        try {
            userService.bulkActivateUsers(userIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users activated successfully");
            response.put("count", userIds.size());
            response.put("userIds", userIds);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully bulk activated {} users", userIds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to bulk activate users", e);
            throw e;
        }
    }

    /**
     * Bulk deactivate users
     */
    @PostMapping("/bulk/deactivate")
    @Operation(summary = "Bulk deactivate users", description = "Deactivate multiple users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users deactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkDeactivateUsers(@RequestBody List<Long> userIds) {
        logger.info("Bulk deactivating {} users", userIds.size());
        
        try {
            userService.bulkDeactivateUsers(userIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users deactivated successfully");
            response.put("count", userIds.size());
            response.put("userIds", userIds);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully bulk deactivated {} users", userIds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to bulk deactivate users", e);
            throw e;
        }
    }
}