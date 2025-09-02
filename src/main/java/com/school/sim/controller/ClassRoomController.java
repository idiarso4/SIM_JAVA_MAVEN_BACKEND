package com.school.sim.controller;

import com.school.sim.entity.ClassRoom;
import com.school.sim.repository.ClassRoomRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for classroom management operations
 * Provides endpoints for classroom CRUD operations, search, and filtering
 */
@RestController
@RequestMapping("/api/v1/classrooms")
@Tag(name = "Classroom Management", description = "Classroom management endpoints")
@Validated
public class ClassRoomController {

    private static final Logger logger = LoggerFactory.getLogger(ClassRoomController.class);

    @Autowired
    private ClassRoomRepository classRoomRepository;

    /**
     * Get all classrooms with pagination
     */
    @GetMapping
    @Operation(summary = "Get all classrooms", description = "Retrieve all classrooms with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Classrooms retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<ClassRoom>> getAllClassRooms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Fetching all classrooms - page: {}, size: {}", page, size);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ClassRoom> classRooms = classRoomRepository.findAll(pageable);
        
        logger.debug("Retrieved {} classrooms", classRooms.getTotalElements());
        return ResponseEntity.ok(classRooms);
    }

    /**
     * Get classroom by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get classroom by ID", description = "Retrieve a specific classroom by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Classroom retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Classroom not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassRoom> getClassRoomById(@PathVariable("id") @NotNull Long classRoomId) {
        logger.debug("Fetching classroom with ID: {}", classRoomId);
        
        return classRoomRepository.findById(classRoomId)
                .map(classRoom -> {
                    logger.debug("Found classroom: {}", classRoom.getName());
                    return ResponseEntity.ok(classRoom);
                })
                .orElseGet(() -> {
                    logger.warn("Classroom not found with ID: {}", classRoomId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get classrooms by grade
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "Get classrooms by grade", description = "Retrieve all classrooms for a specific grade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Classrooms retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<ClassRoom>> getClassRoomsByGrade(
            @PathVariable("grade") @NotNull Integer grade,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching classrooms for grade: {}", grade);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<ClassRoom> classRooms = classRoomRepository.findByGrade(grade, pageable);
        
        logger.debug("Retrieved {} classrooms for grade {}", classRooms.getTotalElements(), grade);
        return ResponseEntity.ok(classRooms);
    }

    /**
     * Get active classrooms
     */
    @GetMapping("/active")
    @Operation(summary = "Get active classrooms", description = "Retrieve all active classrooms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active classrooms retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<ClassRoom>> getActiveClassRooms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching active classrooms");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "grade", "name"));
        Page<ClassRoom> classRooms = classRoomRepository.findByIsActiveTrue(pageable);
        
        logger.debug("Retrieved {} active classrooms", classRooms.getTotalElements());
        return ResponseEntity.ok(classRooms);
    }

    /**
     * Get classroom statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get classroom statistics", description = "Retrieve classroom statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getClassRoomStats() {
        logger.info("Classroom stats request received");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalCount = classRoomRepository.count();
            long activeCount = classRoomRepository.countByIsActiveTrue();
            List<ClassRoom> availableSpaceClassRooms = classRoomRepository.findClassRoomsWithAvailableSpace();
            List<ClassRoom> fullClassRooms = classRoomRepository.findFullClassRooms();
            
            stats.put("totalCount", totalCount);
            stats.put("activeCount", activeCount);
            stats.put("availableSpaceCount", availableSpaceClassRooms.size());
            stats.put("fullClassRoomsCount", fullClassRooms.size());
            stats.put("lastUpdated", LocalDateTime.now());
            
            // Grade statistics
            Map<Integer, Long> gradeStats = new HashMap<>();
            for (int grade = 10; grade <= 12; grade++) {
                long count = classRoomRepository.countByGrade(grade);
                gradeStats.put(grade, count);
            }
            stats.put("gradeStats", gradeStats);
            
            logger.info("Classroom stats retrieved successfully");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving classroom stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve classroom statistics");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Search classrooms
     */
    @GetMapping("/search")
    @Operation(summary = "Search classrooms", description = "Search classrooms by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<ClassRoom>> searchClassRooms(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Searching classrooms with query: {}", query);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<ClassRoom> classRooms = classRoomRepository.findByNameContainingIgnoreCase(query, pageable);
        
        logger.debug("Found {} classrooms matching query: {}", classRooms.getTotalElements(), query);
        return ResponseEntity.ok(classRooms);
    }

    /**
     * Get classrooms with available space
     */
    @GetMapping("/available")
    @Operation(summary = "Get available classrooms", description = "Retrieve classrooms with available space")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available classrooms retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<ClassRoom>> getAvailableClassRooms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        logger.debug("Fetching classrooms with available space");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "grade", "name"));
        Page<ClassRoom> classRooms = classRoomRepository.findClassRoomsWithAvailableSpace(pageable);
        
        logger.debug("Retrieved {} classrooms with available space", classRooms.getTotalElements());
        return ResponseEntity.ok(classRooms);
    }
}
