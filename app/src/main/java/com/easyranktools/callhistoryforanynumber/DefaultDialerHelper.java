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
        // Always ask when not default; avoid permanently suppressing the prompt
        return !isDefaultDialer(context);
    }

    public static void markDoNotAskAgain(Context context) {
        getPrefs(context).edit().putBoolean(KEY_DO_NOT_ASK_DEFAULT_DIALER, true).apply();
    }

    public static void requestToBeDefaultDialer(Activity activity, int requestCode) {
        boolean started = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                    } catch (Exception ignored) { }
                }
            }
        }

        if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                if (!activity.getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.getPackageName());
                    try {
                        activity.startActivityForResult(intent, requestCode);
                        started = true;
                    } catch (Exception ignored) { }
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

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

