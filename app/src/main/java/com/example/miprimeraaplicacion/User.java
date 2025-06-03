package com.example.miprimeraaplicacion;

public class User {
    private String uid;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String profileImageUrl;

    public User() {
        // Constructor vacío requerido por Firestore
    }

    public User(String uid, String username, String email, String phone, String address, String profileImageUrl) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}