package com.mehboob.dialeradmin;

import android.app.Application;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static MyApplication instance;
    private AdminModel currentAdmin;
    private boolean isAdminLoaded = false;
    private boolean isAdminModeEnabled = false;
    private OnModeChangeListener modeChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initializeCashfreeSDK();
        checkAppMode();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    private void initializeCashfreeSDK() {
        // Cashfree SDK initialization is handled in PacakageActivity
        Log.d(TAG, "Cashfree SDK will be initialized when needed");
    }

    /**
     * Check the current app mode from Firebase
     */
    private void checkAppMode() {
        DatabaseReference configRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_APP_CONFIG_NODE);
        
        configRef.child(Config.FIREBASE_ADMIN_MODE_KEY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean newMode = snapshot.exists() && snapshot.getValue(Boolean.class);
                if (newMode != isAdminModeEnabled) {
                    isAdminModeEnabled = newMode;
                    Log.d(TAG, "App mode changed to: " + (isAdminModeEnabled ? "ADMIN" : "DIALER"));
                    
                    if (modeChangeListener != null) {
                        modeChangeListener.onModeChanged(isAdminModeEnabled);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking app mode: " + error.getMessage());
                // Default to dialer mode if there's an error
                isAdminModeEnabled = false;
            }
        });
    }

    /**
     * Get current app mode
     */
    public boolean isAdminModeEnabled() {
        return isAdminModeEnabled;
    }

    /**
     * Get current app mode as string
     */
    public String getCurrentMode() {
        return isAdminModeEnabled ? Config.MODE_ADMIN : Config.MODE_DIALER;
    }

    /**
     * Set mode change listener
     */
    public void setModeChangeListener(OnModeChangeListener listener) {
        this.modeChangeListener = listener;
    }

    /**
     * Remove mode change listener
     */
    public void removeModeChangeListener() {
        this.modeChangeListener = null;
    }

    public AdminModel getCurrentAdmin() {
        return currentAdmin;
    }

    public void setCurrentAdmin(AdminModel admin) {
        this.currentAdmin = admin;
        this.isAdminLoaded = true;
    }

    public boolean isAdminLoaded() {
        return isAdminLoaded;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Check if user has an active premium plan
     */
    public boolean hasActivePlan() {
        if (currentAdmin == null) return false;
        if (!currentAdmin.isPremium()) return false;
        
        long currentTime = System.currentTimeMillis();
        return currentAdmin.getPlanExpiryAt() > currentTime;
    }

    /**
     * Check if plan is expired
     */
    public boolean isPlanExpired() {
        if (currentAdmin == null) return true;
        if (!currentAdmin.isPremium()) return true;
        
        long currentTime = System.currentTimeMillis();
        return currentAdmin.getPlanExpiryAt() <= currentTime;
    }

    /**
     * Get current plan type
     */
    public String getCurrentPlanType() {
        if (currentAdmin == null) return "";
        return currentAdmin.getPlanType();
    }

    /**
     * Get max trackable numbers for current plan
     */
    public int getMaxTrackableNumbers() {
        if (currentAdmin == null) return 0;
        return Config.getMaxTrackableNumbers(currentAdmin.getPlanType());
    }

    /**
     * Get current child numbers count
     */
    public int getCurrentChildNumbersCount() {
        if (currentAdmin == null) return 0;
        // This will be updated when we load child numbers
        return currentAdmin.getChildNumbers() != null ? currentAdmin.getChildNumbers().size() : 0;
    }

    /**
     * Check if user can add more child numbers
     */
    public boolean canAddMoreChildNumbers() {
        if (!hasActivePlan()) return false;
        
        int currentCount = getCurrentChildNumbersCount();
        int maxAllowed = getMaxTrackableNumbers();
        
        return currentCount < maxAllowed;
    }

    /**
     * Load admin data from Firebase
     */
    public void loadAdminData(String uid, OnAdminLoadedListener listener) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);
        
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AdminModel admin = snapshot.getValue(AdminModel.class);
                    if (admin != null) {
                        setCurrentAdmin(admin);
                        if (listener != null) {
                            listener.onAdminLoaded(admin);
                        }
                    } else {
                        if (listener != null) {
                            listener.onAdminLoadFailed("Failed to parse admin data");
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onAdminLoadFailed("Admin data not found");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (listener != null) {
                    listener.onAdminLoadFailed("Database error: " + error.getMessage());
                }
            }
        });
    }

    public interface OnAdminLoadedListener {
        void onAdminLoaded(AdminModel admin);
        void onAdminLoadFailed(String error);
    }

    public interface OnModeChangeListener {
        void onModeChanged(boolean isAdminMode);
    }
}
