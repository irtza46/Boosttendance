package com.ilumastech.smart_attendance_system.main_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.general_activities.AboutActivity;
import com.ilumastech.smart_attendance_system.general_activities.ProfileActivity;
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;
import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;
import com.ilumastech.smart_attendance_system.main_activities.adapter.ClassListAdapter;
import com.ilumastech.smart_attendance_system.notification_activities.NotificationActivity;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.student_activities.MarkAttendanceActivity;
import com.ilumastech.smart_attendance_system.teacher_activities.ClassDetailsActivity;
import com.ilumastech.smart_attendance_system.teacher_activities.CreateClassActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ClassListAdapter joinedClassListAdapter, createdClassListAdapter;
    private ListView listView;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    private Prompt prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        // setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // setting navigation menu icon
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        // creating navigation menu item select listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                navigationMenu(menuItem.getItemId());
                return true;
            }
        });

        // setting navigation header properties
        View navigationHeader = navigationView.getHeaderView(0);
        ((TextView) navigationHeader.findViewById(R.id.nav_username)).setText(FirebaseDatabase.getUser().getDisplayName());
        ((TextView) navigationHeader.findViewById(R.id.nav_email)).setText(FirebaseDatabase.getUser().getEmail());

        // initializing the classroom list views
        listView = findViewById(R.id.list_view);
        listView.addFooterView(new View(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewClass(parent, position);
            }
        });

        // created joined and created classes arrays adapter
        joinedClassListAdapter = new ClassListAdapter(this, R.layout.class_card);
        createdClassListAdapter = new ClassListAdapter(this, R.layout.class_card);
        FirebaseDatabase.getJoinedClasses(joinedClassListAdapter);
        FirebaseDatabase.getCreatedClasses(createdClassListAdapter);

        // displaying joined classes by default
        listView.setAdapter(joinedClassListAdapter);

        // bottom navigation view properties
        bottomNavigationView = findViewById(R.id.bottom_nav_menu);
        bottomNavigationView.setSelectedItemId(R.id.joined_classes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                // if joined classes list is selected
                if (menuItem.getItemId() == R.id.joined_classes) {
                    listView.setAdapter(joinedClassListAdapter);
                    return true;
                }

                // if created classes list is selected
                if (menuItem.getItemId() == R.id.created_classes) {
                    listView.setAdapter(createdClassListAdapter);
                    return true;
                }
                return false;
            }
        });

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);
    }

    public void createClass(View view) {
        startActivity(new Intent(this, CreateClassActivity.class));
    }

    private void viewClass(AdapterView<?> parent, int position) {

        // getting classroom details
        ClassRoom classRoom = (ClassRoom) parent.getItemAtPosition(position);

        // if it's a joined class
        if (bottomNavigationView.getSelectedItemId() == R.id.joined_classes)
            startActivity(new Intent(MainActivity.this, MarkAttendanceActivity.class)
                    .putExtra("classRoom", classRoom));

            // if it's a created class
        else
            startActivity(new Intent(MainActivity.this, ClassDetailsActivity.class)
                    .putExtra("classRoom", classRoom));
    }

    private void navigationMenu(int itemId) {

        // if user choose to view profile
        if (itemId == R.id.profile)
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        // if user choose to view notifications
        if (itemId == R.id.notifications)
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));

        // if user choose to logout
        else if (itemId == R.id.logout) {

            // show prompt to user to choose if user wants to logout or not
            prompt.showInputMessagePrompt("Logout", "Are you sure you want to logout?", "Yes", "No");
            prompt.setOkButtonListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    prompt.hideInputPrompt();

                    // sign out current user
                    FirebaseDatabase.getFirebaseAuthInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    // finishing main activity
                    MainActivity.this.finish();
                }
            });
        }

        // if user choose to share our application
        else if (itemId == R.id.share) {

            // calling the sharing intent
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Boosttendance (Smart Attendance System)");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Install it from here: ");
            startActivity(Intent.createChooser(sharingIntent, "Share Via"));
        }

        // if user chooses to view about details
        else if (itemId == R.id.about)
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    @Override
    public void onBackPressed() {

        // if navigation menu is open then close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

            // if navigation menu is open then close it
        else {

            // show prompt to user to choose if user wants to exit application
            prompt.showInputMessagePrompt("Quit", "Are you sure you quit?", "Yes", "No");
            prompt.setOkButtonListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    prompt.hideInputPrompt();

                    // finishing main activity
                    MainActivity.this.finish();
                }
            });
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}