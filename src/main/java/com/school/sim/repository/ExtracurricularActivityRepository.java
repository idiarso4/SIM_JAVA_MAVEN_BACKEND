package com.school.sim.repository;

import com.school.sim.entity.ExtracurricularActivity;
import com.school.sim.entity.ExtracurricularActivity.ActivityStatus;
import com.school.sim.entity.ExtracurricularActivity.ActivityType;
import com.school.sim.entity.Student;
import com.school.sim.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


/**
 * Repository interface for ExtracurricularActivity entity
 * Provides data access methods for extracurricular activity management
 */
@Repository
public interface ExtracurricularActivityRepository extends JpaRepository<ExtracurricularActivity, Long> {

    /**
     * Find all active extracurricular activities
     */
    List<ExtracurricularActivity> findByIsActiveTrue();

    /**
     * Find activities by status
     */
    List<ExtracurricularActivity> findByStatusAndIsActiveTrue(ActivityStatus status);

    /**
     * Find activities by type
     */
    List<ExtracurricularActivity> findByTypeAndIsActiveTrue(ActivityType type);

    /**
     * Find activities by supervisor
     */
    List<ExtracurricularActivity> findBySupervisorAndIsActiveTrue(User supervisor);

    /**
     * Find activities by date range
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.activityDate BETWEEN :startDate AND :endDate " +
           "AND ea.isActive = true ORDER BY ea.activityDate ASC, ea.startTime ASC")
    List<ExtracurricularActivity> findByActivityDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find activities by date range with pagination
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.activityDate BETWEEN :startDate AND :endDate " +
           "AND ea.isActive = true ORDER BY ea.activityDate ASC, ea.startTime ASC")
    Page<ExtracurricularActivity> findByActivityDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Find activities by specific date
     */
    List<ExtracurricularActivity> findByActivityDateAndIsActiveTrueOrderByStartTimeAsc(LocalDate activityDate);

    /**
     * Find upcoming activities
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.activityDate >= :currentDate " +
           "AND ea.isActive = true ORDER BY ea.activityDate ASC, ea.startTime ASC")
    List<ExtracurricularActivity> findUpcomingActivities(@Param("currentDate") LocalDate currentDate);

    /**
     * Find activities open for registration
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.status = 'OPEN_FOR_REGISTRATION' " +
           "AND ea.isActive = true AND (ea.registrationDeadline IS NULL OR ea.registrationDeadline >= :currentDate) " +
           "AND (ea.maxParticipants IS NULL OR ea.currentParticipants < ea.maxParticipants) " +
           "ORDER BY ea.activityDate ASC")
    List<ExtracurricularActivity> findActivitiesOpenForRegistration(@Param("currentDate") LocalDate currentDate);

    /**
     * Find activities by academic year and semester
     */
    List<ExtracurricularActivity> findByAcademicYearAndSemesterAndIsActiveTrueOrderByActivityDateAsc(
            String academicYear, Integer semester);

    /**
     * Find activities by student participation
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea JOIN ea.participants p WHERE p = :student " +
           "AND ea.isActive = true ORDER BY ea.activityDate DESC")
    List<ExtracurricularActivity> findByParticipant(@Param("student") Student student);

    /**
     * Find activities by student participation with date range
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea JOIN ea.participants p WHERE p = :student " +
           "AND ea.activityDate BETWEEN :startDate AND :endDate AND ea.isActive = true " +
           "ORDER BY ea.activityDate DESC")
    List<ExtracurricularActivity> findByParticipantAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find mandatory activities
     */
    List<ExtracurricularActivity> findByIsMandatoryTrueAndIsActiveTrueOrderByActivityDateAsc();

    /**
     * Find activities requiring permission
     */
    List<ExtracurricularActivity> findByRequiresPermissionTrueAndIsActiveTrueOrderByActivityDateAsc();

