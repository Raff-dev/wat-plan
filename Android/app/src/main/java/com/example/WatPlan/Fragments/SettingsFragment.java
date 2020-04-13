package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Objects;
import java.util.Set;

public class SettingsFragment extends Fragment {
    private MainActivity mainActivity;
    private UpdateHandler updateHandler;
    private View view;
    private Spinner semesterSpinner, subjectSpinner;
    private SearchableSpinner groupSpinner;

    private WeekAdapter weekAdapter;
    private ArrayAdapter<String> groupAdapter, smesterAdapter, subjectAdapter;
    private ArrayList<String> groups, semesters, subjects = new ArrayList<>();

    private Map<Switch, BlockFilter> switchMap = new HashMap<>();
    private int[] switchIds = new int[]{R.id.lectureSwitch, R.id.exerciseSwitch, R.id.laboratorySwitch};
    private BlockFilter subjectBlockFilter;
    private BlockFilter[] switchFilters = new BlockFilter[]{
            block -> !block.getClassType().equals("w"),
            block -> !block.getClassType().equals("ć"),
            block -> !block.getClassType().equals("L")
    };
    private String NO_FILTER = "---";

    public SettingsFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        enter();
        getViews();

        updateHandler = mainActivity.getUpdateHandler();
        semesters = updateHandler.getAvailableSemesters();
        groups = updateHandler.getAvailableGroups();

        int spinnerItem = R.layout.support_simple_spinner_dropdown_item;
        weekAdapter = mainActivity.getScheduleFragment().getWeekAdapter();
        smesterAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, semesters);
        groupAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, groups);
        subjectAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, subjects);

        semesterSpinner.setAdapter(smesterAdapter);
        groupSpinner.setAdapter(groupAdapter);
        subjectSpinner.setAdapter(subjectAdapter);

        addListeners();
        return this.view;
    }

    private void getViews() {
        for (int i = 0; i < switchIds.length; i++)
            switchMap.put(view.findViewById(switchIds[i]), switchFilters[i]);
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        groupSpinner = view.findViewById(R.id.groupSpinner);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
    }

    private void addListeners() {
        switchMap.forEach((switch_, filter) -> switch_.setOnCheckedChangeListener(
                (buttonView, isChecked) -> weekAdapter.switchBlockFilter(filter, isChecked))
        );

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String subjectFilter = subjectSpinner.getSelectedItem().toString();
                WeekAdapter weekAdapter = mainActivity.getScheduleFragment().getWeekAdapter();
                weekAdapter.switchBlockFilter(subjectBlockFilter, false);
                if (subjectFilter.equals(NO_FILTER)) subjectBlockFilter = block -> true;
                else subjectBlockFilter = block -> block.getSubject().equals(subjectFilter);
                weekAdapter.switchBlockFilter(subjectBlockFilter, true);
                weekAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String semester = semesterSpinner.getSelectedItem().toString();
                if (semester.equals(updateHandler.getActiveSemester())) return;
                updateHandler.setActiveSemester(semester);
                groups = updateHandler.getAvailableGroups();
                groupAdapter.notifyDataSetChanged();

                String group = updateHandler.getActiveGroup();
                updateHandler.changeGroup(semester, group);
                groupSpinner.setSelection(0);
                subjectSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String group = groupSpinner.getSelectedItem().toString();
                if (group.equals(updateHandler.getActiveGroup())) return;
                String semester = semesterSpinner.getSelectedItem().toString();
                updateHandler.changeGroup(semester, group);
                subjectSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setFilters(Map<String, Set<String>> uniqueValues) {
        subjects.clear();
        subjects.add(NO_FILTER);
        subjects.addAll(Objects.requireNonNull(uniqueValues.get("subject")));
    }

    private void enter() {
        Animation enter = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_settings_enter);
        enter.setDuration(500);
        view.startAnimation(enter);
    }

    public void exit() {
        Animation exit = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_settings_exit);
        exit.setDuration(500);
        view.startAnimation(exit);
    }


}