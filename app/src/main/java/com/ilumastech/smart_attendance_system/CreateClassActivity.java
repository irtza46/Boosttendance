package com.ilumastech.smart_attendance_system;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateClassActivity extends AppCompatActivity {

    private static final String TAG = "CreateClassActivity";

    private TextView filename;
    private EditText classname;

    private Prompt prompt;

    private boolean fileOK;

    private List<String> studentsIds, studentsEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        prompt = new Prompt(this);

        filename = findViewById(R.id.filename_tv);
        classname = findViewById(R.id.classname_tf);

        fileOK = false;
    }

    public void createClass(View view) {

        final String className = classname.getText().toString();

        if (TextUtils.isEmpty(className)) {
            classname.setError("Required");
            return;
        }

        if (fileOK) {

            final String teacherId = FirebaseAuth.getInstance().getUid();

            // check if class with same name already exists
            FirebaseDatabase.getInstance().getReference("classes")
                    .orderByChild("teacherId").equalTo(teacherId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // if this teacher has not created any class before
                            if (!dataSnapshot.exists()) {

                                addClass(teacherId);
                            } else {

                                // checking for all classes created by current teacher uid
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    // if class with this name has not been created before
                                    if (!snapshot.exists() ||
                                            !String.valueOf(snapshot.child("className").getValue())
                                                    .equalsIgnoreCase(className)) {

                                        addClass(teacherId);
                                    } else {
                                        prompt.showFailureMessagePrompt("Class with same name already exists.");
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        } else {
            prompt.showFailureMessagePrompt("Please select student list file first.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            }, 3000);
        }
    }

    private void addClass(String teacherId) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String classId = reference.push().getKey();
        Log.d(TAG, "Creating class classId: " + classId);

        if (classId != null) {
            final Map<String, String> newClass = new HashMap<>();
            newClass.put("className", classname.getText().toString());
            newClass.put("teacherId", Objects.requireNonNull(
                    teacherId));

            reference.child("classes/" + classId).setValue(newClass);
            reference.child("attendances/" + classId).setValue("");

            // removing teacher id after storing
            newClass.remove("teacherId");

            reference.child("users/" + teacherId + "/classes/created/" + classId)
                    .setValue(newClass);

            // adding students
            for (int i = 0; i < studentsEmails.size(); i++) {

                final String email = studentsEmails.get(i);
                final String attendanceId = studentsIds.get(i);

                reference.child("users").orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                        String uid = snapshot.getKey();

                                        newClass.put("attendanceId", attendanceId);
                                        reference.child("users/" + uid + "/classes/joined")
                                                .child(classId).setValue(newClass);

                                        registerToTopic(uid);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }

            prompt.showSuccessMessagePrompt("Class has been created.");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                    CreateClassActivity.this.finish();
                }
            }, 3000);
        }

        Log.d(TAG, "Class created classId: " + classId);
    }

    private void registerToTopic(String uid) {
        //TODO
    }

    public void selectFile(View view) {

        DialogProperties properties = new DialogProperties();
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"csv"};

        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Select student file (csv)");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {

                studentsIds = new ArrayList<>();
                studentsEmails = new ArrayList<>();

                try {
                    CSVReader reader = new CSVReader(new FileReader(files[0]));

                    String[] columns = reader.readNext();
                    while ((columns = reader.readNext()) != null) {
                        studentsIds.add(columns[0]);
                        studentsEmails.add(columns[1]);
                    }

                    filename.setText(files[0].substring(files[0].lastIndexOf("/") + 1));
                    fileOK = true;
                } catch (IOException e) {
                    prompt.showFailureMessagePrompt(
                            "File is not supported or not in correct format!");
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            prompt.hidePrompt();
                        }
                    }, 3000);

                }
            }
        });
        dialog.show();
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

}
