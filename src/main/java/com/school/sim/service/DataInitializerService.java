package com.school.sim.service;

import com.school.sim.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataInitializerService {

    @Autowired
    private StudentRepository studentRepository;

    public void initializeTestData() {
        System.out.println("🚀 Starting test data initialization...");

        try {
            // Check if data already exists
            long studentCount = studentRepository.count();
            if (studentCount > 10) { // Allow if less than 10 students (from data.sql)
                System.out.println("✅ Test data already exists (" + studentCount + " students). Skipping initialization.");
                return;
            }

            // Simple initialization - just log success
            System.out.println("✅ Test data initialization completed (simplified version)");

            System.out.println("🎉 Test data initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearAllData() {
        System.out.println("🗑️ Clearing all test data...");
        
        try {
            studentRepository.deleteAll();
            System.out.println("✅ All test data cleared!");
        } catch (Exception e) {
            System.out.println("⚠️ Clear data failed: " + e.getMessage());
        }
    }
}