package com.ilumastech.smart_attendance_system;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;

public class SplashScreen extends AppCompatActivity {

    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setStartOffset(1000);
        logo.startAnimation(animation);

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 2000);
    }
}
