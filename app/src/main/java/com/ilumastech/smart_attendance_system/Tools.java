package com.ilumastech.smart_attendance_system;

import android.os.Handler;

public class Tools {

    public static void wait(int seconds, Runnable runnable) {

        (new Handler()).postDelayed(runnable, seconds * 1000);
    }

}
