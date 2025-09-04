package com.easyranktools.callhistoryforanynumber;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.easyranktools.callhistoryforanynumber.databinding.ActivityDialerHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.easyranktools.callhistoryforanynumber.fragment.CallHis_AddtoContactFragment;
import com.easyranktools.callhistoryforanynumber.fragment.CallHis_FragmentHomeData;
import com.easyranktools.callhistoryforanynumber.fragment.CallHis_SettingFragment;

public class DialerHomeActivity extends AppCompatActivity implements MyApplication.OnModeChangeListener {
    ActivityDialerHomeBinding binding;
    private PhoneAccountManager phoneAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        AdManager.initialize(this);
        AdManager.loadInterstitial(this);

        // Initialize PhoneAccountManager
        phoneAccountManager = new PhoneAccountManager(this);
        phoneAccountManager.setupActivityResultLauncher(this);
        phoneAccountManager.registerPhoneAccount();

        // Check if this is a dialer intent (ACTION_DIAL, ACTION_CALL, etc.)
        if (handleDialerIntent(getIntent())) {
            // If it's a dialer intent, show the dialer UI directly
            setupDialerUI();
            return;
        }

        // Check app mode and route accordingly
        checkAppModeAndRoute();

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Request READ_PHONE_NUMBERS permission for Android 14+
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            if (checkSelfPermission(android.Manifest.permission.READ_PHONE_NUMBERS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_NUMBERS}, 102);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_fragment, fragment);
        transaction.commit();
    }

    @Override
    public void onModeChanged(boolean isAdminMode) {
        if (isAdminMode) {
            // Add exit animation before switching

                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().removeModeChangeListener();
        if (phoneAccountManager != null) {
            phoneAccountManager.unregisterPhoneAccount();
        }
    }

    private void showDefaultDialerStatus() {
        if (phoneAccountManager != null) {
            // Show debug info
            phoneAccountManager.showDefaultDialerInfo();
            
            if (phoneAccountManager.isDefaultDialer()) {
                Toast.makeText(this, "Call Dialer is your default dialer!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call Dialer is not your default dialer. Use Settings to change.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleIncomingCallState() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("call_state")) {
            String callState = intent.getStringExtra("call_state");
            if ("incoming".equals(callState)) {
                Toast.makeText(this, "Incoming call detected", Toast.LENGTH_SHORT).show();
                // You can add specific UI handling for incoming calls here
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) { // READ_PHONE_NUMBERS permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DialerHomeActivity", "READ_PHONE_NUMBERS permission granted");
                // Re-register phone account now that we have permission
                if (phoneAccountManager != null) {
                    phoneAccountManager.registerPhoneAccount();
                }
            } else {
                Log.e("DialerHomeActivity", "READ_PHONE_NUMBERS permission denied");
                Toast.makeText(this, "Phone numbers permission required for default dialer functionality", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) { // Default dialer request
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Call Dialer is now your default dialer!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Default dialer request was denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public PhoneAccountManager getPhoneAccountManager() {
        return phoneAccountManager;
    }

    /**
     * Handle dialer intents (ACTION_DIAL, ACTION_CALL, tel: scheme)
     */
    private boolean handleDialerIntent(Intent intent) {
        if (intent == null) return false;

        String action = intent.getAction();
        android.net.Uri data = intent.getData();

        if (Intent.ACTION_DIAL.equals(action) ||
                Intent.ACTION_CALL.equals(action) ||
                Intent.ACTION_VIEW.equals(action) ||
                Intent.ACTION_CALL_BUTTON.equals(action)) {
            
            if (data != null && "tel".equals(data.getScheme())) {
                String number = data.getSchemeSpecificPart();
                Log.d("DialerHomeActivity", "Handling dialer intent: " + number);
                
                // Forward to DialerActivity with the number
                Intent dialerIntent = new Intent(this, DialerActivity.class);
                dialerIntent.setData(data);
                startActivity(dialerIntent);
                finish();
                return true;
            }
        }
        return false;
    }

    /**
     * Setup the dialer UI (tabs, fragments, etc.)
     */
    private void setupDialerUI() {
        binding = ActivityDialerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Home").setIcon(R.drawable.ic_home));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Add Contact").setIcon(R.drawable.ic_addcontact));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Setting").setIcon(R.drawable.ic_setting));

        binding.tabLayout.getTabAt(0).getIcon().setTint(ContextCompat.getColor(this, R.color.primary));
        loadFragment(new CallHis_FragmentHomeData());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                AdManager.showInterstitial(DialerHomeActivity.this);
                tab.getIcon().setTint(ContextCompat.getColor(DialerHomeActivity.this, R.color.primary));
                Fragment selectedFragment;
                if (tab.getPosition() == 0) {
                    selectedFragment = new CallHis_FragmentHomeData();
                } else if (tab.getPosition() == 1) {
                    selectedFragment = new CallHis_AddtoContactFragment();
                } else {
                    selectedFragment = new CallHis_SettingFragment();
                }
                loadFragment(selectedFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setTint(ContextCompat.getColor(DialerHomeActivity.this, R.color.black));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Show default dialer status
        showDefaultDialerStatus();
        
        // Handle incoming call state if launched from InCallService
        handleIncomingCallState();
    }

    /**
     * Check app mode and route to appropriate flow
     */
    private void checkAppModeAndRoute() {
        // Check if we should ask to be default dialer
        if (DefaultDialerHelper.shouldAskToBeDefault(this)) {
            Log.d("DialerHomeActivity", "Requesting default dialer role...");
            DefaultDialerHelper.requestToBeDefaultDialer(this, 1001);
        }

        // Check app mode from Firebase
        MyApplication.getInstance().setModeChangeListener(this);
        
        if (MyApplication.getInstance().isAdminModeEnabled()) {
            // Route to admin flow
            MyApplication.getInstance().routeToAdminFlow(this, true);
        } else {
            // Route to dialer flow (show dialer UI)
            setupDialerUI();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Handle new dialer intents
        if (handleDialerIntent(intent)) {
            return;
        }
    }

//    @Override
//    public void onBackPressed() {
//        // Add custom back animation if needed
//        if (currentFragment instanceof DialPadFragment) {
//            super.onBackPressed();
//        } else {
//            // Navigate back to dialer
//            bottomNavigationView.setSelectedItemId(R.id.nav_dialpad);
//        }
//    }
}