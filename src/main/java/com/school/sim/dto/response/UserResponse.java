package com.school.sim.dto.response;

import com.school.sim.entity.Role;
import com.school.sim.entity.User;
import com.school.sim.entity.UserType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for user information
 */
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String nip;
    private String phone;
    private String address;
    private UserType userType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoleResponse> roles;

    // Constructors
    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getFirstName() + " " + user.getLastName();
        this.email = user.getEmail();
        this.nip = user.getNip();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.userType = user.getUserType();
        this.active = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.roles = user.getRoles().stream()
            .map(RoleResponse::new)
            .collect(Collectors.toList());
    }

    // Static factory method
    public static UserResponse from(User user) {
        return new UserResponse(user);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleResponse> roles) {
        this.roles = roles;
    }

    /**
     * Inner class for role information in user response
     */
    public static class RoleResponse {
        private Long id;
        private String name;
        private String description;

        public RoleResponse() {}

        public RoleResponse(Role role) {
            this.id = role.getId();
            this.name = role.getName();
            this.description = role.getDescription();
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}