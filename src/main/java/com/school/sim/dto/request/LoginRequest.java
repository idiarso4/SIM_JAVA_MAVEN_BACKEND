package com.school.sim.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Login request DTO for user authentication
 * Supports login with email or NIP (for teachers)
 */
public class LoginRequest {

    @NotBlank(message = "Identifier is required")
    @Size(max = 100, message = "Identifier must not exceed 100 characters")
    private String identifier; // Can be email or NIP

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private boolean rememberMe = false;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public LoginRequest(String identifier, String password, boolean rememberMe) {
        this.identifier = identifier;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "identifier='" + identifier + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
