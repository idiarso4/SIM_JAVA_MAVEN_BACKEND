package com.school.sim.repository;

import com.school.sim.entity.User;
import com.school.sim.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by NIP (for teachers and admin)
     */
    Optional<User> findByNip(String nip);

    /**
     * Find users by user type
     */
    List<User> findByUserType(UserType userType);

    /**
     * Find users by user type with pagination
     */
    Page<User> findByUserType(UserType userType, Pageable pageable);

    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find active users with pagination
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * Find inactive users
     */
    List<User> findByIsActiveFalse();

    /**
     * Find users by first name containing (case insensitive)
     */
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    
    /**
     * Find users by last name containing (case insensitive)
     */
    Page<User> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    /**
     * Find users by email containing (case insensitive)
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Find users by user type and active status
     */
    List<User> findByUserTypeAndIsActive(UserType userType, Boolean isActive);

    /**
     * Find users by user type and active status with pagination
     */
    Page<User> findByUserTypeAndIsActive(UserType userType, Boolean isActive, Pageable pageable);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if NIP exists
     */
    boolean existsByNip(String nip);

    /**
     * Find users who haven't logged in since a specific date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :date")
    List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);

    /**
     * Find users with unverified emails
     */
    @Query("SELECT u FROM User u WHERE u.emailVerifiedAt IS NULL")
    List<User> findUsersWithUnverifiedEmails();

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find users by role name with pagination
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Find users with specific permission
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    List<User> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Count users by user type
     */
    long countByUserType(UserType userType);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Count inactive users
     */
    long countByIsActiveFalse();

    /**
     * Find users created between dates
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Search users by multiple criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:userType IS NULL OR u.userType = :userType) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> searchUsers(@Param("name") String name,
                          @Param("email") String email,
                          @Param("userType") UserType userType,
                          @Param("isActive") Boolean isActive,
                          Pageable pageable);

    /**
     * Find teachers (users with TEACHER user type)
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'TEACHER' AND u.isActive = true")
    List<User> findActiveTeachers();

    /**
     * Find teachers with pagination
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'TEACHER' AND u.isActive = true")
    Page<User> findActiveTeachers(Pageable pageable);

    /**
     * Find teachers by NIP pattern
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'TEACHER' AND u.nip LIKE CONCAT('%', :nipPattern, '%')")
    List<User> findTeachersByNipPattern(@Param("nipPattern") String nipPattern);

    /**
     * Find users for authentication (active users only)
     */
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.nip = :identifier) AND u.isActive = true")
    Optional<User> findByEmailOrNipForAuthentication(@Param("identifier") String identifier);

    /**
     * Find teachers available for homeroom assignment (not assigned to any class)
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'TEACHER' AND u.isActive = true " +
           "AND u.id NOT IN (SELECT c.homeroomTeacher.id FROM ClassRoom c WHERE c.homeroomTeacher IS NOT NULL)")
    List<User> findAvailableHomeroomTeachers();

    /**
     * Find teachers assigned as homeroom teachers
     */
    @Query("SELECT DISTINCT u FROM User u JOIN ClassRoom c ON u.id = c.homeroomTeacher.id WHERE u.isActive = true")
    List<User> findHomeroomTeachers();

    /**
     * Find users by phone number
     */
    Optional<User> findByPhone(String phone);

    /**
     * Find users with expired password reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken IS NOT NULL AND u.passwordResetExpires < :currentTime")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find users by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Count teachers
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'TEACHER' AND u.isActive = true")
    long countActiveTeachers();

    /**
     * Count administrators
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'ADMIN' AND u.isActive = true")
    long countActiveAdministrators();

    /**
     * Find recently registered users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegisteredUsers(@Param("since") LocalDateTime since);

    /**
     * Find users who need to verify their email
     */
    @Query("SELECT u FROM User u WHERE u.emailVerifiedAt IS NULL AND u.createdAt < :cutoffTime")
    List<User> findUsersNeedingEmailVerification(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Search users by type and query (name or email)
     */
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> findByUserTypeAndQuery(@Param("userType") UserType userType,
                                     @Param("query") String query,
                                     Pageable pageable);

    /**
     * Count users by type and active status
     */
    long countByUserTypeAndIsActive(UserType userType, Boolean isActive);

    /**
     * Count users by type created after date
     */
    long countByUserTypeAndCreatedAtAfter(UserType userType, LocalDateTime date);
}
