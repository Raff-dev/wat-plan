package com.example.watplan.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Block {
    private Map<String, String> values = new HashMap<>();
    private String[] valueNames = new String[]{
            "date", "index", "title", "subject", "teacher", "place", "class_type", "class_index"};

    public Block() {
    }

    public Block(String... args) {
        for (int i = 0; i < args.length; i++) values.put(valueNames[i], args[i]);
    }

    public String getIndex() {
        return values.get("index");
    }

    public String getTitle() {
        return values.get("title");
    }

    public String getSubject() {
        return values.get("subject");
    }

    public String getTeacher() {
        return values.get("teacher");
    }

    public String getPlace() {
        return values.get("place");
    }

    public String getClassType() {
        return values.get("class_type");
    }

    public String getClassIndex() {
        return values.get("class_index");
    }

    public void insert(String key, String value) {
        values.put(key, value);
    }

    public String get(String key) {
        return values.get(key);
    }
}