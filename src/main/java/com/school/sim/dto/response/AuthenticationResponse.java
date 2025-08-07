package com.school.sim.dto.response;

import com.school.sim.entity.UserType;

import java.util.List;

/**
 * Authentication response DTO containing JWT tokens and user information
 */
public class AuthenticationResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    // Constructors
    public AuthenticationResponse() {}

    private AuthenticationResponse(Builder builder) {
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenType = builder.tokenType;
        this.expiresIn = builder.expiresIn;
        this.user = builder.user;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserInfo user) {
            this.user = user;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(this);
        }
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * User information DTO for authentication response
     */
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String nip;
        private UserType userType;
        private List<String> roles;
        private List<String> permissions;

        // Constructors
        public UserInfo() {}

        private UserInfo(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.email = builder.email;
            this.nip = builder.nip;
            this.userType = builder.userType;
            this.roles = builder.roles;
            this.permissions = builder.permissions;
        }

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String name;
            private String email;
            private String nip;
            private UserType userType;
            private List<String> roles;
            private List<String> permissions;

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder nip(String nip) {
                this.nip = nip;
                return this;
            }

            public Builder userType(UserType userType) {
                this.userType = userType;
                return this;
            }

            public Builder roles(List<String> roles) {
                this.roles = roles;
                return this;
            }

            public Builder permissions(List<String> permissions) {
                this.permissions = permissions;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(this);
            }
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

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }
}
