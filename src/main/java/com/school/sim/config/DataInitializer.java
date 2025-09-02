package com.school.sim.config;

import com.school.sim.entity.*;
import com.school.sim.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data Initializer to populate database with initial data
 */
@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        try {
            logger.info("Starting data initialization...");

            // Initialize essential users only
            initializeEssentialUsers();

            // Check other entities but don't create sample data
            checkDatabaseStatus();

            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
            throw e;
        }
    }

    /**
     * Initialize only essential system users (admin)
     */
    private void initializeEssentialUsers() {
        logger.info("Checking system users...");

        try {
            if (userRepository.count() == 0) {
                logger.info("No users found. Creating default admin user...");

                User admin = User.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .username("admin")
                    .email("admin@sim.edu")
                    .phone("081234567890")
                    .password(passwordEncoder.encode("admin123"))
                    .userType(UserType.ADMIN)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

                userRepository.save(admin);
                logger.info("Created default admin user: admin/admin123");
            } else {
                logger.info("Users already exist, skipping user initialization");
            }
        } catch (Exception e) {
            logger.warn("Could not initialize users - database may not be ready yet: {}", e.getMessage());
        }
    }

    /**
     * Check database status without creating sample data
     */
    private void checkDatabaseStatus() {
        logger.info("Checking database status...");

        try {
            long studentCount = studentRepository.count();

            logger.info("Database status:");
            logger.info("- Students: {}", studentCount);

            if (studentCount == 0) {
                logger.info("No students found. Students can be added through the application interface.");
            }
        } catch (Exception e) {
            logger.warn("Could not check database status: {}", e.getMessage());
        }
    }
}