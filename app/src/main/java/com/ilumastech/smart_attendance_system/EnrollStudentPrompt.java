package com.ilumastech.smart_attendance_system;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

public class EnrollStudentPrompt {

    private AlertDialog alertDialog;
    private Activity activity;
    private AlertDialog.Builder builder;
    private View view;

    private EditText student_id;
    private EditText student_email;
    private Button prompt_ok_btn;

    public EnrollStudentPrompt(Activity activity) {
        this.activity = activity;
        initView();
        showPrompt();
    }

    private void initView() {
        builder = new AlertDialog.Builder(activity);
        view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.enroll_student_prompt,
                (ViewGroup) activity.findViewById(android.R.id.content), false);

        student_id = view.findViewById(R.id.student_id);
        student_email = view.findViewById(R.id.student_email);
        prompt_ok_btn = view.findViewById(R.id.prompt_ok);
        Button prompt_cancel_btn = view.findViewById(R.id.prompt_cancel);
        prompt_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePrompt();
            }
        });
    }

    private void showPrompt() {

        // setting builder view and creating alert dialog
        builder.setView(view);
        alertDialog = builder.create();

        // setting alert dialog properties
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        // displaying alert dialog
        alertDialog.show();
    }

    public void hidePrompt() {
        ((ViewGroup) activity.findViewById(android.R.id.content)).removeView(view);
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    public EditText getStudent_id() {
        return student_id;
    }

    public EditText getStudent_email() {
        return student_email;
    }

    public void setEnrollButtonListener(View.OnClickListener clickListener) {
        prompt_ok_btn.setOnClickListener(clickListener);
    }
}
