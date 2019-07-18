package com.ilumastech.smart_attendance_system;

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
import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ClassArrayAdapter joinedClassArrayAdapter, createdClassArrayAdapter;
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

        // creating navigation menu item select listener
        NavigationView.OnNavigationItemSelectedListener selectedListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                navigationMenu(menuItem.getItemId());
                return true;
            }
        };

        // setting navigation menu properties
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(selectedListener);

        // setting navigation header properties
        View navigationHeader = navigationView.getHeaderView(0);
        ((TextView) navigationHeader.findViewById(R.id.nav_username)).setText(Database.getUser().getDisplayName());
        ((TextView) navigationHeader.findViewById(R.id.nav_email)).setText(Database.getUser().getEmail());

        // initializing the classroom list views
        listView = findViewById(R.id.list_view);
        listView.addFooterView(new View(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // getting classroom details
                ClassRoom classRoom = (ClassRoom) parent.getItemAtPosition(position);

                if (bottomNavigationView.getSelectedItemId() == R.id.joined_classes)
                    startActivity(new Intent(MainActivity.this,
                            MarkAttendanceActivity.class).putExtra("classRoom", classRoom));

                else
                    startActivity(new Intent(MainActivity.this,
                            ClassDetailsActivity.class).putExtra("classRoom", classRoom));
            }
        });

        joinedClassArrayAdapter = new ClassArrayAdapter(this, R.layout.class_card);
        createdClassArrayAdapter = new ClassArrayAdapter(this, R.layout.class_card);
//        Database.getJoinedClasses(user.getUid(), joinedClassArrayAdapter);
//        Database.getCreatedClasses(user.getUid(), createdClassArrayAdapter);
        listView.setAdapter(joinedClassArrayAdapter);

        //TODO        listView.setTextFilterEnabled(true);
        //editText.addTextChangedListener(new TextWatcher() {
        //
        //            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
        //                    int arg3) {
        //
        //            }
        //
        //            public void beforeTextChanged(CharSequence arg0, int arg1,
        //                    int arg2, int arg3) {
        //
        //            }
        //
        //            public void afterTextChanged(Editable arg0) {
        //                MyActivityName.this.adapter.getFilter().filter(arg0);
        //
        //            }
        //        });

        bottomNavigationView = findViewById(R.id.bottom_nav_menu);
        bottomNavigationView.setSelectedItemId(R.id.joined_classes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.joined_classes) {
                    listView.setAdapter(joinedClassArrayAdapter);
                    return true;
                }

                if (menuItem.getItemId() == R.id.created_classes) {

                    listView.setAdapter(createdClassArrayAdapter);
                    return true;
                }

                return false;
            }
        });

        prompt = new Prompt(this);
    }

    public void searchClass(View view) {
        startActivity(new Intent(this, SearchClassesActivity.class));
    }

    public void createClass(View view) {
        startActivity(new Intent(this, CreateClassActivity.class));
    }

    private void navigationMenu(int itemId) {

        // getting navigation menu selected item id
        if (itemId == R.id.profile)
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

            // if user choose to logout
        else if (itemId == R.id.logout) {

            // show prompt to user to choose if user wants to logout or not
            prompt.showInputMessagePrompt("Logout", "Are you sure you want to logout?", "Yes", "No");
            prompt.setOkButtonListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    prompt.hideInputPrompt();

                    // sign out current user
                    Database.getFirebaseAuthInstance().signOut();
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
