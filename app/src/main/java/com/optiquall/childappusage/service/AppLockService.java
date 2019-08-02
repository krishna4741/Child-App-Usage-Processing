package com.optiquall.childappusage.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.optiquall.childappusage.util.AppUtil;

import java.util.List;

public class AppLockService extends Service {
    // Write code here to run the service contiuously, and call every 50 to 300 ms getRecentApps(Context context) method to get the current open application

    public String getRecentApps(Context context) {
        String topPackageName = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            long time = System.currentTimeMillis();

            UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 30, System.currentTimeMillis() + (10 * 1000));
            UsageEvents.Event event = new UsageEvents.Event();
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
            }

            if (event != null && !TextUtils.isEmpty(event.getPackageName()) && event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (AppUtil.isRecentActivity(event.getClassName())) {
                    return event.getClassName();
                }
                return event.getPackageName();
            } else {
                topPackageName = "";
            }
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;

            // If the current running activity it will return not the package name it will return the activity refernce.
            if (AppUtil.isRecentActivity(componentInfo.getClassName())) {
                return componentInfo.getClassName();
            }

            topPackageName = componentInfo.getPackageName();
        }


        return topPackageName;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String runapp = getRecentApps(this);
        Toast.makeText(this, runapp, Toast.LENGTH_SHORT).show();
    }
}