package com.mehboob.dialeradmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mehboob.dialeradmin.fragment.CallHistoryFragment;
import com.mehboob.dialeradmin.fragment.ContactsFragment;
import com.mehboob.dialeradmin.fragment.DialPadFragment;

public class DialerHomeActivity extends AppCompatActivity implements MyApplication.OnModeChangeListener {
    private static final String TAG = "DialerHomeActivity";

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dialer Mode");
        }

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.nav_dialpad) {
                fragment = new DialPadFragment();
            } else if (id == R.id.nav_history) {
                fragment = new CallHistoryFragment();
            } else {
                fragment = new ContactsFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dialpad);
        }

        MyApplication.getInstance().setModeChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dialer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_switch_mode) {
            switchToAdminMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchToAdminMode() {
        DatabaseReference configRef = FirebaseDatabase.getInstance()
                .getReference(Config.FIREBASE_APP_CONFIG_NODE)
                .child(Config.FIREBASE_ADMIN_MODE_KEY);
        configRef.setValue(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to switch mode: " + (task.getException() != null ? task.getException().getMessage() : "unknown"));
            }
        });
    }

    @Override
    public void onModeChanged(boolean isAdminMode) {
        if (isAdminMode) {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().removeModeChangeListener();
    }
}


