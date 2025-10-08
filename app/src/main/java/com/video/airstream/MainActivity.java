package com.video.airstream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.video.airstream.modal.Event.DELETE_ALL_VIDEOS;
import static com.video.airstream.modal.Event.DELETE_VIDEO;
import static com.video.airstream.modal.Event.DEVICE_BOOT;
import static com.video.airstream.modal.Event.LIVE_URL_START;
import static com.video.airstream.modal.Event.LIVE_URL_STOP;
import static com.video.airstream.modal.Event.LOG_DETAILS;
import static com.video.airstream.modal.Event.PLAY_ALL;
import static com.video.airstream.modal.Event.PLAY_LIVE_URL;
import static com.video.airstream.modal.Event.UPDATE_VIDEOS;
import static com.video.airstream.modal.Event.UPLOAD_VIDEOS;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.Event;
import com.video.airstream.service.APIInterface;
import com.video.airstream.worker.DeviceDetailsWorker;
import com.video.airstream.worker.DeviceFireBaseTokenWorker;
import com.video.airstream.worker.DownloadStatusCheckerWorker;
import com.video.airstream.worker.MultiVideosDownloadWorker;
import com.video.airstream.worker.SyncVideosWorker;
import com.video.airstream.worker.TickerWorker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private TextView tickerTextView;
    private TextView newsZoneTextView;
    private YouTubePlayerView youTubePlayerView;
    AtomicInteger currentPosition = new AtomicInteger();
    String token = null;
    int videoLength = 0;
    File[] videoFiles = null;
    String urlPlayerId;
    String deviceNumber;
    String host;
    String downloadPath;


    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = "";
            if(intent.getAction() !=null) {
                action = intent.getAction();
            }
            switch (action) {
                case "UPLOAD_VIDEOS":
                case "UPDATE_VIDEOS":
                case "LIVE_URL_START":
                case "LIVE_URL_STOP":
                    startConditionalWorkChain(getBaseContext());
                    break;
                case "DELETE_VIDEO":
                case "DELETE_ALL_VIDEOS":
                    startDeletingVideos(getBaseContext());
                    resetVideoView();
                    break;
                case "PLAY_ALL":
                    playAllVideo(DEVICE_BOOT);
                    break;

            }

        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(myReceiver, new IntentFilter(UPDATE_VIDEOS.name()));
        registerReceiver(myReceiver, new IntentFilter(UPLOAD_VIDEOS.name()));
        registerReceiver(myReceiver, new IntentFilter(DELETE_VIDEO.name()));
        registerReceiver(myReceiver, new IntentFilter(DELETE_ALL_VIDEOS.name()));
        registerReceiver(myReceiver, new IntentFilter(LOG_DETAILS.name()));
        registerReceiver(myReceiver, new IntentFilter(PLAY_ALL.name()));
        registerReceiver(myReceiver, new IntentFilter(PLAY_LIVE_URL.name()));
        registerReceiver(myReceiver, new IntentFilter(LIVE_URL_START.name()));
        registerReceiver(myReceiver, new IntentFilter(LIVE_URL_STOP.name()));
        this.videoView.setVisibility(VISIBLE);
        this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
        this.videoView.start();
        this.videoView.setOnCompletionListener(mp -> playAllVideo(PLAY_ALL));
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    token = task.getResult();
                    startConditionalWorkChain(getBaseContext());
                });


    }

    @Override
    protected void onStop() {
        super.onStop();
        this.videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceNumber = getSerialNumber();
        host = getString(R.string.host_path);
        downloadPath = getString(R.string.download_path);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        videoView = findViewById(R.id.idVideoView);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        tickerTextView = findViewById(R.id.tickerTextView);
        newsZoneTextView = findViewById(R.id.news_zone);

    }

    @Override
    public void onPostResume(){
        super.onPostResume();
    }
    public void setNewsTicker(String ticker) {
        tickerTextView.setSelected(true);
        tickerTextView.setText(ticker);
    }
    public void resetVideoView() {
        videoView.stopPlayback();
        this.videoView.setVisibility(VISIBLE);
        this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
        this.videoView.start();
        this.videoView.setOnCompletionListener(mp -> {});
    }
    public void playAllVideo(Event event) {
        if(videoView.getVisibility() == GONE) {
            stopLiveUrl();
            videoView.setVisibility(VISIBLE);
        }
        // Show WebView
        File videoDir= this.getBaseContext().getExternalFilesDir("airstream");
        if(null != videoDir && videoDir.listFiles() != null && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
            videoFiles = videoDir.listFiles();
            videoLength = videoFiles != null ? videoFiles.length : 0;
            if (!event.equals(UPLOAD_VIDEOS) ) {
                if(videoView.isPlaying()) {
                    this.videoView.suspend();
                }

                this.videoView.setVideoPath(videoFiles[0].getAbsolutePath());
                this.videoView.start();
                this.videoView.setOnCompletionListener(mp -> {
                    if(currentPosition.incrementAndGet() >= videoLength) {
                        currentPosition.set(0);
                    }
                    videoView.setVideoPath(videoFiles[currentPosition.get()].getAbsolutePath());
                    videoView.start();
                });
                this.videoView.setOnErrorListener((mp, what, extra) -> {
                      videoView.stopPlayback();
                      playAllVideo(DEVICE_BOOT);
                      return true;
                });
            }

        } else {
            this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
            Toast.makeText(getBaseContext(), "Loading..... No videos available", Toast.LENGTH_LONG).show();
        }

    }

    public void playLiveUrl(String liveUrlPath) {
        Uri uri = Uri.parse(liveUrlPath);
        urlPlayerId = uri.getQueryParameter("v");
        videoView.stopPlayback(); // Stop video playback
        videoView.setVisibility(GONE);
        tickerTextView.setVisibility(GONE);
        newsZoneTextView.setText(R.string.news_live);
        youTubePlayerView.setVisibility(VISIBLE);
        youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> youTubePlayer.loadVideo(urlPlayerId,0));

    }

    public void stopLiveUrl() {
        tickerTextView.setVisibility(VISIBLE);
        newsZoneTextView.setText(R.string.news_zone);
        youTubePlayerView.setVisibility(GONE);
        videoView.setVisibility(VISIBLE);
    }

    public String getSerialNumber() {
        String serialNumber;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber != null && serialNumber.isEmpty())
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber != null && serialNumber.isEmpty())
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber != null && serialNumber.isEmpty())
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber != null && serialNumber.isEmpty())
                serialNumber = Build.getSerial();
        } catch (Exception e) {
            serialNumber = null;
        }
        return serialNumber;
    }

    public void startConditionalWorkChain(Context context) {

        Data inputData = new Data.Builder()
                .putString("HOST", host)
                .putString("DEVICE_MAC_ID", deviceNumber)
                .putString("DOWNLOAD_PATH", downloadPath)
                .putString("TOKEN", token)
                .build();

        // Create the individual work requests
        OneTimeWorkRequest deviceDetailsRequest = new OneTimeWorkRequest.Builder(DeviceDetailsWorker.class)
                .setInputData(inputData).build();
        OneTimeWorkRequest deviceTokenRequest = new OneTimeWorkRequest.Builder(DeviceFireBaseTokenWorker.class)
                .setInputData(inputData).build();
        OneTimeWorkRequest videoSyncRequest = new OneTimeWorkRequest.Builder(SyncVideosWorker.class).build();
        OneTimeWorkRequest multiVideoDownloadRequest = new OneTimeWorkRequest.Builder(MultiVideosDownloadWorker.class)
                .setInputData(inputData).build();
        OneTimeWorkRequest downloadStatusWorker = new OneTimeWorkRequest.Builder(DownloadStatusCheckerWorker.class)
                .setInputData(inputData).build();

        OneTimeWorkRequest tickerWorker = new OneTimeWorkRequest.Builder(TickerWorker.class)
                .setInputData(inputData).build();

        // Enqueue the chain of workers
        WorkManager.getInstance(context)
                .beginWith(deviceDetailsRequest)
                .then(deviceTokenRequest)
                .then(tickerWorker)
                .then(videoSyncRequest)
                .then(multiVideoDownloadRequest)
                .then(downloadStatusWorker)
                .enqueue();

        listenDeviceDetails(deviceDetailsRequest);
        listenVideoSyncDetails(videoSyncRequest);
        listenMultiVideoDownloader(multiVideoDownloadRequest, context);
        listenAllVideoDownloaderStatus(downloadStatusWorker);
        listenForTicker(tickerWorker);

    }

    public void startDeletingVideos(Context context) {
        Data inputData = new Data.Builder()
                .putString("HOST", host)
                .putString("DEVICE_MAC_ID", deviceNumber)
                .putString("DOWNLOAD_PATH", downloadPath)
                .build();
        // Create the individual work requests
        OneTimeWorkRequest deviceDetailsRequest = new OneTimeWorkRequest.Builder(DeviceDetailsWorker.class)
                .setInputData(inputData).build();
        OneTimeWorkRequest videoSyncRequest = new OneTimeWorkRequest.Builder(SyncVideosWorker.class).build();

        // Enqueue the chain of workers
        WorkManager.getInstance(context)
                .beginWith(deviceDetailsRequest)
                .then(videoSyncRequest)
                .enqueue();

        listenVideoSyncDetails(videoSyncRequest);

    }

    private void fetchTickerData(String orgId) {
        Data inputData = new Data.Builder()
                .putString("HOST", host)
                .putString("orgId", orgId)
                .build();
        // Create the individual work requests
        OneTimeWorkRequest tickerRequest = new OneTimeWorkRequest.Builder(TickerWorker.class)
                .setInputData(inputData).build();
        WorkManager.getInstance(getBaseContext())
                .beginWith(tickerRequest)
                .enqueue();
    }

    private void listenDeviceDetails(OneTimeWorkRequest deviceDetailsRequest){
        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(deviceDetailsRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Data outputData = workInfo.getOutputData();
                            String failureResult = outputData.getString("device_not_registered");
                            String liveUrl = outputData.getString("live_url");
                            if(failureResult !=null) {
                                Toast.makeText(getBaseContext(), "Device is not registered", Toast.LENGTH_LONG).show();
                            }
                            if(liveUrl !=null) {
                                playLiveUrl(liveUrl);
                            }
                        }
                    }
                });

    }

    private void listenMultiVideoDownloader(OneTimeWorkRequest multiVideoDownloadRequest, Context context){
        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(multiVideoDownloadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Data outputData = workInfo.getOutputData();
                            String failureResult = outputData.getString("videos_not_available");
                            if(failureResult !=null) {
                                Toast.makeText(getBaseContext(), "Loading..... No videos available", Toast.LENGTH_LONG).show();
                                startConditionalWorkChain(context);
                            }
                        }
                    }
                });
    }

    private void listenAllVideoDownloaderStatus(OneTimeWorkRequest downloadStatusWorker){
        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(downloadStatusWorker.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                           playAllVideo(DEVICE_BOOT);
                        }
                    }
                });
    }

    private void listenVideoSyncDetails(OneTimeWorkRequest videoSyncRequest){
        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(videoSyncRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data outputData = workInfo.getOutputData();
                            boolean videoDeleted = outputData.getBoolean("deleted", false);
                            if(videoDeleted) playAllVideo(PLAY_ALL);
                        }
                    }
                });
    }

    private void listenForTicker(OneTimeWorkRequest tickerWorker){
        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(tickerWorker.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data outputData = workInfo.getOutputData();
                            String ticker = outputData.getString("ticker");
                            if(ticker !=null && !ticker.isEmpty()) {
                                setNewsTicker(ticker);
                            }
                        }
                    }
                });

    }


}