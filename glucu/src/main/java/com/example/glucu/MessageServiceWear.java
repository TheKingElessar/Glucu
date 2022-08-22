package com.example.glucu;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageServiceWear extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        System.out.println("Watch received message.");

        if (messageEvent.getPath().equals("/my_path")) {
            final String message = new String(messageEvent.getData());
            System.out.println("Watch received message: " + message);
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

}
