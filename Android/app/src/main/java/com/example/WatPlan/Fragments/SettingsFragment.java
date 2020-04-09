package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Adapters.BlockFilter;
import com.example.WatPlan.Adapters.WeekAdapter;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SettingsFragment extends Fragment {
    private int spinnerItem = R.layout.support_simple_spinner_dropdown_item;
    private boolean ready = false;
    private UpdateHandler updateHandler;
    private MainActivity mainActivity;
    private Button pullDataButton;
    private ArrayAdapter<String> groupAdapter, smesterAdapter;
    private ArrayList<String> groups, semesters;
    private Spinner semesterSpinner;
    private SearchableSpinner groupSpinner;
    private Map<String, Vector<String>> values;
    private WeekAdapter weekAdapter;

    private Map<Switch, BlockFilter> switchMap = new HashMap<>();
    private int[] switchIds = new int[]{R.id.lectureSwitch, R.id.exerciseSwitch, R.id.laboratorySwitch};
    private BlockFilter[] switchFilters = new BlockFilter[]{
            block -> !block.getClassType().equals("w"),
            block -> !block.getClassType().equals("ć"),
            block -> !block.getClassType().equals("L")
    };

    public SettingsFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        weekAdapter = mainActivity.getScheduleFragment().getWeekAdapter();
        updateHandler = mainActivity.getUpdateHandler();
        semesters = updateHandler.getAvaliableSemesters();
        groups = updateHandler.getAvailableGroups();
        for (int i = 0; i < switchIds.length; i++)
            switchMap.put(view.findViewById(switchIds[i]), switchFilters[i]);
        pullDataButton = view.findViewById(R.id.pullDataButton);
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        groupSpinner = view.findViewById(R.id.groupSpinner);
        if (!ready) {
            ready = true;
            smesterAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, semesters);
            groupAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, groups);
        }
        semesterSpinner.setAdapter(smesterAdapter);
        groupSpinner.setAdapter(groupAdapter);
        addListeners();
        return view;
    }

    private void addListeners() {
        switchMap.forEach((switch_, filter) -> switch_.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    weekAdapter.switchBlockFilter(filter, isChecked);
                    weekAdapter.notifyDataSetChanged();
                })
        );
        pullDataButton.setOnClickListener(v -> {
            Toast.makeText(mainActivity, "TOASTY", Toast.LENGTH_LONG).show();
        });

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String group = groupSpinner.getSelectedItem().toString();
                String semester = updateHandler.getActiveSemester();
                updateHandler.changeGroup(semester, group);
                values = mainActivity.getValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String semester = semesterSpinner.getSelectedItem().toString();
                updateHandler.setActiveSemester(semester);
                String group = updateHandler.getActiveGroup();
                groups = updateHandler.getAvailableGroups();
                groupAdapter.notifyDataSetChanged();
                updateHandler.changeGroup(semester, group);
                values = mainActivity.getValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
