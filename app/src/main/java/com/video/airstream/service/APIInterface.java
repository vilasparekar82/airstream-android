package com.video.airstream.service;

import com.video.airstream.modal.Device;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Path;

public interface APIInterface {
   @GET("api/device/{staticIp}")
   Call<Device> getDeviceDetails(@Path("staticIp") String staticIp);

   @GET("api/video/download/{deviceId}/{fileName}")
   Call<Device> downloadVideo(@Path("deviceId") Integer deviceId, @Path("fileName") String fileName);
}
