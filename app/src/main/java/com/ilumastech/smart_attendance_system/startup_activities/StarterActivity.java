package com.ilumastech.smart_attendance_system.startup_activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;

import java.util.Objects;

public class StarterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Class<?> activityClass = LoginActivity.class;
        try {

            SharedPreferences sharedPreferences = getSharedPreferences("TEMP", MODE_PRIVATE);

            // if email is already verified
            if (!sharedPreferences.getBoolean("emailVerified", true))
                activityClass = Class.forName(Objects.requireNonNull(sharedPreferences.getString("activityName", LoginActivity.class.getName())));
        } catch (ClassNotFoundException ignored) {}

        startActivity(new Intent(this, activityClass));
    }
}
