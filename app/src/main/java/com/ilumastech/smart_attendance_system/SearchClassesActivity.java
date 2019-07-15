package com.ilumastech.smart_attendance_system;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SearchClassesActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ClassArrayAdapter classArrayAdapter;
    private ListView listView;

    private Prompt prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_classes);

        init();
    }

    private void init() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.list_view);
        listView.addFooterView(new View(this));

        prompt = new Prompt(this);


        classArrayAdapter = new ClassArrayAdapter(this, R.layout.class_card);
//        showClasses();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
