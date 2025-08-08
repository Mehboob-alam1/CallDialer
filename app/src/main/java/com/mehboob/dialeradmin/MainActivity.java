package com.mehboob.dialeradmin;


import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE}, 1);
        }
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
