package com.ilumastech.smart_attendance_system.main_activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseController;
import com.ilumastech.smart_attendance_system.general_activities.AboutActivity;
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;
import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;
import com.ilumastech.smart_attendance_system.main_activities.adapter.ClassListAdapter;
import com.ilumastech.smart_attendance_system.notification_activities.NotificationActivity;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;
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
    private FloatingActionMenu floatingButton;

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

        // creating navigation menu item select listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return navigationMenu(menuItem.getItemId());
            }
        });

        // setting navigation header properties
        View navigationHeader = navigationView.getHeaderView(0);
        ((TextView) navigationHeader.findViewById(R.id.nav_username)).setText(FirebaseController.getUser().getDisplayName());
        ((TextView) navigationHeader.findViewById(R.id.nav_email)).setText(FirebaseController.getUser().getEmail());

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

        // displaying joined classes by default
        listView.setAdapter(joinedClassListAdapter);

        // bottom navigation view properties
        bottomNavigationView = findViewById(R.id.bottom_nav_menu);
        bottomNavigationView.setSelectedItemId(R.id.joined_classes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return bottomNavigationMenu(menuItem.getItemId());
            }
        });

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);

        // do not show floating button when joined classes are selected
        floatingButton = findViewById(R.id.float_button);
        floatingButton.setVisibility(View.GONE);

        // if user has not already granted GPS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            // restarting main activity
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        // if user has already granted GPS permission
        else {

            // requesting a location update
            final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            if (locationManager != null)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
        }
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

    private boolean navigationMenu(int itemId) {

        // if user choose to view notifications
        if (itemId == R.id.notifications) {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            return true;
        }

        // if user choose to logout
        else if (itemId == R.id.logout) {

            // show prompt to user to choose if user wants to logout or not
            prompt.showInputMessagePrompt("Logout", "Are you sure you want to logout?", "Yes", "No");
            prompt.setOkButtonListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    prompt.hideInputPrompt();

                    // sign out current user
                    FirebaseController.getAuthInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    // finishing main activity
                    MainActivity.this.finish();
                }
            });
            return true;
        }

        // if user choose to share our application
        else if (itemId == R.id.share) {

            // calling the sharing intent
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart Attendance System (Boosttendance)");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Download and install:\n" + SASConstants.APPLICATION_DOWNLOAD_LINK);
            startActivity(Intent.createChooser(sharingIntent, "Share Download link"));
            return true;
        }

        // if user chooses to view about details
        else if (itemId == R.id.about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return false;
    }

    private boolean bottomNavigationMenu(int itemId) {

        // if joined classes list is selected
        if (itemId == R.id.joined_classes) {
            listView.setAdapter(joinedClassListAdapter);

            // do not show floating button when joined classes are selected
            floatingButton.setVisibility(View.GONE);
            return true;
        }

        // if created classes list is selected
        if (itemId == R.id.created_classes) {
            listView.setAdapter(createdClassListAdapter);

            // show floating button when joined classes are selected
            floatingButton.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {

        // if request code is of GPS permission
        if (requestCode == 1) {

            // if permission is not given
            if (results.length > 0 && results[0] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to about GPS must required
                prompt.showFailureMessagePrompt("Location permission is must required.");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {

        // clearing joined class list
        joinedClassListAdapter.clearList();

        // clearing created class list
        createdClassListAdapter.clearList();

        // refreshing classes list
        FirebaseController.getJoinedClasses(joinedClassListAdapter);
        FirebaseController.getCreatedClasses(createdClassListAdapter);
        super.onStart();
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}
