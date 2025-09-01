package com.easyranktools.callhistoryforanynumber;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "FCM token: " + token);
        // Optional: send to your server / Firebase DB:
        //TokenUploader.sendToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        Log.d(TAG, "From: " + msg.getFrom());

        // Title/body from Notification payload (if present)
        String title = msg.getNotification() != null ? msg.getNotification().getTitle() : null;
        String body  = msg.getNotification() != null ? msg.getNotification().getBody()  : null;

        // You can also read custom keys from data payload:
        // Map<String, String> data = msg.getData();
        // String screen = data.get("screen");

        showSimpleNotification(
                title != null ? title : getString(R.string.app_name),
                body  != null ? body  : "You have a new message"
        );
    }

    private void showSimpleNotification(String title, String text) {
        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, MyApplication.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_phone) // <-- add a 24x24dp white icon in res/drawable
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), b.build());
    }
}

