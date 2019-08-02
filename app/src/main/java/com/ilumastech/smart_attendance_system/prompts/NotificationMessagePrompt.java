package com.ilumastech.smart_attendance_system.prompts;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ilumastech.smart_attendance_system.R;

import java.util.Objects;

public class NotificationMessagePrompt {

    private AlertDialog alertDialog;
    private Activity activity;
    private AlertDialog.Builder builder;
    private View view;

    private TextView heading;
    private TextView msg;

    public NotificationMessagePrompt(Activity activity, int type, String msg) {
        this.activity = activity;
        initView();
        if (type == 1)
            this.heading.setText("Application");
        this.msg.setText(msg);
    }

    private void initView() {
        builder = new AlertDialog.Builder(activity);
        view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.notification_message_prompt,
                (ViewGroup) activity.findViewById(android.R.id.content), false);

        heading = view.findViewById(R.id.prompt_heading);
        msg = view.findViewById(R.id.msg_show);
        Button prompt_ok_btn = view.findViewById(R.id.prompt_ok);
        prompt_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePrompt();
            }
        });
    }

    public void showPrompt() {

        // setting builder view and creating alert dialog
        builder.setView(view);
        alertDialog = builder.create();

        // setting alert dialog properties
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        // displaying alert dialog
        alertDialog.show();
    }

    public void hidePrompt() {
        ((ViewGroup) activity.findViewById(android.R.id.content)).removeView(view);
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

}
