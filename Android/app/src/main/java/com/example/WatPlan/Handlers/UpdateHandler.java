package com.example.WatPlan.Handlers;

import android.widget.Toast;

import androidx.core.util.Pair;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.Models.Block;
import com.example.WatPlan.Models.Day;
import com.example.WatPlan.Models.Week;
import com.example.WatPlan.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UpdateHandler extends Thread {
    private DBHandler dbHandler;
    private MainActivity mainActivity;
    private ScheduleFragment scheduleFragment;
    private SettingsFragment settingsFragment;
    private ArrayList<Runnable> queue = new ArrayList<>();
    private ArrayList<String> availableGroups;
    private ArrayList<String> availableSemesters;
    private Map<String, Map<String, String>> versions;
    private Map<String, Set<String>> uniqueValues;

    public UpdateHandler(MainActivity mainActivity, DBHandler dbHandler) {
        this.mainActivity = mainActivity;
        this.scheduleFragment = mainActivity.getScheduleFragment();
        this.settingsFragment = mainActivity.getSettingsFragment();
        this.dbHandler = dbHandler;
        setUp();
        start();
    }

    void setUp() {
        versions = dbHandler.getVersionMap();
        availableSemesters = new ArrayList<>(versions.keySet());
        availableGroups = new ArrayList<>(Objects.requireNonNull(
                versions.get(dbHandler.getPreference("semester"))).keySet());
    }

    @Override
    public void run() {
        while (true) if (queue.size() > 0) {
            queue.get(0).run();
            queue.remove(0);
        }
    }

    private void checkForUpdate() {
        new Thread(() -> {
            Map<String, Map<String, String>> newVersions = ConnectionHandler.getVersionMap();
            if (newVersions == null) return;
            if (!newVersions.equals(versions))
                addToQueue(() -> {
                    System.out.println("VERSION MAPS DIFFER");
                    versions = newVersions;
                    changeGroup(getActiveSemester(), getActiveGroup());
                });
        }).start();
    }

    public void setActiveSemester(String semesterName) {
        availableGroups.clear();
        availableGroups.addAll(Objects.requireNonNull(versions.get(semesterName)).keySet());
        dbHandler.setPreference("semester", availableSemesters.get(0));
    }

    public void setDefaultGroup() {
        String semesterName = dbHandler.getPreference("semester");
        String groupName = dbHandler.getPreference("group");
        assert groupName != null && semesterName != null : "no semester or group in preferences";
        addToQueue(() -> changeGroup(semesterName, groupName));
    }

    public void changeGroup(String semesterName, String groupName) {
        mainActivity.runOnUiThread(() -> scheduleFragment.clearPlan());
        dbHandler.setPreference("semester", semesterName);
        dbHandler.setPreference("group", groupName);

        addToQueue(() -> {
            try {
                Map<Pair<String, String>, Block> newBlocks = getBlockMap(semesterName, groupName);
                Pair<LocalDate, LocalDate> borderDates = getBorderDates(semesterName, groupName);
                ArrayList<Week> plan = getPlan(newBlocks, borderDates);
                settingsFragment.setFilters(uniqueValues);
                mainActivity.runOnUiThread(() -> {
                    scheduleFragment.togglePastPlan();
                    scheduleFragment.setPlan(plan);
                    scheduleFragment.setNames(semesterName, groupName);
                });
                checkForUpdate();
            } catch (NullPointerException | AssertionError e) {
                mainActivity.runOnUiThread(() -> scheduleFragment.displayFailureMessage());
            }
        });
    }

    private ArrayList<Week> getPlan(Map<Pair<String, String>, Block> newBlocks, Pair<LocalDate, LocalDate> borderDates) throws AssertionError {
        assert borderDates != null;
        LocalDate firstDay = borderDates.first;
        LocalDate lastDay = borderDates.second;
        assert firstDay != null;
        assert lastDay != null;
        uniqueValues = new HashMap<>();
        ArrayList<Week> plan = new ArrayList<>();
        ArrayList<String> blockColumns = dbHandler.getBlockColumns();
        blockColumns.forEach(column -> uniqueValues.put(column, new HashSet<>()));

        while (!firstDay.equals(lastDay.plusDays(1))) {

            ArrayList<Day> week = new ArrayList<>();
            for (int dayCounter = 0; dayCounter < 7; dayCounter++) {

                String date = firstDay.toString();
                ArrayList<Block> day = new ArrayList<>();
                for (int index = 1; index <= 7; index++) {

                    Pair<String, String> key = new Pair<>(date, String.valueOf(index));
                    if (newBlocks.containsKey(key)) {
                        Block block = newBlocks.get(key);
                        blockColumns.forEach(column -> {
                            assert block != null;
                            Objects.requireNonNull(uniqueValues.get(column)).add(block.get(column));
                        });
                        day.add(block);
                    } else day.add(null);
                }
                week.add(new Day(day, date));
                firstDay = firstDay.plusDays(1);
            }
            plan.add(new Week(week));
        }
        return plan;
    }

    private Pair<LocalDate, LocalDate> getBorderDates(String semesterName, String groupName) throws AssertionError {
        Pair<String, String> borderDates = dbHandler.getBorderDates(semesterName, groupName);
        if (borderDates == null) {
            borderDates = ConnectionHandler.getBorderDates(semesterName, groupName);
            assert borderDates != null : "Border dates not found";
            dbHandler.updateBorderDates(semesterName, groupName, borderDates);
        }
        assert borderDates.first != null;
        assert borderDates.second != null;
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

    private Map<Pair<String, String>, Block> getBlockMap(String semesterName, String groupName) throws AssertionError {
        Map<Pair<String, String>, Block> newBlockMap = new HashMap<>();
        try {
            assert versions.containsKey(semesterName) : "Semester not found in versions";
            assert versions.get(semesterName) != null;
            assert Objects.requireNonNull(versions.get(semesterName)).containsKey(groupName)
                    : "Group not found in versions";

            String version = dbHandler.getVersion(semesterName, groupName);
            String upToDateVersion = Objects.requireNonNull(
                    versions.get(semesterName)).get(groupName);
            System.out.println("versions " + version + " " + upToDateVersion);
            if (version.equals(upToDateVersion) && !version.equals("-1"))
                newBlockMap = dbHandler.getGroupBlocks(semesterName, groupName);
            else {
                newBlockMap = ConnectionHandler.getGroupBlocks(semesterName, groupName);
                dbHandler.updateGroup(semesterName, groupName, newBlockMap, upToDateVersion);
            }
        } catch (NullPointerException e) {
            System.out.println("NULL EXCEPTION");
            System.out.println(e.getMessage());
            newBlockMap = dbHandler.getGroupBlocks(semesterName, groupName);
            assert newBlockMap != null;
        }
        return newBlockMap;
    }

    public ArrayList<String> getAvailableGroups() {
        return availableGroups;
    }

    public ArrayList<String> getAvailableSemesters() {
        return availableSemesters;
    }

    public String getActiveSemester() {
        return dbHandler.getPreference("semester");
    }

    public String getActiveGroup() {
        return dbHandler.getPreference("group");
    }

    private void addToQueue(Runnable runnable) {
        queue.add(runnable);
    }
}
