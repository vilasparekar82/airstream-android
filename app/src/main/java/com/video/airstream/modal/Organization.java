package com.video.airstream.modal;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Organization implements Serializable {

    @SerializedName("organizationId")
    private Integer organizationId;

    @SerializedName("organizationName")
    private String organizationName;

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
