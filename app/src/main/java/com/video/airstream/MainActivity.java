package com.video.airstream;

import static android.content.ContentValues.TAG;

import static com.video.airstream.modal.Event.DELETE_ALL_VIDEOS;
import static com.video.airstream.modal.Event.DELETE_VIDEO;
import static com.video.airstream.modal.Event.DEVICE_BOOT;
import static com.video.airstream.modal.Event.LOG_DETAILS;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    String downloadEndpoint;
    String ipAddress;
    String host;
    private VideoView videoView;
    APIInterface apiInterface;
    AtomicInteger currentPosition = new AtomicInteger();
    int videoLength = 0;
    File[] videoFiles = null;


    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = "";
            if(intent.getAction() !=null) {
                action = intent.getAction();
            }
            switch (action) {
                case "UPLOAD_VIDEOS":
                    syncDeviceDetails(UPLOAD_VIDEOS);
                    break;
                case "UPDATE_VIDEOS":
                case "DELETE_VIDEO":
                case "DELETE_ALL_VIDEOS":
                    syncDeviceDetails(UPDATE_VIDEOS);
                    break;

                case "LOG_DETAILS":
                    sendLogDetails();

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        host = getString(R.string.host_path);
        downloadEndpoint = host + getString(R.string.download_path);
        apiInterface = APIClient.getClient(host).create(APIInterface.class);
        this.videoView = findViewById(R.id.idVideoView);
        this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
        this.videoView.start();
        this.videoView.setOnCompletionListener(mp -> {
            this.playAllVideo(DEVICE_BOOT);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    syncDeviceDetails(DEVICE_BOOT);
                }
            }).start();

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
                          return true;
                    }
                });
            }

        } else {
            this.videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
            Toast.makeText(getBaseContext(), "Loading..... No videos available", Toast.LENGTH_LONG).show();
            callSyncMethod();
        }

    }


    public synchronized void syncDeviceDetails(Event event) {
        Call<Device> deviceDetailsCall = apiInterface.getDeviceDetails(ipAddress);
        deviceDetailsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                Device device = response.body();
                if (device != null && videoView.isPlaying() && !event.equals(UPLOAD_VIDEOS) ) {
                    videoView.suspend();
                    syncLocalVideos(device);
                    setupFirebaseToken(device);
                }

                if (device != null && !device.getVideoDataSet().isEmpty()) {
                    downloadAllVideos(MainActivity.super.getBaseContext(), device);
                    playAllVideo(event);
                } else {
                    if(device != null) {
                        syncLocalVideos(device);
                    }
                    videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.welcomevideo);
                    Toast.makeText(getBaseContext(), "Loading..... No videos available", Toast.LENGTH_LONG).show();
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
            helper.beginDownload(context, device.getDeviceId(), videoData, downloadEndpoint);
        });

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
                syncDeviceDetails(DEVICE_BOOT);
            }
        }, 60000);
    }

    public void setupFirebaseToken(Device device) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    if(device.getDeviceToken() == null || (device.getDeviceToken() != null && !device.getDeviceToken().equals(token))){
                        device.setDeviceToken(token);
                        updateDeviceToken(device);
                    }
                    Log.d(TAG, token);
        });
    }

    public void updateDeviceToken(Device device){
        Call<Device> deviceTokenUpdateCall = apiInterface.updateDeviceToken(device);
        deviceTokenUpdateCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                Log.d(TAG, "Device Token updated");
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void sendLogDetails(){
        BufferedWriter buf = null;
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD");
        String todayString = formatter.format(todayDate);
        File logFile = new File("sdcard/log_airstream_" + ipAddress + "_" +todayString + ".file");
        RequestBody requestFile = RequestBody.create(MediaType.parse("text/plain"), logFile);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("logFile", logFile.getName(), requestFile);
        Call<String> uploadDeviceLog = apiInterface.upload(body);
        uploadDeviceLog.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call,
                                   Response<String> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });

    }

}