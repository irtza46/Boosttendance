package com.ilumastech.smart_attendance_system.login_registration_activities.login_activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;
import com.ilumastech.smart_attendance_system.main_activities.MainActivity;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobileLoginActivity extends AppCompatActivity {

    private static final String TAG = "MobileLoginActivity";

    private EditText number_tf, code_tf;
    private LinearLayout c_resend;
    private CountryCodePicker countryCodePicker;

    private Prompt prompt;

    // for resending code and listening to verification callbacks
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken token;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);
        init();
    }

    private void init() {
        number_tf = findViewById(R.id.number_tf);
        code_tf = findViewById(R.id.code_tf);
        c_resend = findViewById(R.id.c_resend);
        countryCodePicker = findViewById(R.id.country_picker);
        countryCodePicker.registerCarrierNumberEditText(number_tf);

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);

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
                MobileLoginActivity.this.verificationId = verificationId;
                MobileLoginActivity.this.token = token;

                // displaying code text view to enter code received and verify
                c_resend.setVisibility(View.VISIBLE);
            }
        };
    }

    public void registerScreen(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void authenticate(View view) {

        // if code has been sent and code text view has been displayed
        if (c_resend.getVisibility() == View.VISIBLE) {

            // validating if code has been input in the required data fields
            if (TextUtils.isEmpty(code_tf.getText())) {
                code_tf.setError("Enter code received through SMS");
                return;
            }

            // getting entered code
            String code = code_tf.getText().toString();
            Log.d(TAG, "loginAccountWithCode:" + code);

            // verifying the providing code and login
            signInWithPhoneAuth(PhoneAuthProvider.getCredential(verificationId, code));
        }

        // if code has not been sent and code text view has is invisible
        else {

            // getting entered mobile number
            final String number = countryCodePicker.getFullNumberWithPlus();

            // validating entered mobile number
            if (!validateNumber(number))
                return;

            // showing prompt to user about progress
            prompt.showProgress("Authenticating", "Please wait...");

            // check if user don't exist with this number
            FirebaseDatabase.getUserByMobileNumber(number).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    prompt.hideProgress();

                    // if user with this mobile number already not registered
                    if (!dataSnapshot.exists()) {
                        number_tf.setError("Mobile number is not registered with an email account.\n" +
                                "Please register a new account with this mobile number or\n" +
                                "enter already registered mobile number.");

                        // prompt user about mobile not registered.
                        prompt.showFailureMessagePrompt(
                                "Mobile number is not registered with an email account.\n" +
                                        "Please register a new account with this mobile number or\n" +
                                        "enter already registered mobile number.");
                    }

                    // if given phone number is registered with an account
                    else {

                        Log.d(TAG, "loginAccount:" + number);

                        // sending verification code
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, SASConstants.MOBILE_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, MobileLoginActivity.this, verificationCallbacks);

                        // prompt user about code has been sent
                        prompt.showSuccessMessagePrompt("Code has been sent through SMS");
                    }

                    // show short wait prompt
                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                        @Override
                        public void run() {
                            prompt.hidePrompt();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    public void resendCode(View view) {

        // getting entered mobile number
        String number = countryCodePicker.getFullNumberWithPlus();

        // validating entered mobile number
        if (!validateNumber(number))
            return;

        Log.d(TAG, "resendCodeNumber:" + number);

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

    private boolean validateNumber(String number) {

        // if nothing is entered in number field
        if (TextUtils.isEmpty(number)) {
            number_tf.setError("Number is required.");
            return false;
        }

        // if number entered is not valid according to the format of country selected
        if (!countryCodePicker.isValidFullNumber()) {
            number_tf.setError("Mobile number is invalid.\nPlease re-enter Mobile number.");

            // show short wait prompt to user about mobile number is not valid
            prompt.showFailureMessagePrompt("Mobile number is invalid.\nPlease re-enter Mobile number.");
            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            });
            return false;
        }

        return true;
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {

        // prompt user for login in
        prompt.showProgress("Sign In", "Login in...");

        // authenticating phone number credential
        FirebaseDatabase.getFirebaseAuthInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        prompt.hideProgress();

                        // if login was successful
                        if (task.isSuccessful()) {

                            Log.d(TAG, "loginWithNumber:success");

                            // prompt show for login successful
                            prompt.showSuccessMessagePrompt("Login successful");

                            // show short wait prompt before starting main activity
                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();

                                            // starting main activity
                                            startActivity(new Intent(MobileLoginActivity.this, MainActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                            // finishing this email login activity
                                            MobileLoginActivity.this.finish();
                                        }
                                    }
                            );
                        }

                        // if login was not successful
                        else {

                            Log.w(TAG, "loginWithNumber:failure", task.getException());

                            // if code entered was invalid
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                code_tf.setError("Code is invalid.\nPLease resend and retry code.");
                                prompt.showFailureMessagePrompt("Code is invalid.\nPLease resend and retry code.");
                            }

                            // prompt user about login failure and provide the reason
                            else
                                prompt.showFailureMessagePrompt("Login not successful\n" + Objects.requireNonNull(task.getException()).getMessage());

                            // show prompt for 3 seconds
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}