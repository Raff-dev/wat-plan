package com.example.WatPlan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Models.BlockFilter;
import com.example.WatPlan.Models.Day;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.util.ArrayList;
import java.util.HashSet;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {
    private HashSet<BlockFilter> blockFilterHashSet = new HashSet<>();
    private MainActivity mainActivity;
    private ArrayList<Week> weekArrayList;
    private int startPosition = 0;

    public WeekAdapter(MainActivity mainActivity, ArrayList<Week> weekArrayList) {
        this.mainActivity = mainActivity;
        this.weekArrayList = weekArrayList;
    }


    public void switchBlockFilter(BlockFilter blockFilter, boolean active) {
        if (active) blockFilterHashSet.add(blockFilter);
        else blockFilterHashSet.remove(blockFilter);
    }

    public void setStartingWeekPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        RecyclerView dayRecyclerView;

        WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            dayRecyclerView = itemView.findViewById(R.id.dayRecyclerView);
        }
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_week, parent, false);
        return new WeekViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        //filtering for only future plan
        Week week;
        if (position + startPosition < weekArrayList.size())
            week = weekArrayList.get(position + startPosition);
        else return;
        ArrayList<Day> dayArrayList = week.getDayArrayList();
        DayAdapter dayAdapter = new DayAdapter(mainActivity, dayArrayList, blockFilterHashSet);

        holder.dayRecyclerView.setAdapter(dayAdapter);
        holder.dayRecyclerView.setHasFixedSize(true);
        holder.dayRecyclerView.setLayoutManager(new LinearLayoutManager
                (mainActivity, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public int getItemCount() {
        return weekArrayList.size() - startPosition;
    }
}
