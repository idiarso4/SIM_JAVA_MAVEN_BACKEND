package com.school.sim.controller;

import com.school.sim.dto.response.UserResponse;
import com.school.sim.entity.User;
import com.school.sim.entity.UserType;
import com.school.sim.repository.UserRepository;
import com.school.sim.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for teacher management operations
 * Provides endpoints for teacher CRUD operations, search, filtering, and statistics
 */
@RestController
@RequestMapping("/api/v1/teachers")
@Tag(name = "Teacher Management", description = "Teacher management endpoints")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all teachers with pagination and sorting
     */
    @GetMapping
    @Operation(summary = "Get all teachers", description = "Retrieve all teachers with pagination and sorting")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserResponse>> getAllTeachers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "firstName") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.info("Teachers list request received - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                   page, size, sortBy, sortDir);

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<UserResponse> teachers = userService.getUsersByType(UserType.TEACHER, pageable);
            
            logger.info("Teachers retrieved successfully - total: {}, page: {}", 
                       teachers.getTotalElements(), teachers.getNumber());
            
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            logger.error("Error retrieving teachers", e);
            throw e;
        }
    }

    /**
     * Get teacher by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID", description = "Retrieve a specific teacher by their ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<UserResponse> getTeacherById(@PathVariable("id") @NotNull Long teacherId) {
        logger.debug("Fetching teacher by ID: {}", teacherId);
        
        return userService.getUserById(teacherId)
                .filter(user -> user.getUserType() == UserType.TEACHER)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search teachers by name or email
     */
    @GetMapping("/search")
    @Operation(summary = "Search teachers", description = "Search teachers by name or email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserResponse>> searchTeachers(
            @Parameter(description = "Search query")
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        logger.info("Teacher search request - query: '{}', page: {}, size: {}", query, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName"));
            Page<UserResponse> teachers = userService.searchUsersByTypeAndQuery(UserType.TEACHER, query, pageable);
            
            logger.info("Teacher search completed - found: {} teachers", teachers.getTotalElements());
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            logger.error("Error searching teachers with query: {}", query, e);
            throw e;
        }
    }

    /**
     * Get active teachers only
     */
    @GetMapping("/active")
    @Operation(summary = "Get active teachers", description = "Retrieve only active teachers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserResponse>> getActiveTeachers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        logger.info("Active teachers request received - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName"));
            Page<UserResponse> teachers = userService.getActiveUsersByType(UserType.TEACHER, pageable);
            
            logger.info("Active teachers retrieved successfully - total: {}", teachers.getTotalElements());
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            logger.error("Error retrieving active teachers", e);
            throw e;
        }
    }

    /**
     * Get teacher statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get teacher statistics", description = "Retrieve teacher statistics and metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherStats() {
        logger.info("Teacher stats request received");

        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Basic counts
            long totalTeachers = userRepository.countByUserType(UserType.TEACHER);
            long activeTeachers = userRepository.countByUserTypeAndIsActive(UserType.TEACHER, true);
            long inactiveTeachers = totalTeachers - activeTeachers;
            
            stats.put("totalTeachers", totalTeachers);
            stats.put("activeTeachers", activeTeachers);
            stats.put("inactiveTeachers", inactiveTeachers);
            
            // Recent additions (last 30 days)
            java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
            long recentTeachers = userRepository.countByUserTypeAndCreatedAtAfter(UserType.TEACHER, thirtyDaysAgo);
            stats.put("recentTeachers", recentTeachers);
            
            // Department distribution (placeholder - can be enhanced with actual department data)
            Map<String, Long> departmentStats = new HashMap<>();
            departmentStats.put("General", totalTeachers);
            stats.put("byDepartment", departmentStats);
            
            logger.info("Teacher stats retrieved successfully");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving teacher stats", e);
            throw e;
        }
    }

    /**
     * Get teachers by department (placeholder implementation)
     */
    @GetMapping("/department/{department}")
    @Operation(summary = "Get teachers by department", description = "Retrieve teachers by department")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<UserResponse>> getTeachersByDepartment(
            @PathVariable("department") String department) {
        
        logger.info("Teachers by department request - department: {}", department);
        
        try {
            // For now, return all teachers as we don't have department field
            List<User> teachers = userRepository.findByUserType(UserType.TEACHER);
            List<UserResponse> teacherResponses = teachers.stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());
            
            logger.info("Teachers by department retrieved - count: {}", teacherResponses.size());
            return ResponseEntity.ok(teacherResponses);
        } catch (Exception e) {
            logger.error("Error retrieving teachers by department: {}", department, e);
            throw e;
        }
    }
}
