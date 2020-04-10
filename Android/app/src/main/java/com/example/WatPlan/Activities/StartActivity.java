package com.example.WatPlan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.WatPlan.Handlers.ConnectionHandler;
import com.example.WatPlan.Handlers.DBHandler;
import com.example.WatPlan.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class StartActivity extends AppCompatActivity {
    private DBHandler dbHandler;
    private SearchableSpinner groupSpinner;
    private Spinner semesterSpinner;
    private Button getStartedButton, tryAgainButton;
    private ConstraintLayout failureLayout, getStartedLayout;
    private TextView messageTextView;

    private ArrayList<String> semesters = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();
    private Map<String, Map<String, String>> versions;
    private ArrayAdapter<String> semesterAdapter,groupAdapter;
    private String activeGroup, activeSemester;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViews();
        setUp();
        if (dbHandler.isEmpty()) getStarted();
        else startActivity(new Intent(this, MainActivity.class));
    }

    private void setUp() {
        dbHandler = new DBHandler(this);
//        dbHandler.onUpgrade(dbHandler.getWritableDatabase(), 1, 1);

        int spinnerItem = R.layout.support_simple_spinner_dropdown_item;
        semesterAdapter = new ArrayAdapter<>(this, spinnerItem, semesters);
        groupAdapter = new ArrayAdapter<>(this, spinnerItem, groups);
        semesterSpinner.setAdapter(semesterAdapter);
        groupSpinner.setAdapter(groupAdapter);
        groupSpinner.setTitle("Choose Group");
        groupSpinner.setPositiveButton("OK");
        messageTextView.setText(getString(R.string.loading));
    }

    private void getStarted() {
        new Thread(() -> {
            try {
                dbHandler.onUpgrade(dbHandler.getWritableDatabase(), 1, 1);
                versions = ConnectionHandler.getVersionMap();
                semesters.addAll(versions.keySet());
                semesterAdapter.notifyDataSetChanged();
                addListeners();
                runOnUiThread(this::setLoaded);
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                runOnUiThread(this::connectionFailed);
            }
        }).start();
    }

    private void addListeners() {
        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeSemester = semesterSpinner.getSelectedItem().toString();
                dbHandler.setActiveSemester(activeSemester);
                groups.addAll(Objects.requireNonNull(versions.get(activeSemester)).keySet());
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeGroup = groupSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getStartedButton.setOnClickListener(v -> {
            activeSemester = semesterSpinner.getSelectedItem().toString();
            activeGroup = groupSpinner.getSelectedItem().toString();
            dbHandler.setActiveSemester(activeSemester);
            dbHandler.setActiveGroup(activeGroup);
            dbHandler.initialInsert(versions);
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    private void connectionFailed() {
        setFailure();
        tryAgainButton.setOnClickListener(v -> {
            setLoading();
            getStarted();
        });
    }

    private void setLoading() {
        failureLayout.setVisibility(View.GONE);
        messageTextView.setText(getString(R.string.loading));
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setLoaded() {
        progressBar.setVisibility(View.GONE);
        failureLayout.setVisibility(View.GONE);
        messageTextView.setText(null);
        getStartedLayout.setVisibility(View.VISIBLE);
    }

    private void setFailure() {
        progressBar.setVisibility(View.GONE);
        failureLayout.setVisibility(View.VISIBLE);
        messageTextView.setText(getString(R.string.connection_failure));
    }

    private void findViews() {
        messageTextView = findViewById(R.id.messageTextView);
        progressBar = findViewById(R.id.progressBar);
        failureLayout = findViewById(R.id.failureLayout);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        getStartedLayout = findViewById(R.id.getStartedLayout);
        semesterSpinner = findViewById(R.id.semesterSpinner);
        getStartedButton = findViewById(R.id.getStartedButton);
        groupSpinner = findViewById(R.id.groupSpinner);
    }
}
