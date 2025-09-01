package com.easyranktools.callhistoryforanynumber.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallHistory {
    private String id;
    private String adminId;
    private String childNumber;
    private String contactNumber;
    private String contactName;
    private String callType; // INCOMING, OUTGOING, MISSED
    private long callStartTime;
    private long callEndTime;
    private long callDuration;
    private boolean isPremiumCall;
    private String planType;
    private long createdAt;

    // Required empty constructor for Firebase
    public CallHistory() {}

    public CallHistory(String id, String adminId, String childNumber, String contactNumber, 
                      String contactName, String callType, long callStartTime, long callEndTime, 
                      long callDuration, boolean isPremiumCall, String planType, long createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.childNumber = childNumber;
        this.contactNumber = contactNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.callStartTime = callStartTime;
        this.callEndTime = callEndTime;
        this.callDuration = callDuration;
        this.isPremiumCall = isPremiumCall;
        this.planType = planType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getChildNumber() { return childNumber; }
    public void setChildNumber(String childNumber) { this.childNumber = childNumber; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public long getCallStartTime() { return callStartTime; }
    public void setCallStartTime(long callStartTime) { this.callStartTime = callStartTime; }

    public long getCallEndTime() { return callEndTime; }
    public void setCallEndTime(long callEndTime) { this.callEndTime = callEndTime; }

    public long getCallDuration() { return callDuration; }
    public void setCallDuration(long callDuration) { this.callDuration = callDuration; }

    public boolean isPremiumCall() { return isPremiumCall; }
    public void setIsPremiumCall(boolean isPremiumCall) { this.isPremiumCall = isPremiumCall; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getFormattedDuration() {
        if (callDuration <= 0) return "0:00";
        
        long seconds = callDuration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public String getFormattedDate() {
        if (createdAt == 0) return "Unknown";
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getFormattedCallTime() {
        if (callStartTime == 0) return "Unknown";
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(callStartTime));
    }

    public boolean isToday() {
        if (createdAt == 0) return false;
        
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;
        
        return (now - createdAt) < oneDay;
    }

    public String getRelativeTime() {
        if (createdAt == 0) return "Unknown";
        
        long now = System.currentTimeMillis();
        long diff = now - createdAt;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
}
