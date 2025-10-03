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
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.video.airstream.modal.Event;
import com.video.airstream.service.LogInformation;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView youTubePlayerView;
    AtomicInteger currentPosition = new AtomicInteger();
    int videoLength = 0;
    File[] videoFiles = null;
    DeviceAsyncTask deviceAsyncTask;

    String urlPlayerId;

    String serialNumber;


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
                case "LIVE_URL_START":
                    deviceAsyncTask.runAsyncTask(DEVICE_BOOT);
                    break;
                case "PLAY_ALL":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playAllVideo(DEVICE_BOOT);
                        }
                    });

                    break;
                case "PLAY_LIVE_URL":
                    String liveUrl = Objects.requireNonNull(intent.getExtras()).getString(PLAY_LIVE_URL.name());
                    playLiveUrl(liveUrl);
                    break;
                case "LIVE_URL_STOP":
                    stopLiveUrl();
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
        this.videoView.setOnCompletionListener(mp -> {
            this.playAllVideo(DEVICE_BOOT);
            deviceAsyncTask.runAsyncTask(DEVICE_BOOT);
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
        serialNumber = getSerialNumber();
        deviceAsyncTask = new DeviceAsyncTask(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.videoView = findViewById(R.id.idVideoView);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
    }

    @Override
    public void onPostResume(){
        super.onPostResume();
    }

    public void playAllVideo(Event event) {

        videoView.setVisibility(VISIBLE);
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
                LogInformation.appendLog(serialNumber, videoFiles[0].getName()  + " " + LocalDateTime.now());
                System.out.println(videoFiles[0].getName()  + " " + LocalDateTime.now());
                this.videoView.setOnCompletionListener(mp -> {
                    if(currentPosition.incrementAndGet() >= videoLength) {
                        currentPosition.set(0);
                    }
                    videoView.setVideoPath(videoFiles[currentPosition.get()].getAbsolutePath());
                    videoView.start();
                    LogInformation.appendLog(serialNumber, videoFiles[currentPosition.get()].getName()  + " " + LocalDateTime.now());
                    System.out.println(videoFiles[currentPosition.get()].getName() + " " + LocalDateTime.now());
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.stopPlayback(); // Stop video playback
                videoView.setVisibility(GONE);
                youTubePlayerView.setVisibility(VISIBLE);
                youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> {
                    youTubePlayer.loadVideo(urlPlayerId,0);
                });
            }
        });

    }

    public void stopLiveUrl() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                youTubePlayerView.setVisibility(GONE);
                videoView.setVisibility(VISIBLE);
                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
                videoView.start();
                videoView.setOnCompletionListener(mp -> {
                    playAllVideo(DEVICE_BOOT);
                    deviceAsyncTask.runAsyncTask(DEVICE_BOOT);
                });
            }
        });

    }

    public static String getSerialNumber() {
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

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            serialNumber = null;
        }

        return serialNumber;
    }

}