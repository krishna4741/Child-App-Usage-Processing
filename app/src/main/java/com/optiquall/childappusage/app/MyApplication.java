package com.optiquall.childappusage.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.optiquall.childappusage.AppConst;
import com.optiquall.childappusage.BuildConfig;
import com.optiquall.childappusage.data.AppItem;
import com.optiquall.childappusage.data.DataManager;
import com.optiquall.childappusage.db.DbHistoryExecutor;
import com.optiquall.childappusage.db.DbIgnoreExecutor;
import com.optiquall.childappusage.service.AppService;
import com.optiquall.childappusage.ui.AppIconRequestHandler;
import com.optiquall.childappusage.util.CrashHandler;
import com.optiquall.childappusage.util.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {
public static String BASE_URL="http://192.168.0.155:8080/child_app_usage";
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.init(this);
        AndroidProcesses.setLoggingEnabled(true);
        Picasso.setSingletonInstance(new Picasso.Builder(this)
                .addRequestHandler(new AppIconRequestHandler(this))
                .build());
        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                getApplicationContext().startForegroundService(new Intent(this, AppService.class));
//            } else {
            getApplicationContext().startService(new Intent(getApplicationContext(), AppService.class));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DbIgnoreExecutor.init(getApplicationContext());
        DbHistoryExecutor.init(getApplicationContext());
        DataManager.init();
        addDefaultIgnoreAppsToDB();
        if (AppConst.CRASH_TO_FILE) CrashHandler.getInstance().init();

    }

    private void addDefaultIgnoreAppsToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> mDefaults = new ArrayList<>();
                mDefaults.add("com.android.settings");
                mDefaults.add(BuildConfig.APPLICATION_ID);
                for (String packageName : mDefaults) {
                    AppItem item = new AppItem();
                    item.mPackageName = packageName;
                    item.mEventTime = System.currentTimeMillis();
                    DbIgnoreExecutor.getInstance().insertItem(item);
                }
            }
        }).run();
    }
    public  boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
