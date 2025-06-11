package com.example.miprimeraaplicacion;

public class User {
    private String uid;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String profileImageUrl;
    private String fullName; // Nuevo campo: Nombre completo
    private int reportCount; // Nuevo campo: Conteo de reportes (para ProfileActivity)

    // Nuevos campos para preferencias de visibilidad
    private boolean showFullNamePublic;
    private boolean showProfilePhotoInComments;
    private boolean showEmailPublic;
    private boolean showPhonePublic;

    public User() {
        // Constructor vacío requerido por Firestore
    }

    // Constructor completo actualizado con los nuevos campos
    public User(String uid, String username, String email, String phone, String address,
                String profileImageUrl, String fullName, int reportCount,
                boolean showFullNamePublic, boolean showProfilePhotoInComments,
                boolean showEmailPublic, boolean showPhonePublic) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.fullName = fullName;
        this.reportCount = reportCount;
        this.showFullNamePublic = showFullNamePublic;
        this.showProfilePhotoInComments = showProfilePhotoInComments;
        this.showEmailPublic = showEmailPublic;
        this.showPhonePublic = showPhonePublic;
    }

    // Getters y Setters existentes
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

    // Getters y Setters para los nuevos campos

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getReportCount() {
        return reportCount;
    }
    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public boolean isShowFullNamePublic() {
        return showFullNamePublic;
    }
    public void setShowFullNamePublic(boolean showFullNamePublic) {
        this.showFullNamePublic = showFullNamePublic;
    }

    public boolean isShowProfilePhotoInComments() {
        return showProfilePhotoInComments;
    }
    public void setShowProfilePhotoInComments(boolean showProfilePhotoInComments) {
        this.showProfilePhotoInComments = showProfilePhotoInComments;
    }

    public boolean isShowEmailPublic() {
        return showEmailPublic;
    }
    public void setShowEmailPublic(boolean showEmailPublic) {
        this.showEmailPublic = showEmailPublic;
    }

    public boolean isShowPhonePublic() {
        return showPhonePublic;
    }
    public void setShowPhonePublic(boolean showPhonePublic) {
        this.showPhonePublic = showPhonePublic;
    }

    public String getReportsCount() {
        return "";
    }
}