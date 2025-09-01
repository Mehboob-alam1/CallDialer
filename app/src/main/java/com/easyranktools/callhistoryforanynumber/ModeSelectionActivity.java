package com.easyranktools.callhistoryforanynumber;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    
    private ProgressBar progressBar;
//    private TextView tvStatus;
    private Handler handler;
    private boolean modeChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        
        // Start checking mode after a short delay
        handler.postDelayed(this::checkAppMode, 500);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
//        tvStatus = findViewById(R.id.tvStatus);
    }

    private void checkAppMode() {
//        tvStatus.setText("Checking app mode...");
        
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
                
                // Default to dialer mode on error
                launchDialerMode();
            }
        });
        
        // Fallback timeout
        handler.postDelayed(() -> {
            if (!modeChecked) {
                Log.w(TAG, "Mode check timeout, defaulting to dialer mode");
                launchDialerMode();
            }
        }, 10000); // 10 second timeout
    }

    private void launchAdminMode() {
//        tvStatus.setText("Launching Admin Mode...");
        
        // Small delay for better UX
        handler.postDelayed(() -> MyApplication.getInstance().routeToAdminFlow(ModeSelectionActivity.this, true), 1000);
    }

    private void launchDialerMode() {
//        tvStatus.setText("Launching Dialer Mode...");
        
        // Small delay for better UX
        handler.postDelayed(() -> MyApplication.getInstance().routeToDialerFlow(ModeSelectionActivity.this, true), 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
