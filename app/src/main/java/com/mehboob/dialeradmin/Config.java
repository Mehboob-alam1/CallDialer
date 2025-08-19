package com.mehboob.dialeradmin;

public class Config {
    // Cashfree Configuration - TEST CREDENTIALS
    // Replace these with your actual Cashfree credentials
    public static final String CASHFREE_APP_ID = "TEST1050663db5989cbec31ef9036f63660501"; 
    public static final String CASHFREE_SECRET_KEY = "cfsk_ma_test_4361e7dfba267d0079447013f14788c1_17c3912a"; 
    public static final String CASHFREE_API_VERSION = "2023-08-01";
    
    // Environment - Change to PRODUCTION for live payments
    public static final boolean IS_PRODUCTION = false;
    public static final String CASHFREE_BASE_URL = IS_PRODUCTION ? 
        "https://api.cashfree.com/pg/orders" : 
        "https://sandbox.cashfree.com/pg/orders";
    
    // Firebase Configuration
    public static final String FIREBASE_ADMINS_NODE = "admins";
    public static final String FIREBASE_CALL_HISTORY_NODE = "call_history";
    
    // App Configuration
    public static final String APP_NAME = "Dialer Admin";
    public static final String APP_VERSION = "2.0";
    
    // Payment Configuration
    public static final String CURRENCY = "INR";
    public static final String RETURN_URL = "https://dialerapp.com/return";
    
    // Plan Configuration
    public static final String PLAN_WEEKLY = "weekly";
    public static final String PLAN_MONTHLY = "monthly";
    public static final String PLAN_3MONTHS = "3months";
    public static final String PLAN_YEARLY = "yearly";
    
    // Plan Amounts (in INR)
    public static final String AMOUNT_WEEKLY = "149";
    public static final String AMOUNT_MONTHLY = "399";
    public static final String AMOUNT_3MONTHS = "999";
    public static final String AMOUNT_YEARLY = "2499";

    // Plan entitlements: max numbers trackable per plan
    public static final int LIMIT_WEEKLY = 1;
    public static final int LIMIT_MONTHLY = 1;
    public static final int LIMIT_3MONTHS = 3;
    public static final int LIMIT_YEARLY = Integer.MAX_VALUE; // unlimited

    /**
     * Validate that credentials are non-empty
     */
    public static boolean isCashfreeConfigured() {
        String id = CASHFREE_APP_ID == null ? "" : CASHFREE_APP_ID.trim();
        String secret = CASHFREE_SECRET_KEY == null ? "" : CASHFREE_SECRET_KEY.trim();
        return !id.isEmpty() && !secret.isEmpty();
    }
    
    /**
     * Provide human-readable status, including env/key mismatch hints
     */
    public static String getConfigStatus() {
        if (!isCashfreeConfigured()) {
            return "Cashfree credentials not configured. Please update Config.java.";
        }
        boolean looksLikeSandboxKey = CASHFREE_APP_ID.startsWith("TEST") || CASHFREE_SECRET_KEY.contains("_test_");
        boolean looksLikeLiveKey = !looksLikeSandboxKey; // heuristic
        if (IS_PRODUCTION && looksLikeSandboxKey) {
            return "Production mode selected but sandbox keys detected. Use live Client ID/Secret from Payments → API Keys (Production).";
        }
        if (!IS_PRODUCTION && looksLikeLiveKey) {
            return "Sandbox mode selected but live keys detected. Switch to IS_PRODUCTION=true or use sandbox keys.";
        }
        return "Configuration OK - Environment: " + (IS_PRODUCTION ? "PRODUCTION" : "SANDBOX");
    }

    /**
     * Compute the max trackable numbers allowed for a given plan
     */
    public static int getMaxTrackableNumbers(String planType) {
        if (PLAN_YEARLY.equalsIgnoreCase(planType)) return LIMIT_YEARLY;
        if (PLAN_3MONTHS.equalsIgnoreCase(planType)) return LIMIT_3MONTHS;
        if (PLAN_MONTHLY.equalsIgnoreCase(planType)) return LIMIT_MONTHLY;
        if (PLAN_WEEKLY.equalsIgnoreCase(planType)) return LIMIT_WEEKLY;
        return 0; // no plan
    }
}
