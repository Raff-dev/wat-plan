package com.example.watplan;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watplan.Adapters.WeekAdapter;
import com.example.watplan.Models.Week;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static android.widget.Toast.*;

//wjebac pliki DO BAZY
//czytac z bazy danych
//sprawdzic ile czasu bedzie czytac | zrobic loading
//loading na klik w inna grupe

public class MainActivity extends AppCompatActivity {
    RecyclerView weekRecyclerView;
    ArrayList<Week> weekArrayList = new ArrayList<>();
    RecyclerView.Adapter weekAdapter = new WeekAdapter(this, weekArrayList);
    Button burger, settings;
    Context context;
    DBHandler dbHandler = new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();
        addListeners();
        displayGroup(getActiveGroup(), getActivetSemester());
    }

    private String getActivetSemester() {
        return "letni";
    }

    private String getActiveGroup() {
        return "WCY18IY5S1";
    }

    private void displayGroup(String nameGroup, String nameSemester) {
        ArrayList<Week> plan = dbHandler.getPlan(nameGroup, nameSemester);
        weekArrayList.clear();
        weekArrayList.addAll(plan);
    }

    private void setUp() {
        weekRecyclerView = findViewById(R.id.weekRecyclerView);
        weekRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weekRecyclerView.setAdapter(weekAdapter);

        burger = findViewById(R.id.burger);
        settings = findViewById(R.id.settings);
        context = getApplicationContext();
    }

    private void addListeners() {
        burger.setOnClickListener(v -> System.out.println("none"));
        settings.setOnClickListener(v -> makeText(this, "text", LENGTH_LONG).show());
    }


    public Context getContext() {
        return context;
    }
}
