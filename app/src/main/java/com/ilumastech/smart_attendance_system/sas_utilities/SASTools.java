package com.ilumastech.smart_attendance_system.sas_utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

public class SASTools {

    public static void wait(int seconds, Runnable runnable) {
        (new Handler()).postDelayed(runnable, seconds * 1000);
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
