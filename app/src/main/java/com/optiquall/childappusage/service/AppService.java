package com.optiquall.childappusage.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.optiquall.childappusage.R;
import com.optiquall.childappusage.data.DataManager;
import com.optiquall.childappusage.ui.MainActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class AppService extends Service {

    public static final String SERVICE_ACTION = "service_action";
    public static final String SERVICE_ACTION_CHECK = "service_action_check";

    static final long CHECK_INTERVAL = 400;
    static private String TAG = "AppService";
    private DataManager mManager;
    private Context mContext;
    private Handler mHandler = new Handler();
    private Runnable mRepeatCheckTask = new Runnable() {
        @Override
        public void run() {
            if (!mManager.hasPermission(mContext)) {
                mHandler.postDelayed(mRepeatCheckTask, CHECK_INTERVAL);
            } else {
                mHandler.removeCallbacks(mRepeatCheckTask);
                Toast.makeText(mContext, R.string.grant_success, Toast.LENGTH_SHORT).show();
                startService(new Intent(mContext, AlarmService.class));
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mManager = new DataManager();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra(SERVICE_ACTION);
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case SERVICE_ACTION_CHECK:
                        startIntervalCheck();
                        break;
                }
            }
        }
        retriveNewApp();
//        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(10, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
//        for (ActivityManager.RecentTaskInfo recentTask : recentTasks) {
//            try {
//                String packageName = recentTask.baseIntent.getComponent().getPackageName();
//                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//                String label = getPackageManager().getApplicationLabel(appInfo).toString();
//                Log.e("Running application", label);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(new Intent(mContext, AppService.class));
            else
                startService(new Intent(mContext, AppService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String retriveNewApp() {
        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            Log.e(TAG, "Current App in foreground is: " + currentApp);

            return currentApp;

        } else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            Log.e(TAG, "Current App in foreground is: " + mm);
            return mm;
        }
    }

    private void startIntervalCheck() {
        boolean valid = true;
        try {
            mManager.requestPermission(mContext);
        } catch (ActivityNotFoundException e) {
            valid = false;
        }
        if (valid) {
            Toast.makeText(mContext, R.string.toast_permission, Toast.LENGTH_LONG).show();
            mHandler.post(mRepeatCheckTask);
        } else {
            Toast.makeText(mContext, R.string.not_support, Toast.LENGTH_LONG).show();
        }
    }

}

