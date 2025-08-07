package com.school.sim.repository;

import com.school.sim.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);

    /**
     * Find roles by name containing (case insensitive)
     */
    List<Role> findByNameContainingIgnoreCase(String name);

    /**
     * Find roles by name containing with pagination
     */
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find system roles
     */
    List<Role> findByIsSystemRoleTrue();

    /**
     * Find non-system roles
     */
    List<Role> findByIsSystemRoleFalse();

    /**
     * Find non-system roles with pagination
     */
    Page<Role> findByIsSystemRoleFalse(Pageable pageable);

    /**
     * Check if role name exists
     */
    boolean existsByName(String name);

    /**
     * Find roles with specific permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Find roles assigned to users
     */
    @Query("SELECT DISTINCT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();

    /**
     * Find roles not assigned to any users
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    /**
     * Count roles by system role flag
     */
    long countByIsSystemRole(Boolean isSystemRole);

    /**
     * Find roles by user ID
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * Search roles by name and description
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:isSystemRole IS NULL OR r.isSystemRole = :isSystemRole)")
    Page<Role> searchRoles(@Param("name") String name,
                          @Param("description") String description,
                          @Param("isSystemRole") Boolean isSystemRole,
                          Pageable pageable);
}
