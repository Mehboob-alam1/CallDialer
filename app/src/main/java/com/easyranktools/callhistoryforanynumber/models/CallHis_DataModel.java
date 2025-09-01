package com.easyranktools.callhistoryforanynumber.models;

public class CallHis_DataModel {
    int icon;
    String name;
    String totalNumber;
    String tintColor;
    int background;

    public CallHis_DataModel() {}

    public CallHis_DataModel(int icon, String name, String totalNumber, String tintColor, int background) {
        this.icon = icon;
        this.name = name;
        this.totalNumber = totalNumber;
        this.tintColor = tintColor;
        this.background = background;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(String totalNumber) {
        this.totalNumber = totalNumber;
    }

    public String getTintColor() {
        return tintColor;
    }

    public int getBackground() {
        return background;
    }
}
