package com.ilumastech.smart_attendance_system.login_registration_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ilumastech.smart_attendance_system.MainActivity;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.login_registration_activities.login_activities.EmailLoginActivity;
import com.ilumastech.smart_attendance_system.login_registration_activities.login_activities.MobileLoginActivity;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {

/*
            if (CheckUser.checkIfUserExistThroughNumber(number))
                startActivity(new Intent(this, MobileVerificationActivity.class)
                        .putExtra("number", number));

*/

            startActivity(new Intent(this, MainActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            this.finish();
        }
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

}
