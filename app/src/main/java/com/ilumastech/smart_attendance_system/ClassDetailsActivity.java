package com.ilumastech.smart_attendance_system;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ClassDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ClassDetailsActivity";

    private Prompt prompt;

    private ClassRoom classRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);
        init();
    }

    private void init() {

        // setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // getting classroom object
        classRoom = (ClassRoom) getIntent().getSerializableExtra("classRoom");

        // setting class name
        ((TextView) findViewById(R.id.class_name)).setText(classRoom.getClass_Name());

        // setting attendance date
        ((TextView) findViewById(R.id.last_attendance_date)).setText((classRoom.getAttendace_Date().isEmpty()) ? "No Attendance taken yet." : classRoom.getAttendace_Date());

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);
    }

    public void enrollStudent(View view) {

        // showing
        final EnrollStudentPrompt enrollStudentPrompt = new EnrollStudentPrompt(this);
        enrollStudentPrompt.setEnrollButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting student id and email
                final String id = enrollStudentPrompt.getStudent_id().getText().toString();
                final String email = enrollStudentPrompt.getStudent_email().getText().toString();

                // if any of student Id is not entered
                if (id.isEmpty()) {
                    enrollStudentPrompt.getStudent_id().setError("Student Id is required");
                    return;
                }

                // if any of student email is not entered
                if (email.isEmpty()) {
                    enrollStudentPrompt.getStudent_email().setError("Student email is required");
                    return;
                }

                // show progress to teacher about enrolling student
                prompt.showProgress("Enroll Student", "Enrolling...");
                Database.getDatabaseReference(Database.USERS).orderByChild(Database.EMAIL).equalTo(email.toLowerCase())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                prompt.hideProgress();

                                // if student is found
                                if (dataSnapshot.exists()) {

                                    // adding this class to joined classes of student

                                    for (DataSnapshot temp : dataSnapshot.getChildren()) {
                                        Database.getUserByU_ID(temp.getKey()).child(Database.JOINED).child(classRoom.getClass_Id()).child(Database.ATTENDANCE_ID).setValue(id);
                                        break;
                                    }

                                    // show prompt to teacher that student has been enrolled
                                    prompt.showSuccessMessagePrompt("Enrolled.");
                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();
                                            enrollStudentPrompt.hidePrompt();
                                        }
                                    });
                                }

                                // if student not found
                                else {

                                    // show short wait prompt to teacher about student account no available
                                    prompt.showFailureMessagePrompt("Student account with email:\n" + email + "\n doesn't exists.");
                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }
        });
    }

    public void exportAttendanceRecord(View view) {

        Database.getDatabaseReference(Database.ATTENDANCES).child(Database.CLASS_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // if there is no attendance record
                        if (dataSnapshot.getChildrenCount() == 0) {

                            prompt.showFailureMessagePrompt("There is no attendance record for this class yet.");
                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                @Override
                                public void run() {
                                    prompt.hidePrompt();
                                }
                            });
                        }

                        // if there is attendance record
                        else {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    public void sendNotification(View view) {
        //TODO
    }

    public void startSession(View view) {

        // getting current time
        Calendar calendar = Calendar.getInstance();
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMins = calendar.get(Calendar.MINUTE);

        // creating time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour,
                                  int min) {

                // if teacher selected time after current time
                if (hour >= currentHour && min > currentMins) {

                    // converting timeout into minutes
                    int timeoutMins = (((hour - currentHour) * 60) + (min - currentMins));
                    getLocation(timeoutMins);
                }

                // if teacher selected time before current time
                else {

                    // show prompt to teacher about selecting a timeout after current time
                    prompt.showFailureMessagePrompt("Please select a time which is after current time");
                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                        @Override
                        public void run() {
                            prompt.hidePrompt();
                        }
                    });
                }
            }
        }, currentHour, currentMins, false);

        // displaying time picker dialog to teacher to enter session timeout
        timePickerDialog.show();
    }

    private void getLocation(final int timeoutMins) {

        // if user has already granted GPS permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // getting current location
            final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));

            // if location is ON
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // if there is no location update right now
                if (location == null) {

                    // requesting location updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "GPS ON");

                            // stop for requesting updates again and again
                            locationManager.removeUpdates(this);

                            // creating attendance session
                            createSession(timeoutMins, location.getLongitude(), location.getLatitude());
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

                // if location is found, creating attendance session
                else
                    createSession(timeoutMins, location.getLongitude(), location.getLatitude());
            }

            // if location is not ON
            else {

                // show prompt to teacher about turning the location ON
                prompt.showFailureMessagePrompt("Please turn your location ON to start session");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }

        // if user has not granted GPS permission already
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void createSession(final int timeoutMins, final double longitude, final double latitude) {

        // show prompt to teacher about fetching time from server
        prompt.showProgress("Time", "Fetching time from server...");

        // calling firebase function for getting current time
        FirebaseFunctions.getInstance().getHttpsCallable("getCurrentTime")
                .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                prompt.hideProgress();

                // getting timestamp from firebase function
                long timestamp = (long) httpsCallableResult.getData();

                // getting class Id
                final String classId = classRoom.getClass_Id();

                // getting current date
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                final String attendanceDate = (new SimpleDateFormat("dd-MMM-yyyy", Locale.US)).format(calendar.getTime());

                // adding timeout minutes to current time
                calendar.add(Calendar.MINUTE, timeoutMins);

                // getting attendance timeout
                final String attendanceTimeout = (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US)).format(calendar.getTime());

                Log.d(TAG, classId + " " + attendanceDate + " " + attendanceTimeout + " " + latitude + " " + longitude + " " + timeoutMins);
                // starting session
                Database.getClassByAttendanceDate(classId, attendanceDate).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                // if session was not started already in today date
                                if (!dataSnapshot.exists()) {

                                    // adding new session in database
                                    Database.addSession(classId, attendanceDate, attendanceTimeout, longitude, latitude);

                                    // show extra wait prompt to teacher that session has started and display session timeout
                                    prompt.showSuccessMessagePrompt("Session has started.\nSession timeout is set:\n" + attendanceTimeout);
                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_EXTRA, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();
                                        }
                                    });
                                }

                                // if session was started already in today date
                                else {

                                    // show extra wait prompt to teacher that session was already started and display previous session timeout
                                    prompt.showFailureMessagePrompt("Session was already started.\nHaving attendance timeout:\n" + dataSnapshot.child("attendanceTimeout"));
                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_EXTRA, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();

                                            // show prompt asking teacher if he wants to update session timeout
                                            prompt.showInputMessagePrompt("Update session timeout",
                                                    "Want to update session timeout to:\n" + attendanceTimeout, "Yes", "No");

                                            // if teacher wants to update session timeout
                                            prompt.setOkButtonListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    prompt.hideInputPrompt();

                                                    // show prompt to teacher about surety of updating session timeout
                                                    prompt.showInputMessagePrompt("Update session timeout",
                                                            "Are you sure you want to update session timeout to:\n" + attendanceTimeout, "Yes", "No");

                                                    // if teacher confirms to update session timeout
                                                    prompt.setOkButtonListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            prompt.hideInputPrompt();

                                                            // updating session timeout in database
                                                            Database.updateSessionTimeout(classId, attendanceTimeout);

                                                            // show extra wait prompt about session has update and display new timeout
                                                            prompt.showSuccessMessagePrompt("Session timeout has been updated to:\n" + attendanceTimeout);
                                                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_EXTRA, new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    prompt.hidePrompt();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {

        // if request code is of GPS permission
        if (requestCode == 1) {

            // if permission is not given
            if (results.length > 0 || results[0] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to teacher about GPS required for class session starting
                prompt.showFailureMessagePrompt("Location permission is required for class session starting");
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(prompt).hidePrompt();
        prompt = null;
    }

}
