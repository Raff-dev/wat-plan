package com.example.watplan.Models;

import java.util.ArrayList;

public class Day {
    private ArrayList<Block> blockArrayList;
    String date;

    public Day(ArrayList<Block> blockArrayList, String date) {
        this.blockArrayList = blockArrayList;
        this.date = date;
    }

    public ArrayList<Block> getBlockArrayList() {
        return blockArrayList;
    }

    public void setBlockArrayList(ArrayList<Block> blockArrayList) {
        this.blockArrayList = blockArrayList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
