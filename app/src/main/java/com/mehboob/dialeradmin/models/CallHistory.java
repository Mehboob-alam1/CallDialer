package com.mehboob.dialeradmin.models;

import com.google.firebase.database.PropertyName;

public class CallHistory {
    private String id;
    private String adminId;
    private String childNumber;
    private String contactNumber;
    private String contactName;
    private String callType; // "INCOMING", "OUTGOING", "MISSED"
    private long callStartTime;
    private long callEndTime;
    private long callDuration; // in seconds
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

    @PropertyName("isPremiumCall")
    public boolean getIsPremiumCall() { return isPremiumCall; }
    @PropertyName("isPremiumCall")
    public void setIsPremiumCall(boolean isPremiumCall) { this.isPremiumCall = isPremiumCall; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper method to format duration
    public String getFormattedDuration() {
        long hours = callDuration / 3600;
        long minutes = (callDuration % 3600) / 60;
        long seconds = callDuration % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Helper method to get formatted date
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(callStartTime));
    }
}
