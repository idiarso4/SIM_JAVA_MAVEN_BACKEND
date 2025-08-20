package com.school.sim.config;

import com.school.sim.entity.*;
import com.school.sim.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Data Initializer to populate database with initial data
 */
//@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        logger.info("Starting data initialization...");

        try {
            // Initialize roles
            initializeRoles();
            
            // Initialize permissions
            initializePermissions();
            
            // Initialize users
            initializeUsers();
            
            // Initialize students
            initializeStudents();
            
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
            throw e;
        }
    }

    private void initializeRoles() {
        logger.info("Initializing roles...");
        
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                new Role("ADMIN", "System Administrator", true),
                new Role("TEACHER", "Teacher", true),
                new Role("STUDENT", "Student", true)
            );
            
            roleRepository.saveAll(roles);
            logger.info("Created {} roles", roles.size());
        } else {
            logger.info("Roles already exist, skipping initialization");
        }
    }

    private void initializePermissions() {
        logger.info("Initializing permissions...");
        
        if (permissionRepository.count() == 0) {
            List<Permission> permissions = Arrays.asList(
                new Permission("VIEW_STUDENTS", "View student information"),
                new Permission("MANAGE_STUDENTS", "Create, update, delete students"),
                new Permission("VIEW_USERS", "View user information"),
                new Permission("MANAGE_USERS", "Create, update, delete users"),
                new Permission("VIEW_GRADES", "View grades and assessments"),
                new Permission("MANAGE_GRADES", "Create, update, delete grades"),
                new Permission("VIEW_REPORTS", "View reports"),
                new Permission("GENERATE_REPORTS", "Generate and export reports"),
                new Permission("MANAGE_CLASSES", "Manage class schedules and assignments"),
                new Permission("VIEW_ATTENDANCE", "View attendance records"),
                new Permission("MANAGE_ATTENDANCE", "Manage attendance records")
            );
            
            permissionRepository.saveAll(permissions);
            logger.info("Created {} permissions", permissions.size());
            
            // Assign permissions to roles
            assignPermissionsToRoles();
        } else {
            logger.info("Permissions already exist, skipping initialization");
        }
    }

    private void assignPermissionsToRoles() {
        logger.info("Assigning permissions to roles...");
        
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        Role teacherRole = roleRepository.findByName("TEACHER").orElse(null);
        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        
        if (adminRole != null) {
            // Admin gets all permissions
            List<Permission> allPermissions = permissionRepository.findAll();
            adminRole.getPermissions().addAll(allPermissions);
            roleRepository.save(adminRole);
            logger.info("Assigned {} permissions to ADMIN role", allPermissions.size());
        }
        
        if (teacherRole != null) {
            // Teacher gets limited permissions
            List<String> teacherPermissionNames = Arrays.asList(
                "VIEW_STUDENTS", "VIEW_GRADES", "MANAGE_GRADES", 
                "VIEW_ATTENDANCE", "MANAGE_ATTENDANCE", "VIEW_REPORTS"
            );
            for (String permissionName : teacherPermissionNames) {
                Permission permission = permissionRepository.findByName(permissionName).orElse(null);
                if (permission != null) {
                    teacherRole.getPermissions().add(permission);
                }
            }
            roleRepository.save(teacherRole);
            logger.info("Assigned {} permissions to TEACHER role", teacherRole.getPermissions().size());
        }
        
        if (studentRole != null) {
            // Student gets very limited permissions
            Permission viewGradesPermission = permissionRepository.findByName("VIEW_GRADES").orElse(null);
            if (viewGradesPermission != null) {
                studentRole.getPermissions().add(viewGradesPermission);
            }
            roleRepository.save(studentRole);
            logger.info("Assigned {} permissions to STUDENT role", studentRole.getPermissions().size());
        }
    }

    private void initializeUsers() {
        logger.info("Initializing users...");
        
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            
            // Create admin user
            if (adminRole != null) {
                User adminUser = User.builder()
                    .username("admin")
                    .email("admin@sim.edu")
                    .firstName("System")
                    .lastName("Administrator")
                    .password(passwordEncoder.encode("admin123"))
                    .userType(UserType.ADMIN)
                    .isActive(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .build();
                
                adminUser.getRoles().add(adminRole);
                userRepository.save(adminUser);
                logger.info("Created admin user");
            }
            
            // Teachers can be added through the application interface
        } else {
            logger.info("Users already exist, skipping initialization");
        }
    }

    private void initializeStudents() {
        logger.info("Checking students...");

        if (studentRepository.count() == 0) {
            logger.info("No students found in database. Students can be added through the application interface.");
            // No sample students will be created automatically


        } else {
            logger.info("Students already exist, skipping initialization");
        }
    }
}