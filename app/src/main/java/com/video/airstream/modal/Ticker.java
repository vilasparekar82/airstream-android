package com.video.airstream.modal;

import com.google.gson.annotations.SerializedName;

public class Ticker {

    @SerializedName("tickerId")
    private Integer tickerId;

    @SerializedName("tickerName")
    private String tickerName;

    @SerializedName("tickerContent")
    private String tickerContent;

    @SerializedName("tickerStatus")
    private String tickerStatus;

    @SerializedName("tickerOwner")
    private User tickerOwner;

    public Integer getTickerId() {
        return tickerId;
    }

    public String getTickerName() {
        return tickerName;
    }

    public String getTickerContent() {
        return tickerContent;
    }

    public String getTickerStatus() {
        return tickerStatus;
    }

    public User getTickerOwner() {
        return tickerOwner;
    }
}
