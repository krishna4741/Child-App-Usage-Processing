package com.optiquall.childappusage.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.log.FileLogManager;

import java.util.ArrayList;
import java.util.List;


public class SplashActivity extends AppCompatActivity {
    AppPreference appPreference;
    private String TAG = "SplashActivity";
    private String childEmail,parentEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        appPreference = new AppPreference(this);
        getNotification();
        if (checkPermissions().size() == 0) {
            delayEnter();
        } else {
            requestPermissions();
        }
    }

    private void delayEnter() {
        new CountDownTimer(1200, 1200) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(SplashActivity.this, LoginTabActivity.class));
                finish();
            }
        }.start();
    }

    private List<String> checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        return permissions;
    }

    private void requestPermissions() {
        List<String> permissions = checkPermissions();
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                FileLogManager.init();
                break;
            }
        }
        delayEnter();
    }


    private void getNotification() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            //when app is in foreground and click on notification

             parentEmail = extras.getString("parentEmail");
             childEmail = extras.getString("childEmail");
            Log.e(TAG, "getNotification: parentEmail: " + parentEmail);
            Log.e(TAG, "getNotification: childEmail: " + childEmail);
            if (parentEmail != null) {
                //if app is in foreground when user click on notification

                parentEmail = extras.getString("parentEmail");
                childEmail = extras.getString("childEmail");
                Log.e(TAG, "getNotification: parentEmail destination" + extras.getString("parentEmail"));
            } else {
                //if app is in background when user click on notification
                for (String key : extras.keySet()) {
                    if (key.equals("parentEmail")) {

                        parentEmail = extras.getString("parentEmail");
                        childEmail = extras.getString("childEmail");
                        Log.e(TAG, "getNotification: xxx parentEmail: " + getIntent().getExtras().get("parentEmail"));
                    }


                }
            }
            //getNotificationDirection(destination);
            sendToNotificationRequest(childEmail);

        } else {
            // first time app open login flow after splash screen
            //autoLoginFlow();
        }
    }

    private void sendToNotificationRequest(String childEmail) {
        Log.e(TAG, "sendToNotificationRequest: childEmail: " + childEmail);
        Intent confirmRequestIntent = new Intent(SplashActivity.this, ConfirmRequestActivity.class);
        confirmRequestIntent.putExtra("childEmail", childEmail);
        startActivity(confirmRequestIntent);
    }

}
