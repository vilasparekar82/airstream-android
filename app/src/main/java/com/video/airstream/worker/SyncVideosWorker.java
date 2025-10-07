package com.video.airstream.worker;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.modal.Device;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncVideosWorker extends Worker {
    public SyncVideosWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AtomicBoolean deleteStatus = new AtomicBoolean(false);
        Data inputData = getInputData();
        String deviceJson = inputData.getString("device_details");
        if (TextUtils.isEmpty(deviceJson)) {
            return Result.failure();
        } else {
            Gson gson = new Gson();
            Device device = gson.fromJson(deviceJson, Device.class);
            File videoDir = getApplicationContext().getExternalFilesDir("airstream");
            if(null != videoDir && null != videoDir.listFiles() && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
                Arrays.stream(Objects.requireNonNull(videoDir.listFiles())).forEach(video -> {
                    long videoCount = device.getVideoDataSet().stream().filter(videoData -> video.getName().contains(videoData.getVideoName())).count();
                    if(videoCount == 0) {
                        video.delete();
                        deleteStatus.set(true);
                    }
                });
            }
        }
        Data outputData = new Data.Builder()
                .putString("device_details", deviceJson)
                .putBoolean("deleted", deleteStatus.get())
                .build();
        return Result.success(outputData);
    }
}
