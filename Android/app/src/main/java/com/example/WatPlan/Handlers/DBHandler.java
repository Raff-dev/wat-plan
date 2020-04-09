package com.example.WatPlan.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.example.WatPlan.Models.Block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHandler extends SQLiteOpenHelper {
    private SQLiteDatabase readableDb;
    private SQLiteDatabase writableDb;
    private static final String DATABASE_NAME = "WAT_PLAN";
    private static final String PREFERENCES = "preferences";
    private static final String SEMESTER = "semester";
    private static final String GROUP = "'group'";
    private static final String BLOCK = "block";
    private static final int DATABASE_VERSION = 1;

    public DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        readableDb = getReadableDatabase();
        writableDb = getWritableDatabase();
    }

    private boolean planExists(String... args) {
        String groupId = getGroupId(args);
        Cursor cursor = readableDb.rawQuery("select count(block.id) as blocks_count from block" +
                " where block.group_id = " + groupId, null);
        cursor.moveToFirst();
        boolean exists = cursor.getInt(cursor.getColumnIndex("blocks_count")) > 0;
        cursor.close();
        return exists;
    }

    private String getGroupId(String... args) {
        Cursor cursor = readableDb.rawQuery("select * from 'group'" +
                " where semester_name = '" + args[0] + "'" +
                " and name='" + args[1] + "'", null);
        cursor.moveToFirst();
        String groupId = cursor.getString(cursor.getColumnIndex("id"));
        cursor.close();
        return groupId;
    }

    void updateGroup(String semesterName, String groupName, Map<Pair<String, String>, Block> blocksMap, String version) {
        ContentValues values = new ContentValues();
        List<String> keyList = Arrays.asList("date", "index", "title", "subject", "teacher",
                "place", "class_type", "class_index");
        String groupId = getGroupId(semesterName, groupName);
        boolean exists = planExists(semesterName, groupName);

        System.out.println("GROUP ID: " + groupId);
        System.out.println("EXISTS: " + exists);

        blocksMap.values().forEach(block -> {
            keyList.forEach(key -> values.put("'" + key + "'", block.get(key)));
            if (exists) {
                writableDb.update(BLOCK, values, "block.id=(" +
                        "select block.id from 'group' where " +
                        "'group.id' ='" + groupId + "')", null);
            } else {

                values.put("group_id", groupId);
                writableDb.insert(BLOCK, null, values);
            }
            values.clear();
        });
        values.put("version", version);
        writableDb.update(GROUP, values, "id=" + groupId, null);
        System.out.println("FINISHED UPDATING GROUP");
    }

    Map<String, Map<String, String>> getAvailableGroups() {
        Map<String, Map<String, String>> availableGroups = new HashMap<>();
        availableGroups.clear();

        return availableGroups;
    }

    String getVersion(String... args) {
        String groupId = getGroupId(args);
        Cursor cursor = readableDb.rawQuery("select version from 'group'" +
                " where id = " + groupId, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        String version = cursor.getString(cursor.getColumnIndex("version"));
        cursor.close();
        return version;
    }

    String getActiveSemester() {
        String semesterName;
        Cursor cursor = readableDb.rawQuery(
                "select value from preferences where name = 'semester'", null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) semesterName = null;
        else semesterName = cursor.getString(cursor.getColumnIndex("value"));
        cursor.close();
        return semesterName;
    }

    String getActiveGroup() {
        String groupName;
        Cursor cursor = readableDb.rawQuery(
                "select value from preferences where name = 'group'", null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) groupName = null;
        else groupName = cursor.getString(cursor.getColumnIndex("value"));
        cursor.close();
        return groupName;
    }

    public void setActiveSemester(String semesterName) {
        ContentValues values = new ContentValues();
        values.put("name", "semester");
        values.put("value", semesterName);
        if (getActiveSemester() == null) writableDb.insert(PREFERENCES, null, values);
        else writableDb.update(PREFERENCES, values, "name='semester'", null);
    }

    public void setActiveGroup(String groupName) {
        ContentValues values = new ContentValues();
        values.put("name", "group");
        values.put("value", groupName);
        if (getActiveGroup() == null) writableDb.insert(PREFERENCES, null, values);
        else writableDb.update(PREFERENCES, values, "name='group'", null);
    }

    Pair<String, String> getBorderDates(String semesterName, String groupName) {
        Cursor cursor = readableDb.rawQuery("select first_day,last_day from 'group'" +
                " where 'group.semester_name' = '" + semesterName + "'" +
                " and 'group.name' = " + "'" + groupName + "'", null);
        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        String firstDay = cursor.getString(cursor.getColumnIndex("first_day"));
        String lastDay = cursor.getString(cursor.getColumnIndex("last_day"));
        cursor.close();
        return new Pair<>(firstDay, lastDay);
    }

    Map<Pair<String, String>, Block> getGroupBlocks(String... args) {
        String groupId = getGroupId(args);
        Cursor cursor = readableDb.rawQuery("select * from block" +
                " where group_id = " + groupId, null);
        System.out.println("DAS COUNT" + cursor.getCount());
        System.out.println("Group " + args[1] + "ID: " + groupId);
        if (cursor.getCount() == 0)
            throw new NullPointerException("Database is missing " + args[1] + " blocks ");

        cursor.moveToFirst();

        Map<Pair<String, String>, Block> blocksMap = new HashMap<>();
        do {
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String index = cursor.getString(cursor.getColumnIndex("index"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subject = cursor.getString(cursor.getColumnIndex("subject"));
            String teacher = cursor.getString(cursor.getColumnIndex("teacher"));
            String place = cursor.getString(cursor.getColumnIndex("place"));
            String classType = cursor.getString(cursor.getColumnIndex("class_type"));
            String classIndex = cursor.getString(cursor.getColumnIndex("class_index"));
            Block block = new Block(date, index, title, subject, teacher, place, classType, classIndex);
            blocksMap.put(new Pair<>(date, index), block);
            cursor.moveToNext();
        } while (!cursor.isLast());
        cursor.close();
        return blocksMap;
    }

    void updateBorderDates(String semesterName, String groupName, Pair<String, String> borderDates) {
        ContentValues values = new ContentValues();
        values.put("first_day", borderDates.first);
        values.put("last_day", borderDates.second);
        writableDb.update(GROUP, values, "'group.semester_name'='" + semesterName + "'" +
                " AND 'group.name'='" + groupName + "'", null);
    }

    public boolean isEmpty() {
        return getActiveGroup() == null || getActiveSemester() == null;
    }

    public void initialInsert(Map<String, Map<String, String>> versions) {
        assert versions != null;
        ContentValues values = new ContentValues();

        versions.forEach((semester, groups) -> {
            values.put("name", semester);
            writableDb.insert(SEMESTER, null, values);
            values.clear();
            groups.forEach((group, version) -> {
                values.put("semester_name", semester);
                values.put("name", group);
                values.put("version", "-1");
                writableDb.insert(GROUP, null, values);
                values.clear();
            });
        });
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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

        db.execSQL("CREATE TABLE " + BLOCK + " (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "group_id integer NOT NULL REFERENCES 'group' (id) DEFERRABLE INITIALLY DEFERRED," +
                "'date' varchar(30) NOT NULL," +
                "'index' integer NOT NULL," +
                "title varchar(100)," +
                "subject varchar(30)," +
                "teacher varchar(30)," +
                "place varchar(30)," +
                "class_type varchar(30)," +
                "class_index varchar(3))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS preferences");
        db.execSQL("DROP TABLE IF EXISTS semester");
        db.execSQL("DROP TABLE IF EXISTS 'group'");
        db.execSQL("DROP TABLE IF EXISTS block");
        onCreate(db);
    }
}
