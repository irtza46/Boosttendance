package com.ilumastech.smart_attendance_system;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class Prompt {

    private AlertDialog alertDialog;
    private Activity activity;
    private AlertDialog.Builder builder;
    private View view;

    private ImageView prompt_icon;
    private TextView prompt_heading, prompt_message;
    private ProgressBar prompt_progress;
    private EditText prompt_input;
    private LinearLayout prompt_buttons;
    private Button prompt_ok_btn, prompt_cancel_btn;

    private String input;

    public Prompt(Activity activity) {
        this.activity = activity;
    }

    private void initView() {
        builder = new AlertDialog.Builder(activity);
        view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.prompt,
                (ViewGroup) activity.findViewById(android.R.id.content), false);

        prompt_icon = view.findViewById(R.id.prompt_icon);
        prompt_heading = view.findViewById(R.id.prompt_heading);
        prompt_message = view.findViewById(R.id.prompt_message);
        prompt_progress = view.findViewById(R.id.prompt_progress);
        prompt_input = view.findViewById(R.id.prompt_input);
        prompt_buttons = view.findViewById(R.id.prompt_buttons);
        prompt_ok_btn = view.findViewById(R.id.prompt_ok);
        prompt_cancel_btn = view.findViewById(R.id.prompt_cancel);

        prompt_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = prompt_input.getText().toString();
            }
        });
        prompt_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputPrompt();
            }
        });
    }

    private void showPrompt() {
        builder.setView(view);

        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void hidePrompt() {
        ((ViewGroup) activity.findViewById(android.R.id.content)).removeView(view);
        alertDialog.dismiss();
    }

    public void hideProgress() {
        prompt_progress.setVisibility(View.GONE);
        hidePrompt();
    }

    public void hideInputPrompt() {
        prompt_input.setVisibility(View.GONE);
        prompt_buttons.setVisibility(View.GONE);
        hidePrompt();
    }

    private void setValues(int icon, String heading, int progress_visible, String msg,
                           int input_visible, int buttons_visible) {
        initView();

        prompt_icon.setBackground(ContextCompat.getDrawable(activity.getApplicationContext(),
                icon));

        prompt_heading.setText(heading);

        prompt_progress.setVisibility(progress_visible);

        prompt_message.setText(msg);

        prompt_input.setVisibility(input_visible);

        prompt_buttons.setVisibility(buttons_visible);

        showPrompt();
    }

    public void showProgress(String heading, String msg) {
        setValues(R.drawable.logo, heading, View.VISIBLE, msg, View.GONE, View.GONE);
    }

    public void showSuccessMessagePrompt(String msg) {
        setValues(R.drawable.ic_success, "Success", View.GONE, msg, View.GONE, View.GONE);
    }

    public void showFailureMessagePrompt(String msg) {
        setValues(R.drawable.ic_failure, "Failure", View.GONE, msg, View.GONE, View.GONE);
    }

    public void showInputMessagePrompt(String heading, String msg, int input_type) {
        prompt_input.setInputType(input_type);
        setValues(R.drawable.logo, heading, View.GONE, msg, View.VISIBLE, View.VISIBLE);
    }

    public String getInput() {
        return input;
    }

}
