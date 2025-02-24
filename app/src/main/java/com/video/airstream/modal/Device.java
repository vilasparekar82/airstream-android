package com.video.airstream.modal;

import java.util.Set;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("deviceId")
    private Integer deviceId;

    @SerializedName("staticIp")
    private String staticIp;

    @SerializedName("deviceNumber")
    private String deviceNumber;

    @SerializedName("deviceOwner")
    private Organization deviceOwner;

    @SerializedName("videoDataSet")
    Set<VideoData> videoDataSet;

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getStaticIp() {
        return staticIp;
    }

    public void setStaticIp(String staticIp) {
        this.staticIp = staticIp;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public Organization getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(Organization deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public Set<VideoData> getVideoDataSet() {
        return videoDataSet;
    }

    public void setVideoDataSet(Set<VideoData> videoDataSet) {
        this.videoDataSet = videoDataSet;
    }
}
