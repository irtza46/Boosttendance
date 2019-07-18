package com.ilumastech.smart_attendance_system;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class Database {

    private static final String TAG = "Database";

    public static final String USERS = "USERS";
    public static final String U_ID = "U_ID";
    public static final String FULL_NAME = "FULL_NAME";
    public static final String EMAIL = "EMAIL";
    public static final String PHONE_NUMBER = "Database";
    public static final String JOINED = "JOINED";
    public static final String CREATED = "CREATED";
    public static final String ATTENDANCE_ID = "ATTENDANCE_ID";

    public static final String CLASSES = "CLASSES";
    public static final String CLASS_ID = "CLASS_ID";
    public static final String CLASS_NAME = "CLASS_NAME";
    public static final String SESSION = "SESSION";
    public static final String ATTENDANCE_DATE = "ATTENDANCE_DATE";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";

    public  static final String IMEI = "IMEI";

    public static final String MSG = "MSG";
    public static final String DATE_TIME = "DATE_TIME";

    public static FirebaseAuth getFirebaseAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getDatabaseReference(String location) {
        return FirebaseDatabase.getInstance().getReference(location);
    }

    public static Query getUserByMobileNumber(String number) {
        return getDatabaseReference(USERS).orderByChild(PHONE_NUMBER).equalTo(number);
    }

    public static void createUser(String uid, String accType, String fullName, String email, String phoneNumber) {

        FirebaseDatabase.getInstance().getReference().child("users/" + uid).setValue(
                new User(
                        uid,
                        fullName,
                        email,
                        accType,
                        phoneNumber,
                        null,
                        null
                )
        );
    }

    static void getUserAndSaveRecord(final String uid,
                                     final SharedPreferences.Editor editor) {

        Log.d(TAG, "Searching uid: " + uid);

        // checking in users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" +
                uid);

        reference.keepSynced(true);
        FirebaseDatabase.getInstance().getReference("attendances").keepSynced(true);
        FirebaseDatabase.getInstance().getReference("attendances").keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    User user_record = dataSnapshot.getValue(User.class);

                    Log.d(TAG, "(getUserAndSaveRecord) Found uid: " + uid);

                    editor.putString("user", ((new Gson()).toJson(user_record)));
                    editor.apply();
                    Log.d(TAG, "User data saved to shared preference" + user_record);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void getJoinedClasses(final String uid,
                                        final ClassArrayAdapter classArrayAdapter) {

        // checking in users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" +
                uid + "/classes/joined");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                classArrayAdapter.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        ClassRoom classRoom = snapshot.getValue(ClassRoom.class);
                        if (classRoom != null) {
                            classRoom.setClassId(snapshot.getKey());
                            classArrayAdapter.add(classRoom);

                            Log.d(TAG, "(Joined) ClassId: " + classRoom.getClassId());
                        }
                    }
                    classArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void getCreatedClasses(final String uid,
                                         final ClassArrayAdapter classArrayAdapter) {

        // checking in users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" +
                uid + "/classes/created");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                classArrayAdapter.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        ClassRoom classRoom = snapshot.getValue(ClassRoom.class);
                        if (classRoom != null) {
                            classRoom.setClassId(snapshot.getKey());
                            classArrayAdapter.add(classRoom);

                            Log.d(TAG, "(Created) ClassId: " + classRoom.getClassId());
                        }
                    }
                    classArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
