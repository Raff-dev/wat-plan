package com.example.watplan.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;
import com.example.watplan.R;

import java.util.ArrayList;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private Context context;
    private ArrayList<Day> dayArrayList;

    public DayAdapter(Context context, ArrayList<Day> dayArrayList){
        this.context=context;
        this.dayArrayList=dayArrayList;

    }
    public static class DayViewHolder extends RecyclerView.ViewHolder{
        TextView dateTextView;
        TextView dayNameTextView;
        RecyclerView blockRecyclerView;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView=itemView.findViewById(R.id.dayDateTextView);
            dayNameTextView=itemView.findViewById(R.id.dayNameTextView);
            blockRecyclerView=itemView.findViewById(R.id.blockRecyclerView);

        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_day,parent,false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Day day = dayArrayList.get(position);
        String date = day.getDate();
        String dayName="Day Name";
        switch (position){
            case 0: dayName="Mon"; break;
            case 1: dayName="Tue"; break;
            case 2: dayName="Wed"; break;
            case 3: dayName="Thu"; break;
            case 4: dayName="Fri"; break;
            case 5: dayName="Sat"; break;
            case 6: dayName="Sun"; break;
            default: dayName="nie dziala";
        }
        holder.dateTextView.setText(date);
        holder.dayNameTextView.setText(dayName);

        ArrayList<Block> blockArrayList=day.getBlockArrayList();
        BlockAdapter blockAdapter = new BlockAdapter(context,blockArrayList);

        holder.blockRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager blocklayoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        holder.blockRecyclerView.setLayoutManager(blocklayoutManager);
        //new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        holder.blockRecyclerView.setAdapter(blockAdapter);
    }

    @Override
    public int getItemCount() {
        return dayArrayList.size();
    }


}
