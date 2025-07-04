package com.example.palayan.Helper;

public class AdminModel {

    public String userId;
    public String fullName;
    public String username;
    public String password;
    public String role;
    public String securityQ1;
    public String securityQ2;
    public String status;

    public AdminModel() {
    }

    public AdminModel(String userId, String fullName, String username, String password, String role, String securityQ1, String securityQ2, String status) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.securityQ1 = securityQ1;
        this.securityQ2 = securityQ2;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getSecurityQ1() {
        return securityQ1;
    }

    public String getSecurityQ2() {
        return securityQ2;
    }

    public String getStatus() {
        return status;
    }
}
