package com.example.miprimeraaplicacion;

public class User {
    private String userId; // Cambiado de 'id' a 'userId' para mayor claridad y consistencia con DBLocal
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

    // Constructor completo con todos los campos
    public User(String userId, String email, String password, String username, String fullName,
                String phone, String address, String profileImageUrl, int reportsCount,
                boolean showFullNamePublic, boolean showProfilePhotoInComments,
                boolean showEmailPublic, boolean showPhonePublic) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.reportsCount = reportsCount;
        this.showFullNamePublic = showFullNamePublic;
        this.showProfilePhotoInComments = showProfilePhotoInComments;
        this.showEmailPublic = showEmailPublic;
        this.showPhonePublic = showPhonePublic;
    }

    // Constructor mínimo para registro (si algunos campos son opcionales al inicio)
    public User(String userId, String email, String password, String username, String fullName) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullName = fullName;
        // Inicializar otros campos con valores predeterminados
        this.phone = "";
        this.address = "";
        this.profileImageUrl = "";
        this.reportsCount = 0;
        this.showFullNamePublic = false;
        this.showProfilePhotoInComments = false;
        this.showEmailPublic = false;
        this.showPhonePublic = false;
    }

    // Getters
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

    // Setters
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