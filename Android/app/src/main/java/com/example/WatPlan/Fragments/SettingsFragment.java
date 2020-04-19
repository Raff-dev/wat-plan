package com.example.WatPlan.Fragments;

import android.content.Intent;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Handlers.ConnectionHandler;
import com.example.WatPlan.Models.BlockFilter;
import com.example.WatPlan.Handlers.DBHandler;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.Models.Switch_;
import com.example.WatPlan.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.example.WatPlan.Models.BlockFilter.NO_FILTER;
import static com.example.WatPlan.Models.Preferences.*;

public class SettingsFragment extends Fragment {
    private MainActivity mainActivity;
    private UpdateHandler updateHandler;
    private DBHandler dbHandler;

    private View view;
    private Spinner semesterSpinner, subjectSpinner;
    private SearchableSpinner groupSpinner;
    private Button infoButton;
    private Switch pastPlanSwitch;

    private ArrayAdapter<String> groupAdapter, semesterAdapter, subjectAdapter;
    private ArrayList<String> groups = new ArrayList<>();
    private ArrayList<String> semesters = new ArrayList<>();
    private ArrayList<String> subjects = new ArrayList<>();
    private ArrayList<Switch_> switchArrayList = new ArrayList<>();

    private String[] switchNames = new String[]{LECTURE, EXERCISE, LABORATORY};
    private int[] switchIds = new int[]{R.id.lectureSwitch, R.id.exerciseSwitch, R.id.laboratorySwitch};

    private Map<String, Integer> colorMap = new Hashtable<>();


    public SettingsFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setUp(UpdateHandler updateHandler, DBHandler dbHandler) {
        this.updateHandler = updateHandler;
        this.dbHandler = dbHandler;

        semesters = updateHandler.getAvailableSemesters();
        groups = updateHandler.getAvailableGroups();

        int spinnerItem = R.layout.support_simple_spinner_dropdown_item;
        semesterAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, semesters);
        groupAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, groups);
        subjectAdapter = new ArrayAdapter<>(mainActivity, spinnerItem, subjects);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        enterAnimation();
        getViews();

        semesterSpinner.setAdapter(semesterAdapter);
        groupSpinner.setAdapter(groupAdapter);
        subjectSpinner.setAdapter(subjectAdapter);

        applyPreferences();
        addListeners();
        return view;
    }

    private void getViews() {
        for (int i = 0; i < switchIds.length; i++)
            switchArrayList.add(new Switch_(
                    view.findViewById(switchIds[i]),
                    switchNames[i],
                    BlockFilter.getFiltermap().get(switchNames[i])));
        semesterSpinner = view.findViewById(R.id.semesterSpinner);
        groupSpinner = view.findViewById(R.id.groupSpinner);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        pastPlanSwitch = view.findViewById(R.id.pastPlanSwitch);
        infoButton = view.findViewById(R.id.infoButton);
    }

    private void addListeners() {
        infoButton.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse(ConnectionHandler.getBaseUrl()+ "home")))
        );

        switchArrayList.forEach(switch_ ->
                switch_.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) ->
                        dbHandler.setPreference(switch_.getName(), preferenceValue(isChecked))
                ));

        pastPlanSwitch.setOnCheckedChangeListener(((buttonView, isChecked) ->
                dbHandler.setPreference(PAST_PLAN, preferenceValue(isChecked))
        ));

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String subjectName = subjectSpinner.getSelectedItem().toString();
                dbHandler.setPreference(SUBJECT, subjectName);
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
                System.out.println("SEMESTER SPINNER");
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

    private void applyPreferences() {
        //subject spinner
        String subject = dbHandler.getPreference(SUBJECT);
        for (int i = 0; i < subjects.size(); i++)
            if (subjects.get(i).equals(subject)) subjectSpinner.setSelection(i);

        //past plan switch
        if (dbHandler.getPreference(PAST_PLAN).equals(HIDDEN))
            pastPlanSwitch.setChecked(true);

        //lecture, ex, lab switches
        switchArrayList.forEach(switch_ -> {
            if (dbHandler.getPreference(switch_.getName()).equals(HIDDEN))
                switch_.getSwitch().setChecked(true);
        });

        //semester spinner
        String semester = dbHandler.getPreference(SEMESTER);
        for (int i = 0; i < semesters.size(); i++)
            if (semesters.get(i).equals(semester)) semesterSpinner.setSelection(i);

        //group spinner
        String group = dbHandler.getPreference(GROUP);
        for (int i = 0; i < groups.size(); i++)
            if (groups.get(i).equals(group)) groupSpinner.setSelection(i);
    }

    public void setUniqueValues(Map<String, Set<String>> uniqueValues) {
        subjects.clear();
        subjects.add(NO_FILTER);
        subjects.addAll(Objects.requireNonNull(uniqueValues.get("subject")));

        int size = subjects.size();
        for (int i = 0; i < subjects.size(); i++) {
            float hue = 210f / size * (i + 1) - 20f;
            float saturation = (float) (0.375 + 0.04 * (i % 3));
            float lightness = (float) (0.47 - 0.04 * (i % 3));
            colorMap.put(subjects.get(i), ColorUtils.HSLToColor(new float[]{hue, saturation, lightness}));
        }
    }

    public Map<String, Integer> getColorMap() {
        return colorMap;
    }

    private void enterAnimation() {
        Animation enter = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_settings_enter);
        enter.setDuration(500);
        view.startAnimation(enter);
    }

    public void exitAnimation() {
        Animation exit = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_settings_exit);
        exit.setDuration(500);
        view.startAnimation(exit);
    }
}