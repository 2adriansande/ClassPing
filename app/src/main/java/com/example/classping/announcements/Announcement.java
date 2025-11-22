package com.example.classping.announcements;

public class Announcement {
    private String id;
    private String title;
    private String message;
    private String department;
    private String author;
    private long timestamp;

    public Announcement() { }

    public Announcement(String id, String title, String message, String department, String author, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.department = department;
        this.author = author;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getDepartment() { return department; }
    public String getAuthor() { return author; }
    public long getTimestamp() { return timestamp; }
}
