package com.ilumastech.smart_attendance_system;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        firebaseAuth.signOut();
        FirebaseUser user = firebaseAuth.getCurrentUser();
//        if(user != null)
//            startActivity(new Intent(this, MainActivity.class).putExtra("user", user));
    }

    public void registerScreen(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void goToEmailLogin(View view) {
        startActivity(new Intent(getApplicationContext(), EmailLoginActivity.class));
    }

    public void goToMobileLogin(View view) {
        startActivity(new Intent(getApplicationContext(), MobileLoginActivity.class));
    }

}
