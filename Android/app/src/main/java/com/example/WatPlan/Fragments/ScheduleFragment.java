package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Adapters.WeekAdapter;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {
    public MainActivity mainActivity;
    private UpdateHandler updateHandler;
    private ArrayList<Week> plan = new ArrayList<>();
    private WeekAdapter weekAdapter;
    private RecyclerView planRecyclerView;
    private TextView semesterNameTextView, groupNameTextView;
    private View view;
    private boolean loading = true;

    public ScheduleFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        weekAdapter = new WeekAdapter(mainActivity, plan);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_schedule, container, false);
        enter();
        getViews();
        switchLoading(this.loading);

        if (plan.size() == 0) {
            updateHandler = mainActivity.getUpdateHandler();
            updateHandler.setDefaultGroup();
        }


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

    public void clearPlan() {
        this.plan.clear();
        switchLoading(true);
    }

    public void setPlan(ArrayList<Week> plan) {
        this.plan.addAll(plan);
        switchLoading(false);
        System.out.println("FINISHED APPLYTING PLAN");
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

    WeekAdapter getWeekAdapter() {
        return weekAdapter;
    }

    public void displayFailureMessage() {

    }

    private void enter() {
        Animation enter = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_schedule_enter);
        enter.setDuration(500);
        this.view.startAnimation(enter);
    }

    public void exit() {
        Animation exit = AnimationUtils.loadAnimation(mainActivity, R.anim.fragment_schedule_exit);
        exit.setDuration(500);
        view.startAnimation(exit);
    }
}
