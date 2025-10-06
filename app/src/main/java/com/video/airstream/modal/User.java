package com.video.airstream.modal;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("mobileNumber")
    private String mobileNumber;

    @SerializedName("role")
    private String role;

    @SerializedName("addedBy")
    private String addedBy;

    @SerializedName("organization")
    private Organization organization;

    @SerializedName("username")
    private String username;

    @SerializedName("enabled")
    private Boolean enabled;

    @SerializedName("accountNonLocked")
    private Boolean accountNonLocked;

    @SerializedName("credentialsNonExpired")
    private Boolean credentialsNonExpired;

    @SerializedName("accountNonExpired")
    private Boolean accountNonExpired;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
}
