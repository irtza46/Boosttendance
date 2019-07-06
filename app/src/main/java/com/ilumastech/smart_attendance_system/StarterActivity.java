package com.ilumastech.smart_attendance_system;

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

        Class<?> activityClass;

        try {
            SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
            activityClass = Class.forName(Objects.requireNonNull(
                    prefs.getString("lastActivity", LoginActivity.class.getName())));
        } catch(ClassNotFoundException ex) {
            activityClass = LoginActivity.class;
        }

        startActivity(new Intent(this, activityClass));
    }
}
