package com.example.watplan;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watplan.Adapters.WeekAdapter;
import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;


public class MainActivity extends AppCompatActivity {
    private ArrayList<Week> plan = new ArrayList<>();
    private RecyclerView.Adapter weekAdapter = new WeekAdapter(
            this, plan);
    private UpdateManager updateManager = new UpdateManager(this,this);
    private static RecyclerView planRecyclerView;
    private Context context;
    private Button burger, settings, drop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();
        addListeners();
//        ArrayList<Week> plan = dbHandler.getPlan(getActiveGroup(), getActivetSemester());
//        displayGroup(plan);
    }

    private void setUp() {
        planRecyclerView = findViewById(R.id.planRecyclerView);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testData();
        planRecyclerView.setAdapter(weekAdapter);

        burger = findViewById(R.id.burger);
        drop = findViewById(R.id.drop);
        settings = findViewById(R.id.settings);
        context = getApplicationContext();
    }

    private void addListeners() {
        drop.setOnClickListener(v -> {
            updateManager.changeGroup("letni","WCY18IY5S1");

        });

        burger.setOnClickListener(v -> {
            DBHandler dbHandler = new DBHandler(this,updateManager);
            dbHandler.onUpgrade(dbHandler.getReadableDatabase(),0,0);

        });

        settings.setOnClickListener(v->{

        });
    }

    private void testData(){
        String string = String.valueOf(new Random().nextInt(60));
        ArrayList<Week> plan = new ArrayList<>();
        IntStream.range(0,7).forEach(i->{
            ArrayList<Day> week = new ArrayList<>();
            IntStream.range(0,7).forEach(j->{
                ArrayList<Block> day = new ArrayList<>();

                IntStream.range(0,7).forEach(k->{
                    day.add(new Block(string,string,string,string,string,string,String.valueOf(k)));
                });
                week.add(new Day(day,"date"));
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

    private String getActivetSemester() {
        return "letni";
    }

    private String getActiveGroup() {
        return "WCY18IY5S1";
    }

    public Context getContext() {
        return context;
    }
}
