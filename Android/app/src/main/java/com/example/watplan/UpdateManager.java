package com.example.watplan;

import android.content.Context;

import androidx.core.util.Pair;

import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//wjebac pliki DO BAZY
//czytac z bazy danych
//sprawdzic ile czasu bedzie czytac | zrobic loading
//loading na klik w inna grupe


// semestry w bazie
// grupy w bazie


// SCENARIO:
// aplikacja przechowuje potencjalnie nieaktualną wersję dancyh
// uzytkownik chce wyswietlic grupe

// sprawdz, czy wersja sie zgadza
// request-> tworzenie listy blokow -> wyswietlenie ich -> wrzucenie ich od bazy danych

public class UpdateManager extends Thread {
    private ArrayList<Runnable> tasks = new ArrayList<>();

    private static DBHandler dbHandler;
    private Context context;
    private MainActivity mainActivity;

    private Map<String, Map<String, String>> upToDateVersions;
    private String activeSemester;
    private String activeGroup;

    public UpdateManager(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        setUp();
    }

    private void setUp() {
        new Thread(() -> {
            upToDateVersions = ConnectionHandler.getVersionMap();
            dbHandler = new DBHandler(context, this);
            activeSemester = dbHandler.getActiveSemester();
            activeGroup = dbHandler.getActiveGroup();
        }).start();
    }

    public void changeGroup(String semesterName, String groupName) {
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
        mainActivity.runOnUiThread(() -> {
            mainActivity.setPlan(plan);
            mainActivity.setNames(semesterName,groupName);
        });
    }

    private Pair<LocalDate, LocalDate> getBorderDates(String semesterName, String groupName) {
        Pair<String, String> borderDates = dbHandler.getBorderDates(semesterName, groupName);
        if (borderDates == null) {
            borderDates = ConnectionHandler.getBorderDates(semesterName, groupName);
            assert borderDates != null;
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
            if (!upToDateVersions.containsKey(semesterName)) throw new AssertionError();
            if (!upToDateVersions.get(semesterName).containsKey(groupName))
                throw new AssertionError();

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

    Map<String, Map<String, String>> getVersionMap() {
        return upToDateVersions;
    }
}
