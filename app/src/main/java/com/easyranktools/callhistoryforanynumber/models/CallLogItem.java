package com.easyranktools.callhistoryforanynumber.models;


public class CallLogItem {
    private String nameOrNumber;
    private String type;
    private String date;

    public CallLogItem(String nameOrNumber, String type, String date) {
        this.nameOrNumber = nameOrNumber;
        this.type = type;
        this.date = date;
    }

    public String getNameOrNumber() { return nameOrNumber; }
    public String getType() { return type; }
    public String getDate() { return date; }
}
