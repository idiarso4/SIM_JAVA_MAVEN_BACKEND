package com.school.sim.repository;

import com.school.sim.entity.Major;
import com.school.sim.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Major entity
 * Provides data access methods for academic majors/programs
 */
@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    
    /**
     * Find major by code
     */
    Optional<Major> findByCode(String code);
    
    /**
     * Find major by name
     */
    Optional<Major> findByName(String name);
    
    /**
     * Find majors by department
     */
    List<Major> findByDepartmentAndIsActiveTrue(Department department);
    
    /**
     * Find majors by department with pagination
     */
    Page<Major> findByDepartmentAndIsActiveTrue(Department department, Pageable pageable);
    
    /**
     * Find majors by department ID
     */
    @Query("SELECT m FROM Major m WHERE m.department.id = :departmentId AND m.isActive = true")
    List<Major> findByDepartmentIdAndIsActiveTrue(@Param("departmentId") Long departmentId);
    
    /**
     * Find all active majors
     */
    List<Major> findByIsActiveTrue();
    
    /**
     * Find all active majors with pagination
     */
    Page<Major> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find majors by name containing search term (case insensitive)
     */
    @Query("SELECT m FROM Major m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND m.isActive = true")
    List<Major> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    /**
     * Find majors by name containing search term with pagination
     */
    @Query("SELECT m FROM Major m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND m.isActive = true")
    Page<Major> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Check if major code exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if major name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active majors
     */
    long countByIsActiveTrue();
    
    /**
     * Count majors by department
     */
    long countByDepartmentAndIsActiveTrue(Department department);
    
    /**
     * Find majors with class rooms count
     */
    @Query("SELECT m, COUNT(c) FROM Major m LEFT JOIN m.classRooms c WHERE m.isActive = true GROUP BY m ORDER BY m.name")
    List<Object[]> findMajorsWithClassRoomCount();
    
    /**
     * Find majors that have class rooms
     */
    @Query("SELECT DISTINCT m FROM Major m JOIN m.classRooms c WHERE m.isActive = true ORDER BY m.name")
    List<Major> findMajorsWithClassRooms();
    
    /**
     * Find majors without class rooms
     */
    @Query("SELECT m FROM Major m WHERE m.isActive = true AND m.classRooms IS EMPTY ORDER BY m.name")
    List<Major> findMajorsWithoutClassRooms();
    
    /**
     * Find majors by department name
     */
    @Query("SELECT m FROM Major m WHERE LOWER(m.department.name) LIKE LOWER(CONCAT('%', :departmentName, '%')) AND m.isActive = true")
    List<Major> findByDepartmentNameContainingIgnoreCase(@Param("departmentName") String departmentName);
    
    /**
     * Search majors by multiple criteria
     */
    @Query("SELECT m FROM Major m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(m.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:departmentId IS NULL OR m.department.id = :departmentId) AND " +
           "(:isActive IS NULL OR m.isActive = :isActive)")
    Page<Major> searchMajors(@Param("name") String name,
                           @Param("code") String code,
                           @Param("departmentId") Long departmentId,
                           @Param("isActive") Boolean isActive,
                           Pageable pageable);
}
