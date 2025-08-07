package com.school.sim.repository;

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
 * Repository interface for Department entity
 * Provides data access methods for academic departments
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by code
     */
    Optional<Department> findByCode(String code);
    
    /**
     * Find department by name
     */
    Optional<Department> findByName(String name);
    
    /**
     * Find all active departments
     */
    List<Department> findByIsActiveTrue();
    
    /**
     * Find all active departments with pagination
     */
    Page<Department> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find departments by name containing search term (case insensitive)
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND d.isActive = true")
    List<Department> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    /**
     * Find departments by name containing search term with pagination
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND d.isActive = true")
    Page<Department> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Check if department code exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if department name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active departments
     */
    long countByIsActiveTrue();
    
    /**
     * Find departments with majors count
     */
    @Query("SELECT d, COUNT(m) FROM Department d LEFT JOIN d.majors m WHERE d.isActive = true GROUP BY d ORDER BY d.name")
    List<Object[]> findDepartmentsWithMajorCount();
    
    /**
     * Find departments that have majors
     */
    @Query("SELECT DISTINCT d FROM Department d JOIN d.majors m WHERE d.isActive = true ORDER BY d.name")
    List<Department> findDepartmentsWithMajors();
    
    /**
     * Find departments without majors
     */
    @Query("SELECT d FROM Department d WHERE d.isActive = true AND d.majors IS EMPTY ORDER BY d.name")
    List<Department> findDepartmentsWithoutMajors();
    
    /**
     * Search departments by multiple criteria
     */
    @Query("SELECT d FROM Department d WHERE " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive)")
    Page<Department> searchDepartments(@Param("name") String name,
                                     @Param("code") String code,
                                     @Param("isActive") Boolean isActive,
                                     Pageable pageable);
}
