package com.example.WatPlan.Activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Adapters.WeekAdapter;
import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Handlers.UpdateHandler;
import com.example.WatPlan.Models.Block;
import com.example.WatPlan.Models.Day;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class save extends AppCompatActivity {
    private ArrayList<Week> plan = new ArrayList<>();
    private RecyclerView.Adapter weekAdapter = new WeekAdapter(this, plan);
//    private UpdateHandler updateHandler = new UpdateHandler(this, new ScheduleFragment()));
    private static RecyclerView planRecyclerView;
    private Context context;
    private Button settings, search;
    private TextView semesterName, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Fragment scheduleFragment = new ScheduleFragment(this);
//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.fragment_container,scheduleFragment).commit();

        setUp();
        addListeners();
//        updateHandler.setDefaultgroup();
    }

    private void setUp() {
//        planRecyclerView = findViewById(R.id.planRecyclerView);
//        planRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        testData();
//        planRecyclerView.setAdapter(weekAdapter);

        search = findViewById(R.id.search);
        settings = findViewById(R.id.settings);
        context = getApplicationContext();

        groupName = findViewById(R.id.groupTextView);
        semesterName = findViewById(R.id.semesterTextView);

    }

    private void addListeners() {
        search.setOnClickListener(v -> {


        });

        settings.setOnClickListener(v -> {
//            new Thread(() ->
//                    updateHandler.changeGroup("letni", "WCY18ZZ1S1")
//            ).start();

        });
    }

    private void testData() {
        ArrayList<Week> plan = new ArrayList<>();
        IntStream.range(0, 7).forEach(i -> {
            ArrayList<Day> week = new ArrayList<>();
            IntStream.range(0, 7).forEach(j -> {
                ArrayList<Block> day = new ArrayList<>();
                IntStream.range(0, 7).forEach(k -> {
                    day.add(new Block());
                });
                week.add(new Day(day, ""));
            });
            plan.add(new Week(week));
        });
        setPlan(plan);
    }

    public void setPlan(ArrayList<Week> plan) {
        this.plan.clear();
        this.plan.addAll(plan);
        weekAdapter.notifyDataSetChanged();
//        planRecyclerView.setAdapter(weekAdapter);
        System.out.println("FINISHED APPLYTING PLAN");
    }


    public Context getContext() {
        return context;
    }

    public void setNames(String semesterName, String groupName) {
        this.semesterName.setText(semesterName);
        this.groupName.setText(groupName);
    }
}
