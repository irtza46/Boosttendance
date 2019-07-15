package com.ilumastech.smart_attendance_system;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "MarkAttendanceActivity";

    private Prompt prompt;

    private ClassRoom classRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        prompt = new Prompt(this);

        classRoom = (ClassRoom) getIntent().getSerializableExtra("classRoom");

        ((TextView) findViewById(R.id.class_name)).setText(classRoom.getClassName());
        ((TextView) findViewById(R.id.attendance_id)).setText(classRoom.getAttendanceId());
    }

//    public void updateAttendanceID(View view) {
//
//        Log.d(TAG, "updateAttendanceID called");
//
//        prompt.showInputMessagePrompt("Update attendance ID", "Enter attendance ID",
//                InputType.TYPE_CLASS_TEXT, "Update", "Cancel");
//
//        prompt.getPrompt_input().setText(classRoom.getAttendanceId());
//        prompt.setOkButtonListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final String newAttendanceId = prompt.getPrompt_input().getText().toString();
//
//                //Database.updateAttendanceId(user.getUid(), classRoom.getClassId(), newAttendanceId);
//
//                FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
//                        .child("classes").child("joined").child(classRoom.getClassId())
//                        .child("attendanceId").setValue(newAttendanceId)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                                attendanceId.setText(newAttendanceId);
//                                classRoom.setAttendanceId(newAttendanceId);
//                                Log.d(TAG, "Attendance Id updated: " +
//                                        classRoom.getClassId() + " : " + newAttendanceId);
//                            }
//                        });
//                prompt.hideInputPrompt();
//            }
//        });
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prompt != null) {
            prompt.hideInputPrompt();
            prompt = null;
        }
    }

    private double getDistance(double latitude_1, double longitude_1,
                               double latitude_2, double longitude_2) {

        double rad_latitude_1 = (latitude_1 * Math.PI / 180.0);
        double rad_latitude_2 = (latitude_2 * Math.PI / 180.0);
        double rad_longitude_diff = ((longitude_1 - longitude_2) * Math.PI / 180.0);
        double distance = Math.sin(rad_latitude_1) * Math.sin(rad_latitude_2) +
                Math.cos(rad_latitude_1) * Math.cos(rad_latitude_2) * Math.cos(rad_longitude_diff);
        distance = (Math.acos(distance) * 180.0 / Math.PI);
        distance *= 60 * 1.1515 * 1.609344;
        return distance;
    }

    public void showAttendanceHistory(View view) {
        startActivity(new Intent(this, AttendanceHistoryActivity.class)
                .putExtra("classId", classRoom.getClassId()));
    }

    public void sendApplication(View view) {
        //TODO
    }

    public void markAttendance(View view) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final String classId = classRoom.getClassId();

            Location location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double studentLongitude = location.getLongitude();
            double studentLatitude = location.getLatitude();

            Calendar calendar = Calendar.getInstance();
            final String attendanceDate = (new SimpleDateFormat("dd-MMM-yyyy", Locale.US))
                    .format(calendar.getTime());

            FirebaseDatabase.getInstance().getReference().child("attendanceSessions/" + classId)
                    .orderByChild("attendanceDate").equalTo(attendanceDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                String attendanceTimeout = String.valueOf(dataSnapshot.child("attendanceTimeout").getValue());

                                try {
                                    if (Calendar.getInstance().getTime().after(
                                            new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US).parse(attendanceTimeout))) {

                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                                Log.d(TAG, "(markAttendance) Found classId: " + classId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] strings,
                                           @NonNull int[] result) {

        if (requestCode == 100) {
            if (result.length > 0 || result[0] != PackageManager.PERMISSION_GRANTED) {

                prompt.showFailureMessagePrompt("Location permission is required for " +
                        "class session starting.");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
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

}
