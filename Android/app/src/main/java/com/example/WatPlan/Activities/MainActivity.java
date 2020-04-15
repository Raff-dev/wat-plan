package com.example.WatPlan.Activities;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.Handlers.DBHandler;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.R;

public class MainActivity extends AppCompatActivity {
    private ScheduleFragment scheduleFragment = new ScheduleFragment(this);
    private SettingsFragment settingsFragment = new SettingsFragment(this);
    private UpdateHandler updateHandler;
    private DBHandler dbHandler;
    private Animation hideButton, showButton, rotateSettings;
    private FrameLayout scheduleButtonFrame, settingsButtonFrame;
    private Button schedule, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHandler = new DBHandler(this);
        updateHandler = new UpdateHandler(this, dbHandler);
        scheduleFragment.setHandlers(updateHandler, dbHandler);
        settingsFragment.setHandlers(updateHandler, dbHandler);

        setAnimations();
        findViews();
        hideButton(scheduleButtonFrame);
        openFragment(scheduleFragment);
        setListeners();
    }

    private void setListeners() {
        settings.setOnClickListener(v -> {
            System.out.println("settings click");
            hideButton(settingsButtonFrame);
            showButton(scheduleButtonFrame);
            settings.startAnimation(rotateSettings);
            scheduleFragment.exitAnimation();
            openFragment(settingsFragment);
        });
        schedule.setOnClickListener(v -> {
            hideButton(scheduleButtonFrame);
            showButton(settingsButtonFrame);
            settings.startAnimation(rotateSettings);
            settingsFragment.exitAnimation();
            openFragment(scheduleFragment);
        });
    }


    private void setAnimations() {
        hideButton = AnimationUtils.loadAnimation(this, R.anim.hide_nav_button);
        showButton = AnimationUtils.loadAnimation(this, R.anim.show_nav_button);
        rotateSettings = AnimationUtils.loadAnimation(this, R.anim.settings_rotate);
        hideButton.setFillAfter(true);
        showButton.setFillAfter(true);
        rotateSettings.setDuration(1000);
        hideButton.setDuration(500);
        showButton.setDuration(500);
    }

    private void hideButton(FrameLayout frameLayout) {
        frameLayout.setTranslationZ(0);
        frameLayout.startAnimation(hideButton);
    }

    private void showButton(FrameLayout frameLayout) {
        frameLayout.setTranslationZ(1);
        frameLayout.startAnimation(showButton);
    }

    private void findViews() {
        scheduleButtonFrame = findViewById(R.id.search_frame);
        settingsButtonFrame = findViewById(R.id.settings_frame);
        schedule = findViewById(R.id.search);
        settings = findViewById(R.id.settings);
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, fragment).commit();
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
}
