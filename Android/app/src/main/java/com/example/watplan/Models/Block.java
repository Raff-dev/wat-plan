package com.example.watplan.Models;

import java.util.HashMap;
import java.util.Map;

public class Block {
    private Map<String, String> values = new HashMap<>();
    public Block(){

    }
    public Block(Block block){
        this.values=block.values;

    }

    public Block(String title, String subject, String teacher, String place, String classType, String classIndex) {
        this.values.put("title", title);
        this.values.put("subject", subject);
        this.values.put("teacher", teacher);
        this.values.put("place", place);
        this.values.put("class_type", classType);
        this.values.put("class_index", classIndex);
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
}