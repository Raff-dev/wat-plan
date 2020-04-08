package com.example.WatPlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ArrayList<Week> plan = new ArrayList<>();
    private RecyclerView.Adapter weekAdapter = new WeekAdapter(getContext(), plan);
    private UpdateHandler updateHandler;
    private TextView semesterName, groupName;
    private View view;
    public MainActivity mainActivity;

    public ScheduleFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        updateHandler = new UpdateHandler(mainActivity, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("WIDOK JEST KREOWANY");
        this.view = inflater.inflate(R.layout.fragment_schedule, container, false);
        RecyclerView planRecyclerView = view.findViewById(R.id.planRecyclerView);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        planRecyclerView.setAdapter(weekAdapter);
        groupName = view.findViewById(R.id.groupTextView);
        semesterName = view.findViewById(R.id.semesterTextView);
        updateHandler.setDefaultGroup();
        System.out.println("DOMYSLNA GRUPA JEST USTAWIANA");
        return view;
    }

    public void setPlan(ArrayList<Week> plan) {
        this.plan.clear();
        this.plan.addAll(plan);
        weekAdapter.notifyDataSetChanged();
        System.out.println("FINISHED APPLYTING PLAN");
    }

    public void setNames(String semesterName, String groupName) {
        this.semesterName.setText(semesterName);
        this.groupName.setText(groupName);
    }

}
