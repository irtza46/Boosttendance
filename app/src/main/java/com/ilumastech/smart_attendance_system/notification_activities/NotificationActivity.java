package com.ilumastech.smart_attendance_system.notification_activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.list_classes.Notification;
import com.ilumastech.smart_attendance_system.notification_activities.adapter.NotificationListAdapter;
import com.ilumastech.smart_attendance_system.prompts.NotificationMessagePrompt;
import com.ilumastech.smart_attendance_system.prompts.Prompt;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        init();
    }

    private void init() {

        // setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // initializing the classroom list views
        ListView listView = findViewById(R.id.list_view);
        listView.addFooterView(new View(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewNotification(parent, position);
            }
        });

        // created joined and created classes arrays adapter
        NotificationListAdapter notificationListAdapter = new NotificationListAdapter(this, R.layout.notification_card);
        FirebaseDatabase.getNotifications(notificationListAdapter);

        // displaying joined classes by default
        listView.setAdapter(notificationListAdapter);

        // creating prompt instance to display prompts to user
        Prompt prompt = new Prompt(this);
    }

    private void viewNotification(AdapterView<?> parent, int position) {

        // getting notification details
        Notification notification = (Notification) parent.getItemAtPosition(position);

        // if notification is from teacher
        NotificationMessagePrompt notificationMessagePrompt;
        if (notification.getId().contains("@"))
            notificationMessagePrompt = new NotificationMessagePrompt(this, 0, notification.getMsg());

        // if application is from student
        else
            notificationMessagePrompt = new NotificationMessagePrompt(this, 1, notification.getMsg());

        // showing notification message prompt
        notificationMessagePrompt.showPrompt();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
