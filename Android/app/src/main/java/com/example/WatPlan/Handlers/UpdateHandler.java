package com.example.WatPlan.Handlers;

import android.content.Context;

import androidx.core.util.Pair;

import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Models.Block;
import com.example.WatPlan.Models.Day;
import com.example.WatPlan.Models.Week;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UpdateHandler extends Thread {
    private ArrayList<Runnable> tasks = new ArrayList<>();

    private static DBHandler dbHandler;
    private Context context;
    private ScheduleFragment scheduleFragment;

    private Map<String, Map<String, String>> upToDateVersions;
    private String activeSemester;
    private String activeGroup;

    public UpdateHandler(Context context, ScheduleFragment scheduleFragment) {
        this.context = context;
        this.scheduleFragment = scheduleFragment;
        start();
    }

    private void setUp() {
        upToDateVersions = ConnectionHandler.getVersionMap();
        dbHandler = new DBHandler(context);
        activeSemester = dbHandler.getActiveSemester();
        activeGroup = dbHandler.getActiveGroup();
        System.out.println("updatehandler jest odpaleny " + activeSemester + activeGroup);
    }

    public void setDefaultGroup() {
        addTask(()->changeGroup(activeSemester, activeGroup));
    }

    public void changeGroup(String semesterName, String groupName) {
        addTask(() -> {
            System.out.println("grupa jest zmieniana "+activeSemester+" "+upToDateVersions.keySet());
            Map<Pair<String, String>, Block> newBlocks = getBlockMap(semesterName, groupName);
            Pair<LocalDate, LocalDate> borderDates = getBorderDates(semesterName, groupName);
            LocalDate firstDay = borderDates.first;
            LocalDate lastDay = borderDates.second;

            ArrayList<Week> plan = new ArrayList<>();
            while (!firstDay.equals(lastDay.plusDays(1))) {

                ArrayList<Day> week = new ArrayList<>();
                for (int dayCounter = 0; dayCounter < 7; dayCounter++) {

                    String date = firstDay.toString();
                    ArrayList<Block> day = new ArrayList<>();
                    for (int index = 0; index < 7; index++) {

                        Pair<String, String> key = new Pair<>(date, String.valueOf(index));
                        if (newBlocks.containsKey(key)) {
                            day.add(newBlocks.get(key));
                        } else day.add(new Block());
                    }
                    week.add(new Day(day, date));
                    firstDay = firstDay.plusDays(1);
                }
                plan.add(new Week(week));
            }
            dbHandler.setActiveGroup(groupName);
            dbHandler.setActiveSemester(semesterName);
            scheduleFragment.mainActivity.runOnUiThread(() -> {
                scheduleFragment.setPlan(plan);
                scheduleFragment.setNames(semesterName, groupName);
            });
        });

    }

    private Pair<LocalDate, LocalDate> getBorderDates(String semesterName, String groupName) {
        Pair<String, String> borderDates = dbHandler.getBorderDates(semesterName, groupName);
        if (borderDates == null) {
            borderDates = ConnectionHandler.getBorderDates(semesterName, groupName);
            assert borderDates != null : "Border dates not found";
            dbHandler.updateBorderDates(semesterName, groupName, borderDates);
        }
        ArrayList<String> first = new ArrayList<>(Arrays.asList(borderDates.first.split("-")));
        ArrayList<String> second = new ArrayList<>(Arrays.asList(borderDates.second.split("-")));
        LocalDate firstDay = LocalDate.of(
                Integer.parseInt(first.get(0)),
                Integer.parseInt(first.get(1)),
                Integer.parseInt(first.get(2)));
        LocalDate lastDay = LocalDate.of(
                Integer.parseInt(second.get(0)),
                Integer.parseInt(second.get(1)),
                Integer.parseInt(second.get(2)));
        return new Pair<>(firstDay, lastDay);
    }

    private Map<Pair<String, String>, Block> getBlockMap(String semesterName, String groupName) {

        Map<Pair<String, String>, Block> newBlockMap = new HashMap<>();
        try {
            System.out.println("SEM: " + semesterName + " " + upToDateVersions.keySet());

            if (!upToDateVersions.containsKey(semesterName))
                throw new AssertionError("Semester not found in versions");
            if (!upToDateVersions.get(semesterName).containsKey(groupName))
                throw new AssertionError("Group not found in versions");

            String version = dbHandler.getVersion(semesterName, groupName);
            String upToDateVersion = upToDateVersions.get(semesterName).get(groupName);
            if (version.equals(upToDateVersion))
                newBlockMap = dbHandler.getGroupBlocks(semesterName, groupName);
            else {
                newBlockMap = ConnectionHandler.getGroupBlocks(semesterName, groupName);
                dbHandler.updateGroup(semesterName, groupName, newBlockMap, upToDateVersion);
            }
        } catch (NullPointerException e) {
            System.out.println("NULL EXCEPTION");
            System.out.println(e.getMessage());
            return dbHandler.getGroupBlocks(semesterName, groupName);
        } catch (AssertionError e) {
            System.out.println("assertion");
            System.out.println(e.getMessage());
        }
        return newBlockMap;
    }


    private void addTask(Runnable runnable) {
        tasks.add(runnable);
    }

    @Override
    public void run() {
        new Thread(() -> {
            setUp();
            while (true) if (tasks.size() > 0) {
                tasks.get(0).run();
                tasks.remove(0);
            }
        }).start();
    }
}
