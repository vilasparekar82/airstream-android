package com.video.airstream.worker;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.VideoData;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public class MultiVideosDownloadWorker extends Worker {
    public MultiVideosDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String deviceJson = inputData.getString("device_details");
        if (TextUtils.isEmpty(deviceJson)) {
            return Result.failure();
        } else {
            Gson gson = new Gson();
            Device device = gson.fromJson(deviceJson, Device.class);
            Context context = getApplicationContext();
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            File videoDir = getApplicationContext().getExternalFilesDir("airstream");
            if(!device.getVideoDataSet().isEmpty()) {
                Predicate<VideoData> fileChecker = getVideoDataPredicate(videoDir);
                device.getVideoDataSet().stream().filter(fileChecker).forEach(videoData -> {
                    String endpoint = inputData.getString("HOST") + inputData.getString("DOWNLOAD_PATH");
                    String url = endpoint + device.getDeviceId() + "/" + videoData.getVideoId();
                    Uri uri = Uri.parse(url);
                    DownloadManager.Request request = new DownloadManager.Request(uri)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle("Downloading File...")
                            .setDescription(uri.getLastPathSegment());
                    downloadManager.enqueue(request);
                });
            }
        }
        return Result.success();
    }

    @NonNull
    private static Predicate<VideoData> getVideoDataPredicate(File videoDir) {
        String [] listOfFiles = null;
        if(null != videoDir && null != videoDir.list() && Objects.requireNonNull(videoDir.list()).length > 0) {
            listOfFiles = videoDir.list();
        }
        Predicate<VideoData> fileChecker = object -> true;
        if(listOfFiles != null) {
            final String[] listOfFileName = listOfFiles;
            fileChecker = object -> Arrays.stream(listOfFileName).noneMatch(filePath -> filePath.contains(object.getVideoName()));
        }
        return fileChecker;
    }
}
