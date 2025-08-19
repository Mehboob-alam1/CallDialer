package com.mehboob.dialeradmin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mehboob.dialeradmin.fragment.DialPadFragment;
import com.mehboob.dialeradmin.models.AdminModel;
import com.mehboob.dialeradmin.Config;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private AdminModel currentAdmin;
    private boolean isAdminLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        checkAuthentication();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dialer Admin");
        }
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login
            redirectToAuth();
            return;
        }

        // User is authenticated, check admin status and plan
        loadAdminData(currentUser.getUid());
    }

    private void loadAdminData(String uid) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_ADMINS_NODE)
                .child(uid);
        
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentAdmin = snapshot.getValue(AdminModel.class);
                    if (currentAdmin != null) {
                        isAdminLoaded = true;
                        MyApplication.getInstance().setCurrentAdmin(currentAdmin);
                        
                        // Check if admin is activated
                        if (!currentAdmin.getIsActivated()) {
                            showAdminNotActivatedDialog();
                            return;
                        }
                        
                        // Check if user has an active plan
                        if (!currentAdmin.isPremium() || !currentAdmin.isPlanActive()) {
                            showNoPlanDialog();
                            return;
                        }
                        
                        // User is authenticated, has plan, and is activated - proceed to main app
                        setupMainApp();
                        requestPermissions();
                        
                    } else {
                        showErrorDialog("Failed to load admin data");
                    }
                } else {
                    showErrorDialog("Admin account not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorDialog("Database error: " + error.getMessage());
            }
        });
    }

    private void setupMainApp() {
        // Load the dial pad fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new DialPadFragment())
                    .commit();
        }
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void showAdminNotActivatedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account Not Activated")
                .setMessage("Your admin account is not yet activated. Please contact support.")
                .setPositiveButton("OK", (dialog, which) -> {
                    logout();
                })
                .setCancelable(false)
                .show();
    }

    private void showNoPlanDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Active Plan")
                .setMessage("You need an active premium plan to use this app. Please subscribe to a plan.")
                .setPositiveButton("Get Plans", (dialog, which) -> {
                    startActivity(new Intent(this, PacakageActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    logout();
                })
                .setCancelable(false)
                .show();
    }

    private void redirectToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        mAuth.signOut();
        MyApplication.getInstance().setCurrentAdmin(null);
        redirectToAuth();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Some permissions are required for the app to function properly", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_call_history) {
            startActivity(new Intent(this, CallHistoryActivity.class));
            return true;
        } else if (id == R.id.action_packages) {
            startActivity(new Intent(this, PacakageActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh admin data when returning from other activities
        if (mAuth.getCurrentUser() != null && !isAdminLoaded) {
            loadAdminData(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any listeners if needed
    }
}
