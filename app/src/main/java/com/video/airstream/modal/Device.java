package com.video.airstream.modal;

import java.util.Date;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.video.airstream.adapter.DateTypeAdapter;

public class Device {

    @SerializedName("deviceId")
    private Integer deviceId;

    @SerializedName("macId")
    private String macId;

    @SerializedName("deviceNumber")
    private String deviceNumber;

    @SerializedName("deviceToken")
    private String deviceToken;

    @SerializedName("deviceOwner")
    private User deviceOwner;

    @JsonAdapter(DateTypeAdapter.class)
    private Date deviceStatus;

    @SerializedName("videoDataSet")
    Set<VideoData> videoDataSet;

    @SerializedName("liveUrlDataSet")
    Set<LiveUrl> liveUrlDataSet;

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;

    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public User getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(User deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public Set<VideoData> getVideoDataSet() {
        return videoDataSet;
    }

    public void setVideoDataSet(Set<VideoData> videoDataSet) {
        this.videoDataSet = videoDataSet;
    }

    public Set<LiveUrl> getLiveUrlDataSet() {
        return liveUrlDataSet;
    }

    public void setLiveUrlDataSet(Set<LiveUrl> liveUrlDataSet) {
        this.liveUrlDataSet = liveUrlDataSet;
    }
}