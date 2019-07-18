package com.ilumastech.smart_attendance_system;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ilumastech.smart_attendance_system.login_registration_activities.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ClassArrayAdapter joinedClassArrayAdapter, createdClassArrayAdapter;
    private ListView listView;
    private DrawerLayout drawerLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private Prompt prompt;

    private BottomNavigationView bottomNavigationView;
    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new NavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.profile) {
                        startActivity(new Intent(MainActivity.this,
                                SettingsActivity.class));
                    } else if (itemId == R.id.logout) {

                        prompt.showInputMessagePrompt("Logout", "Are you sure " +
                                "you want to logout?", "Yes", "No");
                        prompt.setOkButtonListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                prompt.hideInputPrompt();
                                firebaseAuth.signOut();
                                startActivity(new Intent(MainActivity.this,
                                        LoginActivity.class));
                                MainActivity.this.finish();
                            }
                        });
                    } else if (itemId == R.id.share) {

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                "Boosttendance (Smart Attendance System)");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                                "Install it from here: ");
                        startActivity(Intent.createChooser(sharingIntent, "Share Via"));
                    } else if (itemId == R.id.about) {
                        startActivity(new Intent(MainActivity.this,
                                AboutActivity.class));
                    }
                    return true;
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        loadAndSaveRecord();
    }

    private void init() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // initializing the classroom list views
        listView = findViewById(R.id.list_view);
        listView.addFooterView(new View(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ClassRoom classRoom = (ClassRoom) parent.getItemAtPosition(position);

                if (bottomNavigationView.getSelectedItemId() == R.id.joined_classes)
                    startActivity(new Intent(MainActivity.this,
                            MarkAttendanceActivity.class).putExtra("classRoom", classRoom));

                else
                    startActivity(new Intent(MainActivity.this,
                            ClassDetailsActivity.class).putExtra("classRoom", classRoom));
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        prompt = new Prompt(this);

        bottomNavigationView = findViewById(R.id.bottom_nav_menu);
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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.nav_username)).setText(user.getDisplayName());
        ((TextView) headerView.findViewById(R.id.nav_email)).setText(user.getEmail());

        joinedClassArrayAdapter = new ClassArrayAdapter(this, R.layout.class_card);
        createdClassArrayAdapter = new ClassArrayAdapter(this, R.layout.class_card);
        showClasses();
    }

    private void loadAndSaveRecord() {

        SharedPreferences offline_record = getSharedPreferences("offline_record", MODE_PRIVATE);
        SharedPreferences.Editor editor = offline_record.edit();

        Database.getUserAndSaveRecord(user.getUid(), editor);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        else
            super.onBackPressed();
    }

    private void showClasses() {

        Database.getJoinedClasses(user.getUid(), joinedClassArrayAdapter);
        Database.getCreatedClasses(user.getUid(), createdClassArrayAdapter);
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

        bottomNavigationView.setSelectedItemId(R.id.joined_classes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prompt != null) {
            prompt.hideInputPrompt();
            prompt = null;
        }
    }

    public void searchClass(View view) {
        startActivity(new Intent(this, SearchClassesActivity.class));
    }

    public void createClass(View view) {
        startActivity(new Intent(this, CreateClassActivity.class));
    }

    //
    // EXTRA BELOW
    //

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//
//        savedInstanceState.putBoolean("MyBoolean", true);
//        savedInstanceState.putDouble("myDouble", 1.9);
//        savedInstanceState.putInt("MyInt", 1);
//        savedInstanceState.putString("MyString", "Welcome back to Android");
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
//        double myDouble = savedInstanceState.getDouble("myDouble");
//        int myInt = savedInstanceState.getInt("MyInt");
//        String myString = savedInstanceState.getString("MyString");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("lastActivity", getClass().getName());
//        editor.apply();
//    }

    //
    // EXTRA ABOVE
    //
}

