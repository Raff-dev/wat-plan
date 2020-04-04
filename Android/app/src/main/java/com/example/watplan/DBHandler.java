package com.example.watplan;

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
    public static final String DATABASE_NAME = "WAT_PLAN";
    public static final int DATABASE_VERSION = 1;

    public DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE 'group' (id integer NOT NULL PRIMARY KEY AUTOINCREMENT, name varchar(30) NOT NULL UNIQUE)");
        db.execSQL("create table semester (id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "group_id integer NOT NULL REFERENCES group_ (id) DEFERRABLE INITIALLY DEFERRED," +
                "name varchar(30) NOT NULL," +
                "version integer NOT NULL default 0)");

        db.execSQL("CREATE TABLE block (" +
                "semester_id integer NOT NULL REFERENCES semester (id) DEFERRABLE INITIALLY DEFERRED," +
                "id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "'date' date NOT NULL," +
                " 'index' integer NOT NULL," +
                "title varchar(100)," +
                "subject varchar(30)," +
                "teacher varchar(30)," +
                "place varchar(30)," +
                "class_type varchar(30)," +
                "class_index varchar(1))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS 'group'");
        db.execSQL("DROP TABLE IF EXISTS semester");
        db.execSQL("DROP TABLE IF EXISTS block");
        onCreate(db);
    }

    public ArrayList<Week> getPlan(String nameGroup, String nameSemester) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from block where block.semester_id =" +
                " (select id from semester where name =" + nameSemester + "and group_id=" +
                "(select id from 'group' where name =" + nameGroup + ")) order by ('date','index') asc", null);
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
}
