package com.example.watplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.example.watplan.Models.Block;

import java.util.HashMap;
import java.util.Map;

public class DBHandler extends SQLiteOpenHelper {
    private UpdateManager updateManager;
    private SQLiteDatabase readableDb;
    private SQLiteDatabase writableDb;
    private static final String DATABASE_NAME = "WAT_PLAN";
    private static final String PREFERENCES = "preferences";
    private static final String SEMESTER = "semester";
    private static final String GROUP = "'group'";
    private static final String DAY = "day";
    private static final String BLOCK = "block";
    private static final int DATABASE_VERSION = 1;

    DBHandler(@Nullable Context context,UpdateManager updateManager) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.updateManager = updateManager;
        readableDb = getReadableDatabase();
        writableDb = getWritableDatabase();
        onUpgrade(writableDb,0,0);
    }

    public boolean addPlan(String semesterName, String groupName) {
        ContentValues values = new ContentValues();
        values.put("name", "letni");
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert("semester", null, values);
        System.out.println("result" + result);
        return result != -1;
    }

    void updateGroup(String semesterName, String groupName, Map<Pair<String, String>, Block> blocksMap) {
        blocksMap.values().forEach(block->{

        });
//        writableDb.execSQL("");
    }

    Map<String, Map<String, String>> getAvailableGroups() {
        Map<String, Map<String, String>> availableGroups = new HashMap<>();

        return availableGroups;
    }

    Map<String, String> getVersion(String semesterName, String groupName) {
        Map<String, String> versionMap = new HashMap<>();
        Cursor cursor = readableDb.rawQuery("select version from 'group'" +
                " where 'group.semester_name' = " + "'" + semesterName + "'" +
                " and 'group.name'='" + groupName + "'", null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        String version = cursor.getString(cursor.getColumnIndex("version"));
        versionMap.put(groupName, version);
        cursor.close();
        return versionMap;
    }

    String getAcviteSemester() {
        Cursor cursor = readableDb.rawQuery(
                "select value from preferences where name = 'semester'", null);
        cursor.moveToFirst();
        String semesterName = cursor.getColumnName(cursor.getColumnIndex("value"));
        cursor.close();
        return semesterName;
    }

    String getActiveGroup() {
        Cursor cursor = readableDb.rawQuery(
                "select value from preferences where name = 'group'", null);
        cursor.moveToFirst();
        String groupName = cursor.getColumnName(cursor.getColumnIndex("value"));
        cursor.close();
        return groupName;
    }

    void setActiveSemester(String semesterName) {
        ContentValues values = new ContentValues();
        values.put("name", "semester");
        values.put("value", semesterName);
        readableDb.insert(PREFERENCES, null, values);
    }

    void setActiveGroup(String groupName) {
        ContentValues values = new ContentValues();
        values.put("name", "group");
        values.put("value", groupName);
        readableDb.insert(PREFERENCES, null, values);
    }


    Map<Pair<String, String>, Block> getGroupBlocks(String semesterName, String groupName) {
        Cursor cursor = readableDb.rawQuery("select block.*,day.date from ('group'" +
                " join day on day.group_id = 'group.id'" +
                " join block on block.day_id = day.id)" +
                " where 'group.semester_name' =" + semesterName +
                " group by ('block.index')" +
                " order by ('day.date','block.index') asc", null);
        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();

        Map<Pair<String, String>, Block> blocksMap=new HashMap<>();
        while (!cursor.isLast()) {
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String index = cursor.getString(cursor.getColumnIndex("index"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subject = cursor.getString(cursor.getColumnIndex("subject"));
            String teacher = cursor.getString(cursor.getColumnIndex("teacher"));
            String place = cursor.getString(cursor.getColumnIndex("place"));
            String classType = cursor.getString(cursor.getColumnIndex("class_type"));
            String classIndex = cursor.getString(cursor.getColumnIndex("class_index"));
            Block block = new Block(index,title, subject, teacher, place, classType, classIndex);
            blocksMap.put(new Pair<>(date, classIndex),block);

            System.out.println("date: " + date);
            System.out.println("index: " + index);
            System.out.println("title: " + title);
            System.out.println("subject: " + subject);
            System.out.println("teacher: " + teacher);
            System.out.println("place: " + place);
            System.out.println("classType: " + classType);
            System.out.println("classIndex: " + classIndex);
        }
        cursor.close();
        return blocksMap;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
        onCreateInsert(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS preferences");
        db.execSQL("DROP TABLE IF EXISTS semester");
        db.execSQL("DROP TABLE IF EXISTS 'group'");
        db.execSQL("DROP TABLE IF EXISTS day");
        db.execSQL("DROP TABLE IF EXISTS block");
        onCreate(db);
    }

    private void createDatabase(SQLiteDatabase db) {
        db.execSQL("create table " + PREFERENCES + "(" +
                "name varchar(30) PRIMARY KEY NOT NULL," +
                "value varchar(30) NOT NULL)");
        db.execSQL("create table " + SEMESTER + " (" +
                "name varchar(30) PRIMARY KEY NOT NULL)");

        db.execSQL("CREATE TABLE " + GROUP + " (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "semester_name varchar(30) NOT NULL REFERENCES semester (name) DEFERRABLE INITIALLY DEFERRED," +
                "name varchar(30) NOT NULL," +
                "first_day varchar(30)," +
                "last_day varchar(30)," +
                "version varchar(30) NOT NULL default '0')");

        db.execSQL("CREATE TABLE " + DAY + " (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "group_id integer NOT NULL REFERENCES 'group' (id) DEFERRABLE INITIALLY DEFERRED," +
                "'date' date NOT NULL)");

        db.execSQL("CREATE TABLE " + BLOCK + " (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "day_id integer NOT NULL REFERENCES day (id) DEFERRABLE INITIALLY DEFERRED," +
                "'index' integer NOT NULL," +
                "title varchar(100)," +
                "subject varchar(30)," +
                "teacher varchar(30)," +
                "place varchar(30)," +
                "class_type varchar(30)," +
                "class_index varchar(1))");
    }

    private void onCreateInsert(SQLiteDatabase db) {
        new Thread(() -> {
            Map<String, Map<String, String>> versions = updateManager.getVersionMap();
            ContentValues values = new ContentValues();

            versions.forEach((semester, groups) -> {
                values.put("name", semester);
                db.insert(SEMESTER, null, values);
                values.clear();
                groups.forEach((group, version) -> {
                    values.put("semester_name", semester);
                    values.put("name", group);
                    values.put("version", "-1");
                    db.insert(GROUP, null, values);
                    values.clear();
                });
            });
            //add preferences
        }).start();
    }
}
