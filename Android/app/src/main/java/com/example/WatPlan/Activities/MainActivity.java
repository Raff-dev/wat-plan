package com.example.WatPlan.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ScheduleFragment scheduleFragment = new ScheduleFragment(this);
    private SettingsFragment settingsFragment = new SettingsFragment(this);
    private UpdateHandler updateHandler = new UpdateHandler(this);
    private Map<String, Set<String>> values = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation hideButton = AnimationUtils.loadAnimation(this, R.anim.hide_nav_button);
        Animation showButton = AnimationUtils.loadAnimation(this, R.anim.show_nav_button);
        Animation rotateSettings = AnimationUtils.loadAnimation(this, R.anim.settings_rotate);
        rotateSettings.setDuration(1000);
        hideButton.setFillAfter(true);
        showButton.setFillAfter(true);
//        switchButton.setFillEnabled(true);
        hideButton.setDuration(500);
        showButton.setDuration(500);

        FrameLayout search_frame = findViewById(R.id.search_frame);
        FrameLayout settings_frame = findViewById(R.id.settings_frame);

        Button search = findViewById(R.id.search);
        search_frame.startAnimation(hideButton);
        Button settings = findViewById(R.id.settings);
        search_frame.setTranslationZ(0);
        settings_frame.setTranslationZ(1);
        openFragment(scheduleFragment);

        settings.setOnClickListener(v -> {
            search_frame.setTranslationZ(1);
            settings_frame.setTranslationZ(0);
            search_frame.startAnimation(showButton);
            settings_frame.startAnimation(hideButton);
            settings.startAnimation(rotateSettings);
            scheduleFragment.exit();
            openFragment(settingsFragment);
        });
        search.setOnClickListener(v -> {
            search_frame.setTranslationZ(0);
            settings_frame.setTranslationZ(1);
            settings.startAnimation(rotateSettings);
            settings_frame.startAnimation(showButton);
            search_frame.startAnimation(hideButton);
            settingsFragment.exit();
            openFragment(scheduleFragment);
        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, fragment).commit();
    }

    public void addValue(String type, String value) {
        System.out.println("ADDING VALUE " + type + " " + value);
        if (value == null) return;
        if (values.containsKey(type)) Objects.requireNonNull(values.get(type)).add(value);
        else {
            Set<String> set = new HashSet<>();
            set.add(value);
            values.put(type, set);
        }
        System.out.println(Arrays.toString(Objects.requireNonNull(values.get(type)).toArray()));
    }

    public void clearValues() {
        values.clear();
    }

    public Map<String, Set<String>> getValues() {
        return values;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public ScheduleFragment getScheduleFragment() {
        return scheduleFragment;
    }

    public SettingsFragment getSettingsFragment() {
        return settingsFragment;
    }

    public void setUpSpinner(ArrayAdapter<String> adapter, ArrayList<String> list) {

    }

}
