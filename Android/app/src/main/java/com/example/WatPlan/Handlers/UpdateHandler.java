package com.example.WatPlan.Handlers;

import androidx.core.util.Pair;

import com.example.WatPlan.Activities.MainActivity;
import com.example.WatPlan.Fragments.ScheduleFragment;
import com.example.WatPlan.Fragments.SettingsFragment;
import com.example.WatPlan.Models.Block;
import com.example.WatPlan.Models.Day;
import com.example.WatPlan.Models.Week;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class UpdateHandler extends Thread {
    private DBHandler dbHandler;
    private MainActivity mainActivity;
    private ScheduleFragment scheduleFragment;
    private SettingsFragment settingsFragment;
    private ArrayList<Runnable> queue = new ArrayList<>();
    private ArrayList<String> availableGroups;
    private ArrayList<String> availableSemesters;
    private Map<String, Map<String, String>> upToDateVersions;
    private Map<String, Vector<String>> values;


    public UpdateHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.scheduleFragment = mainActivity.getScheduleFragment();
        this.settingsFragment = mainActivity.getSettingsFragment();
        start();
    }

    @Override
    public void run() {
        dbHandler = new DBHandler(mainActivity);
        setUp();
        while (true) if (queue.size() > 0) {
            queue.get(0).run();
            queue.remove(0);
        }
    }

    private void setUp() {
        //get one from db first
        upToDateVersions = ConnectionHandler.getVersionMap();
        if (upToDateVersions == null)
            upToDateVersions = dbHandler.getVersionMap();

        availableSemesters = new ArrayList<>(upToDateVersions.keySet());
        availableGroups = new ArrayList<>(Objects.requireNonNull(
                upToDateVersions.get(dbHandler.getActiveSemester())).keySet());
    }


    public void setActiveSemester(String semesterName) {
        availableGroups.clear();
        availableGroups.addAll(Objects.requireNonNull(upToDateVersions.get(semesterName)).keySet());
        dbHandler.setActiveSemester(availableGroups.get(0));
    }

    public void setDefaultGroup() {
        String semesterName = dbHandler.getActiveSemester();
        String groupName = dbHandler.getActiveGroup();
        assert groupName != null && semesterName != null : "no semester or group in preferences";
        addToQueue(() -> changeGroup(semesterName, groupName));
    }

    public void changeGroup(String semesterName, String groupName) {
        //check if the group differs from active
        mainActivity.runOnUiThread(() -> scheduleFragment.clearPlan());
        dbHandler.setActiveSemester(semesterName);
        dbHandler.setActiveGroup(groupName);

        addToQueue(() -> {
//            Map<String, Set<String>> uniqueValues = new HashMap<>();
            Map<Pair<String, String>, Block> newBlocks = getBlockMap(semesterName, groupName);
            Pair<LocalDate, LocalDate> borderDates = getBorderDates(semesterName, groupName);
            LocalDate firstDay = borderDates.first;
            LocalDate lastDay = borderDates.second;

            ArrayList<Week> plan = new ArrayList<>();
            assert firstDay != null;
            assert lastDay != null;
            while (!firstDay.equals(lastDay.plusDays(1))) {

                ArrayList<Day> week = new ArrayList<>();
                for (int dayCounter = 0; dayCounter < 7; dayCounter++) {

                    String date = firstDay.toString();
                    ArrayList<Block> day = new ArrayList<>();
                    for (int index = 0; index < 7; index++) {

                        Pair<String, String> key = new Pair<>(date, String.valueOf(index));
                        if (newBlocks.containsKey(key)) {
                            Block block = newBlocks.get(key);
                            //mb try getting unique values here
                            day.add(block);
                        } else day.add(null);
                    }
                    week.add(new Day(day, date));
                    firstDay = firstDay.plusDays(1);
                }
                plan.add(new Week(week));
            }

            settingsFragment.setUniqueValues(dbHandler.getUniqueValues(semesterName, groupName));
            scheduleFragment.mainActivity.runOnUiThread(() -> {
                System.out.println("UI THREAD");
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

    private Map<Pair<String, String>, Block> getBlockMap(String semesterName, String groupName) {
        //to mozna uproscic
        //sprawdzic, czy sciaga najpierw z bazy danych
        Map<Pair<String, String>, Block> newBlockMap = new HashMap<>();
        try {
            assert upToDateVersions.containsKey(semesterName) : "Semester not found in versions";
            assert upToDateVersions.get(semesterName) != null;
            assert upToDateVersions.get(semesterName).containsKey(groupName) : "Group not found in versions";

            String version = dbHandler.getVersion(semesterName, groupName);
            String upToDateVersion = upToDateVersions.get(semesterName).get(groupName);
            System.out.println("versions" + version + " " + upToDateVersion);
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
            scheduleFragment.displayFailureMessage();
        }
        return newBlockMap;
    }

    public void downloadAll() {
        //handle group not in db possibility
        scheduleFragment.clearPlan();
        try {
            Map<String, Map<String, String>> versionMap = ConnectionHandler.getVersionMap();
            assert versionMap != null;
            versionMap.forEach((semesterName, groupMap) -> {
                groupMap.forEach((groupName, version) -> {
                    Map<Pair<String, String>, Block> newBlockMap =
                            ConnectionHandler.getGroupBlocks(semesterName, groupName);
                    assert newBlockMap != null;
                    dbHandler.updateGroup(semesterName, groupName, newBlockMap, version);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error downloading plan");
            scheduleFragment.displayFailureMessage();
        }
        //dbhandler set first
        changeGroup(dbHandler.getActiveSemester(), dbHandler.getActiveGroup());
    }

    public ArrayList<String> getAvailableGroups() {
        return availableGroups;
    }

    public ArrayList<String> getAvailableSemesters() {
        return availableSemesters;
    }

    public String getActiveSemester() {
        return dbHandler.getActiveSemester();
    }

    public String getActiveGroup() {
        return dbHandler.getActiveGroup();
    }

    private void addToQueue(Runnable runnable) {
        queue.add(runnable);
    }


}
