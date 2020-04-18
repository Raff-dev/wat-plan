package com.example.WatPlan.Models;

import java.util.ArrayList;

import static com.example.WatPlan.Fragments.SettingsFragment.*;
import static com.example.WatPlan.Models.BlockFilter.NO_FILTER;

public class Preferences {
    private ArrayList<Preference> preferences = new ArrayList<>();
    public static final String SEMESTER = "SEMESTER";
    public static final String GROUP = "GROUP";

    public static final String LECTURE = "LECTURE";
    public static final String EXERCISE = "EXERCISE";
    public static final String LABORATORY = "LABORATORY";

    public static final String SUBJECT = "SUBJECT";
    public static final String PAST_PLAN = "PAST_PLAN";
    public static final String HIDDEN = "HIDDEN";
    public static final String SHOWN = "SHOWN";
    public static final String NONE = "NONE";

    public static final String[] classTypes = new String[]{LECTURE,EXERCISE,LABORATORY};
    public Preferences() {
        preferences.add(new Preference(LECTURE, SHOWN));
        preferences.add(new Preference(EXERCISE, SHOWN));
        preferences.add(new Preference(LABORATORY, SHOWN));
        preferences.add(new Preference(SUBJECT, NO_FILTER));
        preferences.add(new Preference(PAST_PLAN, HIDDEN));
    }

    public static String preferenceValue(boolean value){
        if (value) return HIDDEN;
        else return SHOWN;

    }
    public ArrayList<Preference> getPreferences() {
        return preferences;
    }

    public void addPreference(String name, String defaultValue) {
        preferences.add(new Preference(name, defaultValue));
    }

    public static class Preference {
        private String defaultValue;
        private String name;

        Preference(String name, String defaultValue) {
            this.defaultValue = defaultValue;
            this.name = name;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getName() {
            return name;
        }
    }
}
