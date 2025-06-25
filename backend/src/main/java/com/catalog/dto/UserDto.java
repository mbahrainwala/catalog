package com.catalog.dto;

import java.time.LocalDateTime;

public class UserDto {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // New profile fields
    private String alternateEmail;
    private String phoneNumber;
    private String alternatePhoneNumber;
    
    // Address 1 fields
    private String address1Line1;
    private String address1Line2;
    private String address1City;
    private String address1State;
    private String address1PostalCode;
    private String address1Country;
    
    // Address 2 fields
    private String address2Line1;
    private String address2Line2;
    private String address2City;
    private String address2State;
    private String address2PostalCode;
    private String address2Country;
    
    // Document and image fields
    private String profilePictureUrl;
    private String idDocument1Url;
    private String idDocument1Filename;
    private String idDocument2Url;
    private String idDocument2Filename;
    
    public UserDto() {}
    
    public UserDto(Long id, String email, String firstName, String lastName,
                   String role, Boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
}