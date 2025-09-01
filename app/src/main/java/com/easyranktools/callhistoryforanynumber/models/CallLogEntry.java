package com.easyranktools.callhistoryforanynumber.models;

public class CallLogEntry {
    private String name;
    private String number;
    private String type;
    private String duration;
    private String time;
    private int iconRes;

    public CallLogEntry(String name, String number, String type, String duration, String time, int iconRes) {
        this.name = name;
        this.number = number;
        this.type = type;
        this.duration = duration;
        this.time = time;
        this.iconRes = iconRes;
    }


    // Getters and Setters
    public String getName() { return name; }
    public String getNumber() { return number; }
    public String getType() { return type; }
    public String getDuration() { return duration; }
    public String getTime() { return time; }
    public int getIconRes() { return iconRes; }
}