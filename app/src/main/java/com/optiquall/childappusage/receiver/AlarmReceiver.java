package com.optiquall.childappusage.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.optiquall.childappusage.service.AppService;


public class AlarmReceiver extends BroadcastReceiver {
    Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(new Intent(context, AppService.class));
//            }else {
            if (!isMyServiceRunning(AppService.class)) {
                context.startService(new Intent(context, AppService.class));
            }
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        // context.startService(new Intent(context, AlarmService.class));
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
