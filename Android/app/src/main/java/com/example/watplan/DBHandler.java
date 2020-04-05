package com.example.watplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.watplan.Models.Block;
import com.example.watplan.Models.Day;
import com.example.watplan.Models.Week;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "WAT_PLAN";
    private static final String SEMESTER = "semester";
    private static final String GROUP = "'group'";
    public static final String DAY = "day";
    public static final String BLOCK = "block";
    private static final int DATABASE_VERSION = 1;

    DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
        onCreateInsert(db);
    }


    public boolean addPlan(String nameSemester, String nameGroup) {
        ContentValues values = new ContentValues();
        values.put("name", "letni");
        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert("semester", null, values);
        System.out.println("result" + result);
        return result != -1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS semester");
        db.execSQL("DROP TABLE IF EXISTS 'group'");
        db.execSQL("DROP TABLE IF EXISTS day");
        db.execSQL("DROP TABLE IF EXISTS block");
        onCreate(getWritableDatabase());
    }

    ArrayList<Week> getPlan(String nameSemester, String nameGroup) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select block.*,day.date from (semester" +
                " join 'group' on 'group.semester_id'=semester.id and 'group.name'=" + nameGroup +
                " join day on day.group_id = 'group.id'" +
                " join block on block.day_id = day.id)" +
                " where semester.name =" + nameSemester +
                " group by ('block.index')" +
                " order by ('day.date','block.index') asc", null);
        cursor.moveToFirst();

        ArrayList<Week> plan = new ArrayList<>();
        while (!cursor.isLast()) {
            ArrayList<Day> week = new ArrayList<>();
            for (int dayCounter = 0; dayCounter < 7; dayCounter++) {
                ArrayList<Block> day = new ArrayList<>();
                String date = cursor.getString(cursor.getColumnIndex("date"));
                for (int blockCounter = 0; blockCounter < 7; blockCounter++) {
                    String title = cursor.getString(cursor.getColumnIndex("title"));
                    String subject = cursor.getString(cursor.getColumnIndex("subject"));
                    String teacher = cursor.getString(cursor.getColumnIndex("teacher"));
                    String place = cursor.getString(cursor.getColumnIndex("place"));
                    String classType = cursor.getString(cursor.getColumnIndex("class_type"));
                    String classIndex = cursor.getString(cursor.getColumnIndex("class_index"));
                    Block block = new Block(title, subject,
                            teacher, place, classType, classIndex);
                    day.add(block);
                    cursor.moveToNext();
                }
                week.add(new Day(day, date));
            }
            plan.add(new Week(week));
        }
        cursor.close();
        return plan;
    }

    private void createDatabase(SQLiteDatabase db) {
        db.execSQL("create table semester (" +
                "name varchar(30) PRIMARY KEY NOT NULL)");

        db.execSQL("CREATE TABLE 'group' (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "semester_name varchar(30) NOT NULL REFERENCES semester (name) DEFERRABLE INITIALLY DEFERRED," +
                "name varchar(30) NOT NULL," +
                "version integer NOT NULL default 0)");

        db.execSQL("CREATE TABLE day (" +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "group_id integer NOT NULL REFERENCES 'group' (id) DEFERRABLE INITIALLY DEFERRED," +
                "'date' date NOT NULL)");

        db.execSQL("CREATE TABLE block (" +
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
            ArrayList<String> semesters = UpdateHandler.getSemesterList();
            ContentValues values = new ContentValues();
            for (String nameSemester : semesters) {
                values.put("name", nameSemester);
                db.insert(SEMESTER, null, values);
                values.clear();

                ArrayList<String> groups = UpdateHandler.getGroupList(nameSemester);
                for (String nameGroup : groups) {
                    values.put("semester_name", nameSemester);
                    values.put("name", nameGroup);
                    db.insert(GROUP, null, values);
                    values.clear();
                }
            }
        }).start();
    }
}
