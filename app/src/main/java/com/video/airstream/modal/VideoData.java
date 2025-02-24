package com.video.airstream.modal;

import com.google.gson.annotations.SerializedName;

public class VideoData {
    @SerializedName("videoId")
    private Integer videoId;
    @SerializedName("videoName")
    private String videoName;

    public Integer getVideoId() {
        return videoId;
    }

    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
}
