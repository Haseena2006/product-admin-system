package com.example.productadmin.dto;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;

    public UserResponse(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
