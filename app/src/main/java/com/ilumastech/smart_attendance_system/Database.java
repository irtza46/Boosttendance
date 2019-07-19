package com.ilumastech.smart_attendance_system;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Database {

    public static final String USERS = "USERS";
    public static final String U_ID = "U_ID";
    public static final String FULL_NAME = "FULL_NAME";
    public static final String EMAIL = "EMAIL";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String JOINED = "CLASSES/JOINED";
    public static final String CREATED = "CLASSES/CREATED";
    public static final String ATTENDANCE_ID = "ATTENDANCE_ID";
    public static final String CLASSES = "CLASSES";
    public static final String CLASS_ID = "CLASS_ID";
    public static final String CLASS_NAME = "CLASS_NAME";
    public static final String SESSION = "SESSION";
    public static final String ATTENDANCE_DATE = "ATTENDANCE_DATE";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String ATTENDANCES = "ATTENDANCES";
    public static final String IMEI = "IMEI";
    public static final String NOTIFICATIONS = "NOTIFICATIONS";
    public static final String MSG = "MSG";
    public static final String DATE_TIME = "DATE_TIME";

    private static final String TAG = "Database";

    public static FirebaseAuth getFirebaseAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseUser getUser() {
        return getFirebaseAuthInstance().getCurrentUser();
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

    public static void createUser(String uid, String fullName, String email, String phoneNumber) {

        // creating a new user object to store in database
        final Map<String, String> newUser = new HashMap<>();
        newUser.put(FULL_NAME, fullName);
        newUser.put(EMAIL, email);
        newUser.put(PHONE_NUMBER, phoneNumber);

        getDatabaseReference(USERS).child(uid).setValue(newUser);
    }


    public static void updateUserPhoneNumber(String number) {
        getDatabaseReference(Database.USERS).child(getUser().getUid()).child(PHONE_NUMBER).setValue(number);
    }

    public static Query getClassByU_ID(String teacherId) {
        return getDatabaseReference(CLASSES).orderByChild(U_ID).equalTo(teacherId);
    }

    public static void getJoinedClasses(final ClassArrayAdapter joinedClassArrayAdapter) {

        // checking in users
        getDatabaseReference(USERS).child(getUser().getUid()).child(JOINED).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clearing joined class list
                joinedClassArrayAdapter.clear();

                // if there exist any joined class
                if (dataSnapshot.exists()) {

                    // reading data from all joined classes
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // fetching class room ID
                        final String class_Id = snapshot.getKey();

                        // fetching user attendance ID for this class
                        final String attendance_Id = (String) snapshot.child(ATTENDANCE_ID).getValue();

                        // fetching class record
                        getClassByClassId(class_Id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                // fetching class name
                                final String class_name = (String) dataSnapshot.child(CLASS_NAME).getValue();

                                // fetching teacher Id
                                final String u_Id = (String) dataSnapshot.child(U_ID).getValue();

                                // fetching teacher record
                                getUserByU_ID(u_Id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        // fetching teacher email
                                        String email = (String) dataSnapshot.child(EMAIL).getValue();

                                        // adding class to joined class adapter
                                        joinedClassArrayAdapter.add(new ClassRoom(class_Id, class_name, u_Id, attendance_Id, email));
                                        Log.d(TAG, "(Joined) ClassId: " + class_Id);

                                        // notifying joined class adapter about any changes
                                        joinedClassArrayAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static DatabaseReference getUserByU_ID(String u_id) {
        return getDatabaseReference(USERS).child(u_id);
    }

    public static DatabaseReference getClassByClassId(String classId) {
        return getDatabaseReference(CLASSES).child(classId);
    }

    public static String getUniqueID() {
        return getDatabaseReference().push().getKey();
    }

    public static void updateCreatedClassesByU_ID(String teacherId, String classId) {
        getDatabaseReference(USERS).child(teacherId).child(CREATED).child(classId).setValue("");
    }

    public static void addNewClass(String classId, String className, String teacherId) {

        // creating a new class object to store in database
        Map<String, String> newClass = new HashMap<>();
        newClass.put(CLASS_NAME, className);
        newClass.put(U_ID, teacherId);

        // adding class to classes record
        getDatabaseReference(CLASSES).child(classId).setValue(newClass);

        // adding class record in attendances for storing class attendances record
        getDatabaseReference(ATTENDANCES).child(classId).setValue("");

        // creating a new session object to store in database
        Map<String, Object> newSession = new HashMap<>();
        newClass.put(ATTENDANCE_DATE, "");
        newClass.put(TIMEOUT, "");
        newClass.put(LONGITUDE, "");
        newClass.put(LATITUDE, "");
        getDatabaseReference(CLASSES).child(classId).child(SESSION).setValue(newSession);
    }

    public static void getCreatedClasses(final ClassArrayAdapter createdClassArrayAdapter) {

        // checking in users
        getDatabaseReference(USERS).child(getUser().getUid()).child(CREATED).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clearing joined class list
                createdClassArrayAdapter.clear();

                // if there exist any joined class
                if (dataSnapshot.exists()) {

                    // reading data from all joined classes
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // fetching class room ID
                        final String class_Id = snapshot.getKey();

                        // fetching class record
                        getClassByClassId(class_Id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                // fetching class name
                                String class_name = (String) dataSnapshot.child(CLASS_NAME).getValue();

                                // fetching last attendance date
                                String attendance_Date = (String) dataSnapshot.child(SESSION).child(ATTENDANCE_DATE).getValue();
                                if (attendance_Date == null)
                                    attendance_Date = "";

                                Log.d(TAG, class_name + attendance_Date);
                                // adding class to joined class adapter
                                createdClassArrayAdapter.add(new ClassRoom(class_Id, class_name, attendance_Date));
                                Log.d(TAG, "(Created) ClassId: " + class_Id);

                                // notifying joined class adapter about any changes
                                createdClassArrayAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public static Query getClassByAttendanceDate(String classId, String attendanceDate) {
        return getDatabaseReference(CLASSES).child(classId).child(SESSION).orderByChild(ATTENDANCE_DATE).equalTo(attendanceDate);
    }

    public static void addSession(String classId, String attendanceDate, String attendanceTimeout, double longitude, double latitude) {

        // creating new session object
        Map<String, String> newSession = new HashMap<>();
        newSession.put(ATTENDANCE_DATE, attendanceDate);
        newSession.put(TIMEOUT, attendanceTimeout);
        newSession.put(LONGITUDE, String.valueOf(longitude));
        newSession.put(LATITUDE, String.valueOf(latitude));

        // adding new session to class
        getDatabaseReference(CLASSES).child(classId).child(SESSION).setValue(newSession);

        // adding attendance date of class in attendances record
        getDatabaseReference(ATTENDANCES).child(classId).child(attendanceDate).setValue("");
    }

    public static void updateSessionTimeout(String classId, String attendanceTimeout) {
        getDatabaseReference(CLASSES).child(classId).child(SESSION).child(TIMEOUT).setValue(attendanceTimeout);
    }

    public static Query getAttendanceByIMEI(String classId, String attendanceDate, String imei) {
        return getDatabaseReference(ATTENDANCES).child(classId).child(attendanceDate).orderByChild(IMEI).equalTo(imei);
    }

    public static void addAttendance(String classId, String attendanceDate, String attendance_id, String imei) {
        getDatabaseReference(ATTENDANCES).child(classId).child(attendanceDate).child(attendance_id).child(imei).setValue("");
    }

    public static void addJoinClass(final String classId, final String email, final String id) {

        // checking if user exists with email
        Database.getDatabaseReference(Database.USERS).orderByChild(Database.EMAIL).equalTo(email.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot temp : dataSnapshot.getChildren()) {

                                // if any email matches with teachers email
                                if (String.valueOf(dataSnapshot.child(EMAIL).getValue()).equalsIgnoreCase(email))
                                    return;

                                Database.getUserByU_ID(temp.getKey()).child(Database.JOINED).child(classId)
                                        .child(Database.ATTENDANCE_ID).setValue(id);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

//    static void getUserAndSaveRecord(final String uid,
//                                     final SharedPreferences.Editor editor) {
//
//        Log.d(TAG, "Searching uid: " + uid);
//
//        // checking in users
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" +
//                uid);
//
//        reference.keepSynced(true);
//        FirebaseDatabase.getInstance().getReference("attendances").keepSynced(true);
//        FirebaseDatabase.getInstance().getReference("attendances").keepSynced(true);
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                if (dataSnapshot.exists()) {
//                    User user_record = dataSnapshot.getValue(User.class);
//
//                    Log.d(TAG, "(getUserAndSaveRecord) Found uid: " + uid);
//
//                    editor.putString("user", ((new Gson()).toJson(user_record)));
//                    editor.apply();
//                    Log.d(TAG, "User data saved to shared preference" + user_record);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//    }

}
