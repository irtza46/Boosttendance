package com.ilumastech.smart_attendance_system;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
        //TODO
    }

    public void sendApplication(View view) {
        //TODO
    }

    public void markAttendance(View view) {

        // if user has already granted GPS permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // getting current location
            final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
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
            else
                addAttendance(location.getLongitude(), location.getLatitude());
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

    private void addAttendance(final double studentLongitude, final double studentLatitude) {

        // if user has already granted GPS permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            // getting IMEI address
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            final String imei = telephonyManager.getDeviceId();

            final String classId = classRoom.getClass_Id();

            // getting current date
            Calendar calendar = Calendar.getInstance();
            final String attendanceDate = (new SimpleDateFormat("dd-MMM-yyyy", Locale.US)).format(calendar.getTime());

            // checking session and marking attendance
            Database.getClassByAttendanceDate(classId, attendanceDate).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // if attendance session was started
                            if (dataSnapshot.exists()) {

                                // fetching attendance session timeout
                                String attendanceTimeout = (String) dataSnapshot.child(Database.TIMEOUT).getValue();

                                // fetching teacher location
                                double teacherLatitude = (double) dataSnapshot.child(Database.LATITUDE).getValue();
                                double teacherLongitude = (double) dataSnapshot.child(Database.LONGITUDE).getValue();

                                // if student is within attendance marking range
                                if (getDistance(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude)
                                        <= SASConstants.ATTENDANCE_MARKING_RANGE_METERS) {
                                    try {

                                        // if student is marking attendance before timeout
                                        if (Calendar.getInstance().getTime().before(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US).parse(attendanceTimeout))) {


                                            // checking if attendance have been marked already using this IMEI
                                            Database.getAttendanceByIMEI(classId, attendanceDate, imei).addListenerForSingleValueEvent(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                            // if attendance is not already marked using device
                                                            if (!dataSnapshot.exists()) {

                                                                // adding attendance record
                                                                Database.addAttendance(classId, attendanceDate, classRoom.getAttendance_Id(), imei);

                                                                // showing prompt to student about attendance marked
                                                                prompt.showSuccessMessagePrompt("Your attendance has been marked for today.");
                                                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        prompt.hidePrompt();
                                                                    }
                                                                });
                                                                Log.d(TAG, "(markAttendance) Class attendance: " + classId);
                                                            }

                                                            // if attendance is already marked with using device
                                                            else {

                                                                // showing prompt to student about attendance already marked
                                                                prompt.showFailureMessagePrompt("Attendance has already been marked using this mobile.\nNo more attendance can be marked using this mobile today.");
                                                                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
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
                                    } catch (ParseException ignored) {
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }

        // if user has not granted GPS permission already
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {

        // if request code is of GPS permission
        if (requestCode == 1) {

            // if permission is not given
            if (results.length > 0 || results[0] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to teacher about GPS required for attendance marking
                prompt.showFailureMessagePrompt("Location permission is required for marking attendance");
                SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                    @Override
                    public void run() {
                        prompt.hidePrompt();
                    }
                });
            }
        }

        // if request code is of phone state read permission
        else if (requestCode == 2) {

            // if permission is not given
            if (results.length > 0 || results[1] != PackageManager.PERMISSION_GRANTED) {

                // show prompt to student about IMEI required for attendance marking
                prompt.showFailureMessagePrompt("Phone state permission is required for marking attendance");
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