    /**
     * Count activities by status
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularActivity ea WHERE ea.status = :status AND ea.isActive = true")
    Long countByStatus(@Param("status") ActivityStatus status);

    /**
     * Count activities by type
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularActivity ea WHERE ea.type = :type AND ea.isActive = true")
    Long countByType(@Param("type") ActivityType type);

    /**
     * Count activities by supervisor
     */
    @Query("SELECT COUNT(ea) FROM ExtracurricularActivity ea WHERE ea.supervisor = :supervisor AND ea.isActive = true")
    Long countBySupervisor(@Param("supervisor") User supervisor);

    /**
     * Find activities with available spots
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.isActive = true " +
           "AND (ea.maxParticipants IS NULL OR ea.currentParticipants < ea.maxParticipants) " +
           "AND ea.status = 'OPEN_FOR_REGISTRATION' ORDER BY ea.activityDate ASC")
    List<ExtracurricularActivity> findActivitiesWithAvailableSpots();

    /**
     * Find activities by name (case-insensitive search)
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE LOWER(ea.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND ea.isActive = true ORDER BY ea.name ASC")
    List<ExtracurricularActivity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find activities by location
     */
    List<ExtracurricularActivity> findByLocationContainingIgnoreCaseAndIsActiveTrueOrderByActivityDateAsc(String location);

    /**
     * Find activities with registration deadline approaching
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.registrationDeadline BETWEEN :startDate AND :endDate " +
           "AND ea.status = 'OPEN_FOR_REGISTRATION' AND ea.isActive = true " +
           "ORDER BY ea.registrationDeadline ASC")
    List<ExtracurricularActivity> findActivitiesWithUpcomingDeadlines(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find popular activities (by participation count)
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.isActive = true " +
           "ORDER BY ea.currentParticipants DESC")
    List<ExtracurricularActivity> findPopularActivities(Pageable pageable);

    /**
     * Find activities by multiple criteria (for advanced search)
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.isActive = true " +
           "AND (:type IS NULL OR ea.type = :type) " +
           "AND (:status IS NULL OR ea.status = :status) " +
           "AND (:supervisor IS NULL OR ea.supervisor = :supervisor) " +
           "AND (:startDate IS NULL OR ea.activityDate >= :startDate) " +
           "AND (:endDate IS NULL OR ea.activityDate <= :endDate) " +
           "AND (:isMandatory IS NULL OR ea.isMandatory = :isMandatory) " +
           "ORDER BY ea.activityDate ASC, ea.startTime ASC")
    Page<ExtracurricularActivity> findByMultipleCriteria(
            @Param("type") ActivityType type,
            @Param("status") ActivityStatus status,
            @Param("supervisor") User supervisor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isMandatory") Boolean isMandatory,
            Pageable pageable
    );

    /**
     * Check if student is already registered for activity
     */
    @Query("SELECT COUNT(ea) > 0 FROM ExtracurricularActivity ea JOIN ea.participants p " +
           "WHERE ea.id = :activityId AND p = :student")
    boolean isStudentRegistered(@Param("activityId") Long activityId, @Param("student") Student student);

    /**
     * Get activity statistics
     */
    @Query("SELECT new map(" +
           "COUNT(ea) as totalActivities, " +
           "SUM(ea.currentParticipants) as totalParticipants, " +
           "AVG(ea.currentParticipants) as averageParticipants" +
           ") FROM ExtracurricularActivity ea WHERE ea.isActive = true")
    List<Object> getActivityStatistics();

    /**
     * Find conflicting activities (same date, time, and location)
     */
    @Query("SELECT ea FROM ExtracurricularActivity ea WHERE ea.activityDate = :date " +
           "AND ea.location = :location AND ea.isActive = true " +
           "AND ((ea.startTime <= :startTime AND ea.endTime > :startTime) " +
           "OR (ea.startTime < :endTime AND ea.endTime >= :endTime) " +
           "OR (ea.startTime >= :startTime AND ea.endTime <= :endTime))")
    List<ExtracurricularActivity> findConflictingActivities(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("location") String location
    );
}