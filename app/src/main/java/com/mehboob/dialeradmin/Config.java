package com.mehboob.dialeradmin;

public class Config {
	// App Mode Configuration
	public static final String FIREBASE_APP_CONFIG_NODE = "app_config";
	public static final String FIREBASE_ADMIN_MODE_KEY = "admin_mode_enabled";
	
	// App Modes
	public static final String MODE_DIALER = "dialer";
	public static final String MODE_ADMIN = "admin";
	
	// Environment - toggle between SANDBOX and PRODUCTION
	public static final boolean IS_PRODUCTION = true;

	// Direct Cashfree integration mode (no backend)
	public static final boolean USE_DIRECT_CASHFREE = true;

	// Cashfree API version and Orders endpoint
	public static final String CASHFREE_API_VERSION = "2023-08-01";
	public static final String CASHFREE_ORDERS_URL = IS_PRODUCTION ?
		"https://api.cashfree.com/pg/orders" :
		"https://sandbox.cashfree.com/pg/orders";

	// Provide BOTH sets of credentials
	// Sandbox (TEST) keys
	public static final String CF_SANDBOX_CLIENT_ID = "TEST10761613400f1b459ec15d4660f831616701";
	public static final String CF_SANDBOX_CLIENT_SECRET = "cfsk_ma_test_3038e22c6e7e77c83d2b3f6d1bcc95e6_1f4501d3";
	// Production (LIVE) keys
	public static final String CF_PROD_CLIENT_ID = "1050663db5989cbec31ef9036f63660501";
	public static final String CF_PROD_CLIENT_SECRET = "cfsk_ma_prod_4361e7dfba267d0079447013f14788c1_17c3912a";

	// These are the active keys used by the app in direct mode based on IS_PRODUCTION
	public static final String CASHFREE_CLIENT_ID = IS_PRODUCTION ? CF_PROD_CLIENT_ID : CF_SANDBOX_CLIENT_ID;
	public static final String CASHFREE_CLIENT_SECRET = IS_PRODUCTION ? CF_PROD_CLIENT_SECRET : CF_SANDBOX_CLIENT_SECRET;

	// Backend base URL is unused in direct mode
	public static final String BACKEND_BASE_URL = "";

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
