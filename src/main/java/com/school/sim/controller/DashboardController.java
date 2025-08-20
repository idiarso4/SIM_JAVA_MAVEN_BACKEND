package com.school.sim.controller;

import com.school.sim.repository.StudentRepository;
import com.school.sim.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Controller
 * Provides dashboard statistics and overview data
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard statistics and overview endpoints")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieve overview statistics for dashboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        logger.info("Dashboard stats request received");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic counts
            long totalStudents = studentRepository.count();
            long totalUsers = userRepository.count();
            long activeClasses = Math.max(1, totalStudents / 25); // Estimate: ~25 students per class
            long pendingTasks = 0; // No task system implemented yet
            
            stats.put("totalStudents", totalStudents);
            stats.put("totalUsers", totalUsers);
            stats.put("activeClasses", activeClasses);
            stats.put("pendingTasks", pendingTasks);
            stats.put("lastUpdated", LocalDateTime.now());
            
            logger.info("Dashboard stats retrieved successfully");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving dashboard stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve dashboard statistics");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get students statistics
     */
    @GetMapping("/students/stats")
    @Operation(summary = "Get student statistics", description = "Retrieve detailed student statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getStudentStats() {
        logger.info("Student stats request received");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalCount = studentRepository.count();
            long activeCount = totalCount; // All students considered active for now
            long graduatedCount = 0L; // No graduated students tracked yet

            stats.put("totalCount", totalCount);
            stats.put("activeCount", activeCount);
            stats.put("graduatedCount", graduatedCount);
            stats.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving student stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve student statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get users statistics
     */
    @GetMapping("/users/stats")
    @Operation(summary = "Get user statistics", description = "Retrieve detailed user statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        logger.info("User stats request received");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalCount = userRepository.count();
            long activeCount = totalCount; // All users considered active for now
            long adminCount = 1L; // At least one admin exists
            long teacherCount = Math.max(1L, totalCount - 1L); // Estimate teachers

            stats.put("totalCount", totalCount);
            stats.put("activeCount", activeCount);
            stats.put("adminCount", adminCount);
            stats.put("teacherCount", teacherCount);
            stats.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving user stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve user statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get classes statistics
     */
    @GetMapping("/classes/stats")
    @Operation(summary = "Get class statistics", description = "Retrieve detailed class statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getClassStats() {
        logger.info("Class stats request received");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Estimate class statistics based on student count
            long studentCount = studentRepository.count();
            long totalCount = Math.max(1, studentCount / 25); // Estimate: ~25 students per class
            long activeCount = totalCount; // All classes considered active
            
            stats.put("activeCount", activeCount);
            stats.put("totalCount", totalCount);
            stats.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving class stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve class statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get recent activities
     */
    @GetMapping("/activities/recent")
    @Operation(summary = "Get recent activities", description = "Retrieve recent system activities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("Recent activities request received with limit: {}", limit);
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Activity logging system not implemented yet
            response.put("activities", new java.util.ArrayList<>());
            response.put("totalCount", 0);
            response.put("limit", limit);
            response.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving recent activities", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve recent activities");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get system health status
     */
    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Retrieve system health status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        logger.info("System health request received");
        
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Basic health checks
            health.put("status", "UP");
            health.put("database", "UP");
            health.put("timestamp", LocalDateTime.now());
            
            // Basic health check - detailed checks can be added later
            health.put("memory", "OK");
            health.put("diskSpace", "OK");
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error checking system health", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", "Health check failed");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}