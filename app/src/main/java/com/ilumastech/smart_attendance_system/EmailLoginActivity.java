package com.ilumastech.smart_attendance_system;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailLoginActivity extends AppCompatActivity {

    EditText email_tf, pass_tf;
    Button login_btn, register_btn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        email_tf = findViewById(R.id.email_tf);
        pass_tf = findViewById(R.id.password_tf);
        login_btn = findViewById(R.id.login_btn);
        register_btn = findViewById(R.id.register_btn);

        mAuth = FirebaseAuth.getInstance();
    }

    public void authenticate(View view) {

        String email = email_tf.getText().toString();
        String password = pass_tf.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            updateUI(null);
                        }
                    }
                });
    }

    public void registerScreen(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {

        if(currentUser == null) {

        }
    }

}
