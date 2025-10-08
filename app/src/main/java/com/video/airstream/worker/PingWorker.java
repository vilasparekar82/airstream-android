package com.video.airstream.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.service.APIInterface;

import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class PingWorker extends Worker {

    public PingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
            apiInterface.pingDevice(deviceNumber).execute();
        } catch (Exception ignored) {}

        return Result.success();
    }
}
