package com.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usr")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "is_temporary_password", nullable = false)
    private Boolean isTemporaryPassword = false;
    
    @Column(name = "account_activated", nullable = false)
    private Boolean accountActivated = false;
    
    @Column(name = "activation_deadline")
    private LocalDateTime activationDeadline;
    
    // New profile fields
    @Email(message = "Alternate email should be valid")
    @Column(name = "alternate_email")
    private String alternateEmail;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Size(max = 20, message = "Alternate phone number must not exceed 20 characters")
    @Column(name = "alternate_phone_number")
    private String alternatePhoneNumber;
    
    // Address 1 fields
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Column(name = "address1_line1")
    private String address1Line1;
    
    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Column(name = "address1_line2")
    private String address1Line2;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "address1_city")
    private String address1City;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "address1_state")
    private String address1State;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Column(name = "address1_postal_code")
    private String address1PostalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "address1_country")
    private String address1Country;
    
    // Address 2 fields
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Column(name = "address2_line1")
    private String address2Line1;
    
    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Column(name = "address2_line2")
    private String address2Line2;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "address2_city")
    private String address2City;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "address2_state")
    private String address2State;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Column(name = "address2_postal_code")
    private String address2PostalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "address2_country")
    private String address2Country;
    
    // Document and image fields
    @Size(max = 1000, message = "Profile picture URL must not exceed 1000 characters")
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Size(max = 1000, message = "ID document 1 URL must not exceed 1000 characters")
    @Column(name = "id_document1_url")
    private String idDocument1Url;
    
    @Size(max = 255, message = "ID document 1 filename must not exceed 255 characters")
    @Column(name = "id_document1_filename")
    private String idDocument1Filename;
    
    @Size(max = 1000, message = "ID document 2 URL must not exceed 1000 characters")
    @Column(name = "id_document2_url")
    private String idDocument2Url;
    
    @Size(max = 255, message = "ID document 2 filename must not exceed 255 characters")
    @Column(name = "id_document2_filename")
    private String idDocument2Filename;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {}
    
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Helper methods
    public boolean isActivationExpired() {
        return activationDeadline != null && LocalDateTime.now().isAfter(activationDeadline);
    }
    
    public boolean needsActivation() {
        return !accountActivated && isTemporaryPassword;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getIsTemporaryPassword() {
        return isTemporaryPassword;
    }
    
    public void setIsTemporaryPassword(Boolean isTemporaryPassword) {
        this.isTemporaryPassword = isTemporaryPassword;
    }
    
    public Boolean getAccountActivated() {
        return accountActivated;
    }
    
    public void setAccountActivated(Boolean accountActivated) {
        this.accountActivated = accountActivated;
    }
    
    public LocalDateTime getActivationDeadline() {
        return activationDeadline;
    }
    
    public void setActivationDeadline(LocalDateTime activationDeadline) {
        this.activationDeadline = activationDeadline;
    }
    
    public String getAlternateEmail() {
        return alternateEmail;
    }
    
    public void setAlternateEmail(String alternateEmail) {
        this.alternateEmail = alternateEmail;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAlternatePhoneNumber() {
        return alternatePhoneNumber;
    }
    
    public void setAlternatePhoneNumber(String alternatePhoneNumber) {
        this.alternatePhoneNumber = alternatePhoneNumber;
    }
    
    public String getAddress1Line1() {
        return address1Line1;
    }
    
    public void setAddress1Line1(String address1Line1) {
        this.address1Line1 = address1Line1;
    }
    
    public String getAddress1Line2() {
        return address1Line2;
    }
    
    public void setAddress1Line2(String address1Line2) {
        this.address1Line2 = address1Line2;
    }
    
    public String getAddress1City() {
        return address1City;
    }
    
    public void setAddress1City(String address1City) {
        this.address1City = address1City;
    }
    
    public String getAddress1State() {
        return address1State;
    }
    
    public void setAddress1State(String address1State) {
        this.address1State = address1State;
    }
    
    public String getAddress1PostalCode() {
        return address1PostalCode;
    }
    
    public void setAddress1PostalCode(String address1PostalCode) {
        this.address1PostalCode = address1PostalCode;
    }
    
    public String getAddress1Country() {
        return address1Country;
    }
    
    public void setAddress1Country(String address1Country) {
        this.address1Country = address1Country;
    }
    
    public String getAddress2Line1() {
        return address2Line1;
    }
    
    public void setAddress2Line1(String address2Line1) {
        this.address2Line1 = address2Line1;
    }
    
    public String getAddress2Line2() {
        return address2Line2;
    }
    
    public void setAddress2Line2(String address2Line2) {
        this.address2Line2 = address2Line2;
    }
    
    public String getAddress2City() {
        return address2City;
    }
    
    public void setAddress2City(String address2City) {
        this.address2City = address2City;
    }
    
    public String getAddress2State() {
        return address2State;
    }
    
    public void setAddress2State(String address2State) {
        this.address2State = address2State;
    }
    
    public String getAddress2PostalCode() {
        return address2PostalCode;
    }
    
    public void setAddress2PostalCode(String address2PostalCode) {
        this.address2PostalCode = address2PostalCode;
    }
    
    public String getAddress2Country() {
        return address2Country;
    }
    
    public void setAddress2Country(String address2Country) {
        this.address2Country = address2Country;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public String getIdDocument1Url() {
        return idDocument1Url;
    }
    
    public void setIdDocument1Url(String idDocument1Url) {
        this.idDocument1Url = idDocument1Url;
    }
    
    public String getIdDocument1Filename() {
        return idDocument1Filename;
    }
    
    public void setIdDocument1Filename(String idDocument1Filename) {
        this.idDocument1Filename = idDocument1Filename;
    }
    
    public String getIdDocument2Url() {
        return idDocument2Url;
    }
    
    public void setIdDocument2Url(String idDocument2Url) {
        this.idDocument2Url = idDocument2Url;
    }
    
    public String getIdDocument2Filename() {
        return idDocument2Filename;
    }
    
    public void setIdDocument2Filename(String idDocument2Filename) {
        this.idDocument2Filename = idDocument2Filename;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public enum Role {
        USER, ADMIN, OWNER
    }
}