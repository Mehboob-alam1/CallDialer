package com.easyranktools.callhistoryforanynumber.models;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class AdminModel {
    private String uid;
    private String email;
    private String phoneNumber;
    private String name;
    private String role; // "admin"
    private boolean isActivated;
    private boolean isPremium;
    private String planType; // "yearly", "monthly", "weekly", "3months"
    private long planActivatedAt;
    private long planExpiryAt;
    private long createdAt;
    private String childNumber; // Legacy field for backward compatibility
    private List<String> childNumbers; // New field for multiple child numbers

    // Required empty constructor for Firebase


    public AdminModel() {
    }


    public AdminModel(String uid, String email, String phoneNumber, String name, String role,
                      boolean isActivated, boolean isPremium,
                      String planType, long planActivatedAt, long planExpiryAt,
                      long createdAt, String childNumber) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.role = role;
        this.isActivated = isActivated;
        this.isPremium = isPremium;
        this.planType = planType;
        this.planActivatedAt = planActivatedAt;
        this.planExpiryAt = planExpiryAt;
        this.createdAt = createdAt;
        this.childNumber = childNumber;
        this.childNumbers = new ArrayList<>();
        if (childNumber != null && !childNumber.isEmpty()) {
            this.childNumbers.add(childNumber);
        }
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean getIsActivated() { return isActivated; }
    public void setIsActivated(boolean isActivated) { this.isActivated = isActivated; }

    // Force Firebase to use the key "isPremium" (avoid default "premium")
    @PropertyName("isPremium")
    public boolean isPremium() { return isPremium; }

    @PropertyName("isPremium")
    public void setIsPremium(boolean isPremium) { this.isPremium = isPremium; }

    // Legacy mapping: support reading old "premium" key but always write only "isPremium"
    @PropertyName("premium")
    public void setPremiumLegacy(boolean premium) { this.isPremium = premium; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public long getPlanActivatedAt() { return planActivatedAt; }
    public void setPlanActivatedAt(long planActivatedAt) { this.planActivatedAt = planActivatedAt; }

    public long getPlanExpiryAt() { return planExpiryAt; }
    public void setPlanExpiryAt(long planExpiryAt) { this.planExpiryAt = planExpiryAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getChildNumber() { return childNumber; }
    public void setChildNumber(String childNumber) { this.childNumber = childNumber; }

    public List<String> getChildNumbers() { 
        if (childNumbers == null) {
            childNumbers = new ArrayList<>();
        }
        return childNumbers; 
    }
    
    public void setChildNumbers(List<String> childNumbers) { this.childNumbers = childNumbers; }

    /**
     * Add a new child number
     */
    public boolean addChildNumber(String number) {
        if (childNumbers == null) {
            childNumbers = new ArrayList<>();
        }
        if (!childNumbers.contains(number)) {
            childNumbers.add(number);
            // Update legacy field for backward compatibility
            if (childNumbers.size() == 1) {
                this.childNumber = number;
            }
            return true;
        }
        return false;
    }

    /**
     * Remove a child number
     */
    public boolean removeChildNumber(String number) {
        if (childNumbers != null && childNumbers.remove(number)) {
            // Update legacy field if needed
            if (childNumbers.isEmpty()) {
                this.childNumber = "";
            } else if (this.childNumber.equals(number)) {
                this.childNumber = childNumbers.get(0);
            }
            return true;
        }
        return false;
    }

    /**
     * Check if a number is already tracked
     */
    public boolean hasChildNumber(String number) {
        return childNumbers != null && childNumbers.contains(number);
    }

    /**
     * Get the count of child numbers
     */
    public int getChildNumbersCount() {
        return childNumbers != null ? childNumbers.size() : 0;
    }

    /**
     * Check if plan is active
     */
    public boolean isPlanActive() {
        if (!isPremium) return false;
        long currentTime = System.currentTimeMillis();
        return planExpiryAt > currentTime;
    }

    /**
     * Get plan status text
     */
    public String getPlanStatusText() {
        if (!isPremium) return "No Plan";
        if (!isPlanActive()) return "Expired";
        return planType.substring(0, 1).toUpperCase() + planType.substring(1) + " Plan";
    }

    /**
     * Get plan expiry date as formatted string
     */
    public String getPlanExpiryDateText() {
        if (!isPremium || planExpiryAt == 0) return "N/A";
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(planExpiryAt));
    }
}
