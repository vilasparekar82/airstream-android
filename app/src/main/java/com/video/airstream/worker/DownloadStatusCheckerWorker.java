package com.video.airstream.worker;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.modal.Device;

import java.util.ArrayList;
import java.util.List;

public class DownloadStatusCheckerWorker extends Worker {

    public DownloadStatusCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String downloadIdsJson = inputData.getString("videos_download_ids");
        if (TextUtils.isEmpty(downloadIdsJson)) {
            return Result.failure();
        } else {
            Gson gson = new Gson();
            List<Long> downloadIds = gson.fromJson(downloadIdsJson, List.class);
            if(downloadIds.isEmpty()) {
                return Result.success();
            }
            DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            while (downloadIds.isEmpty()) {
                for (var id : downloadIds) {
                    DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
                    try (android.database.Cursor cursor = downloadManager.query(query)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloadIds.remove(id);
                            }
                            if (status == DownloadManager.STATUS_FAILED) {
                                // Handle failed download
                                return Result.failure();
                            }
                        }
                    }
                }
            }
        }

        return Result.success();
    }
}
