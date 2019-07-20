package com.ilumastech.smart_attendance_system.teacher_activities;

import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.firebase_database.FirebaseDatabase;
import com.ilumastech.smart_attendance_system.prompts.Prompt;
import com.ilumastech.smart_attendance_system.sas_utilities.SASConstants;
import com.ilumastech.smart_attendance_system.sas_utilities.SASTools;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateClassActivity extends AppCompatActivity {

    private static final String TAG = "CreateClassActivity";

    private TextView filename;
    private EditText classname;

    private Prompt prompt;

    private List<String> studentsIds, studentsEmails;
    private boolean fileSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        init();
    }

    private void init() {

        // setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        filename = findViewById(R.id.filename_tv);
        classname = findViewById(R.id.classname_tf);

        // creating prompt instance to display prompts to user
        prompt = new Prompt(this);

        // for checking if students list file has been selected
        fileSelected = false;
    }

    public void selectFile(View view) {

        // setting file picker dialog properties
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

                // to read students ID and email
                studentsIds = new ArrayList<>();
                studentsEmails = new ArrayList<>();

                try {

                    // opening csv file to read data
                    CSVReader reader = new CSVReader(new FileReader(files[0]));

                    // skipping header row
                    String[] columns = reader.readNext();

                    // reading data
                    while ((columns = reader.readNext()) != null) {

                        // if each row has student id and email
                        if (!columns[0].isEmpty() && !columns[1].isEmpty()) {
                            studentsIds.add(columns[0]);
                            studentsEmails.add(columns[1]);
                        }

                        // if their exist a row where a student id or email
                        else
                            throw new IOException("Some data is missing");
                    }

                    // displaying selected file name
                    filename.setText(files[0].substring(files[0].lastIndexOf("/") + 1));

                    // setting check to true as file has been selected
                    fileSelected = true;
                } catch (IOException e) {

                    // showing short prompt to user if file format is not correct or some data is missing
                    prompt.showFailureMessagePrompt("File is not in correct format of some data is missing.");
                    SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                        @Override
                        public void run() {
                            prompt.hidePrompt();
                        }
                    });
                }
            }
        });

        // displaying file picker dialog to user
        dialog.show();
    }

    public void createClass(View view) {

        // getting entered class name
        final String className = classname.getText().toString();

        // checking if entered class name is not empty
        if (className.isEmpty()) {
            classname.setError("Class name is required.");
            return;
        }

        // if file is selected by user
        if (fileSelected) {

            final String teacherId = FirebaseAuth.getInstance().getUid();

            // check if class with same name already exists
            FirebaseDatabase.getClassByU_ID(teacherId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // if this teacher has not created any class before
                            if (!dataSnapshot.exists())
                                addClass(className, teacherId);

                                // if this teacher has already created any class
                            else {

                                // checking for all classes created by current teacher uid
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    // if class with this name has been created before
                                    String alreadyClassName = (String) snapshot.child(FirebaseDatabase.CLASS_NAME).getValue();
                                    if (Objects.requireNonNull(alreadyClassName).equalsIgnoreCase(className)) {

                                        // showing prompt to teacher about class with same name already exists
                                        prompt.showFailureMessagePrompt("Class with same name already exists");
                                        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_LONG, new Runnable() {
                                            @Override
                                            public void run() {
                                                prompt.hidePrompt();
                                            }
                                        });
                                        return;
                                    }
                                }

                                // if no class exists already with the entered class name
                                addClass(className, teacherId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }

        // if user have not selected any file already
        else {

            // showing short prompt to user about selecting a file first
            prompt.showFailureMessagePrompt("Please select student list file to enrolled in class");
            SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
                @Override
                public void run() {
                    prompt.hidePrompt();
                }
            });
        }
    }

    private void addClass(String className, String teacherId) {

        final String classId = FirebaseDatabase.getUniqueID();
        Log.d(TAG, "Creating class classId: " + classId);

        // creating new class in database
        FirebaseDatabase.addNewClass(classId, className, teacherId);

        // updating created classes of user in database
        FirebaseDatabase.updateCreatedClassesByU_ID(teacherId, classId);

        // adding students in class
        for (int i = 0; i < studentsEmails.size(); i++)
            FirebaseDatabase.addJoinClass(classId, studentsEmails.get(i), studentsIds.get(i));

        // show short wait prompt to user that class has been created
        prompt.showSuccessMessagePrompt("Class has been created.");
        SASTools.wait(SASConstants.PROMPT_DISPLAY_WAIT_SHORT, new Runnable() {
            @Override
            public void run() {
                prompt.hidePrompt();
                CreateClassActivity.this.finish();
            }
        });

        Log.d(TAG, "Class created classId: " + classId);
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
