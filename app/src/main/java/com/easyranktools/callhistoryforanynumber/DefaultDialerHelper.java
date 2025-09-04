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

    public static void openDefaultDialerSettingsDirectly(Activity activity) {
        android.util.Log.d("DefaultDialerHelper", "Opening default dialer settings directly...");
        openDefaultDialerSettings(activity);
    }

    /**
     * Special method for Android 9 compatibility - tries multiple approaches
     */
    public static void requestDefaultDialerAndroid9(Activity activity, int requestCode) {
        android.util.Log.d("DefaultDialerHelper", "=== Android 9 Special Request ===");
        android.util.Log.d("DefaultDialerHelper", "Android version: " + Build.VERSION.SDK_INT);
        
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.P) {
            android.util.Log.d("DefaultDialerHelper", "Not Android 9, using standard method");
            requestToBeDefaultDialer(activity, requestCode);
            return;
        }
        
        // Record that we're making a request
        getPrefs(activity).edit().putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis()).apply();
        
        boolean started = false;
        TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
        
        if (telecomManager != null) {
            String currentDefault = telecomManager.getDefaultDialerPackage();
            android.util.Log.d("DefaultDialerHelper", "Current default dialer: " + currentDefault);
            
            if (!activity.getPackageName().equals(currentDefault)) {
                // Try Android 9 specific approaches
                Intent[] intents = {
                    // Try TelecomManager with different flags
                    new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER),
                    // Try with different intent flags
                    new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER),
                    // Try settings directly
                    new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                    new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"),
                    // Try app details
                    new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + activity.getPackageName()))
                };
                
                for (int i = 0; i < intents.length; i++) {
                    Intent intent = intents[i];
                    if (i < 2) {
                        // First two are TelecomManager intents
                        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.getPackageName());
                        if (i == 1) {
                            // Second intent with different flags
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        } else {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        
                        android.util.Log.d("DefaultDialerHelper", "Trying TelecomManager intent " + (i + 1));
                        try {
                            activity.startActivityForResult(intent, requestCode);
                            started = true;
                            android.util.Log.d("DefaultDialerHelper", "Successfully started TelecomManager intent " + (i + 1));
                            break;
                        } catch (Exception e) {
                            android.util.Log.e("DefaultDialerHelper", "Failed TelecomManager intent " + (i + 1), e);
                        }
                    } else {
                        // Settings intents
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        android.util.Log.d("DefaultDialerHelper", "Trying settings intent " + (i - 1));
                        try {
                            activity.startActivity(intent);
                            started = true;
                            android.util.Log.d("DefaultDialerHelper", "Successfully started settings intent " + (i - 1));
                            break;
                        } catch (Exception e) {
                            android.util.Log.e("DefaultDialerHelper", "Failed settings intent " + (i - 1), e);
                        }
                    }
                }
            }
        }
        
        if (!started) {
            android.util.Log.d("DefaultDialerHelper", "All Android 9 approaches failed, using fallback");
            openDefaultDialerSettings(activity);
        }
        
        android.util.Log.d("DefaultDialerHelper", "=== Android 9 Request Complete ===");
    }

    public static void requestToBeDefaultDialer(Activity activity, int requestCode) {
        android.util.Log.d("DefaultDialerHelper", "=== Requesting Default Dialer ===");
        android.util.Log.d("DefaultDialerHelper", "Android version: " + Build.VERSION.SDK_INT);
        android.util.Log.d("DefaultDialerHelper", "App package: " + activity.getPackageName());
        
        // Use special Android 9 method if needed
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            android.util.Log.d("DefaultDialerHelper", "Using Android 9 special method");
            requestDefaultDialerAndroid9(activity, requestCode);
            return;
        }
        
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
                    // Enhanced approach for Android 9 and below with more fallbacks
                    Intent[] intents = {
                        // Primary: TelecomManager direct request
                        new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER),
                        // Secondary: Direct settings intents
                        new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"),
                        new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                        // Tertiary: App-specific settings
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + activity.getPackageName())),
                        // Quaternary: General settings
                        new Intent(Settings.ACTION_SETTINGS)
                    };
                    
                    for (int i = 0; i < intents.length; i++) {
                        Intent intent = intents[i];
                        if (i == 0) {
                            // Only the first intent (TelecomManager) should have the package name extra
                            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.getPackageName());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        
                        android.util.Log.d("DefaultDialerHelper", "Trying intent " + (i + 1) + ": " + intent.getAction());
                        try {
                            if (i == 0) {
                                // Use startActivityForResult for the first intent (TelecomManager)
                                activity.startActivityForResult(intent, requestCode);
                            } else {
                                // Use startActivity for settings intents
                                activity.startActivity(intent);
                            }
                            started = true;
                            android.util.Log.d("DefaultDialerHelper", "Successfully started intent " + (i + 1));
                            break;
                        } catch (Exception e) {
                            android.util.Log.e("DefaultDialerHelper", "Failed to start intent " + (i + 1), e);
                        }
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
        android.util.Log.d("DefaultDialerHelper", "Opening default dialer settings...");
        
        Intent[] intents = new Intent[] {
                new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"),
                new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + activity.getPackageName())),
                new Intent(Settings.ACTION_SETTINGS)
        };
        
        for (int i = 0; i < intents.length; i++) {
            Intent intent = intents[i];
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            android.util.Log.d("DefaultDialerHelper", "Trying settings intent " + (i + 1) + ": " + intent.getAction());
            try {
                activity.startActivity(intent);
                android.util.Log.d("DefaultDialerHelper", "Successfully opened settings intent " + (i + 1));
                return;
            } catch (Exception e) {
                android.util.Log.e("DefaultDialerHelper", "Failed to open settings intent " + (i + 1), e);
            }
        }
        
        android.util.Log.e("DefaultDialerHelper", "All settings intents failed");
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

