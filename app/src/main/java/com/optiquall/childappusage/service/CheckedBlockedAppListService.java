package com.optiquall.childappusage.service;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optiquall.childappusage.data.AppItem;
import com.optiquall.childappusage.data.DataManager;
import com.optiquall.childappusage.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CheckedBlockedAppListService extends Service {
    public static final int notify = 16000;  //interval between two services(Here Service run every 5 Minute)
    private static List<AppItem> blocked_list;
    private static List<AppItem> blocked_list_new;
    private static AlertDialog.Builder builder;
    private static AlertDialog dialog;
    MainActivity mact;
    MainActivity act;
    //creating a mediaplayer object
    private String email;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    private String TAG = "CheckedBlockedAppListService";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            blocked_list = new ArrayList<>();
            blocked_list_new = new ArrayList<>();
            mact = (MainActivity) act.mactivity;
            builder = new AlertDialog.Builder(mact);

            if (mTimer != null) // Cancel if already existed
                mTimer.cancel();
            else
                mTimer = new Timer();   //recreate new
            mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        email = intent.getStringExtra("email");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("users").child(email).hasChild("blocked_apps")) {
                    //if (snapshot.hasChild("users")) {
                } else {
                    stopSelf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Log.d("service is ", "Destroyed");
    }

    private void getBlockedAppsList() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String todaysDate = df.format(c);
        Log.e(TAG, "getBlockedAppsList: email: " + email);
        Log.e(TAG, "getBlockedAppsList: todaysDate: " + todaysDate);
        blocked_list.clear();
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference collionRef = rootReference.child("users").child(email).child("blocked_apps").child(todaysDate);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AppItem appItem = ds.getValue(AppItem.class);
                    blocked_list.add(appItem);
                }
                checkRunningAppList(blocked_list);
                Log.e(TAG, "getBlockedAppsList11: blocked_list.size()" + blocked_list.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        collionRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void checkRunningAppList(List<AppItem> blocked_list) {
        new MyAsyncTask(blocked_list).execute(1, 0);
    }

    private void showBlockedListAlert(List<AppItem> foundedApplication) {


        String[] appNames = new String[foundedApplication.size()];
        int i = 0;
        for (AppItem appItem : foundedApplication) {
            appNames[i] = appItem.mName;
            i++;
        }


        builder.setTitle("Blocked App List")
                .setItems(appNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                    }
                });
        dialog = builder.create();
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Toast.makeText(mact, "Thanks", Toast.LENGTH_SHORT).show();
            }
        });


        try {
            if (!dialog.isShowing()) {
                dialog.show();
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getBlockedAppsList();
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {
        List<AppItem> blocked_list = new ArrayList<>();

        public MyAsyncTask(List<AppItem> blocked_list) {
            this.blocked_list = blocked_list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected List<AppItem> doInBackground(Integer... integers) {
            Log.e(TAG, "doInBackground: " + blocked_list.size());
            return DataManager.getInstance().getApps(getApplicationContext(), 1, 0);
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {
            List<AppItem> foundedApplication = new ArrayList<AppItem>(blocked_list);
            foundedApplication.retainAll(appItems);
            for (AppItem appItem : foundedApplication) {
                Log.e(TAG, "Blocked Applications: " + appItem.mName);
            }
            if (foundedApplication.size() > 0)
                showBlockedListAlert(foundedApplication);
        }
    }
}

