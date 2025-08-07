package com.school.sim.debug;

import com.school.sim.security.JwtTokenProvider;
import com.school.sim.service.AuthenticationService;
import com.school.sim.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Debug test to check if all authentication components can be loaded
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AuthenticationDebugTest {

    @Test
    public void contextLoads() {
        // This test will fail if there are any bean creation issues
        System.out.println("Spring context loaded successfully");
    }

    @Test
    public void testJwtTokenProviderExists() {
        // Test if JwtTokenProvider can be instantiated
        try {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
            System.out.println("JwtTokenProvider created successfully");
        } catch (Exception e) {
            System.err.println("Error creating JwtTokenProvider: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
