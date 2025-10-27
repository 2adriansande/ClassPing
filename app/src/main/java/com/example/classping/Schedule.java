package com.example.classping;

public class Schedule {
    private int id;
    private String day;
    private String subject;
    private String program; // section
    private String room;
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
    private String notes;
    private String reminder;

    // No-argument constructor
    public Schedule() {
    }

    // Constructor with id
    public Schedule(int id, String day, String subject, String program, String room,
                    String startTime, String endTime, String notes, String reminder) {
        this.id = id;
        this.day = day;
        this.subject = subject;
        this.program = program;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
        this.reminder = reminder;
    }

    // Constructor without id
    public Schedule(String day, String subject, String program, String room,
                    String startTime, String endTime, String notes, String reminder) {
        this.day = day;
        this.subject = subject;
        this.program = program;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
        this.reminder = reminder;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }
}
