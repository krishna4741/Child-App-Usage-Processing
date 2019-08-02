package com.optiquall.childappusage.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.optiquall.childappusage.R;

import java.util.Calendar;
import java.util.Locale;


public final class AppUtil {

    private static final long A_DAY = 86400 * 1000;
    private static final String RECENT_ACTIVITY;

    static {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            RECENT_ACTIVITY = "com.android.systemui.recents.RecentsActivity";
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            RECENT_ACTIVITY = "com.android.systemui.recent.RecentsActivity";
        } else {
            RECENT_ACTIVITY = "com.android.internal.policy.impl.RecentApplicationDialog";
        }
    }

    public static boolean isRecentActivity(String className) {
        return RECENT_ACTIVITY.equalsIgnoreCase(className);

    }

    public static String parsePackageName(PackageManager pckManager, String data) {
        ApplicationInfo applicationInformation;
        try {
            applicationInformation = pckManager.getApplicationInfo(data, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInformation = null;
        }
        return (String) (applicationInformation != null ? pckManager.getApplicationLabel(applicationInformation) : data);
    }

    public static Drawable getPackageIcon(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            return manager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return context.getResources().getDrawable(R.drawable.ic_default_app);
    }

    public static String formatMilliSeconds(long milliSeconds) {
        long second = milliSeconds / 1000L;
        if (second < 60) {
            return String.format("%ss", second);
        } else if (second < 60 * 60) {
            return String.format("%sm %ss", second / 60, second % 60);
        } else {
            return String.format("%sh %sm %ss", second / 3600, second % (3600) / 60, second % (3600) % 60);
        }
    }

    public static boolean isSystemApp(PackageManager manager, String packageName) {

        boolean isSystemApp = false;
        try {
            ApplicationInfo applicationInfo = manager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        || (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isSystemApp;
    }

    public static boolean isInstalled(PackageManager packageManager, String packageName) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo != null;
    }

    public static boolean openable(PackageManager packageManager, String packageName) {
        return packageManager.getLaunchIntentForPackage(packageName) != null;
    }

    public static int getAppUid(PackageManager packageManager, String packageName) {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long[] getTimeRange(SortEnum sort) {
        long[] range;
        switch (sort) {
            case TODAY:
                range = getTodayRange();
                break;
            case YESTERDAY:
                range = getYesterday();
                break;
            case THIS_WEEK:
                range = getThisWeek();
                break;
            case THIS_MONTH:
                range = getThisMonth();
                break;
            case THIS_YEAR:
                range = getThisYear();
                break;
            default:
                range = getTodayRange();
        }
        Log.d("**********", range[0] + " ~ " + range[1]);
        return range;
    }


    private static long[] getTodayRange() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new long[]{cal.getTimeInMillis(), timeNow};
    }

    public static long getYesterdayTimestamp() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeNow - A_DAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long[] getYesterday() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeNow - A_DAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = start + A_DAY > timeNow ? timeNow : start + A_DAY;
        return new long[]{start, end};
    }

    private static long[] getThisWeek() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = start + A_DAY > timeNow ? timeNow : start + A_DAY;
        return new long[]{start, end};
    }

    private static long[] getThisMonth() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new long[]{cal.getTimeInMillis(), timeNow};
    }

    private static long[] getThisYear() {
        long timeNow = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new long[]{cal.getTimeInMillis(), timeNow};
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

