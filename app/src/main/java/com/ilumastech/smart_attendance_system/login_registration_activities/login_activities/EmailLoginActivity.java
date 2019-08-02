package com.ilumastech.smart_attendance_system.login_registration_activities.login_activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseController;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;
import com.ilumastech.smart_attendance_system.main_activities.MainActivity;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;

import java.util.Objects;

public class EmailLoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailLoginActivity";

    private EditText email_tf, password_tf;

    private Prompt prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        init();
    }

    private void init() {
        email_tf = findViewById(R.id.email_tf);
        password_tf = findViewById(R.id.password_tf);
    }

    public void registerScreen(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void authenticate(View view) {

        // getting entered email and password
        String email = email_tf.getText().toString();
        String password = password_tf.getText().toString();

        // validating if email and password has been entered in the required data fields
        if (!validateInputs(email, password))
            return;

        Log.d(TAG, "loginAccount:" + email);

        // creating instance of prompt for showing prompts to user
        final Prompt prompt = new Prompt(this);

        // prompt user for login in
        prompt.showProgress("Sign In", "Login in...");

        // authenticating email and password
        FirebaseController.getAuthInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        prompt.hideProgress();

                        // if login was successful
                        if (task.isSuccessful()) {
                            Log.d(TAG, "loginWithEmail:success");

                            // show short wait prompt for login successful
                            prompt.showSuccessMessagePrompt("Login successful");
                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();

                                            // starting main activity
                                            startActivity(new Intent(EmailLoginActivity.this, MainActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                            // finishing this email login activity
                                            EmailLoginActivity.this.finish();
                                        }
                                    }
                            );
                        }

                        // if login was not successful
                        else {
                            Log.w(TAG, "loginWithEmail:failure", task.getException());

                            // show long wait prompt to user about login failure and provide the reason
                            prompt.showFailureMessagePrompt("Login not successful\n" + Objects.requireNonNull(task.getException()).getMessage());
                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                                @Override
                                public void run() {
                                    prompt.hidePrompt();
                                }
                            });
                        }
                    }
                });
    }

    private boolean validateInputs(String email, String password) {

        // if nothing is entered in email field
        if (email.isEmpty()) {
            email_tf.setError("Email is required.");
            return false;
        }

        // if nothing is entered in password field
        if (password.isEmpty()) {
            password_tf.setError("Password is required.");
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}
