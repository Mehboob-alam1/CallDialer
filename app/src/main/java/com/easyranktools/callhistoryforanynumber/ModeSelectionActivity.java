package com.easyranktools.callhistoryforanynumber;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModeSelectionActivity extends AppCompatActivity {
    private static final String TAG = "ModeSelectionActivity";
    // Removed splash delay to avoid timing issues during startup

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    private ProgressBar progressBar;
    private Handler handler;
    private boolean modeChecked = false;
    private boolean isRequestingDefaultDialer = false;
    private boolean isModeCheckStarted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mode_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        handler = new Handler(Looper.getMainLooper());

        // Prompt to be default dialer if needed, but do not block the flow
        if (DefaultDialerHelper.shouldAskToBeDefault(this)) {
            isRequestingDefaultDialer = true;
            DefaultDialerHelper.requestToBeDefaultDialer(this, REQUEST_CODE_SET_DEFAULT_DIALER);
        }
        // Always continue with mode check immediately
        maybeStartModeCheck();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
    }


    // requestDefaultDialer() now handled by DefaultDialerHelper



    /**
     * Request to become the default dialer if not already
     */
    private void ensureDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager =
                    (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

            if (telecomManager != null &&
                    !getPackageName().equals(telecomManager.getDefaultDialerPackage())) {

                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            } else {
                Log.d(TAG, "Already default dialer");
            }
        }
    }


    /**
     * If the app is launched via ACTION_DIAL / ACTION_CALL / tel: link,
     * forward the number to DialerActivity
     */


    private void checkAppMode() {
        DatabaseReference configRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_APP_CONFIG_NODE);

        configRef.child(Config.FIREBASE_ADMIN_MODE_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                modeChecked = true;
                boolean isAdminMode = snapshot.exists() && snapshot.getValue(Boolean.class);

                Log.d(TAG, "App mode: " + (isAdminMode ? "ADMIN" : "DIALER"));

                if (isAdminMode) {
                    MyApplication.getInstance().routeToAdminFlow(ModeSelectionActivity.this, true);
                } else {
                    MyApplication.getInstance().routeToDialerFlow(ModeSelectionActivity.this, true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                modeChecked = true;
                Log.e(TAG, "Error checking app mode: " + error.getMessage());
                Toast.makeText(ModeSelectionActivity.this, "Error checking app mode", Toast.LENGTH_SHORT).show();
                launchDialerMode();
            }
        });

        // Removed fallback timeout to prevent delayed navigation conflicts
    }

    private void launchDialerMode() {
        MyApplication.getInstance().routeToDialerFlow(ModeSelectionActivity.this, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDialerIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Avoid re-triggering the role prompt while a request is in-flight
        if (!isRequestingDefaultDialer && DefaultDialerHelper.shouldAskToBeDefault(this)) {
            isRequestingDefaultDialer = true;
            DefaultDialerHelper.requestToBeDefaultDialer(this, REQUEST_CODE_SET_DEFAULT_DIALER);
        }
        TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            Log.d(TAG, "Current default dialer: " + tm.getDefaultDialerPackage());
            if (getPackageName().equals(tm.getDefaultDialerPackage())) {
                Toast.makeText(this, "App is default dialer", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void handleDialerIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_DIAL.equals(action) ||
                Intent.ACTION_CALL.equals(action) ||
                Intent.ACTION_VIEW.equals(action)) {
            if (data != null && "tel".equals(data.getScheme())) {
                String number = data.getSchemeSpecificPart();
                Log.d(TAG, "Incoming dial intent: " + number);

                Intent dialerIntent = new Intent(this, DialerActivity.class);
                dialerIntent.setData(data);
                startActivity(dialerIntent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            isRequestingDefaultDialer = false;

            // Double-check actual state instead of relying on resultCode which may vary by OEM
            boolean isNowDefault = DefaultDialerHelper.isDefaultDialer(this);
            if (isNowDefault) {
                Toast.makeText(this, "✅ App is now the default dialer!", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed quietly; do not permanently suppress future prompts
            }

            // Proceed with normal flow after the system role dialog resolves
            maybeStartModeCheck();
        }
    }

    private void maybeStartModeCheck() {
        if (isModeCheckStarted) return;
        isModeCheckStarted = true;
        checkAppMode();
    }

}
