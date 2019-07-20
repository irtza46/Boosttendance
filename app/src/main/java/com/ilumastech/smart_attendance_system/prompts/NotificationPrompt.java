package com.ilumastech.smart_attendance_system.prompts;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ilumastech.smart_attendance_system.R;

import java.util.Objects;

public class NotificationPrompt {

    private AlertDialog alertDialog;
    private Activity activity;
    private AlertDialog.Builder builder;
    private View view;

    private TextView heading;
    private EditText msg;
    private Button prompt_ok_btn;

    public NotificationPrompt(Activity activity, int type) {
        this.activity = activity;
        initView();
        if (type == 1)
            this.heading.setText("Application");
        showPrompt();
    }

    private void initView() {
        builder = new AlertDialog.Builder(activity);
        view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.notification_prompt,
                (ViewGroup) activity.findViewById(android.R.id.content), false);

        heading = view.findViewById(R.id.prompt_heading);
        msg = view.findViewById(R.id.msg);
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

    public EditText getMsg() {
        return msg;
    }

    public void setSendButtonListener(View.OnClickListener clickListener) {
        prompt_ok_btn.setOnClickListener(clickListener);
    }
}
