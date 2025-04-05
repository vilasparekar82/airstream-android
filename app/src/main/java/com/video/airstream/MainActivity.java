package com.video.airstream;

import static android.content.ContentValues.TAG;

import static com.video.airstream.modal.Event.DELETE_ALL_VIDEOS;
import static com.video.airstream.modal.Event.DELETE_VIDEO;
import static com.video.airstream.modal.Event.DEVICE_BOOT;
import static com.video.airstream.modal.Event.LOG_DETAILS;
import static com.video.airstream.modal.Event.PLAY_ALL;
import static com.video.airstream.modal.Event.UPDATE_VIDEOS;
import static com.video.airstream.modal.Event.UPLOAD_VIDEOS;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.FileUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.Event;
import com.video.airstream.modal.VideoData;
import com.video.airstream.service.APIInterface;
import com.video.airstream.service.DownloadHelper;
import com.video.airstream.service.LogInformation;

import java.io.BufferedWriter;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    AtomicInteger currentPosition = new AtomicInteger();
    int videoLength = 0;
    File[] videoFiles = null;
    DeviceAsyncTask deviceAsyncTask;

    String ipAddress;


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
                case "DELETE_VIDEO":
                case "DELETE_ALL_VIDEOS":
                    deviceAsyncTask.runAsyncTask(DEVICE_BOOT);
                    break;
                case "PLAY_ALL":
                    playAllVideo(DEVICE_BOOT);
                case "LOG_DETAILS":
                    deviceAsyncTask.sendLogDetails();

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceAsyncTask = new DeviceAsyncTask(this);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.videoView = findViewById(R.id.idVideoView);
        this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
        this.videoView.start();
        this.videoView.setOnCompletionListener(mp -> {
            this.playAllVideo(DEVICE_BOOT);
            deviceAsyncTask.runAsyncTask(DEVICE_BOOT);
        });
    }

    public void playAllVideo(Event event) {
        File videoDir= this.getBaseContext().getExternalFilesDir("airstream");
        if(null != videoDir && videoDir.listFiles() != null && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
            videoFiles = videoDir.listFiles();
            videoLength = videoFiles.length;
            if (!event.equals(UPLOAD_VIDEOS) ) {
                if(videoView.isPlaying()) {
                    this.videoView.suspend();
                }

                this.videoView.setVideoPath(videoFiles[0].getAbsolutePath());
                this.videoView.start();
                LogInformation.appendLog(ipAddress, videoFiles[0].getName()  + " " + LocalDateTime.now());
                System.out.println(videoFiles[0].getName()  + " " + LocalDateTime.now());
                this.videoView.setOnCompletionListener(mp -> {
                    if(currentPosition.incrementAndGet() >= videoLength) {
                        currentPosition.set(0);
                    }
                    videoView.setVideoPath(videoFiles[currentPosition.get()].getAbsolutePath());
                    videoView.start();
                    LogInformation.appendLog(ipAddress, videoFiles[currentPosition.get()].getName()  + " " + LocalDateTime.now());
                    System.out.println(videoFiles[currentPosition.get()].getName() + " " + LocalDateTime.now());
                });
                this.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                          videoView.stopPlayback();
                          playAllVideo(DEVICE_BOOT);
                          return true;
                    }
                });
            }

        } else {
            this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
            Toast.makeText(getBaseContext(), "Loading..... No videos available", Toast.LENGTH_LONG).show();
        }

    }

}