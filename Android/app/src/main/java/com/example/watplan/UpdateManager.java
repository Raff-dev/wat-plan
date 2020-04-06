package com.example.watplan;

import android.content.Context;
import android.os.AsyncTask;

import androidx.core.util.Pair;

import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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

    private Map<String, Map<String, String>> currentVersions;
    private Map<Pair<String, String>, Block> activeBlocksMap = new HashMap<>();
    private String activeSemester;
    private String activeGroup;

    public UpdateManager(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        setUp();
    }

    private void setUp() {
        new Thread(()->{
        currentVersions = ConnectionHandler.getVersionMap();
        dbHandler = new DBHandler(context, this);
//        activeSemester = dbHandler.getAcviteSemester();
//        activeGroup = dbHandler.getActiveGroup();
        activeSemester = "letni";
        activeGroup = "WCY18IY5S1";
        changeGroup(activeSemester, activeGroup);
        }).start();
    }

    Map<String, Map<String, String>> getVersionMap() {
        return currentVersions;
    }

    public void changeGroup(String semesterName, String groupName) {
            Map<Pair<String, String>, Block> newBlocks;
            Map<String, String> version = dbHandler.getVersion(semesterName, groupName);
            boolean upToDate = currentVersions.containsValue(version);
            if (upToDate)
                newBlocks = dbHandler.getGroupBlocks(semesterName, groupName);
            else {
                newBlocks = ConnectionHandler.getGroupBlocks(semesterName, groupName);
                if (newBlocks == null) throw new NullPointerException("Null blocks");
                dbHandler.updateGroup(semesterName, groupName, activeBlocksMap);
            }
            dbHandler.setActiveGroup(groupName);
            dbHandler.setActiveSemester(semesterName);
            activeBlocksMap = newBlocks;
            ArrayList<Week> plan = new ArrayList<>();

            Pair<String, String> borderDates = ConnectionHandler.getBorderDates(semesterName, groupName);
            if (borderDates == null) throw new NullPointerException("Null border dates");

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

        while (!firstDay.equals(lastDay.plusDays(1))) {

                ArrayList<Day> week = new ArrayList<>();
                for (int dayCounter = 0; dayCounter < 7; dayCounter++) {

                    ArrayList<Block> day = new ArrayList<>();
                    String date = firstDay.toString();
                    for (int index = 0; index < 7; index++) {

                        Pair<String, String> key = new Pair<>(date, String.valueOf(index));
                        if (activeBlocksMap.containsKey(key)) {
                            System.out.println(activeBlocksMap.get(key).getTeacher());
                            day.add(activeBlocksMap.get(key));
                        } else day.add(new Block());
                    }
                    week.add(new Day(day, date));
                    firstDay = firstDay.plusDays(1);
                }
                plan.add(new Week(week));
            }

            mainActivity.runOnUiThread(()->{
                mainActivity.setPlan(plan);
            });
    }
}
