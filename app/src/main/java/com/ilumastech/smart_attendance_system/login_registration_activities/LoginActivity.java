package com.ilumastech.smart_attendance_system.login_registration_activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseController;
import com.ilumastech.smart_attendance_system.login_registration_activities.login_activities.EmailLoginActivity;
import com.ilumastech.smart_attendance_system.login_registration_activities.login_activities.MobileLoginActivity;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.MobileVerificationActivity;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;
import com.ilumastech.smart_attendance_system.main_activities.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkUser();
    }

    public void registerScreen(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void goToEmailLogin(View view) {
        startActivity(new Intent(this, EmailLoginActivity.class));
    }

    public void goToMobileLogin(View view) {
        startActivity(new Intent(this, MobileLoginActivity.class));
    }

    public void checkUser() {

        // if user has already logged in
        FirebaseUser user = FirebaseController.getUser();
        if (user != null) {

            // verifying number
            FirebaseController.getUserByU_ID(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // fetching the number
                    String number = String.valueOf(dataSnapshot.child(FirebaseController.PHONE_NUMBER).getValue());

                    // if number not verified yet
                    if (number.charAt(0) == '!') {

                        // fetching the number to verify
                        number = number.substring(1);
                        Log.d(TAG, "Re-verifying phone number: " + number);

                        // starting number verification activity
                        startActivity(new Intent(LoginActivity.this, MobileVerificationActivity.class)
                                .putExtra("number", number));
                    }

                    // if number is verified
                    else {

                        // starting main activity and finishing all the previous activities
                        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        // finishing login activity
                        LoginActivity.this.finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
