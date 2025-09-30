package com.example.classping;

public class Schedule {
    private String day;
    private String subject;
    private String program;
    private String room;
    private String startTime;
    private String endTime;
    private String notes;
    private String reminder;

    public Schedule() {}

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

    public String toDisplayString() {
        return day + " | " + subject + " | " + program + " | " + room +
                " | " + startTime + " - " + endTime +
                (notes.isEmpty() ? "" : " | Notes: " + notes) +
                (reminder.equals("None") ? "" : " | Reminder: " + reminder);
    }
}
