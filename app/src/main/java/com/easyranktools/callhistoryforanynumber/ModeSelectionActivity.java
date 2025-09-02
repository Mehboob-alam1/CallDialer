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
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    private ProgressBar progressBar;
    private Handler handler;
    private boolean modeChecked = false;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    startActivityForResult(intent, 100);
                }
            }
        }


        // 1. Ensure default dialer on first launch

        requestDefaultDialer();
        // 3. Start checking mode after a short delay
    handler.postDelayed(this::checkAppMode, 500);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
    }


    private void requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ → RoleManager
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                    startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
                } else {
                    Toast.makeText(this, "Already default dialer", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6–9 → TelecomManager
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                String currentPackage = telecomManager.getDefaultDialerPackage();
                Log.d("DialerCheck", "Current default dialer: " + currentPackage);

                if (!getPackageName().equals(currentPackage)) {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                    startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
                } else {
                    Toast.makeText(this, "Already default dialer", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Default dialer not supported on this Android version", Toast.LENGTH_SHORT).show();
        }
    }



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

        // Fallback timeout
        handler.postDelayed(() -> {
            if (!modeChecked) {
                Log.w(TAG, "Mode check timeout, defaulting to dialer mode");
                launchDialerMode();
            }
        }, 10000);
    }

    private void launchDialerMode() {
        handler.postDelayed(() -> MyApplication.getInstance().routeToDialerFlow(ModeSelectionActivity.this, true), 1000);
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
        ensureDefaultDialer();
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
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "✅ App is now the default dialer!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ User denied default dialer request", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
