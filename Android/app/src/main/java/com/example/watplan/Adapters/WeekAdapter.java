package com.example.watplan.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watplan.Models.Day;
import com.example.watplan.R;
import com.example.watplan.Models.Week;

import java.util.ArrayList;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

    private Context context;
    private ArrayList<Week> weekArrayList;

    public WeekAdapter(Context context, ArrayList<Week> weekArrayList) {
        this.context = context;
        this.weekArrayList = weekArrayList;
    }

    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        RecyclerView dayRecyclerView;

        public WeekViewHolder(@NonNull View itemView) {
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
        Week week = weekArrayList.get(position);
        ArrayList<Day> dayArrayList = week.getDayArrayList();
        DayAdapter dayAdapter = new DayAdapter(context, dayArrayList);

        holder.dayRecyclerView.setHasFixedSize(true);
        holder.dayRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.dayRecyclerView.setAdapter(dayAdapter);
    }

    @Override
    public int getItemCount() {
        return weekArrayList.size();
    }
}
