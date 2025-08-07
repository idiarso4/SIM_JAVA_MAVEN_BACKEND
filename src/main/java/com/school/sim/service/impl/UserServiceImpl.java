package com.school.sim.service.impl;

import com.school.sim.dto.request.CreateUserRequest;
import com.school.sim.dto.request.UpdateUserRequest;
import com.school.sim.dto.response.UserResponse;
import com.school.sim.entity.Role;
import com.school.sim.entity.User;
import com.school.sim.exception.ResourceNotFoundException;
import com.school.sim.exception.ValidationException;
import com.school.sim.repository.RoleRepository;
import com.school.sim.repository.UserRepository;
import com.school.sim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserService for user management operations
 * Provides comprehensive user CRUD operations, role management, and profile management
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        // Validate unique constraints
        validateUniqueConstraints(request.getEmail(), request.getNip(), null);

        // Create new user entity
        User user = new User();
        String[] nameParts = request.getName().split(" ", 2);
        user.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            user.setLastName(nameParts[1]);
        } else {
            user.setLastName("");
        }
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNip(request.getNip());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setIsActive(request.isActive());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("Successfully created user with ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", userId);

        User user = findUserById(userId);

        // Validate unique constraints if email or NIP is being updated
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            validateUniqueConstraints(request.getEmail(), null, userId);
        }
        if (StringUtils.hasText(request.getNip()) && !request.getNip().equals(user.getNip())) {
            validateUniqueConstraints(null, request.getNip(), userId);
        }

        // Update user fields
        if (StringUtils.hasText(request.getName())) {
            String[] nameParts = request.getName().split(" ", 2);
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            } else {
                user.setLastName("");
            }
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getNip())) {
            user.setNip(request.getNip());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        // Note: Address field may not exist in User entity
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        if (request.getActive() != null) {
            user.setIsActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated user with ID: {}", updatedUser.getId());

        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
            .map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
            .map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByNip(String nip) {
        logger.debug("Fetching user by NIP: {}", nip);
        return userRepository.findByNip(nip)
            .map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
            .map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        logger.debug("Searching users with term: {} and pagination: {}", searchTerm, pageable);
        // Use searchUsers method from repository for combined name/email search
        return userRepository.searchUsers(searchTerm, searchTerm, null, null, pageable)
            .map(UserResponse::from);
    }

    @Override
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);

        User user = findUserById(userId);
        userRepository.delete(user);

        logger.info("Successfully deleted user with ID: {}", userId);
    }

    @Override
    public void activateUser(Long userId) {
        logger.info("Activating user with ID: {}", userId);

        User user = findUserById(userId);
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Successfully activated user with ID: {}", userId);
    }

    @Override
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user with ID: {}", userId);

        User user = findUserById(userId);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Successfully deactivated user with ID: {}", userId);
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        logger.info("Assigning role {} to user {}", roleId, userId);

        User user = findUserById(userId);
        Role role = findRoleById(roleId);

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Successfully assigned role {} to user {}", roleId, userId);
        } else {
            logger.warn("User {} already has role {}", userId, roleId);
        }
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        logger.info("Removing role {} from user {}", roleId, userId);

        User user = findUserById(userId);
        Role role = findRoleById(roleId);

        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Successfully removed role {} from user {}", roleId, userId);
        } else {
            logger.warn("User {} does not have role {}", userId, roleId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        logger.debug("Fetching roles for user: {}", userId);
        User user = findUserById(userId);
        return user.getRoles().stream().collect(Collectors.toList());
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateUserRequest request) {
        logger.info("Updating profile for user: {}", userId);
        // Profile updates typically exclude sensitive fields like userType
        UpdateUserRequest profileRequest = new UpdateUserRequest();
        profileRequest.setName(request.getName());
        profileRequest.setPhone(request.getPhone());
        profileRequest.setAddress(request.getAddress());
        
        return updateUser(userId, profileRequest);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", userId);

        User user = findUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Successfully changed password for user: {}", userId);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        logger.info("Resetting password for user: {}", userId);

        User user = findUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Successfully reset password for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNip(String nip) {
        return userRepository.existsByNip(nip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        logger.debug("Fetching users by role: {}", roleName);
        return userRepository.findByRoleName(roleName).stream()
            .map(UserResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public void bulkActivateUsers(List<Long> userIds) {
        logger.info("Bulk activating {} users", userIds.size());
        
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> {
            user.setIsActive(true);
            user.setUpdatedAt(LocalDateTime.now());
        });
        
        userRepository.saveAll(users);
        logger.info("Successfully activated {} users", users.size());
    }

    @Override
    public void bulkDeactivateUsers(List<Long> userIds) {
        logger.info("Bulk deactivating {} users", userIds.size());
        
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> {
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
        });
        
        userRepository.saveAll(users);
        logger.info("Successfully deactivated {} users", users.size());
    }

    // Helper methods

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
    }

    private void validateUniqueConstraints(String email, String nip, Long excludeUserId) {
        if (StringUtils.hasText(email)) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && 
                (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId))) {
                throw new ValidationException("Email already exists: " + email);
            }
        }

        if (StringUtils.hasText(nip)) {
            Optional<User> existingUser = userRepository.findByNip(nip);
            if (existingUser.isPresent() && 
                (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId))) {
                throw new ValidationException("NIP already exists: " + nip);
            }
        }
    }
}
