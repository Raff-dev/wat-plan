package com.example.WatPlan.Activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.R;

public class MainActivity extends AppCompatActivity {
    private Fragment scheduleFragment = new ScheduleFragment(this);
    private Fragment settingsFragment = new SettingsFragment(this);

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

    private void openFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, fragment).commit();
    }
}
