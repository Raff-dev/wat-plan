package com.example.WatPlan.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private ScheduleFragment scheduleFragment = new ScheduleFragment(this);
    private SettingsFragment settingsFragment = new SettingsFragment(this);
    private UpdateHandler updateHandler = new UpdateHandler(this, scheduleFragment);
    private Map<String, Vector<String>> values = new HashMap<>();
    private Map<String, Color> colors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openFragment(scheduleFragment);
        Button search = findViewById(R.id.search);
        Button settings = findViewById(R.id.settings);
        search.setOnClickListener(v -> openFragment(scheduleFragment));
        settings.setOnClickListener(v -> openFragment(settingsFragment));
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, fragment).commit();
    }

    public void addValue(String type, String value) {
        if (value == null) return;
        if (values.containsKey(type)) Objects.requireNonNull(values.get(type)).add(value);
        else {
            Vector<String> vector = new Vector<>(Collections.singletonList(value));
            values.put(type, vector);
        }
    }

    public void clearValues() {
        values.clear();
    }
    public Map<String, Vector<String>> getValues() {
        return values;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public ScheduleFragment getScheduleFragment() {
        return scheduleFragment;
    }

    public void setUpSpinner(ArrayAdapter<String> adapter, ArrayList<String> list){


    }

}
