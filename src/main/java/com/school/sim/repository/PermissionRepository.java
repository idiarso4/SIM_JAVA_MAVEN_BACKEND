package com.school.sim.repository;

import com.school.sim.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Permission entity operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);

    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by resource with pagination
     */
    Page<Permission> findByResource(String resource, Pageable pageable);

    /**
     * Find permissions by action
     */
    List<Permission> findByAction(String action);

    /**
     * Find permissions by action with pagination
     */
    Page<Permission> findByAction(String action, Pageable pageable);

    /**
     * Find permissions by resource and action
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Find permissions by name containing (case insensitive)
     */
    List<Permission> findByNameContainingIgnoreCase(String name);

    /**
     * Find permissions by name containing with pagination
     */
    Page<Permission> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);

    /**
     * Find permissions assigned to roles
     */
    @Query("SELECT DISTINCT p FROM Permission p WHERE SIZE(p.roles) > 0")
    List<Permission> findPermissionsWithRoles();

    /**
     * Find permissions not assigned to any roles
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0")
    List<Permission> findPermissionsWithoutRoles();

    /**
     * Find permissions by role name
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    List<Permission> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find permissions by user ID (through roles)
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * Find all unique resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.resource IS NOT NULL ORDER BY p.resource")
    List<String> findAllResources();

    /**
     * Find all unique actions
     */
    @Query("SELECT DISTINCT p.action FROM Permission p WHERE p.action IS NOT NULL ORDER BY p.action")
    List<String> findAllActions();

    /**
     * Search permissions by multiple criteria
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:resource IS NULL OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :resource, '%'))) AND " +
           "(:action IS NULL OR LOWER(p.action) LIKE LOWER(CONCAT('%', :action, '%')))")
    Page<Permission> searchPermissions(@Param("name") String name,
                                     @Param("description") String description,
                                     @Param("resource") String resource,
                                     @Param("action") String action,
                                     Pageable pageable);
}
