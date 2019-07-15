package com.ilumastech.smart_attendance_system.login_registration_activities.login_activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ilumastech.smart_attendance_system.MainActivity;
import com.ilumastech.smart_attendance_system.Prompt;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities.RegisterActivity;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobileLoginActivity extends AppCompatActivity {

    private static final String TAG = "MobileLoginActivity";

    private EditText number_tf, code_tf;
    private LinearLayout c_resend;

    private FirebaseAuth firebaseAuth;
    private Prompt prompt;

    private CountryCodePicker countryCodePicker;

    private String resendVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
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
                        number_tf.setError("Code is invalid.\n" +
                                "Please try resending code");
                        prompt.showFailureMessagePrompt("Code is invalid.\n" +
                                "Please try resending code");
                    } else if (e instanceof FirebaseTooManyRequestsException)
                        prompt.showFailureMessagePrompt("SMS Quota has exceeded.");
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    resendVerificationId = verificationId;
                    resendToken = token;

                    c_resend.setVisibility(View.VISIBLE);
                }
            };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        number_tf = findViewById(R.id.number_tf);
        code_tf = findViewById(R.id.code_tf);
        c_resend = findViewById(R.id.c_resend);

        prompt = new Prompt(this);
        firebaseAuth = FirebaseAuth.getInstance();

        countryCodePicker = findViewById(R.id.country_picker);
        countryCodePicker.registerCarrierNumberEditText(number_tf);
    }

    public void authenticate(View view) {

        // if code text view is visible
        if (c_resend.getVisibility() != View.GONE) {

            // validating if code has been input in the required data fields
            if (TextUtils.isEmpty(code_tf.getText())) {
                code_tf.setError("Enter code received through SMS");
                return;
            }

            String code = code_tf.getText().toString();

            Log.d(TAG, "loginAccountWithCode:" + code);

            signInWithPhoneAuth(PhoneAuthProvider.getCredential(resendVerificationId, code));
        } else {

            // validating number
            if (!countryCodePicker.isValidFullNumber()) {
                number_tf.setError("Mobile number is not valid.\nPlease re-enter Mobile number.");
                prompt.showFailureMessagePrompt("Mobile number is not valid.\nPlease re-enter Mobile number.");
                return;
            }

            // check if user don't exist with this number
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String number = countryCodePicker.getFullNumberWithPlus();

                    boolean found = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (Objects.equals(snapshot.child("phoneNumber").getValue(), number)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        number_tf.setError("Mobile number is not registered with an email account.\n" +
                                "Please register a new account with this mobile number or\n" +
                                "enter already registered mobile number.");
                        prompt.showFailureMessagePrompt(
                                "Mobile number is not registered with an email account.\n" +
                                        "Please register a new account with this mobile number or\n" +
                                        "enter already registered mobile number.");
                        return;
                    } else {

                        Log.d(TAG, "loginAccount:" + number);

                        // send verification code
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                number,                             // Phone number to verify
                                60,                              // Timeout duration
                                TimeUnit.SECONDS,                   // Unit of timeout
                                MobileLoginActivity.this,    // Activity (for callback binding)
                                mCallbacks);                        // OnVerificationStateChangedCallbacks

                        prompt.showSuccessMessagePrompt("Code has been sent through SMS");
                    }

                    Log.d(TAG, "Number Found: " + number + " Realtime " + found);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {

        prompt.showProgress("Sign In", "Login in...");

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "loginWithNumber:success");

                            prompt.hideProgress();
                            prompt.showSuccessMessagePrompt("Login successful");
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    prompt.hidePrompt();
                                    if (firebaseAuth.getCurrentUser() != null) {

                                        startActivity(new Intent(MobileLoginActivity.this,
                                                MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                        MobileLoginActivity.this.finish();
                                    }
                                }
                            }, 2000);
                        } else {
                            Log.w(TAG, "loginWithNumber:failure", task.getException());

                            prompt.hideProgress();
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                code_tf.setError("Code is invalid.\n" +
                                        "PLease resend and retry code.");
                                prompt.showFailureMessagePrompt("Code is invalid.\n" +
                                        "PLease resend and retry code.");
                            } else
                                prompt.showFailureMessagePrompt("Login not successful\n" +
                                        Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    public void registerScreen(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void resendCode(View view) {

        String number = countryCodePicker.getFullNumberWithPlus();

        // validating number
        if (!countryCodePicker.isValidFullNumber()) {
            number_tf.setError("Mobile number is not valid.\nPlease re-enter Mobile number.");
            prompt.showFailureMessagePrompt("Mobile number is not valid.\nPlease re-enter Mobile number.");
            return;
        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prompt != null) {
            prompt.hideInputPrompt();
            prompt = null;
        }
    }
}