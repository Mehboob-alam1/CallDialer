package com.mehboob.dialeradmin;

public class Config {
	// Environment - Change to PRODUCTION for live payments on your backend
	public static final boolean IS_PRODUCTION = false;

	// Your backend base URL (implement /create-order and /order-status/{orderId} there)
	// Example for local dev with a tunnel: https://your-ngrok-id.ngrok.io
	public static final String BACKEND_BASE_URL = "https://your-backend.example.com";

	// OPTIONAL: Direct Cashfree integration (for testing only; do NOT ship secrets in app)
	public static final boolean USE_DIRECT_CASHFREE = false; // set true only if you don't have a backend yet
	public static final String CASHFREE_CLIENT_ID = ""; // set your PG Client ID here for direct mode
	public static final String CASHFREE_CLIENT_SECRET = ""; // set your PG Client Secret here for direct mode
	public static final String CASHFREE_API_VERSION = "2023-08-01";
	public static final String CASHFREE_ORDERS_URL = IS_PRODUCTION ?
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
