package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Adapters.WeekAdapter;
import com.example.WatPlan.Handlers.DBHandler;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class ScheduleFragment extends Fragment {
    private MainActivity mainActivity;
    private DBHandler dbHandler;
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

    public void setHandlers(UpdateHandler updateHandler, DBHandler dbHandler) {
        this.updateHandler = updateHandler;
        this.updateHandler.setDefaultGroup();
        this.dbHandler = dbHandler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_schedule, container, false);
        enterAnimation();
        getViews();
        switchLoading(this.loading);


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

    public void togglePastPlan() {
        System.out.println("TOGGLE PLAN");
        int startWeekPosition = 0;
        if (dbHandler.getPreference("hide_past_plan").equals("true"))
            startWeekPosition = getCurrentWeekPosition();
        weekAdapter.setStartingWeekPosition(startWeekPosition);
        weekAdapter.notifyDataSetChanged();
    }

    public int getCurrentWeekPosition() {
        try {
            LocalDate today = LocalDate.now();
            String semester = dbHandler.getPreference("semester");
            String group = dbHandler.getPreference("group");
            String firstDay = dbHandler.getBorderDates(semester, group).first;
            int daysCount = 0;
            while (!firstDay.equals(today.toString())) {
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
        TextView messageTextView = view.findViewById(R.id.messageTextView);
        messageTextView.setText(mainActivity.getString(R.string.connection_failure));
        View connectionFailureLayout = view.findViewById(R.id.connectionFailureLayout);
        Button tryAgainButtton = view.findViewById(R.id.tryAgainButton);

        connectionFailureLayout.setVisibility(View.VISIBLE);
        tryAgainButtton.setOnClickListener(v -> {
            String group = dbHandler.getPreference("group");
            String semester = dbHandler.getPreference("semester");
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
