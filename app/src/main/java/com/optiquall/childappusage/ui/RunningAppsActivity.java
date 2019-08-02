package com.optiquall.childappusage.ui;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.optiquall.childappusage.R;
import com.optiquall.childappusage.adapter.AppNameArrayAdapter;
import com.optiquall.childappusage.data.AppName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class RunningAppsActivity extends AppCompatActivity {
    private static final int MAX_RECENT_TASKS = 10;
    private static RecyclerView myRunningApps;
    ArrayList<AppName> runningAppList = new ArrayList<AppName>();
    AppNameArrayAdapter itemArrayAdapter;
    private String TAG = "RunningAppsActivity";
    private List<ActivityManager.RecentTaskInfo> activityInfoList;

    public static String getAppNameFromPkgName(Context context, String Packagename) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(Packagename, PackageManager.GET_META_DATA);
            String appName = (String) packageManager.getApplicationLabel(info);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.running_apps_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.running_apps);
        }
        initViews();
        startServiceToGetRecentApps();
        activityInfoList = loadRecentTask(this);
        Log.e(TAG, "onCreate: activityInfoList.size " + activityInfoList.size());
    }

    private void initViews() {
        myRunningApps = findViewById(R.id.myRunningApps);
        //handle addapter data here
        //adapter = new RecyclerAdapter();
        AppNameArrayAdapter itemArrayAdapter = new AppNameArrayAdapter(R.layout.list_item_running_app, runningAppList);
        myRunningApps = findViewById(R.id.myRunningApps);
        myRunningApps.setLayoutManager(new LinearLayoutManager(this));
        myRunningApps.setItemAnimator(new DefaultItemAnimator());
        myRunningApps.setAdapter(itemArrayAdapter);
        myRunningApps.setLayoutManager(new LinearLayoutManager(this));
    }

    public void startServiceToGetRecentApps() {

        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(10, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        for (ActivityManager.RecentTaskInfo recentTask : recentTasks) {
            try {
                String packageName = recentTask.baseIntent.getComponent().getPackageName();
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String label = getPackageManager().getApplicationLabel(appInfo).toString();
                //Log.e("Running application", label);
                if (label != null) {
                    runningAppList.add(new AppName(label));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            // We get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            // Sort the stats by the last time used
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    for (Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                        Long key = entry.getKey();
                        UsageStats value = entry.getValue();

                        String appname = getAppNameFromPkgName(this, mySortedMap.get(entry.getKey()).getPackageName());
                        //  Log.e(" for Running task", appname);
                        if (appname != null && !appname.isEmpty())
                            runningAppList.add(new AppName(getAppNameFromPkgName(this, mySortedMap.get(entry.getKey()).getPackageName())));

                    }
                }
            }
        }


    }

    public List<ActivityManager.RecentTaskInfo> loadRecentTask(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> activityInfoList = am.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_WITH_EXCLUDED);
        return activityInfoList;
    }
}
