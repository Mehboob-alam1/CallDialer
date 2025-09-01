package com.easyranktools.callhistoryforanynumber.models;

public class CallLogModel {
    public String number;
    public String type;
    public long timestamp;
    public int duration;

    public CallLogModel() {} // Required for Firebase

    public CallLogModel(String number, String type, long timestamp, int duration) {
        this.number = number;
        this.type = type;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
