package com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseController;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText fullName_tf, number_tf, email_tf, password_tf, rPassword_tf;
    private CountryCodePicker countryCodePicker;

    private Prompt prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        fullName_tf = findViewById(R.id.fullname_tf);
        number_tf = findViewById(R.id.number_tf);
        email_tf = findViewById(R.id.email_tf);
        password_tf = findViewById(R.id.password_tf);
        rPassword_tf = findViewById(R.id.reenter_password_tf);
        countryCodePicker = findViewById(R.id.country_picker);
        countryCodePicker.registerCarrierNumberEditText(number_tf);

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);

        // checking if internet is working or not
        if (!SASTools.isInternetConnected(getApplicationContext())) {

            // show short wait about internet not connected
            prompt.showFailureMessagePrompt("Not connected to internet.\nPlease connect to internet.");
            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            });
        }
    }

    public void registerUser(View view) {

        // checking if internet is working or not
        if (!SASTools.isInternetConnected(getApplicationContext())) {

            // show short wait about internet not connected
            prompt.showFailureMessagePrompt("Not connected to internet.\nPlease connect to internet.");
            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            });
            return;
        }

        // getting entered user details
        final String fullName = fullName_tf.getText().toString();
        final String number = countryCodePicker.getFullNumberWithPlus();
        final String email = email_tf.getText().toString().toLowerCase();
        final String password = password_tf.getText().toString();
        final String repassword = rPassword_tf.getText().toString();

        // validating if all the required data is entered
        if (!validateForm(fullName, number, email, password, repassword))
            return;

        // check if user don't exist with this number
        FirebaseController.getUserByMobileNumber(number).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // if given phone number is registered with an account
                if (dataSnapshot.exists()) {
                    number_tf.setError("Mobile number is already registered with an email account.\n" +
                            "Please register using a different account mobile number.");

                    // prompt user about mobile not registered.
                    prompt.showFailureMessagePrompt(
                            "Mobile number is already registered with an email account.\n" +
                                    "Please register using a different account mobile number.");
                }

                // if user with this mobile number already not registered
                else
                    createAccount(email, password, fullName, number);

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

    private void createAccount(final String email, String password, final String fullName, final String number) {

        Log.d(TAG, "createAccount:" + email);

        // prompt user for registering account
        prompt.showProgress("Sign Up", "Registering...");

        // creating account using email and password
        FirebaseController.getAuthInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        prompt.hideProgress();

                        // if user account is created
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            // getting user account
                            FirebaseUser firebaseUser = FirebaseController.getUser();

                            // setting user account display name
                            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullName).build());

                            // sending verification email to user
                            firebaseUser.sendEmailVerification();

                            // registering user in database with unverified number
                            FirebaseController.createUser(firebaseUser.getUid(), fullName, email, "!" + number);

                            // show short wait prompt to user about account creation
                            prompt.showSuccessMessagePrompt("Account created.");

                            // starting number verification activity
                            startActivity(new Intent(RegisterActivity.this, MobileVerificationActivity.class).putExtra("number", number)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }

                        // if user account not created
                        else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            // prompt user about account creation failure and provide reason
                            prompt.showFailureMessagePrompt("Account not created.\n" + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private boolean validateForm(String fullName, String number, String email, String password, String repassword) {

        // if user has not entered mobile number
        if (number.isEmpty()) {
            number_tf.setError("Mobile number is required.");
            return false;
        }

        // if user has not entered full name
        if (fullName.isEmpty()) {
            fullName_tf.setError("Full name is required.");
            return false;
        }

        // if user has not entered email
        if (email.isEmpty()) {
            email_tf.setError("Email is required.");
            return false;
        }

        // if user has not entered password
        if (password.isEmpty()) {
            password_tf.setError("Password is required.");
            return false;
        }

        // if user has not re-entered password
        if (repassword.isEmpty()) {
            rPassword_tf.setError("Re-enter password is required.");
            return false;
        }

        // validating number
        if (!countryCodePicker.isValidFullNumber()) {
            number_tf.setError("Mobile number is not valid.\nPlease re-enter Mobile number.");

            // show short wait prompt to user about number invalid
            prompt.showFailureMessagePrompt("Mobile number is not valid.\nPlease re-enter Mobile number.");
            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            });
            return false;
        }

        // verifying password
        if (!password.equals(repassword)) {
            password_tf.setError("Password doesn't match.\nPlease re-enter password.");
            rPassword_tf.setError("Password doesn't match.\nPlease re-enter password.");

            // show short wait prompt to user about passwords not match
            prompt.showFailureMessagePrompt("Password doesn't match.\nPlease re-enter password.");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}
