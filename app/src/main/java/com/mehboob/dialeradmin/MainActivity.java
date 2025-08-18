package com.mehboob.dialeradmin;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.mehboob.dialeradmin.fragment.CallListFragment;
import com.mehboob.dialeradmin.fragment.ContactsFragment;
import com.mehboob.dialeradmin.fragment.DialPadFragment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // Request necessary permissions
        requestPermissions();

        // Check if user phone number is set
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String phone = prefs.getString("user_phone", null);
        if (phone == null || phone.isEmpty()) {
            startActivity(new Intent(this, PhoneNumberActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadFragment(new ContactsFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_contacts) {
                fragment = new ContactsFragment();
            } else if (id == R.id.nav_history) {
                fragment = new CallListFragment();
            } else if (id == R.id.nav_dialpad) {
                fragment = new DialPadFragment();
            }
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                break;
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
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
            showProfileDialog();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showProfileDialog() {
        // Show admin profile information
        String profileInfo = "Admin Profile\n\n";
        if (MyApplication.getInstance().getCurrentAdmin() != null) {
            profileInfo += "Email: " + MyApplication.getInstance().getCurrentAdmin().getEmail() + "\n";
            profileInfo += "Phone: " + MyApplication.getInstance().getCurrentAdmin().getPhoneNumber() + "\n";
            profileInfo += "Premium: " + (MyApplication.getInstance().isPremiumActive() ? "Active" : "Inactive") + "\n";
            if (MyApplication.getInstance().isPremiumActive()) {
                profileInfo += "Plan: " + MyApplication.getInstance().getActivePlanName() + "\n";
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Profile")
                .setMessage(profileInfo)
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    MyApplication.getInstance().logout();
                    startActivity(new Intent(this, AuthActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
