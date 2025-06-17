package com.example.miprimeraaplicacion;

public class User {
    private String userId;
    private String email;
    private String password; // Solo para uso interno de DBLocal, no para UI/Firebase
    private String username;
    private String fullName; // <-- NUEVO CAMPO: Para nombre completo
    private String phone;
    private String address;
    private String profileImageUrl;
    private int reportsCount;

    // Preferencias de visibilidad
    private boolean showFullNamePublic;
    private boolean showProfilePhotoInComments; // <-- NUEVO CAMPO
    private boolean showEmailPublic;            // <-- NUEVO CAMPO
    private boolean showPhonePublic;            // <-- NUEVO CAMPO

    // Constructor vacío requerido por Firebase en algunos casos (aunque no lo usemos directamente aquí)
    public User(String id, String email, String password, String username, String fullName, String phone, String address, String profileImageUrl, int reportsCount) {
    }

    // Constructor completo para DBLocal, incluyendo todos los campos
    public User(String userId, String email, String password, String username, String fullName,
                String phone, String address, String profileImageUrl, int reportsCount,
                boolean showFullNamePublic, boolean showProfilePhotoInComments,
                boolean showEmailPublic, boolean showPhonePublic) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullName = fullName; // Asignar nuevo campo
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.reportsCount = reportsCount;
        this.showFullNamePublic = showFullNamePublic;
        this.showProfilePhotoInComments = showProfilePhotoInComments; // Asignar nuevo campo
        this.showEmailPublic = showEmailPublic;                      // Asignar nuevo campo
        this.showPhonePublic = showPhonePublic;                      // Asignar nuevo campo
    }

    // Constructor básico para login (email y userId)
    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
        // Inicializar otros campos a valores predeterminados para evitar NullPointer
        this.password = "";
        this.username = "";
        this.fullName = "";
        this.phone = "";
        this.address = "";
        this.profileImageUrl = "";
        this.reportsCount = 0;
        this.showFullNamePublic = false;
        this.showProfilePhotoInComments = false;
        this.showEmailPublic = false;
        this.showPhonePublic = false;
    }


    // --- Getters ---
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() { // Cuidado con exponer esto en la UI
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() { // <-- NUEVO GETTER
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public boolean isShowFullNamePublic() {
        return showFullNamePublic;
    }

    public boolean isShowProfilePhotoInComments() { // <-- NUEVO GETTER
        return showProfilePhotoInComments;
    }

    public boolean isShowEmailPublic() { // <-- NUEVO GETTER
        return showEmailPublic;
    }

    public boolean isShowPhonePublic() { // <-- NUEVO GETTER
        return showPhonePublic;
    }

    // --- Setters ---
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) { // <-- NUEVO SETTER
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setReportsCount(int reportsCount) {
        this.reportsCount = reportsCount;
    }

    public void setShowFullNamePublic(boolean showFullNamePublic) {
        this.showFullNamePublic = showFullNamePublic;
    }

    public void setShowProfilePhotoInComments(boolean showProfilePhotoInComments) { // <-- NUEVO SETTER
        this.showProfilePhotoInComments = showProfilePhotoInComments;
    }

    public void setShowEmailPublic(boolean showEmailPublic) { // <-- NUEVO SETTER
        this.showEmailPublic = showEmailPublic;
    }

    public void setShowPhonePublic(boolean showPhonePublic) { // <-- NUEVO SETTER
        this.showPhonePublic = showPhonePublic;
    }
}