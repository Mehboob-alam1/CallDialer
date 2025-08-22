package com.mehboob.dialeradmin;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.mehboob.dialeradmin.databinding.ActivityDialerHomeBinding;
import com.mehboob.dialeradmin.fragment.CallHis_AddtoContactFragment;
import com.mehboob.dialeradmin.fragment.CallHis_FragmentHomeData;
import com.mehboob.dialeradmin.fragment.CallHis_SettingFragment;
import com.mehboob.dialeradmin.fragment.CallHistoryFragment;
import com.mehboob.dialeradmin.fragment.ContactsFragment;
import com.mehboob.dialeradmin.fragment.DialPadFragment;

public class DialerHomeActivity extends AppCompatActivity implements MyApplication.OnModeChangeListener {
    ActivityDialerHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

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
                tab.getIcon().setTint(ContextCompat.getColor(DialerHomeActivity.this, R.color.primary));
                Fragment selectedFragment;
                if (tab.getPosition() == 0) {
                    selectedFragment = new CallHis_FragmentHomeData();
                } else if (tab.getPosition() == 1) {
                    selectedFragment = new CallHis_AddtoContactFragment();
                }else {
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