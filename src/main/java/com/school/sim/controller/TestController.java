package com.school.sim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hash/{password}")
    public String generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        return "Password: " + password + "\nHash: " + hash + "\nMatches: " + passwordEncoder.matches(password, hash);
    }

    @PostMapping("/verify")
    public String verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        return "Password: " + password + "\nHash: " + hash + "\nMatches: " + matches;
    }

    @Autowired
    private com.school.sim.repository.UserRepository userRepository;

    @GetMapping("/users")
    public String listUsers() {
        try {
            var users = userRepository.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("Total users: ").append(users.size()).append("\n\n");
            for (var user : users) {
                sb.append("ID: ").append(user.getId()).append("\n");
                sb.append("Username: ").append(user.getUsername()).append("\n");
                sb.append("Email: ").append(user.getEmail()).append("\n");
                sb.append("Password Hash: ").append(user.getPassword()).append("\n");
                sb.append("Active: ").append(user.getIsActive()).append("\n");
                sb.append("---\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/users/simple")
    public String listUsersSimple() {
        try {
            // Try direct SQL query
            return "Database connection test - checking if users table exists";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/db/test")
    public String testDatabase() {
        return "Database test endpoint - backend is running";
    }

    @PostMapping("/create-admin")
    public String createAdmin() {
        try {
            // Create admin user manually
            var user = new com.school.sim.entity.User();
            user.setUsername("admin");
            user.setEmail("admin@sim.edu");
            user.setFirstName("System");
            user.setLastName("Administrator");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setUserType(com.school.sim.entity.UserType.ADMIN);
            user.setIsActive(true);
            user.setEmailVerifiedAt(java.time.LocalDateTime.now());
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setUpdatedAt(java.time.LocalDateTime.now());
            
            var savedUser = userRepository.save(user);
            return "Admin user created with ID: " + savedUser.getId() + " - Hash: " + user.getPassword();
        } catch (Exception e) {
            return "Error creating admin: " + e.getMessage();
        }
    }

    @PostMapping("/force-login")
    public java.util.Map<String, Object> forceLogin() {
        // Force login for development - always return success
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        java.util.Map<String, Object> user = new java.util.HashMap<>();
        
        user.put("id", 1L);
        user.put("name", "System Administrator");
        user.put("email", "admin@sim.edu");
        user.put("userType", "ADMIN");
        
        response.put("success", true);
        response.put("token", "dev-token-" + System.currentTimeMillis());
        response.put("accessToken", "dev-access-token-" + System.currentTimeMillis());
        response.put("user", user);
        response.put("message", "Force login successful for development");
        
        return response;
    }

    @PostMapping("/force-login")
    public java.util.Map<String, Object> forceLogin() {
        try {
            // FORCE LOGIN FOR DEVELOPMENT - BYPASS ALL AUTHENTICATION
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            
            // Create fake token
            String fakeToken = "dev-token-" + System.currentTimeMillis();
            
            // Create fake user data
            java.util.Map<String, Object> user = new java.util.HashMap<>();
            user.put("id", 1L);
            user.put("name", "System Administrator");
            user.put("email", "admin@sim.edu");
            user.put("userType", "ADMIN");
            user.put("roles", java.util.Arrays.asList("ADMIN"));
            
            response.put("success", true);
            response.put("token", fakeToken);
            response.put("accessToken", fakeToken);
            response.put("user", user);
            response.put("message", "DEVELOPMENT LOGIN - BYPASSED AUTHENTICATION");
            
            return response;
        } catch (Exception e) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Force login failed: " + e.getMessage());
            return errorResponse;
        }
    }
}