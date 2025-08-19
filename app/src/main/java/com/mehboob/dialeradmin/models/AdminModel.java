package com.mehboob.dialeradmin.models;

import com.google.firebase.database.PropertyName;

public class AdminModel {
    private String uid;
    private String email;
    private String name; // new: admin full name
    private String phoneNumber; // Added for payment integration
    private String role; // "admin"
    private boolean isActivated;
    private boolean isPremium;
    private String planType; // "yearly", "monthly", "weekly", "3months"
    private long planActivatedAt;
    private long planExpiryAt;
    private long createdAt;
    private String childNumber;

    // Required empty constructor for Firebase
    public AdminModel() {}

    public AdminModel(String uid, String email, String phoneNumber, String role,
                      boolean isActivated, boolean isPremium,
                      String planType, long planActivatedAt, long planExpiryAt,
                      long createdAt, String childNumber) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActivated = isActivated;
        this.isPremium = isPremium;
        this.planType = planType;
        this.planActivatedAt = planActivatedAt;
        this.planExpiryAt = planExpiryAt;
        this.createdAt = createdAt;
        this.childNumber = childNumber;
    }

    // New full constructor including name
    public AdminModel(String uid, String email, String name, String phoneNumber, String role,
                      boolean isActivated, boolean isPremium,
                      String planType, long planActivatedAt, long planExpiryAt,
                      long createdAt, String childNumber) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActivated = isActivated;
        this.isPremium = isPremium;
        this.planType = planType;
        this.planActivatedAt = planActivatedAt;
        this.planExpiryAt = planExpiryAt;
        this.createdAt = createdAt;
        this.childNumber = childNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getChildNumber() {
        return childNumber;
    }

    public void setChildNumber(String childNumber) {
        this.childNumber = childNumber;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Force Firebase to use "isActivated"
    @PropertyName("isActivated")
    public boolean getIsActivated() { return isActivated; }
    @PropertyName("isActivated")
    public void setIsActivated(boolean isActivated) { this.isActivated = isActivated; }

    // Force Firebase to use "isPremium"
    @PropertyName("isPremium")
    public boolean getIsPremium() { return isPremium; }
    @PropertyName("isPremium")
    public void setIsPremium(boolean isPremium) { this.isPremium = isPremium; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getPlanActivatedAt() { return planActivatedAt; }
    public void setPlanActivatedAt(long planActivatedAt) { this.planActivatedAt = planActivatedAt; }

    public long getPlanExpiryAt() { return planExpiryAt; }
    public void setPlanExpiryAt(long planExpiryAt) { this.planExpiryAt = planExpiryAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
