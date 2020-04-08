package com.example.WatPlan.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WatPlan.Models.Block;
import com.example.WatPlan.R;

import java.util.ArrayList;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.BlockViewHolder> {

    Context context;
    private ArrayList<Block> blockArrayList;

    public BlockAdapter(Context context, ArrayList<Block> blockArrayList) {
        this.context = context;
        this.blockArrayList = blockArrayList;
    }

    public static class BlockViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView;
        TextView placeTextView;
        TextView classTypeTextView;
        TextView teacherTextView;
        TextView classIndexTextView;

        public BlockViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            placeTextView = itemView.findViewById(R.id.roomTextView);
            classTypeTextView = itemView.findViewById(R.id.typeTextView);
            teacherTextView = itemView.findViewById(R.id.teacherTextView);
            classIndexTextView = itemView.findViewById(R.id.indexTextView);
        }
    }

    @NonNull
    @Override
    public BlockAdapter.BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_block, parent, false);
        return new BlockViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockAdapter.BlockViewHolder holder, int position) {
//        blockArrayList.forEach(block->{
//            if (block.getClassIndex().equals(String.valueOf(position))){
        Block block = blockArrayList.get(position);
                holder.subjectTextView.setText(block.getSubject());
                holder.placeTextView.setText(block.getPlace());
                holder.teacherTextView.setText(block.getTeacher());
                holder.classTypeTextView.setText(block.getClassType());
                holder.classIndexTextView.setText(block.getIndex());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return blockArrayList.size();
    }
}
