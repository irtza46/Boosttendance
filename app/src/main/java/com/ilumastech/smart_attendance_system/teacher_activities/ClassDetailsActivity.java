package com.ilumastech.smart_attendance_system.teacher_activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;
import com.ilumastech.smart_attendance_system.prompts.EnrollStudentPrompt;
import com.ilumastech.smart_attendance_system.prompts.NotificationPrompt;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        ((TextView) findViewById(R.id.last_attendance_date)).setText((classRoom.getAttendance_Date().isEmpty()) ? "No Attendance taken yet." : classRoom.getAttendance_Date());

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
                FirebaseDatabase.getDatabaseReference(FirebaseDatabase.USERS).orderByChild(FirebaseDatabase.EMAIL).equalTo(email.toLowerCase())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                prompt.hideProgress();

                                // if student is found
                                if (dataSnapshot.exists()) {

                                    // adding this class to joined classes of student

                                    for (DataSnapshot temp : dataSnapshot.getChildren()) {

                                        // storing in joined list of class in student
                                        FirebaseDatabase.getUserByU_ID(temp.getKey()).child(FirebaseDatabase.JOINED).child(classRoom.getClass_Id()).child(FirebaseDatabase.ATTENDANCE_ID).setValue(id);

                                        // storing in enrolled list of class
                                        FirebaseDatabase.getClassByClassId(classRoom.getClass_Id()).child(FirebaseDatabase.ENROLLED).child(id).setValue(temp.getKey());
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

        FirebaseDatabase.getDatabaseReference(FirebaseDatabase.CLASSES).child(classRoom.getClass_Id()).child(FirebaseDatabase.ENROLLED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // getting list of all enrolled students attendance ID
                        final List<String> enrolledStudentsAttendanceID = new ArrayList<>();
                        for (DataSnapshot students : dataSnapshot.getChildren())
                            enrolledStudentsAttendanceID.add(students.getKey());

                        // if there are some enrolled students in class room
                        if (!enrolledStudentsAttendanceID.isEmpty()) {

                            // checking all attendance record for this class
                            FirebaseDatabase.getDatabaseReference(FirebaseDatabase.ATTENDANCES).child(classRoom.getClass_Id())
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

                                                // for storing attendance dates and mapping attendances
                                                final List<String> attendanceDates = new ArrayList<>();
                                                final Map<String, List<String>> attendanceIds = new HashMap<>();

                                                // checking attendance in all dates
                                                for (DataSnapshot dates : dataSnapshot.getChildren()) {

                                                    // storing attendance date
                                                    attendanceDates.add(dates.getKey());

                                                    // fetching all attendance if of students who marked attendance
                                                    List<String> ids = new ArrayList<>();
                                                    for (DataSnapshot id : dates.getChildren())
                                                        ids.add(id.getKey());

                                                    // storing attendance ids of students
                                                    attendanceIds.put(dates.getKey(), ids);
                                                }

                                                // creating attendance record file
                                                createFile(classRoom.getClass_Name(), enrolledStudentsAttendanceID, attendanceDates, attendanceIds);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void createFile(String class_name, List<String> enrolledStudentsAttendanceID, List<String> attendanceDates, Map<String, List<String>> attendanceIds) {

        // if user has already granted WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            // creating file with class name and storing in downloads folder
            File attendanceFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), class_name + ".csv");
            Log.d(TAG, "Storing attendance record to file: " + attendanceFile.toString());

            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(attendanceFile));
                List<String[]> data = new ArrayList<>();

                // creating header
                String[] header = new String[attendanceDates.size() + 1];
                header[0] = "Student ID";
                for (int i = 1; i < attendanceDates.size() + 1; i++)
                    header[i] = attendanceDates.get(i - 1);

                data.add(header);
                Log.e(TAG, String.valueOf(header));

//            writer.writeAll(data);
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        // if user has not granted WRITE_EXTERNAL_STORAGE permission already
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
    }

    public void sendNotification(View view) {

        // showing notification sending form
        final NotificationPrompt notificationPrompt = new NotificationPrompt(this, 0);
        notificationPrompt.setSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationPrompt.hidePrompt();

                // getting entered message
                final String msg = notificationPrompt.getMsg().getText().toString();

                // if no message is entered
                if (msg.isEmpty()) {
                    notificationPrompt.getMsg().setError("Please enter message first.");
                    return;
                }

                FirebaseDatabase.getEnrolledStudentsByClassId(classRoom.getClass_Id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // if there is no student enrolled in this class
                        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                            prompt.showFailureMessagePrompt("No student enrolled in this class.");
                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                @Override
                                public void run() {
                                    prompt.hidePrompt();
                                }
                            });
                            return;
                        }

                        // fetching all enrolled students U_ID
                        final List<String> studentsUids = new ArrayList<>();
                        for (DataSnapshot studentId : dataSnapshot.getChildren())
                            studentsUids.add(String.valueOf(studentId.getValue()));

                        // show prompt about sending notification to students
                        prompt.showProgress("Notification", "Sending notification to students...");
                        FirebaseFunctions.getInstance().getHttpsCallable("getCurrentTime").call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                            @Override
                            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                                prompt.hideProgress();

                                // getting timestamp from firebase function
                                long timestamp = (long) httpsCallableResult.getData();

                                // getting current date
                                final Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(timestamp);
                                final String dateTime = (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US)).format(calendar.getTime());

                                // getting class name
                                final String className = classRoom.getClass_Name();

                                // getting teacher email
                                String email = FirebaseDatabase.getUser().getEmail();

                                Log.d(TAG, dateTime + " : " + className + " : " + email);
                                // sending notification to each enrolled student
                                for (String uid : studentsUids)
                                    FirebaseDatabase.sendNotification(uid, msg, dateTime, className, email);

                                prompt.showSuccessMessagePrompt("Notification has been sent to students.");
                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                    @Override
                                    public void run() {
                                        prompt.hidePrompt();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    public void startSession(View view) {

        // getting current time
        Calendar calendar = Calendar.getInstance();
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMins = calendar.get(Calendar.MINUTE);

        // creating time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int min) {

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
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // if there is no location update right now
                if (location == null) {

                    // requesting location updates
                    Log.d(TAG, "Location requested");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
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
                prompt.showFailureMessagePrompt("Please turn your location ON to start session.");
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

        // show prompt to teacher about starting attendance session
        prompt.showProgress("Session", "Starting attendance session...");

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
                final String attendanceTimeout = (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.US)).format(calendar.getTime());

                // starting session
                Log.d(TAG, classId + " " + attendanceDate + " " + attendanceTimeout + " " + latitude + " " + longitude + " " + timeoutMins);
                FirebaseDatabase.getClassSessionByClassId(classId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                // if session was not started already in today date
                                if (!dataSnapshot.exists() || !String.valueOf(dataSnapshot.child(FirebaseDatabase.ATTENDANCE_DATE).getValue()).equals(attendanceDate)) {

                                    // adding new session in database
                                    FirebaseDatabase.addSession(classId, attendanceDate, attendanceTimeout, longitude, latitude);

                                    // show extra wait prompt to teacher that session has started and display session timeout
                                    prompt.showSuccessMessagePrompt("Session has started.\nSession timeout is set:\n\n" + attendanceTimeout);
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
                                    prompt.showFailureMessagePrompt("Session was already started.\nHaving attendance timeout:\n\n" + dataSnapshot.child(FirebaseDatabase.TIMEOUT).getValue());
                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_EXTRA, new Runnable() {
                                        @Override
                                        public void run() {
                                            prompt.hidePrompt();

                                            // show prompt asking teacher if he wants to update session timeout
                                            prompt.showInputMessagePrompt("Update session timeout",
                                                    "Want to update session timeout to:\n\n" + attendanceTimeout, "Yes", "No");

                                            // if teacher wants to update session timeout
                                            prompt.setOkButtonListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    prompt.hideInputPrompt();

                                                    // show prompt to teacher about surety of updating session timeout
                                                    prompt.showInputMessagePrompt("Update session timeout",
                                                            "Are you sure you want to update session timeout to:\n\n" + attendanceTimeout, "Yes", "No");

                                                    // if teacher confirms to update session timeout
                                                    prompt.setOkButtonListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            prompt.hideInputPrompt();

                                                            // updating session timeout in database
                                                            FirebaseDatabase.updateSessionTimeout(classId, attendanceTimeout);

                                                            // show extra wait prompt about session has update and display new timeout
                                                            prompt.showSuccessMessagePrompt("Session timeout has been updated to:\n\n" + attendanceTimeout);
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
                prompt.showFailureMessagePrompt("Location permission is required for class session starting.");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }

        // if request code is of WRITE_EXTERNAL_STORAGE permission
        if (requestCode == 2) {

            // if permission is not given
            if (results.length > 0 || results[1] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to teacher about WRITE_EXTERNAL_STORAGE required for class session starting
                prompt.showFailureMessagePrompt("Writing to storage permission is required for exporting attendance record.");
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
