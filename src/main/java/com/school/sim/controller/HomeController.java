package com.school.sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Home controller for basic application information
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "School Information Management System");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Welcome to SIM Backend API");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("API Documentation", "/swagger-ui.html");
        endpoints.put("Health Check", "/actuator/health");
        endpoints.put("API Docs JSON", "/api-docs");
        
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "SIM Backend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "School Information Management System");
        response.put("description", "Spring Boot backend for School Information Management");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("spring.version", org.springframework.core.SpringVersion.getVersion());
        
        Map<String, String> features = new HashMap<>();
        features.put("Authentication", "JWT-based authentication and authorization");
        features.put("User Management", "Complete user and role management");
        features.put("Student Management", "Student lifecycle and academic tracking");
        features.put("Attendance", "Attendance recording and reporting");
        features.put("Assessment", "Assessment and grading system");
        features.put("Academic Reports", "Comprehensive academic reporting");
        features.put("Schedule Management", "Timetable and schedule management");
        features.put("Excel Integration", "Import/export functionality");
        
        response.put("features", features);
        
        return ResponseEntity.ok(response);
    }
}
