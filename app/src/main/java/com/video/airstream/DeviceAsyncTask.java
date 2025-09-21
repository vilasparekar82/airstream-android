package com.video.airstream;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.video.airstream.modal.Event.DEVICE_BOOT;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.Event;
import com.video.airstream.modal.VideoData;
import com.video.airstream.service.APIInterface;
import com.video.airstream.service.DownloadHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceAsyncTask  {
    private static final String TAG = "DeviceAsyncTask";
    APIInterface apiInterface;
    String host;
    String downloadEndpoint;

    String serialNumber;
    File videoDir;
    DownloadManager downloadManager;
    Activity activity;

    public DeviceAsyncTask(MainActivity activity){
        this.activity = activity;
        host = activity.getString(R.string.host_path);
        downloadEndpoint = host + activity.getString(R.string.download_path);
        apiInterface = APIClient.getClient(host).create(APIInterface.class);
        serialNumber = activity.serialNumber;
        videoDir = activity.getBaseContext().getExternalFilesDir("airstream");
        downloadManager= (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);

    }

    public void runAsyncTask(Event event){
        new Thread(() -> syncDeviceDetails(event)).start();
    }

    public void syncDeviceDetails(Event event) {
        Call<Device> deviceDetailsCall = apiInterface.getDeviceDetails(serialNumber);
        Log.v(TAG, "Device serial: " + serialNumber); 
        deviceDetailsCall.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                Device device = response.body();
                if(null != device && null != device.getDeviceId()) {
                    syncLocalVideos(device);
                    setupFirebaseToken(device);

                    if (!device.getVideoDataSet().isEmpty()) {
                        downloadAllVideos(device);
                        activity.sendBroadcast(new Intent(Event.PLAY_ALL.name()));
                    } else {
                        callSyncMethod();
                    }
                } else {
                    Toast.makeText(activity.getBaseContext(), "Device is not registered. Please register your device in admin portal",Toast.LENGTH_LONG).show();
                    call.cancel();
                    callSyncMethod();
                }
                Log.v(TAG, "Device details api: " + device.toString());

            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                call.cancel();
                callSyncMethod();
                Log.v(TAG, "Device details api failed: " + t.getMessage());
            }
        });
    }

    public void callSyncMethod(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                syncDeviceDetails(DEVICE_BOOT);
            }
        }, 60000);
    }

    public void downloadAllVideos(Device device){
        DownloadHelper helper = new DownloadHelper();
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
            helper.beginDownload(videoDir, device.getDeviceId(), videoData, downloadEndpoint, downloadManager);
        });

    }

    public void syncLocalVideos(Device device) {
        if(null != videoDir && null != videoDir.listFiles() && Objects.requireNonNull(videoDir.listFiles()).length > 0) {
            Arrays.stream(Objects.requireNonNull(videoDir.listFiles())).forEach(video -> {
                long videoCount = device.getVideoDataSet().stream().filter(videoData -> video.getName().contains(videoData.getVideoName())).count();
                if(videoCount == 0) {
                    video.delete();
                }
            });
        }
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

}
