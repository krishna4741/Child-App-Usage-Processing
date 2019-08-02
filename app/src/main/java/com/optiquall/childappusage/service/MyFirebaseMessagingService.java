package com.optiquall.childappusage.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.ui.ConfirmRequestActivity;
import com.optiquall.childappusage.ui.SplashActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.optiquall.childappusage.app.AppPreference.sharedPreferences;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String FCM_PARAM = "parentEmail";
    private static final String CHANNEL_NAME = "ChildAppTrack";
    private static final String CHANNEL_DESC = "Request for data sharing.";
    Map<String, String> data;
    RemoteMessage.Notification notification;
    private int numMessages = 0;
    private String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notification = remoteMessage.getNotification();
        data = remoteMessage.getData();
        Log.d("FROM", remoteMessage.getFrom());


        Log.d(TAG, "From: " + remoteMessage.getData());
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "From: " + remoteMessage);


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jsonObjectData = new JSONObject(remoteMessage.getData());
                sendNotification(notification, jsonObjectData);
            } catch (Exception e) {
                Log.d(TAG, "onMessageReceived: data error" + e.getMessage());
            }
        } else if (remoteMessage.getNotification() != null) {
            try {
                JSONObject jsonObjectNotification = new JSONObject(remoteMessage.getData());
                sendNotification(notification, jsonObjectNotification);

            } catch (Exception e) {

                Log.d(TAG, "onMessageReceived: notification error" + e.getMessage());
            }
        }


        Log.e(TAG, "onMessageReceived: " + remoteMessage.getData());
        Log.e(TAG, "onMessageReceived: " + remoteMessage.getNotification());
        //sendNotification(notification, data);
    }


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("fcm_token", token).apply();
    }

    private void sendNotification(RemoteMessage.Notification notification, JSONObject jsonObjectData) {

        try {
            Log.e(TAG, "sendNotification: " + jsonObjectData.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putString(FCM_PARAM, data.get(FCM_PARAM));

        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("parentEmail",  data.get("parentEmail"));
        intent.putExtra("childEmail",  data.get("childEmail"));
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
                .setContentIntent(pendingIntent)

                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(getResources().getColor(R.color.colorAccent))
                .setLights(Color.BLUE, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setNumber(++numMessages)
                .setSmallIcon(R.drawable.ic_default_app);

       /* try {
            String picture = data.get(FCM_PARAM);
            if (picture != null && !"".equals(picture)) {
                URL url = new URL(picture);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.notification_channel_id), CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        assert notificationManager != null;
        notificationManager.notify(0, notificationBuilder.build());
    }
}