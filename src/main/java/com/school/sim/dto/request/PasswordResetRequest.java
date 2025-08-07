package com.school.sim.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Password reset request DTO for initiating password reset
 */
public class PasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Constructors
    public PasswordResetRequest() {}

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
