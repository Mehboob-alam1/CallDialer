package com.mehboob.simplecalldialer;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mehboob.simplecalldialer.fragment.CallHistoryFragment;
import com.mehboob.simplecalldialer.fragment.CallListFragment;
import com.mehboob.simplecalldialer.fragment.ContactsFragment;
import com.mehboob.simplecalldialer.fragment.DialPadFragment;

import androidx.fragment.app.Fragment;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
