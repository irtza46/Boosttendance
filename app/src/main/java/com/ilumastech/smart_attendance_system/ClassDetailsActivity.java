package com.ilumastech.smart_attendance_system;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClassDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ClassDetailsActivity";

    private Prompt prompt;

    private ClassRoom classRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        classRoom = (ClassRoom) getIntent().getSerializableExtra("classRoom");
        ((TextView) findViewById(R.id.class_name)).setText(classRoom.getClassName());

        FirebaseDatabase.getInstance().getReference("classes/" + classRoom.getClassId() +
                "/attendanceDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String last_attendance_date = (!dataSnapshot.exists()) ? "No Attendance taken yet." :
                        String.valueOf(dataSnapshot.getValue());

                ((TextView) findViewById(R.id.last_attendance_date)).setText(last_attendance_date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        prompt = new Prompt(this);
    }

    public void startSession(View view) {

        Calendar calendar = Calendar.getInstance();
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMins = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hour,
                                          int min) {

                        if (hour >= currentHour && min > currentMins) {

                            int timeoutMins = (((hour - currentHour) * 60) + (min - currentMins));
                            getLocation(timeoutMins);
                        } else {
                            prompt.showFailureMessagePrompt("Please select a time which is after current time.");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (prompt != null)
                                        prompt.hidePrompt();
                                }
                            }, 3000);
                        }
                    }
                }, currentHour, currentMins, false);
        timePickerDialog.show();
    }

    private void getLocation(final int timeoutMins) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "GPS ON");
                        locationManager.removeUpdates(this);

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
            } else
                createSession(timeoutMins, location.getLongitude(), location.getLatitude());
        } else
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    private void createSession(int timeoutMins, final double longitude, final double latitude) {

        final String classId = classRoom.getClassId();

        Calendar calendar = Calendar.getInstance();
        final String attendanceDate = (new SimpleDateFormat("dd-MMM-yyyy", Locale.US))
                .format(calendar.getTime());

        calendar.add(Calendar.MINUTE, timeoutMins);

        final String attendanceTimeout = (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US))
                .format(calendar.getTime());

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("attendanceSessions/" + classId).orderByChild("attendanceDate")
                .equalTo(attendanceDate).addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // if session was not started already in today date
                        if (!dataSnapshot.exists()) {

                            Map<String, String> newSession = new HashMap<>();
                            newSession.put("attendanceDate", attendanceDate);
                            newSession.put("attendanceTimeout", attendanceTimeout);
                            newSession.put("longitude", String.valueOf(longitude));
                            newSession.put("latitude", String.valueOf(latitude));

                            reference.child("attendanceSessions/" + classId)
                                    .setValue(newSession);

                            reference.child("classes/" + classId + "/attendanceDate")
                                    .setValue(attendanceDate);

                            reference.child("attendances/" + classId + "/" + attendanceDate).setValue("");

                            prompt.showSuccessMessagePrompt("Session has started." +
                                    "Session timeout is set: " + attendanceTimeout);
                        } else {

                            prompt.showFailureMessagePrompt("Session was already started."
                                    + "Having attendance timeout: " +
                                    dataSnapshot.child("attendanceTimeout"));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (prompt != null) {
                                        prompt.hidePrompt();

                                        prompt.showInputMessagePrompt("Update session timeout",
                                                "Update session timeout to: " + attendanceTimeout,
                                                "Yes", "No");
                                        prompt.setOkButtonListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                prompt.hideInputPrompt();
                                                prompt.showInputMessagePrompt(
                                                        "Update session timeout",
                                                        "Are you sure you want to update session " +
                                                                "timeout to: " + attendanceTimeout,
                                                        "Yes", "No");

                                                prompt.setOkButtonListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        reference.child("attendanceSessions/" + classId +
                                                                "attendanceTimeout")
                                                                .setValue(attendanceTimeout);

                                                        prompt.hideInputPrompt();
                                                        prompt.showSuccessMessagePrompt(
                                                                "Session timeout has been updated to: "
                                                                        + attendanceTimeout);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }, 3000);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                }
        );
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (prompt != null)
                    prompt.hidePrompt();
            }
        }, 3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] results) {

        if (requestCode == 100) {
            if (results.length > 0 || results[0] != PackageManager.PERMISSION_GRANTED) {

                prompt.showFailureMessagePrompt("Location permission is required for " +
                        "class session starting.");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (prompt != null)
                            prompt.hidePrompt();
                    }
                }, 3000);
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
        if (prompt != null) {
            prompt.hideInputPrompt();
            prompt = null;
        }
    }

    public void sendNotification(View view) {
        //TODO
    }

    public void exportAttendanceRecord(View view) {
        //TODO
    }

    public void enrollStudent(View view) {
        //TODO
    }
}
