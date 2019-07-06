package com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities;

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
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ilumastech.smart_attendance_system.Prompt;
import com.ilumastech.smart_attendance_system.R;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    private static final String TAG = "MobileVerifActivity";

    private EditText code_tf;
    private String number;

    private FirebaseAuth firebaseAuth;
    private Prompt prompt;

    private String resendVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        code_tf = findViewById(R.id.code_tf);

        prompt = new Prompt(this);
        firebaseAuth = FirebaseAuth.getInstance();

        number = getIntent().getStringExtra("number");

        Log.d(TAG, "loginAccount:" + number);

        // send verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,             // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,        // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        prompt.showSuccessMessagePrompt("Code has been sent through SMS");
    }

    public void authenticate(View view) {

        // validating if code has been input in the required data fields
        if (TextUtils.isEmpty(code_tf.getText())) {
            code_tf.setError("Enter code received through SMS");
            return;
        }

        String code = code_tf.getText().toString();

        Log.d(TAG, "verifyNumberWithCode:" + code);

        signInWithPhoneAuth(PhoneAuthProvider.getCredential(resendVerificationId, code));
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    Log.d(TAG, "onVerificationCompleted:" + credential);

                    signInWithPhoneAuth(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        prompt.showFailureMessagePrompt("Code is invalid.\n" +
                                "Please try resending code");
                    } else if (e instanceof FirebaseTooManyRequestsException)
                        prompt.showFailureMessagePrompt("SMS Quota has exceeded");
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    resendVerificationId = verificationId;
                    resendToken = token;
                }
            };

    public void resendCode(View view) {

        Log.d(TAG, "resendCodeNumber:" + number);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,             // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,        // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                resendToken);       // ForceResendingToken from callbacks

        prompt.showSuccessMessagePrompt("Code has been sent again through SMS");
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            user.updatePhoneNumber(credential).addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInMobileLink:success");

                                prompt.showSuccessMessagePrompt("Number verified and linked!");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        prompt.hidePrompt();
                                        MobileVerificationActivity.this.finish();
                                    }
                                }, 3000);
                            } else {
                                Log.w(TAG, "signInMobileLink:failure", task.getException());

                                if (task.getException() instanceof
                                        FirebaseAuthInvalidCredentialsException) {

                                    code_tf.setError("Code is invalid.\n" +
                                            "PLease resend and retry code.");
                                    prompt.showFailureMessagePrompt("Code is invalid.\n" +
                                            "PLease resend and retry code.");
                                } else
                                    prompt.showFailureMessagePrompt(
                                            "Verification not successful\n" +
                                                    Objects.requireNonNull(task.getException())
                                                            .getMessage());
                            }
                        }
                    });
        }
    }

}
