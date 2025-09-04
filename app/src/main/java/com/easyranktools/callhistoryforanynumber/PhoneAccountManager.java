package com.easyranktools.callhistoryforanynumber;

import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneAccountManager {
    private static final String TAG = "PhoneAccountManager";
    private static final String PHONE_ACCOUNT_ID = "CallDialerPhoneAccount";
    
    private Context context;
    private TelecomManager telecomManager;
    private PhoneAccountHandle phoneAccountHandle;
    private ActivityResultLauncher<Intent> roleRequestLauncher;

    public PhoneAccountManager(Context context) {
        this.context = context;
        this.telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        
        // Create phone account handle
        ComponentName componentName = new ComponentName(context, MyConnectionService.class);
        this.phoneAccountHandle = new PhoneAccountHandle(componentName, PHONE_ACCOUNT_ID);
    }

    public void setupActivityResultLauncher(AppCompatActivity activity) {
        if (roleRequestLauncher == null) {
            setupRoleRequestLauncher(activity);
        }
    }

    private void setupRoleRequestLauncher(AppCompatActivity activity) {
        roleRequestLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Log.d(TAG, "Default dialer role granted");
                    registerPhoneAccount();
                } else {
                    Log.d(TAG, "Default dialer role denied");
                }
            }
        );
    }

    public void registerPhoneAccount() {
        if (telecomManager == null) return;

        PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Call Dialer")
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER | 
                               PhoneAccount.CAPABILITY_CONNECTION_MANAGER)
                .setShortDescription("Call Dialer")
                .setSupportedUriSchemes(new String[]{PhoneAccount.SCHEME_TEL})
                .build();

        try {
            telecomManager.registerPhoneAccount(phoneAccount);
            Log.d(TAG, "Phone account registered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register phone account", e);
        }
    }

    public void unregisterPhoneAccount() {
        if (telecomManager == null) return;

        try {
            telecomManager.unregisterPhoneAccount(phoneAccountHandle);
            Log.d(TAG, "Phone account unregistered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister phone account", e);
        }
    }

    public boolean isDefaultDialer() {
        if (telecomManager == null) return false;
        
        String defaultDialer = telecomManager.getDefaultDialerPackage();
        return context.getPackageName().equals(defaultDialer);
    }

    public void requestDefaultDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use RoleManager for Android 10+
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                if (context instanceof AppCompatActivity && roleRequestLauncher != null) {
                    roleRequestLauncher.launch(intent);
                } else {
                    // Fallback for non-Activity contexts
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } else {
            // Use TelecomManager for older versions
            if (telecomManager != null && !isDefaultDialer()) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, 
                              context.getPackageName());
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).startActivityForResult(intent, 1001);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }
    }

    public void makeCall(String phoneNumber) {
        if (telecomManager == null) return;

        try {
            Uri uri = Uri.fromParts("tel", phoneNumber, null);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to make call", e);
        }
    }

    public PhoneAccountHandle getPhoneAccountHandle() {
        return phoneAccountHandle;
    }

    public boolean canSetAsDefaultDialer() {
        if (telecomManager == null) return false;
        
        try {
            // Check if we can register phone account
            return telecomManager.getDefaultDialerPackage() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking default dialer capability", e);
            return false;
        }
    }

    public String getCurrentDefaultDialer() {
        if (telecomManager == null) return null;
        return telecomManager.getDefaultDialerPackage();
    }
}
