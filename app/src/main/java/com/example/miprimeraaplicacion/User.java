package com.example.miprimeraaplicacion;

// Esta es la clase User completa con todos los campos de tu SettingsActivity.txt
// MUEVE ESTO A SU PROPIO ARCHIVO: User.java
public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private String profileImageUrl;
    private int reportsCount;

    private boolean showFullNamePublic;
    private boolean showProfilePhotoInComments;
    private boolean showEmailPublic;
    private boolean showPhonePublic;

    // Constructor completo con todos los campos.
    public User(String userId, String username, String email, String password, String fullName, String phone, String address, String profileImageUrl, int reportsCount, boolean showFullNamePublic, boolean showProfilePhotoInComments, boolean showEmailPublic, boolean showPhonePublic) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone; // ¡CORREGIDO!
        this.address = address; // ¡CORREGIDO!
        this.profileImageUrl = profileImageUrl; // ¡CORREGIDO!
        this.reportsCount = reportsCount; // ¡CORREGIDO!
        this.showFullNamePublic = showFullNamePublic; // ¡CORREGIDO!
        this.showProfilePhotoInComments = showProfilePhotoInComments; // ¡CORREGIDO!
        this.showEmailPublic = showEmailPublic; // ¡CORREGIDO!
        this.showPhonePublic = showPhonePublic; // ¡CORREGIDO!
    }

    // Constructor más simple (ajusta según tus necesidades, este llamará al constructor completo con valores por defecto)
    public User(String userId, String username, String email, String password, String fullName, String phone) {
        // Llama al constructor completo con valores por defecto para los campos adicionales
        this(userId, username, email, password, fullName, phone, "", "", 0, false, false, false, false);
    }

    // --- Getters y Setters (asegúrate de que estén todos presentes como en tu User.java original) ---
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public int getReportsCount() { return reportsCount; }
    public boolean isShowFullNamePublic() { return showFullNamePublic; }
    public boolean isShowProfilePhotoInComments() { return showProfilePhotoInComments; }
    public boolean isShowEmailPublic() { return showEmailPublic; }
    public boolean isShowPhonePublic() { return showPhonePublic; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setReportsCount(int reportsCount) { this.reportsCount = reportsCount; }
    public void setShowFullNamePublic(boolean showFullNamePublic) { this.showFullNamePublic = showFullNamePublic; }
    public void setShowProfilePhotoInComments(boolean showProfilePhotoInComments) { this.showProfilePhotoInComments = showProfilePhotoInComments; }
    public void setShowEmailPublic(boolean showEmailPublic) { this.showEmailPublic = showEmailPublic; }
    public void setShowPhonePublic(boolean showPhonePublic) { this.showPhonePublic = showPhonePublic; }
}