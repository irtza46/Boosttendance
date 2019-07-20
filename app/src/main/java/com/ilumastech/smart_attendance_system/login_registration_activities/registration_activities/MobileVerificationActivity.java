package com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.main_activities.MainActivity;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    private static final String TAG = "MobileVerifActivity";

    private EditText code_tf;
    private String number;

    private Prompt prompt;

    // for resending code and listening to verification callbacks
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);
        init();
    }

    private void init() {
        code_tf = findViewById(R.id.code_tf);

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);

        // getting phone number from intent
        number = getIntent().getStringExtra("number");

        // setting verification call backs
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);

                // login with mobile credential
                signInWithPhoneAuth(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                // if code entered was invalid
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    code_tf.setError("Code is invalid.\nPLease resend and retry code.");
                    prompt.showFailureMessagePrompt("Code is invalid.\nPLease resend and retry code.");
                }

                // prompt user about login failure and provide the reason
                else
                    prompt.showFailureMessagePrompt("Login not successful\n" + e.getMessage());

                // show long wait prompt
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // saving verification id and token if required for resending the code
                MobileVerificationActivity.this.verificationId = verificationId;
                MobileVerificationActivity.this.token = token;
            }
        };

        Log.d(TAG, "loginAccount:" + number);

        // send verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, SASConstants.MOBILE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, this, verificationCallbacks);

        // show short wait prompt user about code has been sent
        prompt.showSuccessMessagePrompt("Code has been sent through SMS");
        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
            @Override
            public void run() {
                prompt.hidePrompt();
            }
        });
    }

    public void authenticate(View view) {

        // getting entered code
        String code = code_tf.getText().toString();

        // validating entered code
        if (!validate(code))
            return;

        Log.d(TAG, "verifyNumberWithCode: " + code);

        // verifying the providing code and login
        signInWithPhoneAuth(PhoneAuthProvider.getCredential(verificationId, code));
    }

    public void resendCode(View view) {
        Log.d(TAG, "resendCodeNumber: " + number);

        // resend verification code to the provided number and listen to the callbacks
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, SASConstants.MOBILE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, this, verificationCallbacks, token);

        // show short wait prompt to user about code has been sent again
        prompt.showSuccessMessagePrompt("Code has been sent again through SMS");
        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
            @Override
            public void run() {
                prompt.hidePrompt();
            }
        });
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {

        // linking phone number credential with user account
        FirebaseUser firebaseUser = FirebaseDatabase.getUser();
        if (firebaseUser != null) {
            firebaseUser.updatePhoneNumber(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            // if number verification successful
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInMobileLink:success");

                                // show short wait prompt to user about number verification
                                prompt.showSuccessMessagePrompt("Number verified and linked!");
                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                    @Override
                                    public void run() {
                                        prompt.hidePrompt();

                                        // updating phone number in data for this user
                                        FirebaseDatabase.updateUserPhoneNumber(number);

                                        // show long wait prompt to user for login in
                                        prompt.showProgress("Sign In", "Login in...");
                                        SASTools.wait(3, new Runnable() {
                                            @Override
                                            public void run() {
                                                prompt.hideProgress();

                                                // starting main activity
                                                startActivity(new Intent(MobileVerificationActivity.this, MainActivity.class)
                                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                                // finishing this activity
                                                MobileVerificationActivity.this.finish();
                                            }
                                        });
                                    }
                                });
                            }

                            // if number verification fails
                            else {
                                Log.w(TAG, "signInMobileLink:failure", task.getException());

                                // if code entered was invalid
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    code_tf.setError("Code is invalid.\nPLease resend and retry code.");

                                    // prompt user about invalid code
                                    prompt.showFailureMessagePrompt("Code is invalid.\nPLease resend and retry code.");
                                }

                                // prompt user about login failure and provide the reason
                                else
                                    prompt.showFailureMessagePrompt("Verification not successful\n" + Objects.requireNonNull(task.getException()).getMessage());

                                // show long wait prompt
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
    }

    private boolean validate(String code) {

        // validating if code has been input in the required data fields
        if (TextUtils.isEmpty(code)) {
            code_tf.setError("Enter code received through SMS");
            return false;
        }

        return true;
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//        savedInstanceState.putString("number", number);
//        savedInstanceState.putBoolean("verified", verified);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        number = savedInstanceState.getString("number");
//        verified = savedInstanceState.getBoolean("verified");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        SharedPreferences sharedPreferences = getSharedPreferences("TEMP", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("activityName", this.getClass().getName());
//        editor.apply();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}
