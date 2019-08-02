package com.ilumastech.smart_attendance_system.student_activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseController;
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;
import com.ilumastech.smart_attendance_system.prompts.NotificationPrompt;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "MarkAttendanceActivity";

    private Prompt prompt;

    private ClassRoom classRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
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

        // setting attendance id of student
        ((TextView) findViewById(R.id.attendance_id)).setText(classRoom.getAttendance_Id());

        // setting teacher email
        ((TextView) findViewById(R.id.teacher_email)).setText(classRoom.getEmail());

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);
    }

    public void showAttendanceHistory(View view) {

        // checking this student attendance record for this class
        prompt.showProgress("Attendance Record", "Exporting attendance record...");
        FirebaseController.getDatabaseReference(FirebaseController.ATTENDANCES).child(classRoom.getClass_Id())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // if there is no attendance record
                        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                            prompt.hideProgress();
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
                            final Map<String, String> attendances = new HashMap<>();

                            // checking attendance in all dates
                            for (DataSnapshot dates : dataSnapshot.getChildren()) {

                                // storing attendance date
                                attendanceDates.add(dates.getKey());

                                // if attendance of this student is present
                                if (dates.hasChild(classRoom.getAttendance_Id()))
                                    attendances.put(dates.getKey(), "P");

                                    // if attendance of this student is not present
                                else
                                    attendances.put(dates.getKey(), "A");
                            }
                            prompt.hideProgress();

                            // creating attendance record file
                            createFile(classRoom.getClass_Name(), classRoom.getAttendance_Id(), attendanceDates, attendances);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void createFile(String class_name, String attendance_id, List<String> attendanceDates, Map<String, String> attendances) {

        // if user has already granted WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            // creating file with class name and storing in downloads folder
            final File attendanceFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), class_name + ".csv");
            Log.d(TAG, "Storing attendance record to file: " + attendanceFile.toString());

            // exporting attendance record to csv file
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(attendanceFile));
                List<String[]> data = new ArrayList<>();

                // creating attendance row
                String[] attendance = new String[attendanceDates.size() + 1];
                attendance[0] = attendance_id;

                // creating header row
                String[] header = new String[attendanceDates.size() + 1];
                header[0] = "Student ID";
                for (int i = 1; i < attendanceDates.size() + 1; i++) {
                    header[i] = attendanceDates.get(i - 1);
                    attendance[i] = attendances.get(header[i]);
                }
                Log.e(TAG, Arrays.toString(header) + Arrays.toString(attendance));

                // adding header to csv attendance file
                data.add(header);
                data.add(attendance);

                // writing data to attendance file
                writer.writeAll(data);
                writer.close();

                // show long wait prompt to student about file has been saved
                prompt.showSuccessMessagePrompt("Attendance record have been saved for this class in\nDOWNLOADS folder:\n\n" + attendanceFile.getName());
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hideInputPrompt();

                        // if android version is less than Nougat
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {

                            // opening attendance record file after saving
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(attendanceFile), "application/vnd.ms-excel");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                });
            } catch (IOException ignored) {}
        }

        // if user has not granted GPS permission already
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
    }

    public void sendApplication(View view) {

        // showing application sending form
        final NotificationPrompt notificationPrompt = new NotificationPrompt(this, 1);
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

                // show prompt about sending application to students
                prompt.showProgress("Application", "Sending application to teacher...");
                FirebaseFunctions.getInstance().getHttpsCallable("getCurrentTime").call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        prompt.hideProgress();

                        // getting timestamp from firebase function
                        long timestamp = (long) httpsCallableResult.getData();

                        // getting current date
                        final Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(timestamp);
                        final String dateTime = (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.US)).format(calendar.getTime());

                        // getting class name
                        final String className = classRoom.getClass_Name();

                        // getting student Id
                        String attendanceId = classRoom.getAttendance_Id();

                        // sending application to teacher
                        FirebaseController.sendApplication(classRoom.getU_Id(), msg, dateTime, className, attendanceId);

                        Log.d(TAG, "Application sent.");
                        prompt.showSuccessMessagePrompt("Application has been sent to teacher.");
                        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
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

    public void markAttendance(View view) {

        // if user has already granted GPS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // getting current location
            final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));

            // if location is ON
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // if there is no location update right now
                if (location == null) {

                    // requesting location updates
                    prompt.showProgress("GPS", "Getting GPS coordinates. Please move you mobile ");
                    Log.d(TAG, "Location requested");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "GPS ON");
                            prompt.hideProgress();

                            // stop for requesting updates again and again
                            locationManager.removeUpdates(this);

                            // marking attendance of student
                            addAttendance(location.getLongitude(), location.getLatitude());
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
                else {
                    addAttendance(location.getLongitude(), location.getLatitude());
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // stop for requesting updates again and again
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

            // if location is not ON
            else {

                // show prompt to student about turning the location ON
                prompt.showFailureMessagePrompt("Please turn your location ON to mark attendance.");
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

    private double getDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2) {

        // converting teacher latitude into radian
        double rad_latitude_1 = (latitude_1 * Math.PI / 180.0);

        // converting student latitude into radian
        double rad_latitude_2 = (latitude_2 * Math.PI / 180.0);

        // converting difference of teacher and student latitude into radian
        double rad_longitude_diff = ((longitude_1 - longitude_2) * Math.PI / 180.0);

        // calculating distance between student and teacher
        double distance = Math.sin(rad_latitude_1) * Math.sin(rad_latitude_2) +
                Math.cos(rad_latitude_1) * Math.cos(rad_latitude_2) * Math.cos(rad_longitude_diff);

        // converting distance into meters and returning
        return (((Math.acos(distance) * 180.0 / Math.PI) * (111.18957696)) / 1000.0);
    }

    @SuppressLint("HardwareIds")
    private void addAttendance(final double studentLongitude, final double studentLatitude) {

        // if user has already granted READ_PHONE_STATE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            // getting IMEI address
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            final StringBuilder imei = new StringBuilder();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) imei.append(telephonyManager.getDeviceId());
            else imei.append(telephonyManager.getImei());
            Log.d(TAG, "Marking attendance: " + imei.toString());

            // show prompt to student about marking attendance
            prompt.showProgress("Attendance", "Marking your attendance...");

            // calling firebase function for getting current time
            FirebaseFunctions.getInstance().getHttpsCallable("getCurrentTime").call().addOnSuccessListener(
                    new OnSuccessListener<HttpsCallableResult>() {
                        @Override
                        public void onSuccess(HttpsCallableResult httpsCallableResult) {

                            // getting timestamp from firebase function
                            long timestamp = (long) httpsCallableResult.getData();

                            // getting class id
                            final String classId = classRoom.getClass_Id();

                            // getting current date
                            final Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp);
                            final String attendanceDate = (new SimpleDateFormat("dd-MMM-yyyy", Locale.US)).format(calendar.getTime());

                            // checking session and marking attendance
                            FirebaseController.getClassSessionByClassId(classId).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            // if attendance session was started
                                            if (dataSnapshot.exists()) {

                                                // fetching attendance session timeout
                                                String attendanceTimeout = String.valueOf(dataSnapshot.child(FirebaseController.TIMEOUT).getValue());

                                                // fetching teacher location
                                                double teacherLatitude = Double.parseDouble(String.valueOf(dataSnapshot.child(FirebaseController.LATITUDE).getValue()));
                                                double teacherLongitude = Double.parseDouble(String.valueOf(dataSnapshot.child(FirebaseController.LONGITUDE).getValue()));

                                                // if student is within attendance marking range
                                                if (getDistance(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude) <= SASConstants.ATTENDANCE_MARKING_RANGE_METERS) {
                                                    try {

                                                        // if student is marking attendance before session timeout
                                                        if (calendar.getTime().before(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.US).parse(attendanceTimeout))) {

                                                            // checking if attendance have been marked already using this IMEI
                                                            FirebaseController.getAttendanceByAttendanceDate(classId, attendanceDate).addListenerForSingleValueEvent(
                                                                    new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                            prompt.hideProgress();

                                                                            // checking if attendance has been marked before using this imei
                                                                            boolean alreadyMarked = false;
                                                                            for (DataSnapshot ids : dataSnapshot.getChildren()) {
                                                                                if (String.valueOf(ids.getValue()).equals(imei.toString()))
                                                                                    alreadyMarked = true;
                                                                            }

                                                                            // if attendance is not already marked using device
                                                                            if (!alreadyMarked) {

                                                                                // adding attendance record
                                                                                FirebaseController.addAttendance(classId, attendanceDate, classRoom.getAttendance_Id(), imei.toString());

                                                                                // showing prompt to student about attendance marked
                                                                                prompt.showSuccessMessagePrompt("Your attendance has been marked for today.");
                                                                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        prompt.hidePrompt();
                                                                                    }
                                                                                });
                                                                                Log.d(TAG, "(markAttendance) Class attendance: " + classRoom.getAttendance_Id());
                                                                            }

                                                                            // if attendance is already marked with using device
                                                                            else {

                                                                                // showing extra wait prompt to student about attendance already marked
                                                                                prompt.showFailureMessagePrompt("Attendance has already been marked using this mobile.\nNo more attendance can be marked for this class using this mobile today.");
                                                                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_EXTRA, new Runnable() {
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
                                                                    }
                                                            );
                                                        }

                                                        // if student is marking attendance after session timeout
                                                        else {
                                                            prompt.hideProgress();

                                                            // show short prompt to student about session expired
                                                            prompt.showFailureMessagePrompt("Attendance marking session has ended.");
                                                            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    prompt.hidePrompt();
                                                                }
                                                            });
                                                        }
                                                    } catch (ParseException ignored) {
                                                    }
                                                }

                                                // if student is not within class range
                                                else {
                                                    prompt.hideProgress();

                                                    // show long prompt to student about not within class range
                                                    prompt.showFailureMessagePrompt("Attendance cannot be marked as you're not within class range.");
                                                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            prompt.hidePrompt();
                                                        }
                                                    });
                                                }
                                            }

                                            // if session is not created yet
                                            else {
                                                prompt.hideProgress();

                                                // show short prompt to student about attendance session not started yet
                                                prompt.showFailureMessagePrompt("Attendance marking session not started.");
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
                    }
            );
        }

        // if user has not granted READ_PHONE_STATE permission already
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {

        // if request code is of GPS permission
        if (requestCode == 1) {

            // if permission is not given
            if (results.length > 0 && results[0] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to teacher about GPS required for attendance marking
                prompt.showFailureMessagePrompt("Location permission is required for marking attendance.");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }

        // if request code is of phone state read permission
        if (requestCode == 2) {

            // if permission is not given
            if (results.length > 0 && results[0] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to student about IMEI required for attendance marking
                prompt.showFailureMessagePrompt("Phone state permission is required for marking attendance.");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }

        // if request code is of WRITE_EXTERNAL_STORAGE permission
        if (requestCode == 3) {

            // if permission is not given
            if (results.length > 0 && results[0] != PackageManager.PERMISSION_GRANTED) {

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
