package com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.hbb20.CountryCodePicker;
import com.ilumastech.smart_attendance_system.MainActivity;
import com.ilumastech.smart_attendance_system.Prompt;
import com.ilumastech.smart_attendance_system.R;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText fullName_tf, number_tf, email_tf, password_tf, rPassword_tf;
    private RadioGroup type;

    private FirebaseAuth firebaseAuth;
    private Prompt prompt;

    private CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        type = findViewById(R.id.type);
        fullName_tf = findViewById(R.id.fullname_tf);
        number_tf = findViewById(R.id.number_tf);
        email_tf = findViewById(R.id.email_tf);
        password_tf = findViewById(R.id.password_tf);
        rPassword_tf = findViewById(R.id.reenter_password_tf);

        prompt = new Prompt(this);
        firebaseAuth = FirebaseAuth.getInstance();

        countryCodePicker = findViewById(R.id.country_picker);
        countryCodePicker.registerCarrierNumberEditText(number_tf);
    }

    private boolean validateForm(String fullName, String number, String email, String password,
                                 String repassword) {

        if (TextUtils.isEmpty(fullName)) {
            fullName_tf.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(number)) {
            number_tf.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            email_tf.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            password_tf.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(repassword)) {
            rPassword_tf.setError("Required");
            return false;
        }

        return true;
    }

    public void registerUser(View view) {

        String accType = "" + ((RadioButton)findViewById(type.getCheckedRadioButtonId())).getText();
        final String fullName = fullName_tf.getText().toString();
        final String number = countryCodePicker.getFullNumberWithPlus();
        String email = email_tf.getText().toString();
        String password = password_tf.getText().toString();
        String repassword = rPassword_tf.getText().toString();

        // validating if data has been input in the required data fields
        if (!validateForm(fullName, number, email, password, repassword))
            return;

        // validating number
        if (!countryCodePicker.isValidFullNumber()) {
            number_tf.setError("Mobile number is not valid.\nPlease re-enter Mobile number.");
            prompt.showFailureMessagePrompt("Mobile number is not valid.\nPlease re-enter Mobile number.");
            return;
        }

        // verifying password
        if (!password.equals(repassword)) {
            password_tf.setError("Password doesn't match.\nPlease re-enter password.");
            rPassword_tf.setError("Password doesn't match.\nPlease re-enter password.");
            prompt.showFailureMessagePrompt("Password doesn't match.\nPlease re-enter password.");
            return;
        }

        Log.d(TAG, "createAccount:" + email);

        prompt.showProgress("Sign Up", "Registering...");

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            prompt.hideProgress();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {

                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName).build());
                                user.sendEmailVerification();

                                startActivity(new Intent(RegisterActivity.this,
                                        MobileVerificationActivity.class)
                                        .putExtra("number", number));
                            }

                            prompt.showSuccessMessagePrompt("Account created.");
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    prompt.hidePrompt();
                                    prompt.showProgress("Login", "Login in...");
                                    (new Handler()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            prompt.hideProgress();
                                            startActivity(new Intent(
                                                    RegisterActivity.this,
                                                    MainActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                            RegisterActivity.this.finish();
                                        }
                                    }, 3000);

                                }
                            }, 2000);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            prompt.hideProgress();
                            prompt.showFailureMessagePrompt("Account not created.\n" +
                                    Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

}
