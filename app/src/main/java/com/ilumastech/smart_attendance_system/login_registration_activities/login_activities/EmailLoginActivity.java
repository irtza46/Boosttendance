package com.ilumastech.smart_attendance_system.login_registration_activities.login_activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ilumastech.smart_attendance_system.MainActivity;
import com.ilumastech.smart_attendance_system.Prompt;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;

import java.util.Objects;

public class EmailLoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailLoginActivity";

    private EditText email_tf, password_tf;

    private FirebaseAuth firebaseAuth;
    private Prompt prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        email_tf = findViewById(R.id.email_tf);
        password_tf = findViewById(R.id.password_tf);

        firebaseAuth = FirebaseAuth.getInstance();
        prompt = new Prompt(this);
    }

    private boolean validateForm(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            email_tf.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            password_tf.setError("Required");
            return false;
        }

        return true;
    }

    public void authenticate(View view) {

        String email = email_tf.getText().toString();
        String password = password_tf.getText().toString();

        // validating if data has been input in the required data fields
        if (!validateForm(email, password))
            return;

        Log.d(TAG, "loginAccount:" + email);

        prompt.showProgress("Sign In", "Login in...");

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "loginWithEmail:success");

                            prompt.hideProgress();
                            prompt.showSuccessMessagePrompt("Login successful");
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    prompt.hidePrompt();
                                    if (firebaseAuth.getCurrentUser() != null) {

                                        startActivity(new Intent(EmailLoginActivity.this,
                                                MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                        EmailLoginActivity.this.finish();
                                    }
                                }
                            }, 2000);
                        } else {
                            Log.w(TAG, "loginWithEmail:failure", task.getException());

                            prompt.hideProgress();
                            prompt.showFailureMessagePrompt("Login not successful\n" +
                                    Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    public void registerScreen(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
