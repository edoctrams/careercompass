package com.careercompass.careercompass.dto;

public class AuthenticationResponse {

    private String token;
    private String userId;
    private String name;
    private String email;

    public AuthenticationResponse() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String userId;
        private String name;
        private String email;

        private Builder() {}

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
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

        public AuthenticationResponse build() {
            AuthenticationResponse response = new AuthenticationResponse();
            response.setToken(token);
            response.setUserId(userId);
            response.setName(name);
            response.setEmail(email);
            return response;
        }
    }
}