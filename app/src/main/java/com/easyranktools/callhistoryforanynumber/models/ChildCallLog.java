package com.easyranktools.callhistoryforanynumber.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildCallLog {
    private String id;
    private String childNumber;
    private String number;
    private String type; // INCOMING, OUTGOING, MISSED
    private long timestamp;
    private long duration;
    private String contactName; // Optional, for display

    // Required empty constructor for Firebase
    public ChildCallLog() {}

    public ChildCallLog(String id, String childNumber, String number, String type, long timestamp, long duration) {
        this.id = id;
        this.childNumber = childNumber;
        this.number = number;
        this.type = type;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChildNumber() { return childNumber; }
    public void setChildNumber(String childNumber) { this.childNumber = childNumber; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    // Helper methods
    public String getFormattedDuration() {
        if (duration <= 0) return "0:00";
        
        long seconds = duration;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public String getFormattedDate() {
        if (timestamp == 0) return "Unknown";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedTime() {
        if (timestamp == 0) return "Unknown";
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedDateOnly() {
        if (timestamp == 0) return "Unknown";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getCallTypeDisplay() {
        if (type == null) return "Unknown";
        
        switch (type.toUpperCase()) {
            case "INCOMING":
                return "Incoming";
            case "OUTGOING":
                return "Outgoing";
            case "MISSED":
                return "Missed";
            default:
                return type;
        }
    }

    public int getCallTypeIcon() {
        if (type == null) return android.R.drawable.ic_menu_call;
        
        switch (type.toUpperCase()) {
            case "INCOMING":
                return android.R.drawable.ic_menu_call;
            case "OUTGOING":
                return android.R.drawable.ic_menu_call;
            case "MISSED":
                return android.R.drawable.ic_menu_close_clear_cancel;
            default:
                return android.R.drawable.ic_menu_call;
        }
    }

    public int getCallTypeColor() {
        if (type == null) return android.R.color.holo_blue_dark;
        
        switch (type.toUpperCase()) {
            case "INCOMING":
                return android.R.color.holo_green_dark;
            case "OUTGOING":
                return android.R.color.holo_blue_dark;
            case "MISSED":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.holo_blue_dark;
        }
    }
}
