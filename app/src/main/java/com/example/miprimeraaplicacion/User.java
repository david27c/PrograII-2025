package com.example.miprimeraaplicacion;

public class User {
    private String uid;
    private String fullName;
    private String username;
    private String email;
    private String profileImageUrl; // Opcional

    public User() {
        // Constructor vacío requerido para Firestore
    }

    public User(String uid, String fullName, String username, String email, String profileImageUrl) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters y Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}