package com.ilumastech.smart_attendance_system.firebase_database;

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
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;
import com.ilumastech.smart_attendance_system.list_classes.Notification;
import com.ilumastech.smart_attendance_system.main_activities.adapter.ClassListAdapter;
import com.ilumastech.smart_attendance_system.notification_activities.adapter.NotificationListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirebaseController {

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
    public static final String ENROLLED = "ENROLLED";
    public static final String LATITUDE = "LATITUDE";
    public static final String ATTENDANCES = "ATTENDANCES";
    public static final String NOTIFICATIONS = "NOTIFICATIONS";
    public static final String MSG = "MSG";
    public static final String DATE_TIME = "DATE_TIME";
    private static final String TAG = "FirebaseController";

    // Finalized
    public static FirebaseAuth getAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    // Finalized
    public static FirebaseUser getUser() {
        return getAuthInstance().getCurrentUser();
    }

    // Finalized
    public static FirebaseDatabase getDatabaseInstance() {
        return FirebaseDatabase.getInstance();
    }

    // Finalized
    public static DatabaseReference getDatabaseReference(String location) {
        return getDatabaseInstance().getReference(location);
    }

    // Finalized
    public static Query getUserByMobileNumber(String number) {
        return getDatabaseReference(USERS).orderByChild(PHONE_NUMBER).equalTo(number);
    }

    // Finalized
    public static void createUser(String uid, String fullName, String email, String phoneNumber) {

        // creating a new user object to store in database
        final Map<String, String> newUser = new HashMap<>();
        newUser.put(FULL_NAME, fullName);
        newUser.put(EMAIL, email);
        newUser.put(PHONE_NUMBER, phoneNumber);
        getDatabaseReference(USERS).child(uid).setValue(newUser);
    }

    // Finalized
    public static void updateUserPhoneNumber(String number) {
        getDatabaseReference(FirebaseController.USERS).child(getUser().getUid()).child(PHONE_NUMBER).setValue(number);
    }

    // Finalized
    public static Query getClassByU_ID(String teacherId) {
        return getDatabaseReference(CLASSES).orderByChild(U_ID).equalTo(teacherId);
    }

    // Finalized
    public static void getJoinedClasses(final ClassListAdapter joinedClassListAdapter) {

        // checking in users
        DatabaseReference joinedClassesRef = getDatabaseReference(USERS).child(getUser().getUid()).child(JOINED);
        joinedClassesRef.keepSynced(true);
        joinedClassesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clearing joined class list
                joinedClassListAdapter.clearList();

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
                                        joinedClassListAdapter.add(new ClassRoom(class_Id, class_name, u_Id, attendance_Id, email));
                                        Log.d(TAG, "(Joined) ClassId: " + class_Id);

                                        // notifying joined class adapter about any changes
                                        joinedClassListAdapter.notifyDataSetChanged();
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

    // Finalized
    public static DatabaseReference getUserByU_ID(String u_id) {
        return getDatabaseReference(USERS).child(u_id);
    }

    // Finalized
    public static DatabaseReference getClassByClassId(String classId) {
        return getDatabaseReference(CLASSES).child(classId);
    }

    // Finalized
    public static String getUniqueID() {
        return getDatabaseInstance().getReference().push().getKey();
    }

    // Finalized
    public static void updateCreatedClassesByU_ID(String teacherId, String classId) {
        getDatabaseReference(USERS).child(teacherId).child(CREATED).child(classId).setValue("");
    }

    // Finalized
    public static void addNewClass(String classId, String className, String teacherId) {

        // creating a new class object to store in database
        Map<String, String> newClass = new HashMap<>();
        newClass.put(CLASS_NAME, className);
        newClass.put(U_ID, teacherId);

        // adding class to classes record
        getDatabaseReference(CLASSES).child(classId).setValue(newClass);

        // adding class record in attendances for storing class attendances record
        getDatabaseReference(ATTENDANCES).child(classId).setValue("");
    }

    // Finalized
    public static void getCreatedClasses(final ClassListAdapter createdClassListAdapter) {

        // checking in users
        DatabaseReference createdClassesRef = getDatabaseReference(USERS).child(getUser().getUid()).child(CREATED);
        createdClassesRef.keepSynced(true);
        createdClassesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clearing created class list
                createdClassListAdapter.clearList();

                // if there exist any created class
                if (dataSnapshot.exists()) {

                    // reading data from all created classes
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
                                // adding class to created class adapter
                                createdClassListAdapter.add(new ClassRoom(class_Id, class_name, attendance_Date));
                                Log.d(TAG, "(Created) ClassId: " + class_Id);

                                // notifying created class adapter about any changes
                                createdClassListAdapter.notifyDataSetChanged();
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

    // Finalized
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

    // Finalized
    public static void updateSessionTimeout(String classId, String attendanceTimeout, double longitude, double latitude) {
        getDatabaseReference(CLASSES).child(classId).child(SESSION).child(TIMEOUT).setValue(attendanceTimeout);
        getDatabaseReference(CLASSES).child(classId).child(SESSION).child(LONGITUDE).setValue(longitude);
        getDatabaseReference(CLASSES).child(classId).child(SESSION).child(LATITUDE).setValue(latitude);
    }

    // Finalized
    public static Query getAttendanceByAttendanceDate(String classId, String attendanceDate) {
        return getDatabaseReference(ATTENDANCES).child(classId).child(attendanceDate);
    }

    // Finalized
    public static void addAttendance(String classId, String attendanceDate, String attendance_id, String imei) {
        getDatabaseReference(ATTENDANCES).child(classId).child(attendanceDate).child(attendance_id).setValue(imei);
    }

    // Finalized
    public static void addJoinClass(final String classId, final String email, final String id) {

        // checking if user exists with email
        getDatabaseReference(USERS).orderByChild(EMAIL).equalTo(email.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot temp : dataSnapshot.getChildren()) {

                                // if email do not matches with teachers email
                                if (!email.equalsIgnoreCase(getUser().getEmail())) {

                                    // storing in joined list of class in student
                                    getUserByU_ID(temp.getKey()).child(JOINED).child(classId).child(ATTENDANCE_ID).setValue(id);

                                    // storing in enrolled list of class
                                    getClassByClassId(classId).child(ENROLLED).child(id).setValue(temp.getKey());
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    // Finalized
    public static Query getClassSessionByClassId(String classId) {
        return getClassByClassId(classId).child(SESSION);
    }

    // Finalized
    public static DatabaseReference getEnrolledStudentsByClassId(String class_id) {
        return getDatabaseReference(CLASSES).child(class_id).child(ENROLLED);
    }

    // Finalized
    public static void sendNotification(String uid, String msg, String dateTime, String className, String email) {

        // create a new notification object
        Map<String, String> newNotification = new HashMap<>();
        newNotification.put(MSG, msg);
        newNotification.put(DATE_TIME, dateTime);
        newNotification.put(CLASS_NAME, className);
        newNotification.put(EMAIL, email);

        // storing new notification in database
        getDatabaseReference(NOTIFICATIONS).child(uid).push().setValue(newNotification);
    }

    // Finalized
    public static void sendApplication(String u_id, String msg, String dateTime, String className, String attendanceId) {

        // create a new application object
        Map<String, String> newApplication = new HashMap<>();
        newApplication.put(MSG, msg);
        newApplication.put(DATE_TIME, dateTime);
        newApplication.put(CLASS_NAME, className);
        newApplication.put(ATTENDANCE_ID, attendanceId);

        // storing new notification in database
        getDatabaseReference(NOTIFICATIONS).child(u_id).push().setValue(newApplication);
    }

    public static void getNotifications(final NotificationListAdapter notificationListAdapter) {

        // fetching all notifications of this user
        DatabaseReference notificationRef = getDatabaseReference(NOTIFICATIONS).child(getUser().getUid());
        notificationRef.keepSynced(true);
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clearing previous list
                notificationListAdapter.clearList();

                // for creating new list
                ArrayList<Notification> notifications = new ArrayList<>();

                // fetching each notification
                for (DataSnapshot notification : dataSnapshot.getChildren()) {

                    // getting notification details
                    String className = String.valueOf(notification.child(CLASS_NAME).getValue());
                    String dateTime = String.valueOf(notification.child(DATE_TIME).getValue());
                    String msg = String.valueOf(notification.child(MSG).getValue());

                    // if notification is from student
                    String id;
                    if (notification.hasChild(ATTENDANCE_ID))
                        id = String.valueOf(notification.child(ATTENDANCE_ID).getValue());

                        // if notification is from teacher
                    else
                        id = String.valueOf(notification.child(EMAIL).getValue());

                    // adding notification to notification list adapter
                    notifications.add(new Notification(msg, dateTime, className, id));
                    Log.d(TAG, "Notification read: " + id);
                }

                // sorting notifications by date
                Collections.sort(notifications, new Notification.DateComparator());

                // listing all notifications
                for (Notification notification : notifications)
                    notificationListAdapter.add(notification);

                // notifying notification list adapter about any changes
                notificationListAdapter.notifyDataSetChanged();
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
//        DatabaseReference reference = FirebaseController.getInstance().getReference("users/" +
//                uid);
//
//        reference.keepSynced(true);
//        FirebaseController.getInstance().getReference("attendances").keepSynced(true);
//        FirebaseController.getInstance().getReference("attendances").keepSynced(true);
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
