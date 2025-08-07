package com.school.sim.config;

import com.school.sim.entity.Role;
import com.school.sim.entity.User;
import com.school.sim.entity.UserType;
import com.school.sim.repository.RoleRepository;
import com.school.sim.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data initialization component to create default users and roles
 */
@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        logger.info("Starting data initialization...");
        
        try {
            initializeRoles();
            initializeDefaultUsers();
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
        }
    }

    private void initializeRoles() {
        logger.info("Initializing default roles...");
        
        createRoleIfNotExists("ADMIN", "System Administrator");
        createRoleIfNotExists("TEACHER", "Teacher");
        createRoleIfNotExists("STUDENT", "Student");
        
        logger.info("Default roles initialized");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        } else {
            logger.debug("Role already exists: {}", roleName);
        }
    }

    private void initializeDefaultUsers() {
        logger.info("Initializing default users...");
        
        // Create admin user
        createAdminUserIfNotExists();
        
        // Create test teacher
        createTestTeacherIfNotExists();
        
        logger.info("Default users initialized");
    }

    private void createAdminUserIfNotExists() {
        String adminEmail = "admin@sim.edu";
        String adminUsername = "admin";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            
            if (adminRole.isPresent()) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .firstName("System")
                        .lastName("Administrator")
                        .password(passwordEncoder.encode("admin123"))
                        .userType(UserType.ADMIN)
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build();
                
                admin.addRole(adminRole.get());
                userRepository.save(admin);
                
                logger.info("Created admin user: {} / {}", adminUsername, adminEmail);
                logger.info("Admin password: admin123");
            } else {
                logger.error("ADMIN role not found, cannot create admin user");
            }
        } else {
            logger.debug("Admin user already exists");
        }
    }

    private void createTestTeacherIfNotExists() {
        String teacherEmail = "teacher@sim.edu";
        String teacherUsername = "teacher";
        
        if (!userRepository.existsByEmail(teacherEmail)) {
            Optional<Role> teacherRole = roleRepository.findByName("TEACHER");
            
            if (teacherRole.isPresent()) {
                User teacher = User.builder()
                        .username(teacherUsername)
                        .email(teacherEmail)
                        .firstName("Test")
                        .lastName("Teacher")
                        .password(passwordEncoder.encode("teacher123"))
                        .userType(UserType.TEACHER)
                        .nip("T001")
                        .isActive(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build();
                
                teacher.addRole(teacherRole.get());
                userRepository.save(teacher);
                
                logger.info("Created teacher user: {} / {}", teacherUsername, teacherEmail);
                logger.info("Teacher password: teacher123");
            } else {
                logger.error("TEACHER role not found, cannot create teacher user");
            }
        } else {
            logger.debug("Teacher user already exists");
        }
    }
}