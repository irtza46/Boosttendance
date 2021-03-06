package com.ilumastech.smart_attendance_system.startup_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // setting logo animation
        ImageView logo = findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setStartOffset(1000);
        logo.startAnimation(animation);

        // short wait before starting login screen
        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
            @Override
            public void run() {

                // starting login activity
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // finishing splash screen
                SplashScreen.this.finish();
            }
        });
    }
}
