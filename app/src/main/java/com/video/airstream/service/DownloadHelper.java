package com.video.airstream.service;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.video.airstream.modal.VideoData;

import java.io.File;

public class DownloadHelper {
    public void beginDownload(File videoDir, Integer deviceId, VideoData videoData, String endpoint, DownloadManager downloadManager){
        String url = endpoint + deviceId + "/" + videoData.getVideoId();
        File file=new File(videoDir,videoData.getVideoName());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setDestinationUri(Uri.fromFile(file))
                .setTitle(videoData.getVideoName())
                .setDescription("Downloading")
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);
        long downloadID = downloadManager.enqueue(request);

        // using query method
        boolean finishDownload = false;
        int progress;
        while (!finishDownload) {
            Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                    case DownloadManager.STATUS_SUCCESSFUL: {
                        finishDownload = true;
                        break;
                    }
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        break;

                }
            }
        }
    }
}
