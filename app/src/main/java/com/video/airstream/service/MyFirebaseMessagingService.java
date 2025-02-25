package com.video.airstream.service;

import android.annotation.SuppressLint;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> eventMap = remoteMessage.getData();
        if(eventMap.get("event") != null) {
            this.sendBroadcast(new Intent(Intent.ACTION_SEND));
        }
    }
}
