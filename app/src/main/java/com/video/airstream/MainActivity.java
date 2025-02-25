package com.video.airstream;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.VideoData;
import com.video.airstream.service.APIInterface;
import com.video.airstream.service.DownloadHelper;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    APIInterface apiInterface;


    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            syncDeviceDetails();
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_SEND));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        apiInterface = APIClient.getClient().create(APIInterface.class);
        this.videoView = findViewById(R.id.idVideoView);
        this.setupFirebaseToken();
        this.syncDeviceDetails();
        this.playAllVideo();
    }

    public void playAllVideo() {
        File videoDir= this.getBaseContext().getExternalFilesDir("airstream");
        if(null != videoDir && videoDir.listFiles() != null && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
            File[] videoFiles = videoDir.listFiles();
            int videoLength = videoFiles.length;
            AtomicInteger currentPosition = new AtomicInteger();
            this.videoView.setVideoPath(videoFiles[0].getAbsolutePath());
            this.videoView.start();

            this.videoView.setOnCompletionListener(mp -> {
                System.out.println("Video completed");
                if(currentPosition.incrementAndGet() >= videoLength) {
                    currentPosition.set(0);
                }
                videoView.setVideoPath(videoFiles[currentPosition.get()].getAbsolutePath());
                videoView.start();
            });
        }

    }

    public void syncDeviceDetails() {
        Call<Device> deviceDetailsCall = apiInterface.getDeviceDetails("192.168.1.8");
        deviceDetailsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                Device device = response.body();
                if (device != null && !device.getVideoDataSet().isEmpty()) {
                    if (videoView.isPlaying()) {
                        videoView.stopPlayback();
                    }
                    syncLocalVideos(device);
                    downloadAllVideos(MainActivity.super.getBaseContext(), device);
                } else {
                    callSyncMethod();
                }

            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                call.cancel();
                callSyncMethod();
            }
        });
    }

    public void downloadAllVideos(Context context, Device device){
        DownloadHelper helper = new DownloadHelper();
        File videoDir = this.getBaseContext().getExternalFilesDir("airstream");
        String [] listOfFiles = null;
        if(null != videoDir && null != videoDir.list() && Objects.requireNonNull(videoDir.list()).length > 0) {
            listOfFiles = videoDir.list();
        }
        Predicate<VideoData> fileChecker = object -> true;
        if(listOfFiles != null) {
            final String[] listOfFileName = listOfFiles;
            fileChecker = object -> Arrays.stream(listOfFileName).noneMatch(filePath -> filePath.contains(object.getVideoName()));
        }
        device.getVideoDataSet().stream().filter(fileChecker).forEach(videoData -> {
            helper.beginDownload(context, device.getDeviceId(), videoData.getVideoName());
        });
        playAllVideo();
    }

    public void syncLocalVideos(Device device) {
        File videoDir = this.getBaseContext().getExternalFilesDir("airstream");
        if(null != videoDir && null != videoDir.listFiles() && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
            Arrays.stream(Objects.requireNonNull(videoDir.listFiles())).forEach(video -> {
                long videoCount = device.getVideoDataSet().stream().filter(videoData -> video.getName().contains(videoData.getVideoName())).count();
                if(videoCount == 0) {
                    video.delete();
                }
            });
        }
    }

    public void callSyncMethod(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                syncDeviceDetails();
            }
        }, 60000);
    }

    public void setupFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, token);
                });
    }
}