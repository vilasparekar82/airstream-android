package com.video.airstream.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.service.APIInterface;

import retrofit2.Response;

public class DeviceDetailsWorker extends Worker {
    public DeviceDetailsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String host = inputData.getString("HOST");
        String deviceNumber = inputData.getString("DEVICE_MAC_ID");
        APIInterface apiInterface = APIClient.getClient(host).create(APIInterface.class);
        try {
            Response<Device> response = apiInterface.getDeviceDetails(deviceNumber).execute();
            Data outputData = null;
            if (response.isSuccessful()) {
                Device device = response.body();
                if(null != device && null != device.getDeviceId()) {
                    if(device.getLiveUrlPath() != null) {
                        Data outputDataLiveUrl = new Data.Builder()
                                .putString("live_url", device.getLiveUrlPath())
                                .build();
                        return Result.failure(outputDataLiveUrl);
                    }
                    Gson gson = new Gson();
                    String jsonResult = gson.toJson(device);
                    outputData = new Data.Builder()
                            .putString("device_details", jsonResult)
                            .build();
                } else {
                    Data outputDataFailure = new Data.Builder()
                            .putString("device_not_registered", "device_not_registered")
                            .build();
                    return Result.failure(outputDataFailure);
                }
                return Result.success(outputData);
            } else {
                return Result.retry();
            }
        } catch (Exception e) {
            return Result.retry();
        }
    }

}
