package com.ilumastech.smart_attendance_system;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText fullName_tf, number_tf, email_tf, password_tf, rPassword_tf;
    private RadioButton student, teacher;

    private FirebaseAuth firebaseAuth;
    private Progress progress = new Progress(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        student = findViewById(R.id.student);
        teacher = findViewById(R.id.teacher);
        fullName_tf = findViewById(R.id.fullname_tf);
        number_tf = findViewById(R.id.number_tf);
        email_tf = findViewById(R.id.email_tf);
        password_tf = findViewById(R.id.password_tf);
        rPassword_tf = findViewById(R.id.reenter_password_tf);

        student.setSelected(true);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        student.setSelected(true);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        student.setSelected(true);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            email_tf.setError("Required.");
            valid = false;
        } else
            email_tf.setError(null);

        if (TextUtils.isEmpty(password)) {
            password_tf.setError("Required.");
            valid = false;
        } else
            password_tf.setError(null);

        return valid;
    }

    public void registerUser(View view) {

        String email = email_tf.getText().toString();
        String password = password_tf.getText().toString();
        if (!validateForm(email, password)) {
            return;
        }

        Log.d(TAG, "createAccount:" + email);

        progress.showProgressDialog("Registering...");

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            progress.showProgressDialog("Signing in...");

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            updateUI(user);
                        } else {

                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        progress.hideProgressDialog();
        if (user != null)
            startActivity(new Intent(this, MainActivity.class).putExtra("user", user));
    }


}
