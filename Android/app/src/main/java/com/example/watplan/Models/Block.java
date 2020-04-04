package com.example.watplan.Models;

public class Block {
    private String title;
    private String subject;
    private String teacher;
    private String place;
    private String classType;
    private String classIndex;

    public Block(String title, String subject, String teacher, String place, String classType, String classIndex) {
        this.title = title;
        this.subject = subject;
        this.teacher = teacher;
        this.place = place;
        this.classType = classType;
        this.classIndex = classIndex;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getPlace() {
        return place;
    }

    public String getClassType() {
        return classType;
    }

    public String getClassIndex() {
        return classIndex;
    }
}