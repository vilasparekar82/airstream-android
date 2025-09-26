package com.video.airstream.modal;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.video.airstream.adapter.DateTypeAdapter;

import java.sql.Timestamp;
import java.util.Date;

public class LiveUrl {
    @SerializedName("liveUrlId")
    private Integer liveUrlId;
    @SerializedName("liveUrl")
    private String liveUrl;

    @SerializedName("liveStartDate")
    @JsonAdapter(DateTypeAdapter.class)
    private Date liveStartDate;

    @SerializedName("liveEndDate")
    @JsonAdapter(DateTypeAdapter.class)
    private Date liveEndDate;

    @SerializedName("liveStatus")
    private String liveStatus;

    @SerializedName("liveOwner")
    private User liveOwner;

    public Integer getLiveUrlId() {
        return liveUrlId;
    }

    public void setLiveUrlId(Integer liveUrlId) {
        this.liveUrlId = liveUrlId;
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
    }

    public Date getLiveStartDate() {
        return liveStartDate;
    }

    public void setLiveStartDate(Date liveStartDate) {
        this.liveStartDate = liveStartDate;
    }

    public Date getLiveEndDate() {
        return liveEndDate;
    }

    public void setLiveEndDate(Date liveEndDate) {
        this.liveEndDate = liveEndDate;
    }

    public String getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(String liveStatus) {
        this.liveStatus = liveStatus;
    }

    public User getLiveOwner() {
        return liveOwner;
    }

    public void setLiveOwner(User liveOwner) {
        this.liveOwner = liveOwner;
    }
}
