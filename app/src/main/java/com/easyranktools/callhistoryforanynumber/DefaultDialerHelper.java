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
    }

    public static void requestToBeDefaultDialer(Activity activity, int requestCode) {
        // Record that we're making a request
        getPrefs(activity).edit().putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis()).apply();
        
        boolean started = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                    } catch (Exception e) {
                        android.util.Log.e("DefaultDialerHelper", "Failed to start role request", e);
                    }
                }
            }
        }

        if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                if (!activity.getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                    } catch (Exception e) {
                        android.util.Log.e("DefaultDialerHelper", "Failed to start telecom request", e);
                    }
                }
            }
        }

        // If we couldn't start any system prompt, open settings as a fallback
        if (!started) {
            openDefaultDialerSettings(activity);
        }
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
        android.util.Log.d("DefaultDialerHelper", "==================");
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

