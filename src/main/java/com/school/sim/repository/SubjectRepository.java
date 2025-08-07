package com.school.sim.repository;

import com.school.sim.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subject entity
 * Provides data access methods for academic subjects
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    /**
     * Find subject by subject code
     */
    Optional<Subject> findByKodeMapel(String kodeMapel);
    
    /**
     * Find all active subjects
     */
    List<Subject> findByIsActiveTrue();
    
    /**
     * Find subjects by name containing search term (case insensitive)
     */
    @Query("SELECT s FROM Subject s WHERE LOWER(s.namaMapel) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.isActive = true")
    List<Subject> findByNamaMapelContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    /**
     * Check if subject code exists
     */
    boolean existsByKodeMapel(String kodeMapel);
    
    /**
     * Find subjects with minimum SKS
     */
    @Query("SELECT s FROM Subject s WHERE s.sks >= :minSks AND s.isActive = true ORDER BY s.sks DESC")
    List<Subject> findByMinimumSks(@Param("minSks") Integer minSks);
}
