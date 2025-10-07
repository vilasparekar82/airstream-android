package com.video.airstream.worker;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadStatusCheckerWorker extends Worker {

    public DownloadStatusCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query);

        boolean allDownloadsComplete = true;
        if (cursor != null && cursor.moveToFirst()) {
            int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            do {
                int status = cursor.getInt(statusColumnIndex);
                if (status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING) {
                    allDownloadsComplete = false;
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (allDownloadsComplete) {
            return Result.success();
        } else {
            return Result.retry();
        }
    }
}
