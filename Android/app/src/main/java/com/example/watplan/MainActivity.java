package com.example.watplan;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watplan.Adapters.WeekAdapter;
import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;

import java.util.ArrayList;
import java.util.stream.IntStream;


public class MainActivity extends AppCompatActivity {
    private ArrayList<Week> plan = new ArrayList<>();
    private RecyclerView.Adapter weekAdapter = new WeekAdapter(this, plan);
    private UpdateManager updateManager = new UpdateManager(this, this);
    private static RecyclerView planRecyclerView;
    private Context context;
    private Button burger, settings, search;
    private TextView semesterName, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();
        addListeners();
    }

    private void setUp() {
        planRecyclerView = findViewById(R.id.planRecyclerView);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testData();
        planRecyclerView.setAdapter(weekAdapter);

        burger = findViewById(R.id.burger);
        search = findViewById(R.id.search);
        settings = findViewById(R.id.settings);
        context = getApplicationContext();

        groupName = findViewById(R.id.groupTextView);
        semesterName = findViewById(R.id.semesterTextView);

    }

    private void addListeners() {
        search.setOnClickListener(v -> {
            new Thread(() ->
                    updateManager.changeGroup("letni", "WCY18IY5S1")
            ).start();

        });
        burger.setOnClickListener(v -> {
            new Thread(() ->
                    updateManager.changeGroup("letni", "WCY18ZZ1S1")
            ).start();
        });
        settings.setOnClickListener(v -> {
            new Thread(() ->
                    updateManager.changeGroup("letni", "WCY18IY3S1")
            ).start();
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
        planRecyclerView.setAdapter(weekAdapter);
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
