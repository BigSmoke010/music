package com.walid.music;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceUtils {
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void startServiceIfNotRunning(Context context, Class<?> serviceClass) {
        if (!isServiceRunning(context, serviceClass)) {
            Intent serviceIntent = new Intent(context, serviceClass);
            context.startService(serviceIntent);
            Log.d("ServiceUtils", serviceClass.getSimpleName() + " started.");
        } else {
            Log.d("ServiceUtils", serviceClass.getSimpleName() + " is already running.");
        }
    }
}
