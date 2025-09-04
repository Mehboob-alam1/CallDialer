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
        if (telecomManager == null) {
            Log.e(TAG, "TelecomManager is null");
            return;
        }

        try {
            // Check if already registered
            if (telecomManager.getPhoneAccount(phoneAccountHandle) != null) {
                Log.d(TAG, "Phone account already registered");
                return;
            }

            PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Call Dialer")
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER | 
                                   PhoneAccount.CAPABILITY_CONNECTION_MANAGER |
                                   PhoneAccount.CAPABILITY_SELF_MANAGED)
                    .setShortDescription("Call Dialer")
                    .setDescription("Default Call Dialer App")
                    .setSupportedUriSchemes(new String[]{PhoneAccount.SCHEME_TEL})
                    .setIcon(android.R.drawable.ic_menu_call)
                    .build();

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
        Log.d(TAG, "Requesting default dialer role...");
        
        // First ensure phone account is registered
        registerPhoneAccount();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use RoleManager for Android 10+
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null) {
                boolean isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_DIALER);
                Log.d(TAG, "Role held: " + isRoleHeld);
                
                if (!isRoleHeld) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    Log.d(TAG, "Launching role request intent");
                    
                    if (context instanceof AppCompatActivity && roleRequestLauncher != null) {
                        roleRequestLauncher.launch(intent);
                    } else {
                        // Fallback for non-Activity contexts
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } else {
                    Log.d(TAG, "Already default dialer");
                }
            } else {
                Log.e(TAG, "RoleManager is null");
            }
        } else {
            // Use TelecomManager for older versions
            Log.d(TAG, "Using TelecomManager for older Android version");
            if (telecomManager != null && !isDefaultDialer()) {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, 
                              context.getPackageName());
                Log.d(TAG, "Launching TelecomManager intent");
                
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).startActivityForResult(intent, 1001);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            } else {
                Log.d(TAG, "Already default dialer or TelecomManager is null");
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

    public boolean canRequestDefaultDialer() {
        if (telecomManager == null) {
            Log.e(TAG, "TelecomManager is null");
            return false;
        }
        
        try {
            // Check if we can register phone account
            boolean canRegister = telecomManager.getDefaultDialerPackage() != null;
            Log.d(TAG, "Can register phone account: " + canRegister);
            
            // Check if we're already the default dialer
            boolean isDefault = isDefaultDialer();
            Log.d(TAG, "Is default dialer: " + isDefault);
            
            return canRegister && !isDefault;
        } catch (Exception e) {
            Log.e(TAG, "Error checking default dialer capability", e);
            return false;
        }
    }

    public void showDefaultDialerInfo() {
        Log.d(TAG, "=== Default Dialer Info ===");
        Log.d(TAG, "Current default dialer: " + getCurrentDefaultDialer());
        Log.d(TAG, "Is our app default: " + isDefaultDialer());
        Log.d(TAG, "Can request default: " + canRequestDefaultDialer());
        Log.d(TAG, "Phone account registered: " + (telecomManager != null && telecomManager.getPhoneAccount(phoneAccountHandle) != null));
        Log.d(TAG, "========================");
    }
}
