package com.school.sim.controller;

import com.school.sim.service.DataInitializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-init")
@CrossOrigin(origins = "*")
public class DataInitController {

    @Autowired
    private DataInitializerService dataInitializerService;

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dataInitializerService.initializeTestData();
            
            response.put("success", true);
            response.put("message", "Test data initialized successfully!");
            response.put("data", getDataCounts());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to initialize test data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dataInitializerService.clearAllData();
            
            response.put("success", true);
            response.put("message", "All test data cleared successfully!");
            response.put("data", getDataCounts());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear test data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDataStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> counts = getDataCounts();
            boolean hasData = counts.values().stream().anyMatch(count -> count > 0);
            
            response.put("success", true);
            response.put("hasData", hasData);
            response.put("data", counts);
            response.put("message", hasData ? "Database contains data" : "Database is empty");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get data status: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Clear existing data
            dataInitializerService.clearAllData();
            
            // Initialize fresh data
            dataInitializerService.initializeTestData();
            
            response.put("success", true);
            response.put("message", "Database reset and reinitialized successfully!");
            response.put("data", getDataCounts());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reset data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    private Map<String, Long> getDataCounts() {
        Map<String, Long> counts = new HashMap<>();
        
        try {
            // Simple count without repository dependencies
            counts.put("status", 1L);
        } catch (Exception e) {
            counts.put("status", 0L);
        }
        
        return counts;
    }
}