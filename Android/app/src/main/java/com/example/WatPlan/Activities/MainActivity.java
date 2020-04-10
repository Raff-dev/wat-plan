package com.example.WatPlan.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;

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
    private Map<String, Color> colors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Animation expand = AnimationUtils.loadAnimation(this,R.anim.nav_button_expand);
        Animation contract = AnimationUtils.loadAnimation(this,R.anim.nav_button_contract);
        Animation rotate = AnimationUtils.loadAnimation(this,R.anim.settings_rotate);
        Animation switchButton = AnimationUtils.loadAnimation(this,R.anim.switch_nav_button);
        switchButton.setFillAfter(true);
        switchButton.setDuration(500);
        rotate.setDuration(500);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openFragment(scheduleFragment);
        Button search = findViewById(R.id.search);
        Button settings = findViewById(R.id.settings);
        search.setOnClickListener(v -> {
            openFragment(scheduleFragment);
//            search.startAnimation(switchButton);
//            settings.startAnimation(switchButton);

        });
        settings.setOnClickListener(v ->{
            openFragment(settingsFragment);
//            search.startAnimation(switchButton);
//            settings.startAnimation(switchButton);


        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, fragment).commit();
    }

    public void addValue(String type, String value) {
        System.out.println("ADDING VALUE "+type+" " + value);
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
