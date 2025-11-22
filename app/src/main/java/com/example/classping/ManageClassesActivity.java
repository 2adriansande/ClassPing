package com.example.classping;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ManageClassesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        Toolbar toolbar = findViewById(R.id.toolbarManageClasses);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Classes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
