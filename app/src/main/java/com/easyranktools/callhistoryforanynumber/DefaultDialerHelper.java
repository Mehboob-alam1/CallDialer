package com.easyranktools.callhistoryforanynumber;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.TelecomManager;
import android.provider.Settings;
import android.net.Uri;

/**
 * Utilities to check and request default dialer role across Android versions.
 */
public final class DefaultDialerHelper {
    private static final String PREFS_NAME = "dialer_prefs";
    private static final String KEY_DO_NOT_ASK_DEFAULT_DIALER = "do_not_ask_default_dialer";
    private static final String KEY_LAST_REQUEST_TIME = "last_request_time";
    private static final long REQUEST_COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 hours

    private DefaultDialerHelper() {}

    public static boolean isDefaultDialer(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) return false;
            String current = telecomManager.getDefaultDialerPackage();
            return context.getPackageName().equals(current);
        }
        return false;
    }

    public static boolean shouldAskToBeDefault(Context context) {
        // Don't ask if already default
        if (isDefaultDialer(context)) {
            return false;
        }
        
        // Don't ask if user permanently declined
        if (getPrefs(context).getBoolean(KEY_DO_NOT_ASK_DEFAULT_DIALER, false)) {
            return false;
        }
        
        // Don't ask if we asked recently (within cooldown period)
        long lastRequestTime = getPrefs(context).getLong(KEY_LAST_REQUEST_TIME, 0);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < REQUEST_COOLDOWN_MS) {
            return false;
        }
        
        return true;
    }

    public static void markDoNotAskAgain(Context context) {
        getPrefs(context).edit().putBoolean(KEY_DO_NOT_ASK_DEFAULT_DIALER, true).apply();
    }

    public static void resetRequestCooldown(Context context) {
        getPrefs(context).edit().remove(KEY_LAST_REQUEST_TIME).apply();
        android.util.Log.d("DefaultDialerHelper", "Request cooldown reset - user can try again immediately");
    }

    public static void requestToBeDefaultDialer(Activity activity, int requestCode) {
        android.util.Log.d("DefaultDialerHelper", "=== Requesting Default Dialer ===");
        android.util.Log.d("DefaultDialerHelper", "Android version: " + Build.VERSION.SDK_INT);
        android.util.Log.d("DefaultDialerHelper", "App package: " + activity.getPackageName());
        
        // Record that we're making a request
        getPrefs(activity).edit().putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis()).apply();
        
        boolean started = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.util.Log.d("DefaultDialerHelper", "Using RoleManager (Android 10+)");
            RoleManager roleManager = (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null) {
                android.util.Log.d("DefaultDialerHelper", "RoleManager available: " + roleManager.isRoleAvailable(RoleManager.ROLE_DIALER));
                android.util.Log.d("DefaultDialerHelper", "Role held: " + roleManager.isRoleHeld(RoleManager.ROLE_DIALER));
                
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    android.util.Log.d("DefaultDialerHelper", "Created role request intent");
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                        android.util.Log.d("DefaultDialerHelper", "Successfully started role request");
                    } catch (Exception e) {
                        android.util.Log.e("DefaultDialerHelper", "Failed to start role request", e);
                    }
                } else {
                    android.util.Log.d("DefaultDialerHelper", "Role not available or already held");
                }
            } else {
                android.util.Log.e("DefaultDialerHelper", "RoleManager is null");
            }
        }

        if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.util.Log.d("DefaultDialerHelper", "Using TelecomManager (Android 6+)");
            TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                String currentDefault = telecomManager.getDefaultDialerPackage();
                android.util.Log.d("DefaultDialerHelper", "Current default dialer: " + currentDefault);
                android.util.Log.d("DefaultDialerHelper", "Is our app default: " + activity.getPackageName().equals(currentDefault));
                
                if (!activity.getPackageName().equals(currentDefault)) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.getPackageName());
                    android.util.Log.d("DefaultDialerHelper", "Created telecom request intent");
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                        android.util.Log.d("DefaultDialerHelper", "Successfully started telecom request");
                    } catch (Exception e) {
                        android.util.Log.e("DefaultDialerHelper", "Failed to start telecom request", e);
                    }
                } else {
                    android.util.Log.d("DefaultDialerHelper", "Already default dialer");
                }
            } else {
                android.util.Log.e("DefaultDialerHelper", "TelecomManager is null");
            }
        }

        // If we couldn't start any system prompt, open settings as a fallback
        if (!started) {
            android.util.Log.d("DefaultDialerHelper", "No system prompt available, opening settings");
            openDefaultDialerSettings(activity);
        }
        
        android.util.Log.d("DefaultDialerHelper", "=== Request Complete ===");
    }

    public static void openDefaultDialerSettings(Activity activity) {
        Intent[] intents = new Intent[] {
                new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + activity.getPackageName()))
        };
        for (Intent intent : intents) {
            try {
                activity.startActivity(intent);
                return;
            } catch (Exception ignored) { }
        }
    }

    public static void logCurrentState(Context context) {
        android.util.Log.d("DefaultDialerHelper", "=== Current State ===");
        android.util.Log.d("DefaultDialerHelper", "Is default dialer: " + isDefaultDialer(context));
        android.util.Log.d("DefaultDialerHelper", "Should ask: " + shouldAskToBeDefault(context));
        android.util.Log.d("DefaultDialerHelper", "Do not ask again: " + getPrefs(context).getBoolean(KEY_DO_NOT_ASK_DEFAULT_DIALER, false));
        long lastRequest = getPrefs(context).getLong(KEY_LAST_REQUEST_TIME, 0);
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequest;
        android.util.Log.d("DefaultDialerHelper", "Time since last request: " + (timeSinceLastRequest / 1000) + " seconds");
        android.util.Log.d("DefaultDialerHelper", "Cooldown remaining: " + Math.max(0, (REQUEST_COOLDOWN_MS - timeSinceLastRequest) / 1000) + " seconds");
        
        // Check system capabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null) {
                android.util.Log.d("DefaultDialerHelper", "RoleManager available: " + roleManager.isRoleAvailable(RoleManager.ROLE_DIALER));
                android.util.Log.d("DefaultDialerHelper", "Role held: " + roleManager.isRoleHeld(RoleManager.ROLE_DIALER));
            }
        }
        
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            android.util.Log.d("DefaultDialerHelper", "Current default dialer: " + telecomManager.getDefaultDialerPackage());
        }
        
        android.util.Log.d("DefaultDialerHelper", "==================");
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

