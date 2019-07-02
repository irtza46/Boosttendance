package com.ilumastech.smart_attendance_system;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class MobileLoginActivity extends AppCompatActivity {

    EditText mobile_tf, pass_tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        mobile_tf = findViewById(R.id.mobile_tf);
        pass_tf = findViewById(R.id.password_tf);
    }

    public void authenticate(View view) {

        String mobile = mobile_tf.getText().toString();


    }

    public void registerScreen(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

}