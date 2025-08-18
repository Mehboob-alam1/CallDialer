package com.mehboob.dialeradmin;

public class Config {
    // Cashfree Configuration
    public static final String CASHFREE_APP_ID = "TEST1050663db5989cbec31ef9036f63660501"; // Replace with your App ID
    public static final String CASHFREE_SECRET_KEY = "cfsk_ma_test_4361e7dfba267d0079447013f14788c1_17c3912a"; // Replace with your Secret Key
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
}
