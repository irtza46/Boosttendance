package com.ilumastech.smart_attendance_system.login_registration_activities.registration_activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ilumastech.smart_attendance_system.Database;
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

        final String accType = (type.getCheckedRadioButtonId() == R.id.student) ?
                "student" : "teacher";
        final String fullName = fullName_tf.getText().toString();
        final String number = countryCodePicker.getFullNumberWithPlus();
        final String email = email_tf.getText().toString();
        final String password = password_tf.getText().toString();
        final String repassword = rPassword_tf.getText().toString();

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

        prompt.showProgress("Sign Up", "Registering...");

        // check if user don't exist with this number
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Objects.equals(snapshot.child("phoneNumber").getValue(), number)) {
                        found = true;
                        break;
                    }
                }

                if (prompt != null)
                    prompt.hideProgress();
                if (found) {
                    number_tf.setError("Mobile number is already registered with an email account.\n" +
                            "Please register using a different account mobile number.");
                    prompt.showFailureMessagePrompt(
                            "Mobile number is already registered with an email account.\n" +
                                    "Please register using a different account mobile number.");
                } else
                    createAccount(email, password, accType, fullName, number);

                Log.d(TAG, "Number Found: " + number + " Realtime " + found);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void createAccount(final String email, String password, final String accType, final String fullName,
                               final String number) {

        Log.d(TAG, "createAccount:" + email);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {

                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName).build());
                                user.sendEmailVerification();

                                // saving user info to database without number
                                Database.createUser(user.getUid(),
                                        accType, fullName, email, number);
                            }
                            startActivityForResult(new Intent(RegisterActivity.this,
                                    MobileVerificationActivity.class)
                                    .putExtra("number", number), 1);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            prompt.hideProgress();
                            prompt.showFailureMessagePrompt("Account not created.\n" +
                                    Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

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
        }
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
