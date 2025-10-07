package com.video.airstream.worker;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.service.APIInterface;

import java.io.IOException;

public class DeviceFireBaseTokenWorker extends Worker {

    public DeviceFireBaseTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String deviceJson = inputData.getString("device_details");
        String host = inputData.getString("HOST");
        String token = inputData.getString("TOKEN");
        if (TextUtils.isEmpty(deviceJson)) {
            return Result.failure();
        } else {
            Gson gson = new Gson();
            Device device = gson.fromJson(deviceJson, Device.class);
            if(device.getDeviceToken() == null || (device.getDeviceToken() != null && !device.getDeviceToken().equals(token))) {
                device.setDeviceToken(token);
                APIInterface apiInterface = APIClient.getClient(host).create(APIInterface.class);
                try {
                    apiInterface.updateDeviceToken(device).execute();
                } catch (IOException ignored) {}

            }

        }
        Data outputData = new Data.Builder()
                .putString("device_details", deviceJson)
                .build();
        return Result.success(outputData);
    }
}
