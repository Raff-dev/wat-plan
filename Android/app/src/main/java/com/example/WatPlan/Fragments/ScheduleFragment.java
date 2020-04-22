package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Adapters.WeekAdapter;
import com.example.WatPlan.Handlers.DBHandler;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.Models.BlockFilter;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.WatPlan.Models.BlockFilter.NO_FILTER;
import static com.example.WatPlan.Models.Preferences.*;

public class ScheduleFragment extends Fragment {
    private MainActivity mainActivity;
    private DBHandler dbHandler;
    private UpdateHandler updateHandler;
    private ArrayList<Week> plan = new ArrayList<>();
    private WeekAdapter weekAdapter;
    private RecyclerView planRecyclerView;
    private TextView semesterNameTextView, groupNameTextView;
    private View view;
    private BlockFilter subjectBlockFilter;
    private boolean loading = true;

    public ScheduleFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        weekAdapter = new WeekAdapter(mainActivity, plan);
    }

    public void setUp(UpdateHandler updateHandler, DBHandler dbHandler) {
        this.updateHandler = updateHandler;
        this.dbHandler = dbHandler;
        this.updateHandler.setDefaultGroup();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_schedule, container, false);
        enterAnimation();
        getViews();
        switchLoading(loading);
        applyFilters();

        planRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        planRecyclerView.setAdapter(weekAdapter);
        setNames(updateHandler.getActiveSemester(), updateHandler.getActiveGroup());
        return view;
    }

    private void getViews() {
        planRecyclerView = view.findViewById(R.id.planRecyclerView);
        groupNameTextView = view.findViewById(R.id.groupTextView);
        semesterNameTextView = view.findViewById(R.id.semesterTextView);
    }

    private void applyFilters() {
        setPastPlanFilter();
        setClassTypeFilters();
        setClassFilter();
        weekAdapter.notifyDataSetChanged();
    }

    private void setClassTypeFilters() {
        for (String classType : classTypes) {
            boolean apply = dbHandler.getPreference(classType).equals(HIDDEN);
            BlockFilter blockFilter = BlockFilter.getFiltermap().get(classType);
            weekAdapter.switchBlockFilter(blockFilter, apply);
        }
    }

    private void setClassFilter() {
        String subjectName = dbHandler.getPreference(SUBJECT);
        weekAdapter.switchBlockFilter(subjectBlockFilter, false);
        if (subjectName.equals(NO_FILTER)) subjectBlockFilter = block -> true;
        else subjectBlockFilter = block -> block.getSubject().equals(subjectName);
        weekAdapter.switchBlockFilter(subjectBlockFilter, true);
    }

    private void setPastPlanFilter() {
        System.out.println("TOGGLE PLAN");
        int startWeekPosition = 0;
        if (dbHandler.getPreference(PAST_PLAN).equals(HIDDEN))
            startWeekPosition = getCurrentWeekPosition();
        weekAdapter.setStartingWeekPosition(startWeekPosition);
    }

    private int getCurrentWeekPosition() {
        try {
            LocalDate today = LocalDate.now();
            String semester = dbHandler.getPreference(SEMESTER);
            String group = dbHandler.getPreference(GROUP);
            String firstDay = dbHandler.getBorderDates(semester, group).first;
            int daysCount = 0;
            while (!Objects.requireNonNull(firstDay).equals(today.toString())) {
                daysCount += 1;
                today = today.minusDays(1);
            }
            return daysCount / 7;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void clearPlan() {
        this.plan.clear();
        switchLoading(true);
    }

    public void setPlan(ArrayList<Week> plan) {
        this.plan.addAll(plan);
        switchLoading(false);
        System.out.println("FINISHED APPLYTING PLAN");
        if (this.isVisible()) applyFilters();
    }

    private void switchLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            view.findViewById(R.id.planRecyclerView).setVisibility(View.GONE);
            view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.progressBar).setVisibility(View.GONE);
            view.findViewById(R.id.planRecyclerView).setVisibility(View.VISIBLE);
        }
    }

    public void setNames(String semesterName, String groupName) {
        this.semesterNameTextView.setText(semesterName);
        this.groupNameTextView.setText(groupName);
    }

    public void displayFailureMessage() {
        System.out.println("DISPLAY FAILURE MESSAHGE");
        TextView messageTextView = view.findViewById(R.id.messageTextView);
        messageTextView.setText(mainActivity.getString(R.string.connection_failure));
        View connectionFailureLayout = view.findViewById(R.id.connectionFailureLayout);
        Button tryAgainButton = view.findViewById(R.id.tryAgainButton);

        View progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        connectionFailureLayout.setVisibility(View.VISIBLE);
        tryAgainButton.setOnClickListener(v -> {
            String group = dbHandler.getPreference(GROUP);
            String semester = dbHandler.getPreference(SEMESTER);
            connectionFailureLayout.setVisibility(View.GONE);
            updateHandler.changeGroup(semester, group);
        });
    }

    private void enterAnimation() {
        Animation enter = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_schedule_enter);
        enter.setDuration(500);
        this.view.startAnimation(enter);
    }

    public void exitAnimation() {
        Animation exit = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_schedule_exit);
        exit.setDuration(500);
        view.startAnimation(exit);
    }
}
