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
    private Integer addedBy;

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

}
