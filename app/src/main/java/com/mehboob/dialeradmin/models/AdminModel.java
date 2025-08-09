package com.mehboob.dialeradmin.models;

public class AdminModel {
    private String uid;
    private String email;
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

    public AdminModel(String uid, String email, String role,
                      boolean isActivated, boolean isPremium,
                      String planType, long planActivatedAt, long planExpiryAt, long createdAt,String childNumber) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.isActivated = isActivated;
        this.isPremium = isPremium;
        this.planType = planType;
        this.planActivatedAt = planActivatedAt;
        this.planExpiryAt = planExpiryAt;
        this.createdAt = createdAt;
        this.childNumber=childNumber;
    }

    public String getChildNumber() {
        return childNumber;
    }

    public void setChildNumber(String childNumber) {
        this.childNumber = childNumber;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }



    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActivated() { return isActivated; }
    public void setActivated(boolean activated) { isActivated = activated; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getPlanActivatedAt() { return planActivatedAt; }
    public void setPlanActivatedAt(long planActivatedAt) { this.planActivatedAt = planActivatedAt; }

    public long getPlanExpiryAt() { return planExpiryAt; }
    public void setPlanExpiryAt(long planExpiryAt) { this.planExpiryAt = planExpiryAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
