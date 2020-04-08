package com.example.WatPlan.Models;

import java.util.ArrayList;

public class Week {
    private ArrayList<Day> dayArrayList;

    public Week(ArrayList<Day> dayArrayList) {
        this.dayArrayList = dayArrayList;
    }

    public ArrayList<Day> getDayArrayList() {
        return dayArrayList;
    }

    public void setDayArrayList(ArrayList<Day> dayArrayList) {
        this.dayArrayList = dayArrayList;
    }
}
