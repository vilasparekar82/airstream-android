package com.video.airstream.service;

import com.video.airstream.modal.Device;
import com.video.airstream.modal.Ticker;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface APIInterface {
   @GET("api/device/android/{serialNumber}")
   Call<Device> getDeviceDetails(@Path("serialNumber") String staticIp);

   @GET("api/video/android/download/{deviceId}/{videoId}")
   Call<Device> downloadVideo(@Path("deviceId") Integer deviceId, @Path("videoId") Integer videoId);

   @PUT("api/device/android")
   Call<Device> updateDeviceToken(@Body Device device);

   @GET("api/device/android/ping/{serialNumber}")
   Call<Device> pingDevice(@Path("serialNumber") String serialNumber);

   @GET("api/ticker/android/{orgId}")
   Call<List<Ticker>> getTicker(@Path("orgId") String orgId);

}
